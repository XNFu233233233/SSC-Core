package com.xnfu.ssccore;

import com.xnfu.ssccore.content.deconstructor.*;
import com.xnfu.ssccore.content.deconstructor.recipe.DeconstructionRecipe;
import com.xnfu.ssccore.network.ConfirmPayload;
import com.xnfu.ssccore.network.ToggleMachinePayload;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

@Mod(SSCCore.MODID) // 必需的注解：标记这是模组入口类
public class SSCCore {
    public static final String MODID = "ssccore";
    private static final org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger();

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MODID);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, MODID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, MODID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredHolder<Block, DeconstructionTableBlock> DECONSTRUCTION_TABLE = BLOCKS.register("deconstruction_table", 
            () -> new DeconstructionTableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)));
    public static final DeferredHolder<Item, BlockItem> DECONSTRUCTION_TABLE_ITEM = ITEMS.register("deconstruction_table", 
            () -> new BlockItem(DECONSTRUCTION_TABLE.get(), new Item.Properties()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DeconstructionTableBlockEntity>> DECONSTRUCTION_TABLE_BE = 
            BLOCK_ENTITIES.register("deconstruction_table", () -> BlockEntityType.Builder.of(DeconstructionTableBlockEntity::new, DECONSTRUCTION_TABLE.get()).build(null));

    public static final DeferredHolder<MenuType<?>, MenuType<DeconstructionTableMenu>> DECONSTRUCTION_MENU = 
            MENUS.register("deconstruction_table", () -> net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create(DeconstructionTableMenu::new));

    public static final DeferredHolder<RecipeType<?>, RecipeType<DeconstructionRecipe>> DECONSTRUCTION_RECIPE_TYPE = 
            RECIPE_TYPES.register("deconstruction", () -> new RecipeType<>() {});
    public static final DeferredHolder<RecipeSerializer<?>, DeconstructionRecipe.Serializer> DECONSTRUCTION_RECIPE_SERIALIZER = 
            RECIPE_SERIALIZERS.register("deconstruction", DeconstructionRecipe.Serializer::new);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.ssccore"))
            .icon(() -> new ItemStack(DECONSTRUCTION_TABLE_ITEM.get()))
            .displayItems((params, output) -> {
                output.accept(DECONSTRUCTION_TABLE_ITEM.get());
            })
            .build());

    public SSCCore(IEventBus modEventBus, ModContainer container) {
        container.registerConfig(net.neoforged.fml.config.ModConfig.Type.COMMON, Config.SPEC);

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        MENUS.register(modEventBus);
        RECIPE_TYPES.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
        TABS.register(modEventBus);

        modEventBus.addListener(this::registerPayloads);
        modEventBus.addListener(this::registerCapabilities);
        modEventBus.addListener(com.xnfu.ssccore.datagen.SSCDataGenerator::gatherData);
        
        // 客户端专用注册
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(this::registerScreens);
        }
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MODID);
        registrar.playToServer(ConfirmPayload.TYPE, ConfirmPayload.STREAM_CODEC, ConfirmPayload::handle);
        registrar.playToServer(ToggleMachinePayload.TYPE, ToggleMachinePayload.STREAM_CODEC, ToggleMachinePayload::handle);
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, DECONSTRUCTION_TABLE_BE.get(), (be, side) -> be.getItemHandler(side));
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, DECONSTRUCTION_TABLE_BE.get(), (be, side) -> be.getEnergyStorage());
    }

    private void registerScreens(RegisterMenuScreensEvent event) {
        if (DECONSTRUCTION_MENU.isBound()) {
            event.register(DECONSTRUCTION_MENU.get(), DeconstructionTableScreen::new);
        } else {
            LOGGER.warn("SSC Core: DECONSTRUCTION_MENU holder not bound during RegisterMenuScreensEvent! GUI registration skipped.");
        }
    }
}
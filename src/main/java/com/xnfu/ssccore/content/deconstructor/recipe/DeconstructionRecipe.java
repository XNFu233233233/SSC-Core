package com.xnfu.ssccore.content.deconstructor.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xnfu.ssccore.SSCCore;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DeconstructionRecipe implements Recipe<SingleRecipeInput> {
    private static final Logger LOGGER = LogManager.getLogger();

    private final Optional<Ingredient> explicitInput;
    private final Optional<List<ItemStack>> explicitOutputs;
    private final Optional<SourceFilter> recipeSource;

    private final int time;
    private final int energy;
    private final int config;

    private Ingredient resolvedInput = null;
    private int inputCount = 1; 
    private List<ItemStack> resolvedOutputs = null;
    private boolean hasError = false;

    public DeconstructionRecipe(Optional<Ingredient> explicitInput, Optional<List<ItemStack>> explicitOutputs,
                                Optional<SourceFilter> recipeSource, int time, int energy, int config) {
        this.explicitInput = explicitInput;
        this.explicitOutputs = explicitOutputs;
        this.recipeSource = recipeSource;
        this.time = time;
        this.energy = energy;
        this.config = config;
    }

    public record SourceFilter(Optional<ResourceLocation> id, Optional<Item> output, 
                               Optional<ResourceLocation> type, Optional<List<ItemStack>> extraOutputs) {
        
        private static final Codec<SourceFilter> FULL_CODEC = RecordCodecBuilder.<SourceFilter>create(instance -> instance.group(
                ResourceLocation.CODEC.optionalFieldOf("id").forGetter(SourceFilter::id),
                BuiltInRegistries.ITEM.byNameCodec().optionalFieldOf("output").forGetter(SourceFilter::output),
                ResourceLocation.CODEC.optionalFieldOf("type").forGetter(SourceFilter::type),
                ItemStack.CODEC.listOf().optionalFieldOf("outputs").forGetter(SourceFilter::extraOutputs)
        ).apply(instance, SourceFilter::new)).validate(filter -> {
            if (filter.id().isPresent() && (filter.output().isPresent() || filter.type().isPresent())) {
                return DataResult.error(() -> "In 'recipe' object: If 'id' is present, 'output' and 'type' must not be present.");
            }
            return DataResult.success(filter);
        });

        // 支持 ResourceLocation 字符串或完整对象
        public static final Codec<SourceFilter> CODEC = Codec.either(ResourceLocation.CODEC, FULL_CODEC).xmap(
                either -> either.map(rl -> new SourceFilter(Optional.of(rl), Optional.empty(), Optional.empty(), Optional.empty()), f -> f),
                filter -> {
                    if (filter.id().isPresent() && filter.output().isEmpty() && filter.type().isEmpty() && (filter.extraOutputs().isEmpty() || filter.extraOutputs().get().isEmpty())) {
                        return com.mojang.datafixers.util.Either.left(filter.id().get());
                    }
                    return com.mojang.datafixers.util.Either.right(filter);
                }
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, SourceFilter> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), SourceFilter::id,
                ByteBufCodecs.optional(ByteBufCodecs.registry(BuiltInRegistries.ITEM.key())), SourceFilter::output,
                ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), SourceFilter::type,
                ByteBufCodecs.optional(ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list())), SourceFilter::extraOutputs,
                SourceFilter::new
        );
    }

    public void resolve(RecipeManager rm, HolderLookup.Provider registries) {
        if (resolvedInput != null || hasError) return;

        List<ItemStack> rawOutputs = new ArrayList<>();
        Ingredient resultInput = Ingredient.EMPTY;
        int count = 1;

        try {
            if (recipeSource.isPresent()) {
                SourceFilter filter = recipeSource.get();
                RecipeHolder<?> foundRecipe = null;

                if (filter.id().isPresent()) {
                    foundRecipe = rm.byKey(filter.id().get()).orElse(null);
                    if (foundRecipe == null) {
                        logError("SSC Deconstructor: Referenced recipe ID '{}' not found.", filter.id().get());
                        hasError = true; return;
                    }
                } else if (filter.output().isPresent()) {
                    Item target = filter.output().get();
                    List<RecipeHolder<?>> matches = rm.getRecipes().stream()
                            .filter(h -> {
                                ItemStack res = h.value().getResultItem(registries);
                                return !res.isEmpty() && res.is(target);
                            })
                            .filter(h -> filter.type().isEmpty() || BuiltInRegistries.RECIPE_TYPE.getKey(h.value().getType()).equals(filter.type().get()))
                            .toList();

                    if (matches.size() > 1) {
                        logError("SSC Deconstructor: Ambiguous source! {} recipes found for output '{}'.", matches.size(), target);
                        hasError = true; return;
                    } else if (matches.isEmpty()) {
                        logError("SSC Deconstructor: No source recipe found producing '{}'.", target);
                        hasError = true; return;
                    }
                    foundRecipe = matches.get(0);
                }

                if (foundRecipe != null) {
                    Recipe<?> source = foundRecipe.value();
                    ItemStack resultStack = source.getResultItem(registries);
                    
                    if (resultStack.isEmpty() && filter.id().isPresent()) {
                        ResourceLocation id = filter.id().get();
                        if (BuiltInRegistries.ITEM.containsKey(id)) {
                            resultStack = new ItemStack(BuiltInRegistries.ITEM.get(id));
                        }
                    }
                    
                    if (resultStack.isEmpty()) {
                        logError("SSC Deconstructor: Could not determine input item for source recipe '{}'.", foundRecipe.id());
                        hasError = true; return;
                    }

                    resultInput = Ingredient.of(resultStack);
                    count = resultStack.getCount();
                    
                    for (Ingredient ing : source.getIngredients()) {
                        if (ing.isEmpty()) continue;
                        ItemStack[] items = ing.getItems();
                        if (items.length > 0) {
                            rawOutputs.add(items[0].copy());
                        }
                    }
                    
                    filter.extraOutputs().ifPresent(extras -> extras.forEach(s -> rawOutputs.add(s.copy())));
                }
            } else {
                resultInput = explicitInput.orElse(Ingredient.EMPTY);
            }
            // Always process explicit outputs (allows adding extra outputs to derived recipes)
            explicitOutputs.ifPresent(outputs -> outputs.forEach(s -> rawOutputs.add(s.copy())));

            if (resultInput.isEmpty()) {
                hasError = true; return;
            }

            List<ItemStack> merged = new ArrayList<>();
            for (ItemStack stack : rawOutputs) {
                if (stack.isEmpty()) continue;
                boolean found = false;
                for (ItemStack m : merged) {
                    if (ItemStack.isSameItemSameComponents(stack, m)) {
                        m.setCount(m.getCount() + stack.getCount());
                        found = true; break;
                    }
                }
                if (!found) merged.add(stack.copy());
            }
            this.resolvedInput = resultInput;
            this.inputCount = count;
            this.resolvedOutputs = merged;

        } catch (Exception e) {
            logError("SSC Deconstructor: Critical error resolving recipe: " + e.getMessage());
            hasError = true;
        }
    }

    private static final java.util.Set<String> REPORTED_ERRORS = java.util.concurrent.ConcurrentHashMap.newKeySet();

    private void logError(String message, Object... params) {
        String logKey = message;
        if (params.length > 0) {
            logKey += java.util.Arrays.toString(params);
        }
        if (REPORTED_ERRORS.add(logKey)) {
            LOGGER.error(message, params);
        }
    }

    public boolean isInvalid() { return hasError; }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        resolve(level.getRecipeManager(), level.registryAccess());
        return !hasError && resolvedInput != null && !resolvedInput.isEmpty() 
                && resolvedInput.test(input.getItem(0)) 
                && input.getItem(0).getCount() >= inputCount;
    }

    public Ingredient getInput(RecipeManager rm, HolderLookup.Provider registries) {
        resolve(rm, registries);
        return resolvedInput != null ? resolvedInput : Ingredient.EMPTY;
    }
    
    public int getInputCount(RecipeManager rm, HolderLookup.Provider registries) {
        resolve(rm, registries);
        return inputCount;
    }

    public List<ItemStack> getOutputs(RecipeManager rm, HolderLookup.Provider registries) {
        resolve(rm, registries);
        return resolvedOutputs != null ? resolvedOutputs : new ArrayList<>();
    }

    public int getProcessingTime() { 
        return time == -1 ? com.xnfu.ssccore.Config.DEFAULT_TIME.get() : time; 
    }
    
    public int getEnergyCost() { 
        return energy == -1 ? com.xnfu.ssccore.Config.DEFAULT_ENERGY.get() : energy; 
    }
    
    public int getConfig() { return config; }

    @Override public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) { return ItemStack.EMPTY; }
    @Override public boolean canCraftInDimensions(int width, int height) { return true; }
    @Override public ItemStack getResultItem(HolderLookup.Provider registries) { return ItemStack.EMPTY; }
    @Override public RecipeSerializer<?> getSerializer() { return SSCCore.DECONSTRUCTION_RECIPE_SERIALIZER.get(); }
    @Override public RecipeType<?> getType() { return SSCCore.DECONSTRUCTION_RECIPE_TYPE.get(); }

    public static class Serializer implements RecipeSerializer<DeconstructionRecipe> {
        public static final MapCodec<DeconstructionRecipe> CODEC = RecordCodecBuilder.<DeconstructionRecipe>mapCodec(instance -> instance.group(
                Ingredient.CODEC.optionalFieldOf("input").forGetter(r -> r.explicitInput),
                ItemStack.CODEC.listOf().optionalFieldOf("outputs").forGetter(r -> r.explicitOutputs),
                SourceFilter.CODEC.optionalFieldOf("recipe").forGetter(r -> r.recipeSource),
                Codec.INT.optionalFieldOf("time", -1).forGetter(r -> r.time),
                Codec.INT.optionalFieldOf("energy", -1).forGetter(r -> r.energy),
                Codec.INT.optionalFieldOf("config", 0).forGetter(r -> r.config)
        ).apply(instance, DeconstructionRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, DeconstructionRecipe> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.optional(Ingredient.CONTENTS_STREAM_CODEC), r -> r.explicitInput,
                ByteBufCodecs.optional(ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list())), r -> r.explicitOutputs,
                ByteBufCodecs.optional(SourceFilter.STREAM_CODEC), r -> r.recipeSource,
                ByteBufCodecs.INT, r -> r.time,
                ByteBufCodecs.INT, r -> r.energy,
                ByteBufCodecs.INT, r -> r.config,
                DeconstructionRecipe::new
        );

        @Override public MapCodec<DeconstructionRecipe> codec() { return CODEC; }
        @Override public StreamCodec<RegistryFriendlyByteBuf, DeconstructionRecipe> streamCodec() { return STREAM_CODEC; }
    }
}

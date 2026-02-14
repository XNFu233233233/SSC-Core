package com.xnfu.ssccore.compat.jei;

import com.xnfu.ssccore.SSCCore;
import com.xnfu.ssccore.content.deconstructor.recipe.DeconstructionRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.List;

public class DeconstructionCategory implements IRecipeCategory<RecipeHolder<DeconstructionRecipe>> {
    @SuppressWarnings("unchecked")
    public static final RecipeType<RecipeHolder<DeconstructionRecipe>> TYPE = 
            RecipeType.create(SSCCore.MODID, "deconstruction", (Class<RecipeHolder<DeconstructionRecipe>>) (Class<?>) RecipeHolder.class);
    
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(SSCCore.MODID, "textures/gui/deconstruction_table.png");
    
    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableAnimated arrow;

    public DeconstructionCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 20, 15, 136, 115);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, SSCCore.DECONSTRUCTION_TABLE.get().asItem().getDefaultInstance());
        
        IDrawableStatic staticArrow = helper.createDrawable(TEXTURE, 176, 0, 16, 24);
        this.arrow = helper.createAnimatedDrawable(staticArrow, 50, IDrawableAnimated.StartDirection.TOP, false);
    }

    @Override public RecipeType<RecipeHolder<DeconstructionRecipe>> getRecipeType() { return TYPE; }
    @Override public Component getTitle() { return Component.translatable("block.ssccore.deconstruction_table"); }
    @Override public int getWidth() { return 136; }
    @Override public int getHeight() { return 115; }
    @Override public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<DeconstructionRecipe> holder, IFocusGroup focuses) {
        DeconstructionRecipe recipe = holder.value();
        RecipeManager rm = Minecraft.getInstance().level.getRecipeManager();
        var regs = Minecraft.getInstance().level.registryAccess();
        
        // Input (60, 6)
        builder.addSlot(RecipeIngredientRole.INPUT, 60, 6)
                .addIngredients(recipe.getInput(rm, regs));

        // Outputs (6, 54)
        int startX = 6;
        int startY = 54;
        List<ItemStack> outputs = recipe.getOutputs(rm, regs);
        
        int index = 0;
        for (ItemStack stack : outputs) {
            if (index >= 21) break;
            int col = index % 7;
            int row = index / 7;
            builder.addSlot(RecipeIngredientRole.OUTPUT, startX + col * 18, startY + row * 18)
                    .addItemStack(stack);
            index++;
        }
    }

    @Override
    public void draw(RecipeHolder<DeconstructionRecipe> holder, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        background.draw(guiGraphics);
        Component configText = Component.literal("Config: " + holder.value().getConfig());
        guiGraphics.drawString(Minecraft.getInstance().font, configText, 6, 6, 0xFF404040, false);
        arrow.draw(guiGraphics, 60, 27);
    }
}

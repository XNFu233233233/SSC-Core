package com.xnfu.ssccore.datagen;

import com.xnfu.ssccore.SSCCore;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.FalseCondition;
import com.xnfu.ssccore.content.deconstructor.recipe.DeconstructionRecipe;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class SSCRecipeProvider extends RecipeProvider {
    public SSCRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        // Wrapper to add the "neoforge:false" condition to everything
        RecipeOutput disabledOutput = output.withConditions(FalseCondition.INSTANCE);

        // 1. Manual Recipe: Iron Block -> 9 Iron Ingots
        buildManual(disabledOutput, "manual_iron_block", 
                Ingredient.of(Items.IRON_BLOCK), 
                List.of(new ItemStack(Items.IRON_INGOT, 9)), 
                10, 500, 0);

        // 2. Derived Recipe: By Recipe ID (Oak Planks)
        buildDerivedById(disabledOutput, "derived_oak_planks", 
                ResourceLocation.withDefaultNamespace("oak_planks"), 
                5, -1, 0);

        // 3. Derived Recipe: By Output & Type (Iron Ingot from Smelting)
        buildDerivedByOutput(disabledOutput, "derived_iron_ingot_smelting",
                Items.IRON_INGOT,
                ResourceLocation.withDefaultNamespace("smelting"),
                10, 1000, 0);
    }

    private void buildManual(RecipeOutput output, String name, Ingredient input, List<ItemStack> outputs, int time, int energy, int config) {
        DeconstructionRecipe recipe = new DeconstructionRecipe(
                Optional.of(input), 
                Optional.of(outputs), 
                Optional.empty(), 
                time, energy, config
        );
        output.accept(ResourceLocation.fromNamespaceAndPath(SSCCore.MODID, name), recipe, null);
    }

    private void buildDerivedById(RecipeOutput output, String name, ResourceLocation targetId, int time, int energy, int config) {
        DeconstructionRecipe recipe = new DeconstructionRecipe(
                Optional.empty(), 
                Optional.empty(), 
                Optional.of(new DeconstructionRecipe.SourceFilter(Optional.of(targetId), Optional.empty(), Optional.empty(), Optional.empty())),
                time, energy, config
        );
        output.accept(ResourceLocation.fromNamespaceAndPath(SSCCore.MODID, name), recipe, null);
    }

    private void buildDerivedByOutput(RecipeOutput output, String name, ItemLike targetItem, ResourceLocation recipeType, int time, int energy, int config) {
        DeconstructionRecipe recipe = new DeconstructionRecipe(
                Optional.empty(), 
                Optional.empty(), 
                Optional.of(new DeconstructionRecipe.SourceFilter(Optional.empty(), Optional.of(targetItem.asItem()), Optional.of(recipeType), Optional.empty())),
                time, energy, config
        );
        output.accept(ResourceLocation.fromNamespaceAndPath(SSCCore.MODID, name), recipe, null);
    }
}

package com.xnfu.ssccore.compat.jei;

import com.xnfu.ssccore.SSCCore;
import com.xnfu.ssccore.content.deconstructor.DeconstructionTableScreen;
import com.xnfu.ssccore.content.deconstructor.recipe.DeconstructionRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IRecipesGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@JeiPlugin
public class SSCJEIPlugin implements IModPlugin {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(SSCCore.MODID, "jei_plugin");

    @Override public ResourceLocation getPluginUid() { return UID; }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new DeconstructionCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        if (Minecraft.getInstance().level != null) {
            RecipeManager rm = Minecraft.getInstance().level.getRecipeManager();
            var regs = Minecraft.getInstance().level.registryAccess();
            
            List<RecipeHolder<DeconstructionRecipe>> recipes = rm.getAllRecipesFor(SSCCore.DECONSTRUCTION_RECIPE_TYPE.get());
            
            // --- 核心修复：剪枝逻辑 ---
            List<RecipeHolder<DeconstructionRecipe>> validRecipes = recipes.stream()
                    .filter(holder -> {
                        DeconstructionRecipe r = holder.value();
                        r.resolve(rm, regs); // 强制尝试解析
                        return !r.isInvalid(); // 如果不唯一或出错，不予展示
                    })
                    .collect(Collectors.toList());

            registration.addRecipes(DeconstructionCategory.TYPE, validRecipes);
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(SSCCore.DECONSTRUCTION_TABLE.get().asItem().getDefaultInstance(), DeconstructionCategory.TYPE);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGuiContainerHandler(DeconstructionTableScreen.class, new IGuiContainerHandler<DeconstructionTableScreen>() {
            @Override
            public Collection<IGuiClickableArea> getGuiClickableAreas(DeconstructionTableScreen containerScreen, double mouseX, double mouseY) {
                return List.of(new IGuiClickableArea() {
                    @Override public Rect2i getArea() { return new Rect2i(80, 42, 16, 24); }
                    @Override public void onClick(IFocusFactory focusFactory, IRecipesGui recipesGui) {
                        recipesGui.showTypes(List.of(DeconstructionCategory.TYPE));
                    }
                });
            }
        });
    }
}

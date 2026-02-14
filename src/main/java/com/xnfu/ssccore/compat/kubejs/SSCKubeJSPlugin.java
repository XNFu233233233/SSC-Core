package com.xnfu.ssccore.compat.kubejs;

import com.xnfu.ssccore.SSCCore;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaRegistry;
import dev.latvian.mods.kubejs.recipe.component.*;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.Map;

/**
 * SSC Core KubeJS 插件 - 1.21.1 完美修复版
 */
public class SSCKubeJSPlugin implements KubeJSPlugin {

    // 显式 Record 组件初始化
    private static final RecipeComponent<Ingredient> INGREDIENT_COMP = new IngredientComponent(IngredientComponent.INGREDIENT, Ingredient.CODEC, true);
    private static final RecipeComponent<ItemStack> ITEM_STACK_COMP = new ItemStackComponent(ItemStackComponent.ITEM_STACK, false, Ingredient.EMPTY);

    // 键定义
    public static final RecipeKey<Ingredient> INPUT = INGREDIENT_COMP.inputKey("input").optional(Ingredient.EMPTY);
    public static final RecipeKey<List<ItemStack>> OUTPUTS = ListRecipeComponent.create(ITEM_STACK_COMP, true, true).outputKey("outputs").optional(List.of());
    
    // 引用 ID 键 (后端已改为弹性识别)
    public static final RecipeKey<String> RECIPE_ID = StringComponent.ID.inputKey("recipe").optional("");

    public static final RecipeKey<Integer> TIME = NumberComponent.INT.inputKey("time").optional(-1);
    public static final RecipeKey<Integer> ENERGY = NumberComponent.INT.inputKey("energy").optional(-1);
    public static final RecipeKey<Integer> CONFIG = NumberComponent.INT.inputKey("config").optional(0);

    @Override
    public void registerRecipeSchemas(RecipeSchemaRegistry registry) {
        
        // 1. 配置手动定义 Schema (deconstruction)
        RecipeSchema manualSchema = new RecipeSchema(Map.of(), List.of(
            OUTPUTS, INPUT, TIME, ENERGY, CONFIG
        ));
        // 设置顺序: (outputs, input, config)
        manualSchema.constructor(OUTPUTS, INPUT, CONFIG);

        // 2. 配置派生引用 Schema (deconstruction_for_recipe)
        // 关键修复: 强制使用 ssccore:deconstruction 序列化器，避免注册新类型
        // 2. 配置派生引用 Schema (deconstruction_for_recipe)
        // 关键修复: 强制使用 ssccore:deconstruction 序列化器，避免注册新类型
        RecipeSchema fromRecipeSchema = new RecipeSchema(Map.of(), List.of(
            RECIPE_ID, OUTPUTS, TIME, ENERGY, CONFIG
        ))
        .typeOverride(ResourceLocation.fromNamespaceAndPath(SSCCore.MODID, "deconstruction"));
        
        // 语法 1: (id, config) -> deconstruction_for_recipe('furnace')
        fromRecipeSchema.constructor(RECIPE_ID, CONFIG);
        
        // 语法 2: (id, extra_outputs, config) -> deconstruction_for_recipe('golden_apple', ['stick'])
        // ID 优先符合用户直觉
        fromRecipeSchema.constructor(RECIPE_ID, OUTPUTS, CONFIG);

        // 建立模组命名空间
        var group = registry.namespace(SSCCore.MODID);
        
        // 注册到空间
        group.register("deconstruction", manualSchema);
        group.register("deconstruction_for_recipe", fromRecipeSchema);

        // 建立强绑定关联 (ResourceLocation 方式)
        registry.register(ResourceLocation.fromNamespaceAndPath(SSCCore.MODID, "deconstruction"), manualSchema);
        registry.register(ResourceLocation.fromNamespaceAndPath(SSCCore.MODID, "deconstruction_for_recipe"), fromRecipeSchema);
    }
}

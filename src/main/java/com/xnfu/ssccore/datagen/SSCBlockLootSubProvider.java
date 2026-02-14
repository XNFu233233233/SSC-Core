package com.xnfu.ssccore.datagen;

import com.xnfu.ssccore.SSCCore;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

import java.util.Collections;
import java.util.Set;

public class SSCBlockLootSubProvider extends BlockLootSubProvider {
    protected SSCBlockLootSubProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        // 配置为破坏后什么都不掉落（方块本身不掉落）
        // 物品掉落逻辑已在 Block 类中的 onRemove 显式处理
        this.add(SSCCore.DECONSTRUCTION_TABLE.get(), noDrop());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return Collections.singleton(SSCCore.DECONSTRUCTION_TABLE.get());
    }
}

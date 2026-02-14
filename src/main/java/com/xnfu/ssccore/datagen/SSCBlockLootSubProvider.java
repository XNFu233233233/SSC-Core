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
        // 允许破坏后掉落方块本体
        this.dropSelf(SSCCore.DECONSTRUCTION_TABLE.get());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return Collections.singleton(SSCCore.DECONSTRUCTION_TABLE.get());
    }
}

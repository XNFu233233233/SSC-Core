package com.xnfu.ssccore.datagen;

import com.xnfu.ssccore.SSCCore;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class SSCBlockStateProvider extends BlockStateProvider {
    public SSCBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, SSCCore.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        ResourceLocation bottom = ResourceLocation.fromNamespaceAndPath(SSCCore.MODID, "block/deconstruction_table_bottom");
        ResourceLocation top = ResourceLocation.fromNamespaceAndPath(SSCCore.MODID, "block/deconstruction_table_top");
        ResourceLocation side = ResourceLocation.fromNamespaceAndPath(SSCCore.MODID, "block/deconstruction_table_side");

        net.neoforged.neoforge.client.model.generators.ModelFile model = models().cube(
                "deconstruction_table",
                bottom, top, side, side, side, side
        ).texture("particle", side);

        simpleBlock(SSCCore.DECONSTRUCTION_TABLE.get(), model);
        simpleBlockItem(SSCCore.DECONSTRUCTION_TABLE.get(), model);
    }
}

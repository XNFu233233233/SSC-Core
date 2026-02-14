package com.xnfu.ssccore.datagen;

import com.xnfu.ssccore.SSCCore;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

public class SSCDataGenerator {
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> registries = event.getLookupProvider();

        generator.addProvider(event.includeServer(), SSCLootTableProvider.create(output, registries));
        generator.addProvider(event.includeServer(), new SSCBlockTagProvider(output, registries, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new SSCBlockStateProvider(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new SSCLangProvider(output, "en_us"));
        generator.addProvider(event.includeClient(), new SSCLangProvider(output, "zh_cn"));
    }
}

package com.xnfu.ssccore;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue MAX_ENERGY = BUILDER
            .comment("Deconstructor max energy capacity.")
            .defineInRange("maxEnergy", 100000, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue DEFAULT_TIME = BUILDER
            .comment("Default processing time (ticks) if not defined in recipe.")
            .defineInRange("defaultTime", 5, 1, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue DEFAULT_ENERGY = BUILDER
            .comment("Default energy cost per operation if not defined in recipe.")
            .defineInRange("defaultEnergy", 100, 0, Integer.MAX_VALUE);

    static final ModConfigSpec SPEC = BUILDER.build();
}

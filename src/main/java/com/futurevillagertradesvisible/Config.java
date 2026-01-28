package com.futurevillagertradesvisible;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLE_FUTURE_TRADES = BUILDER
            .comment("Enable displaying and sending future villager trades")
            .define("enableFutureTrades", true);

    static final ModConfigSpec SPEC = BUILDER.build();

    private Config() {
    }

    public static boolean isEnabled() {
        return ENABLE_FUTURE_TRADES.get();
    }
}
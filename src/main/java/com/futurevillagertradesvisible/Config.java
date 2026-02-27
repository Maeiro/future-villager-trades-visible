package com.futurevillagertradesvisible;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.BooleanValue ENABLE_FUTURE_TRADES = BUILDER
            .comment("Enable displaying and sending future villager trades")
            .define("enableFutureTrades", true);

    public static final ForgeConfigSpec.BooleanValue COMPATIBILITY_SAFE_PACKET_LEVEL = BUILDER
            .comment("Send vanilla villager level in merchant packets (compat mode for trade UI mods)")
            .define("compatibilitySafePacketLevel", true);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    private Config() {
    }

    public static boolean isEnabled() {
        return ENABLE_FUTURE_TRADES.get();
    }

    public static boolean useCompatibilitySafePacketLevel() {
        return COMPATIBILITY_SAFE_PACKET_LEVEL.get();
    }
}

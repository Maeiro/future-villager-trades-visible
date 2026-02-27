package com.futurevillagertradesvisible;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    private static final List<String> DEFAULT_BYPASSED_PROFESSIONS = List.of(
            "minecraft:armorer",
            "minecraft:butcher",
            "minecraft:cartographer",
            "minecraft:cleric",
            "minecraft:farmer",
            "minecraft:fisherman",
            "minecraft:fletcher",
            "minecraft:leatherworker",
            "minecraft:librarian",
            "minecraft:mason",
            "minecraft:shepherd",
            "minecraft:toolsmith",
            "minecraft:weaponsmith",
            "minecraft:nitwit",
            "minecraft:none"
    );

    private static List<String> cachedRawBypassedProfessions = List.of();
    private static Set<ResourceLocation> cachedBypassedProfessions = Set.of();

    public static final ForgeConfigSpec.BooleanValue ENABLE_FUTURE_TRADES = BUILDER
            .comment("Enable displaying and sending future villager trades")
            .define("enableFutureTrades", true);

    public static final ForgeConfigSpec.BooleanValue COMPATIBILITY_SAFE_PACKET_LEVEL = BUILDER
            .comment("Send vanilla villager level in merchant packets (compat mode for trade UI mods)")
            .define("compatibilitySafePacketLevel", true);

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> BYPASSED_VILLAGER_PROFESSIONS = BUILDER
            .comment("Villager professions where FVTV should not modify trade synchronization")
            .defineListAllowEmpty(
                    "bypassedVillagerProfessions",
                    () -> DEFAULT_BYPASSED_PROFESSIONS,
                    o -> o instanceof String s && ResourceLocation.tryParse(s) != null
            );

    public static final ForgeConfigSpec.BooleanValue FAIL_OPEN_ON_TRADE_SYNC_ERROR = BUILDER
            .comment("If true, fallback to vanilla behavior when FVTV trade sync fails")
            .define("failOpenOnTradeSyncError", true);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    private Config() {
    }

    public static boolean isEnabled() {
        return ENABLE_FUTURE_TRADES.get();
    }

    public static boolean useCompatibilitySafePacketLevel() {
        return COMPATIBILITY_SAFE_PACKET_LEVEL.get();
    }

    public static boolean shouldFailOpenOnTradeSyncError() {
        return FAIL_OPEN_ON_TRADE_SYNC_ERROR.get();
    }

    public static boolean isProfessionBypassed(ResourceLocation professionId) {
        return professionId != null && getBypassedVillagerProfessions().contains(professionId);
    }

    public static Set<ResourceLocation> getBypassedVillagerProfessions() {
        List<String> currentRaw = normalizeRaw(BYPASSED_VILLAGER_PROFESSIONS.get());
        synchronized (Config.class) {
            if (currentRaw.equals(cachedRawBypassedProfessions)) {
                return cachedBypassedProfessions;
            }
            Set<ResourceLocation> parsed = new HashSet<>();
            for (String value : currentRaw) {
                ResourceLocation id = ResourceLocation.tryParse(value);
                if (id != null) {
                    parsed.add(id);
                }
            }
            cachedRawBypassedProfessions = currentRaw;
            cachedBypassedProfessions = Set.copyOf(parsed);
            return cachedBypassedProfessions;
        }
    }

    private static List<String> normalizeRaw(List<? extends String> raw) {
        return raw.stream().map(String::valueOf).map(String::trim).toList();
    }
}

package com.futurevillagertradesvisible;

import java.util.Objects;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.trading.Merchant;
import net.minecraftforge.registries.ForgeRegistries;

public final class TradeEligibility {
    private static final ResourceLocation FALLBACK_NONE = Objects.requireNonNull(
            ResourceLocation.tryParse("minecraft:none"),
            "Invalid fallback villager profession id"
    );

    private TradeEligibility() {
    }

    public static boolean shouldApplyFutureTrades(Merchant merchant) {
        if (!(merchant instanceof Villager villager)) return false;
        return shouldApplyFutureTrades(villager);
    }

    public static boolean shouldApplyFutureTrades(Villager villager) {
        if (!Config.isEnabled() || villager == null) return false;
        ResourceLocation professionId = getProfessionId(villager.getVillagerData().getProfession());
        return !Config.isProfessionBypassed(professionId);
    }

    public static ResourceLocation getProfessionId(VillagerProfession profession) {
        if (profession == null) return FALLBACK_NONE;
        ResourceLocation id = ForgeRegistries.VILLAGER_PROFESSIONS.getKey(profession);
        return id == null ? FALLBACK_NONE : id;
    }
}

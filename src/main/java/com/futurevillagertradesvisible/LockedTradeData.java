package com.futurevillagertradesvisible;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

import com.futurevillagertradesvisible.ducks.VillagerDuck;

public class LockedTradeData {
    private static final String TAG_LOCKED_OFFERS = "FVTvLockedOffers";
    private static final int MAX_LEVEL = 5;

    private final List<MerchantOffers> lockedOffers;

    public LockedTradeData(Villager villager) {
        this.lockedOffers = generateTrades(villager);
    }

    private LockedTradeData(List<MerchantOffers> offers) {
        this.lockedOffers = new ArrayList<>(offers);
    }

    public static @Nullable LockedTradeData load(CompoundTag tag) {
        if (!tag.contains(TAG_LOCKED_OFFERS)) return null;
        ListTag offersTag = tag.getList(TAG_LOCKED_OFFERS, Tag.TAG_COMPOUND);
        List<MerchantOffers> offers = new ArrayList<>(offersTag.size());
        for (Tag entry : offersTag) {
            if (entry instanceof CompoundTag offerTag) {
                offers.add(new MerchantOffers(offerTag));
            }
        }
        return new LockedTradeData(offers);
    }

    public void save(CompoundTag tag) {
        ListTag offersTag = new ListTag();
        for (MerchantOffers offers : this.lockedOffers) {
            offersTag.add(offers.createTag());
        }
        tag.put(TAG_LOCKED_OFFERS, offersTag);
    }

    public MerchantOffers popTradeSet() {
        return this.lockedOffers.isEmpty() ? null : this.lockedOffers.remove(0);
    }

    public MerchantOffers buildLockedOffers() {
        MerchantOffers result = new MerchantOffers();
        for (MerchantOffers listOffers : this.lockedOffers) {
            for (MerchantOffer offer : listOffers) {
                result.add(offer);
            }
        }
        return result;
    }

    public void tick(Villager villager, Runnable popCallback) {
        int requiredSets = MAX_LEVEL - villager.getVillagerData().getLevel();
        while (requiredSets < this.lockedOffers.size()) popCallback.run();
        if (requiredSets > this.lockedOffers.size()) {
            int missing = requiredSets - this.lockedOffers.size();
            DebugLogger.error(
                    "Detected missing locked trade sets (required={}, current={}) for villagerUuid={}. Padding with empty sets to avoid offer corruption.",
                    new IllegalStateException("Missing locked trade sets"),
                    requiredSets,
                    this.lockedOffers.size(),
                    villager.getUUID()
            );
            for (int i = 0; i < missing; i++) {
                this.lockedOffers.add(new MerchantOffers());
            }
        }
    }

    private static List<MerchantOffers> generateTrades(Villager villager) {
        MerchantOffers offers = villager.getOffers();
        VillagerData data = villager.getVillagerData();
        List<MerchantOffers> lockedOffers = new ArrayList<>();
        int level = data.getLevel();
        DebugLogger.info(
                "LockedTradeData generation started villagerUuid={} level={} offersSize={}",
                villager.getUUID(),
                level,
                offers.size()
        );
        try {
            while (level < MAX_LEVEL) {
                villager.setVillagerData(data.setLevel(++level));
                int previousSize = offers.size();
                try {
                    VillagerDuck.of(villager).visibleTraders$updateTrades();
                } catch (Throwable t) {
                    DebugLogger.error(
                            "LockedTradeData failed to generate future trades at level={} villagerUuid={}. Falling back to currently unlocked trades only.",
                            t,
                            level,
                            villager.getUUID()
                    );
                    lockedOffers.add(new MerchantOffers());
                    while (level < MAX_LEVEL) {
                        level++;
                        lockedOffers.add(new MerchantOffers());
                    }
                    break;
                }
                int newCount = offers.size() - previousSize;
                MerchantOffers newOffers = new MerchantOffers();
                for (int i = 0; i < newCount; i++) {
                    newOffers.add(offers.remove(offers.size() - 1));
                }
                lockedOffers.add(newOffers);
            }
        } finally {
            villager.setVillagerData(data);
            DebugLogger.info(
                    "LockedTradeData generation finished villagerUuid={} level={} generatedSets={} offersSize={}",
                    villager.getUUID(),
                    data.getLevel(),
                    lockedOffers.size(),
                    offers.size()
            );
        }
        return lockedOffers;
    }
}

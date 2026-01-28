package com.futurevillagertradesvisible;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
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

    public static @Nullable LockedTradeData load(CompoundTag tag, RegistryAccess registryAccess) {
        if (!tag.contains(TAG_LOCKED_OFFERS)) return null;
        return MerchantOffers.CODEC
                .listOf()
                .parse(registryAccess.createSerializationContext(NbtOps.INSTANCE), tag.get(TAG_LOCKED_OFFERS))
                .resultOrPartial(FutureVillagerTradesVisible.LOGGER::error)
                .map(LockedTradeData::new)
                .orElse(null);
    }

    public void save(CompoundTag tag, RegistryAccess registryAccess) {
        MerchantOffers.CODEC
                .listOf()
                .encodeStart(registryAccess.createSerializationContext(NbtOps.INSTANCE), this.lockedOffers)
                .resultOrPartial(FutureVillagerTradesVisible.LOGGER::error)
                .ifPresent(nbt -> tag.put(TAG_LOCKED_OFFERS, nbt));
    }

    public MerchantOffers popTradeSet() {
        return this.lockedOffers.isEmpty() ? null : this.lockedOffers.removeFirst();
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
            FutureVillagerTradesVisible.LOGGER.error("Detected missing locked trade sets. Rebuilding locked offers");
            this.lockedOffers.clear();
            this.lockedOffers.addAll(generateTrades(villager));
        }
    }

    private static List<MerchantOffers> generateTrades(Villager villager) {
        MerchantOffers offers = villager.getOffers();
        VillagerData data = villager.getVillagerData();
        List<MerchantOffers> lockedOffers = new ArrayList<>();
        int level = data.getLevel();
        while (level < MAX_LEVEL) {
            villager.setVillagerData(data.setLevel(++level));
            int previousSize = offers.size();
            VillagerDuck.of(villager).visibleTraders$updateTrades();
            int newCount = offers.size() - previousSize;
            MerchantOffers newOffers = new MerchantOffers();
            for (int i = 0; i < newCount; i++) {
                newOffers.add(offers.removeLast());
            }
            lockedOffers.add(newOffers);
        }
        villager.setVillagerData(data);
        return lockedOffers;
    }
}
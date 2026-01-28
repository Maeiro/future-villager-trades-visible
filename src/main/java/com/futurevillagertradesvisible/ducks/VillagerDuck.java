package com.futurevillagertradesvisible.ducks;

import java.util.Optional;

import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.trading.MerchantOffers;
import com.futurevillagertradesvisible.LockedTradeData;

public interface VillagerDuck {
    static VillagerDuck of(Villager villager) {
        return (VillagerDuck) villager;
    }

    void visibleTraders$setLockedTradeData(LockedTradeData data);

    Optional<LockedTradeData> visibleTraders$getLockedTradeData();

    void visibleTrades$regenerateTrades();

    MerchantOffers visibleTraders$getCombinedOffers();

    int visibleTraders$getShiftedLevel();

    void visibleTraders$updateTrades();
}

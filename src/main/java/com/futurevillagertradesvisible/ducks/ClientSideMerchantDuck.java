package com.futurevillagertradesvisible.ducks;

import net.minecraft.world.item.trading.MerchantOffers;

public interface ClientSideMerchantDuck {
    MerchantOffers visibleTraders$getClientUnlockedTrades();

    void visibleTraders$setClientUnlockedTrades(MerchantOffers offers);
}

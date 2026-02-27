package com.futurevillagertradesvisible.ducks;

public interface MerchantMenuDuck {
    boolean visibleTraders$shouldAllowTrade(int index);

    void visibleTraders$setUnlockedTradeCount(int unlockedCount);
}

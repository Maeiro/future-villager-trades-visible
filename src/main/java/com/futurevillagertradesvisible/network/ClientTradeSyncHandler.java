package com.futurevillagertradesvisible.network;

import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.MerchantOffers;

import com.futurevillagertradesvisible.DebugLogger;
import com.futurevillagertradesvisible.ducks.MerchantMenuDuck;

public final class ClientTradeSyncHandler {
    private ClientTradeSyncHandler() {
    }

    public static void handleUnlockedTradeCount(int containerId, int unlockedCount) {
        TradeSyncState.setUnlockedCount(containerId, unlockedCount);

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (!(mc.player.containerMenu instanceof MerchantMenu menu)) return;
        if (menu.containerId != containerId) return;
        if (!(menu instanceof MerchantMenuDuck duck)) return;

        duck.visibleTraders$setUnlockedTradeCount(unlockedCount);
        MerchantOffers offers = menu.getOffers();
        DebugLogger.info(
                "ClientTradeSyncHandler applied unlockedCount={} containerId={} offersSize={}",
                unlockedCount,
                containerId,
                offers == null ? -1 : offers.size()
        );
    }
}

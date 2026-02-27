package com.futurevillagertradesvisible.network;

import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.MerchantOffers;

import com.futurevillagertradesvisible.Config;
import com.futurevillagertradesvisible.DebugLogger;
import com.futurevillagertradesvisible.ducks.MerchantMenuDuck;

public final class ClientTradeSyncHandler {
    private ClientTradeSyncHandler() {
    }

    public static void handleUnlockedTradeCount(int containerId, int unlockedCount) {
        try {
            TradeSyncState.setUnlockedCount(containerId, unlockedCount);

            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;
            if (!(mc.player.containerMenu instanceof MerchantMenu menu)) return;
            if (menu.containerId != containerId) return;
            if (!(menu instanceof MerchantMenuDuck duck)) return;

            MerchantOffers offers = menu.getOffers();
            if (offers == null || offers.isEmpty()) {
                DebugLogger.info(
                        "ClientTradeSyncHandler queued pending unlockedCount={} containerId={} offersSize={}",
                        unlockedCount,
                        containerId,
                        offers == null ? -1 : offers.size()
                );
                return;
            }

            duck.visibleTraders$setUnlockedTradeCount(unlockedCount);
            DebugLogger.info(
                    "ClientTradeSyncHandler applied unlockedCount={} containerId={} offersSize={}",
                    unlockedCount,
                    containerId,
                    offers.size()
            );
        } catch (RuntimeException e) {
            DebugLogger.error(
                    "ClientTradeSyncHandler failed containerId={} unlockedCount={}",
                    e,
                    containerId,
                    unlockedCount
            );
            if (!Config.shouldFailOpenOnTradeSyncError()) throw e;
        }
    }
}

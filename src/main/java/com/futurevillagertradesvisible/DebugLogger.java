package com.futurevillagertradesvisible;

import javax.annotation.Nullable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffers;

public final class DebugLogger {
    private static final String PREFIX = "[FVTVDebug]";

    private DebugLogger() {
    }

    public static void info(String message, Object... args) {
        FutureVillagerTradesVisible.LOGGER.info(PREFIX + " " + message, args);
    }

    public static void error(String message, Throwable throwable, Object... args) {
        FutureVillagerTradesVisible.LOGGER.error(PREFIX + " " + message, appendThrowable(args, throwable));
    }

    public static String merchantSummary(@Nullable Merchant merchant) {
        if (merchant == null) return "merchant=<null>";
        StringBuilder sb = new StringBuilder();
        sb.append("merchantClass=").append(merchant.getClass().getName());
        if (merchant instanceof Entity entity) {
            sb.append(", merchantEntityType=").append(entity.getEncodeId());
            sb.append(", merchantUuid=").append(entity.getUUID());
        }
        return sb.toString();
    }

    public static String offersSummary(@Nullable MerchantOffers offers) {
        return "offersSize=" + (offers == null ? -1 : offers.size());
    }

    private static Object[] appendThrowable(Object[] args, Throwable throwable) {
        Object[] full = new Object[args.length + 1];
        System.arraycopy(args, 0, full, 0, args.length);
        full[args.length] = throwable;
        return full;
    }
}

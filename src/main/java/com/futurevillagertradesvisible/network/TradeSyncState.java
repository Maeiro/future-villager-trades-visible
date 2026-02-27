package com.futurevillagertradesvisible.network;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class TradeSyncState {
    private static final Map<Integer, Integer> UNLOCKED_BY_CONTAINER = new ConcurrentHashMap<>();

    private TradeSyncState() {
    }

    public static void setUnlockedCount(int containerId, int unlockedCount) {
        UNLOCKED_BY_CONTAINER.put(containerId, Math.max(unlockedCount, 0));
    }

    public static int getUnlockedCount(int containerId) {
        return UNLOCKED_BY_CONTAINER.getOrDefault(containerId, -1);
    }

    public static void clearUnlockedCount(int containerId) {
        UNLOCKED_BY_CONTAINER.remove(containerId);
    }
}

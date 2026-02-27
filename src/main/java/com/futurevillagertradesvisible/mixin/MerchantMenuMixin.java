package com.futurevillagertradesvisible.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

import com.futurevillagertradesvisible.Config;
import com.futurevillagertradesvisible.DebugLogger;
import com.futurevillagertradesvisible.ducks.ClientSideMerchantDuck;
import com.futurevillagertradesvisible.ducks.MerchantMenuDuck;
import com.futurevillagertradesvisible.network.TradeSyncState;

@Mixin(MerchantMenu.class)
public abstract class MerchantMenuMixin implements MerchantMenuDuck {

    @Shadow
    private int merchantLevel;

    @Shadow
    @Final
    private Merchant trader;

    @Shadow
    public abstract MerchantOffers getOffers();

    @Unique
    private int fvtv$unlockedTradeCount = -1;

    @Inject(method = "setMerchantLevel", at = @At("TAIL"))
    private void fvtv$readUnlockedTradeCountFromLevel(int level, CallbackInfo ci) {
        try {
            if (!Config.isEnabled()) {
                this.fvtv$unlockedTradeCount = -1;
                this.merchantLevel = level;
                fvtv$setClientUnlockedTrades(0);
                return;
            }
            if (Config.useCompatibilitySafePacketLevel()) {
                // Compat mode: unlocked count arrives through custom packet sync.
                int containerId = ((MerchantMenu) (Object) this).containerId;
                int syncedUnlockedCount = TradeSyncState.getUnlockedCount(containerId);
                this.fvtv$unlockedTradeCount = syncedUnlockedCount;
                this.merchantLevel = level;
                fvtv$setClientUnlockedTrades(this.fvtv$unlockedTradeCount);
                DebugLogger.info(
                        "MerchantMenuMixin#setMerchantLevel compat mode active level={} unlockedCount={} containerId={} {}",
                        level,
                        syncedUnlockedCount,
                        containerId,
                        DebugLogger.merchantSummary(this.trader)
                );
                return;
            }
            this.fvtv$unlockedTradeCount = level >> 8;
            fvtv$setClientUnlockedTrades(this.fvtv$unlockedTradeCount);
            this.merchantLevel = level & 255;
            DebugLogger.info(
                    "MerchantMenuMixin#setMerchantLevel decodedLevel={} unlockedCount={} rawLevel={} {}",
                    this.merchantLevel,
                    this.fvtv$unlockedTradeCount,
                    level,
                    DebugLogger.merchantSummary(this.trader)
            );
        } catch (RuntimeException e) {
            DebugLogger.error(
                    "MerchantMenuMixin#setMerchantLevel failed rawLevel={} {}",
                    e,
                    level,
                    DebugLogger.merchantSummary(this.trader)
            );
            this.fvtv$unlockedTradeCount = -1;
            this.merchantLevel = level;
            if (!Config.shouldFailOpenOnTradeSyncError()) throw e;
        }
    }

    @Override
    public boolean visibleTraders$shouldAllowTrade(int index) {
        if (!Config.isEnabled()) return true;
        if (this.fvtv$unlockedTradeCount < 0) return true;
        return index <= this.fvtv$unlockedTradeCount - 1;
    }

    @Override
    public void visibleTraders$setUnlockedTradeCount(int unlockedCount) {
        this.fvtv$unlockedTradeCount = unlockedCount;
        fvtv$setClientUnlockedTrades(unlockedCount);
    }

    @Unique
    private void fvtv$setClientUnlockedTrades(int unlockedCount) {
        try {
            if (!(this.trader instanceof ClientSideMerchantDuck duck)) return;
            if (unlockedCount <= 0) {
                duck.visibleTraders$setClientUnlockedTrades(null);
                return;
            }
            MerchantOffers offers = getOffers();
            if (offers.isEmpty()) {
                duck.visibleTraders$setClientUnlockedTrades(null);
                return;
            }
            int max = Math.min(unlockedCount, offers.size());
            List<MerchantOffer> list = offers.subList(0, max);
            MerchantOffers unlocked = new MerchantOffers();
            unlocked.addAll(list);
            duck.visibleTraders$setClientUnlockedTrades(unlocked);
            DebugLogger.info(
                    "MerchantMenuMixin prepared client unlocked offers unlockedCount={} max={} {} {}",
                    unlockedCount,
                    max,
                    DebugLogger.merchantSummary(this.trader),
                    DebugLogger.offersSummary(offers)
            );
        } catch (RuntimeException e) {
            DebugLogger.error(
                    "MerchantMenuMixin#setClientUnlockedTrades failed unlockedCount={} {}",
                    e,
                    unlockedCount,
                    DebugLogger.merchantSummary(this.trader)
            );
            if (!Config.shouldFailOpenOnTradeSyncError()) throw e;
        }
    }

    @Inject(method = "setOffers", at = @At("TAIL"))
    private void fvtv$onSetOffers(MerchantOffers offers, CallbackInfo ci) {
        if (!Config.isEnabled()) return;
        if (Config.useCompatibilitySafePacketLevel()) {
            int containerId = ((MerchantMenu) (Object) this).containerId;
            int syncedUnlockedCount = TradeSyncState.getUnlockedCount(containerId);
            if (syncedUnlockedCount >= 0) {
                this.fvtv$unlockedTradeCount = syncedUnlockedCount;
            } else if (Config.shouldFailOpenOnTradeSyncError() && offers != null) {
                this.fvtv$unlockedTradeCount = offers.size();
            }
        }
        fvtv$setClientUnlockedTrades(this.fvtv$unlockedTradeCount);
    }
}

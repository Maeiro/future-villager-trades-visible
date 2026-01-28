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
import com.futurevillagertradesvisible.ducks.ClientSideMerchantDuck;
import com.futurevillagertradesvisible.ducks.MerchantMenuDuck;

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
    private int fvtv$unlockedTradeCount = 0;

    @Inject(method = "setMerchantLevel", at = @At("TAIL"))
    private void fvtv$readUnlockedTradeCountFromLevel(int level, CallbackInfo ci) {
        if (!Config.isEnabled()) {
            this.fvtv$unlockedTradeCount = 0;
            this.merchantLevel = level;
            fvtv$setClientUnlockedTrades(0);
            return;
        }
        this.fvtv$unlockedTradeCount = level >> 8;
        fvtv$setClientUnlockedTrades(this.fvtv$unlockedTradeCount);
        this.merchantLevel = level & 255;
    }

    @Override
    public boolean visibleTraders$shouldAllowTrade(int index) {
        if (!Config.isEnabled()) return true;
        return this.fvtv$unlockedTradeCount == 0 || index <= this.fvtv$unlockedTradeCount - 1;
    }

    @Unique
    private void fvtv$setClientUnlockedTrades(int unlockedCount) {
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
    }
}
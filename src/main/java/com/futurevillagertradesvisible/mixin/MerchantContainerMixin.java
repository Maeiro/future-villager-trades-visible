package com.futurevillagertradesvisible.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffers;

import com.futurevillagertradesvisible.Config;
import com.futurevillagertradesvisible.DebugLogger;
import com.futurevillagertradesvisible.ducks.ClientSideMerchantDuck;

@Mixin(MerchantContainer.class)
public class MerchantContainerMixin {

    @Redirect(
            method = "updateSellItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/trading/Merchant;getOffers()Lnet/minecraft/world/item/trading/MerchantOffers;"
            )
    )
    private MerchantOffers fvtv$removeTradeAutoFillIfLocked(Merchant merchant) {
        try {
            MerchantOffers offers = merchant.getOffers();
            if (!Config.isEnabled()) return offers;
            if (merchant instanceof ClientSideMerchantDuck duck) {
                MerchantOffers unlocked = duck.visibleTraders$getClientUnlockedTrades();
                if (unlocked != null) {
                    DebugLogger.info(
                            "MerchantContainerMixin redirect using unlocked offers. {}, {}, unlockedSize={}",
                            DebugLogger.merchantSummary(merchant),
                            DebugLogger.offersSummary(offers),
                            unlocked.size()
                    );
                    return unlocked;
                }
            }
            return offers;
        } catch (RuntimeException e) {
            DebugLogger.error(
                    "MerchantContainerMixin redirect failed. {}",
                    e,
                    DebugLogger.merchantSummary(merchant)
            );
            throw e;
        }
    }
}

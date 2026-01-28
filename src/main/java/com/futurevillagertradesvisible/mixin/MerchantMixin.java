package com.futurevillagertradesvisible.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffers;

import com.futurevillagertradesvisible.Config;
import com.futurevillagertradesvisible.ducks.VillagerDuck;

@Mixin(Merchant.class)
public interface MerchantMixin {

    @Redirect(
            method = "openTradingScreen",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;sendMerchantOffers(ILnet/minecraft/world/item/trading/MerchantOffers;IIZZ)V"
            )
    )
    private void fvtv$sendLockedOffersWithNormalOffersOnScreenOpen(
            Player player,
            int syncId,
            MerchantOffers offers,
            int level,
            int xp,
            boolean showProgress,
            boolean canRestock
    ) {
        if (!Config.isEnabled()) {
            player.sendMerchantOffers(syncId, offers, level, xp, showProgress, canRestock);
            return;
        }
        if ((Object) this instanceof Villager villager) {
            VillagerDuck duck = VillagerDuck.of(villager);
            player.sendMerchantOffers(syncId, duck.visibleTraders$getCombinedOffers(), duck.visibleTraders$getShiftedLevel(), xp, showProgress, canRestock);
        } else {
            player.sendMerchantOffers(syncId, offers, level, xp, showProgress, canRestock);
        }
    }
}
package com.futurevillagertradesvisible.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffers;

import com.futurevillagertradesvisible.Config;
import com.futurevillagertradesvisible.DebugLogger;
import com.futurevillagertradesvisible.ducks.VillagerDuck;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

    @Inject(method = "sendMerchantOffers", at = @At("HEAD"), cancellable = true)
    private void fvtv$sendCombinedOffers(
            int containerId,
            MerchantOffers offers,
            int level,
            int xp,
            boolean showProgress,
            boolean canRestock,
            CallbackInfo ci
    ) {
        try {
            if (!Config.isEnabled()) return;
            ServerPlayer self = (ServerPlayer) (Object) this;
            if (!(self.containerMenu instanceof MerchantMenu menu)) return;
            Merchant trader = ((MerchantMenuAccessor) menu).fvtv$getTrader();
            if (!(trader instanceof Villager villager)) return;
            VillagerDuck duck = VillagerDuck.of(villager);
            MerchantOffers combined = duck.visibleTraders$getCombinedOffers();
            int shiftedLevel = duck.visibleTraders$getShiftedLevel();
            DebugLogger.info(
                    "ServerPlayerMixin sending combined offers containerId={} originalOffers={} combinedOffers={} rawLevel={} shiftedLevel={} villagerUuid={}",
                    containerId,
                    offers.size(),
                    combined.size(),
                    level,
                    shiftedLevel,
                    villager.getUUID()
            );
            self.connection.send(new ClientboundMerchantOffersPacket(containerId, combined, shiftedLevel, xp, showProgress, canRestock));
            ci.cancel();
        } catch (RuntimeException e) {
            DebugLogger.error(
                    "ServerPlayerMixin#sendMerchantOffers failed containerId={} rawLevel={}",
                    e,
                    containerId,
                    level
            );
            throw e;
        }
    }
}

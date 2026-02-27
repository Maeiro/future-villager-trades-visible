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
import com.futurevillagertradesvisible.TradeEligibility;
import com.futurevillagertradesvisible.ducks.VillagerDuck;
import com.futurevillagertradesvisible.network.NetworkHandler;

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
        if (!Config.isEnabled()) return;
        try {
            Merchant trader = fvtv$getCurrentTrader();
            if (!(trader instanceof Villager villager)) return;
            if (!TradeEligibility.shouldApplyFutureTrades(villager)) return;

            ServerPlayer self = (ServerPlayer) (Object) this;
            int unlockedCount = offers == null ? 0 : offers.size();
            VillagerDuck duck = VillagerDuck.of(villager);
            MerchantOffers combined = duck.visibleTraders$getCombinedOffers();
            int shiftedLevel = duck.visibleTraders$getShiftedLevel();
            int packetLevel = Config.useCompatibilitySafePacketLevel() ? level : shiftedLevel;
            NetworkHandler.sendUnlockedCount(self, containerId, unlockedCount);
            DebugLogger.info(
                    "ServerPlayerMixin sending combined offers containerId={} originalOffers={} combinedOffers={} unlockedCount={} rawLevel={} shiftedLevel={} packetLevel={} villagerUuid={}",
                    containerId,
                    offers == null ? -1 : offers.size(),
                    combined.size(),
                    unlockedCount,
                    level,
                    shiftedLevel,
                    packetLevel,
                    villager.getUUID()
            );
            self.connection.send(new ClientboundMerchantOffersPacket(containerId, combined, packetLevel, xp, showProgress, canRestock));
            ci.cancel();
        } catch (Throwable t) {
            DebugLogger.error(
                    "ServerPlayerMixin#sendCombinedOffers failed containerId={} rawLevel={}",
                    t,
                    containerId,
                    level
            );
            if (!Config.shouldFailOpenOnTradeSyncError()) throw fvtv$toRuntime(t);
        }
    }

    private Merchant fvtv$getCurrentTrader() {
        ServerPlayer self = (ServerPlayer) (Object) this;
        if (!(self.containerMenu instanceof MerchantMenu menu)) return null;
        return ((MerchantMenuAccessor) menu).fvtv$getTrader();
    }

    private static RuntimeException fvtv$toRuntime(Throwable t) {
        return t instanceof RuntimeException re ? re : new RuntimeException(t);
    }
}

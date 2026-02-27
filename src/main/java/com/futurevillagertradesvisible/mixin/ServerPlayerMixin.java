package com.futurevillagertradesvisible.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

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

    @Inject(method = "sendMerchantOffers", at = @At("HEAD"))
    private void fvtv$syncUnlockedCount(
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
            NetworkHandler.sendUnlockedCount(self, containerId, unlockedCount);
        } catch (Throwable t) {
            DebugLogger.error(
                    "ServerPlayerMixin#syncUnlockedCount failed containerId={} rawLevel={}",
                    t,
                    containerId,
                    level
            );
            if (!Config.shouldFailOpenOnTradeSyncError()) throw fvtv$toRuntime(t);
        }
    }

    @ModifyArgs(
            method = "sendMerchantOffers",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/protocol/game/ClientboundMerchantOffersPacket;<init>(ILnet/minecraft/world/item/trading/MerchantOffers;IIZZ)V"
            )
    )
    private void fvtv$transformMerchantOffersPacket(
            Args args,
            int containerId,
            MerchantOffers offers,
            int level,
            int xp,
            boolean showProgress,
            boolean canRestock
    ) {
        if (!Config.isEnabled()) return;
        try {
            Merchant trader = fvtv$getCurrentTrader();
            if (!(trader instanceof Villager villager)) return;
            if (!TradeEligibility.shouldApplyFutureTrades(villager)) return;

            VillagerDuck duck = VillagerDuck.of(villager);
            MerchantOffers combined = duck.visibleTraders$getCombinedOffers();
            if (combined == null) return;
            int shiftedLevel = duck.visibleTraders$getShiftedLevel();
            int packetLevel = Config.useCompatibilitySafePacketLevel() ? level : shiftedLevel;

            args.set(1, combined);
            args.set(2, packetLevel);

            DebugLogger.info(
                    "ServerPlayerMixin transformed offers packet containerId={} originalOffers={} combinedOffers={} rawLevel={} shiftedLevel={} packetLevel={} villagerUuid={}",
                    containerId,
                    offers == null ? -1 : offers.size(),
                    combined.size(),
                    level,
                    shiftedLevel,
                    packetLevel,
                    villager.getUUID()
            );
        } catch (Throwable t) {
            DebugLogger.error(
                    "ServerPlayerMixin#transformMerchantOffersPacket failed containerId={} rawLevel={}",
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

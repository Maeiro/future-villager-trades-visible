package com.futurevillagertradesvisible.mixin;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ReputationEventHandler;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;

import com.futurevillagertradesvisible.Config;
import com.futurevillagertradesvisible.LockedTradeData;
import com.futurevillagertradesvisible.ducks.VillagerDuck;

@Mixin(Villager.class)
public abstract class VillagerMixin extends AbstractVillager implements ReputationEventHandler, VillagerDataHolder, VillagerDuck {

    @Shadow
    public abstract VillagerData getVillagerData();

    @Shadow
    protected abstract void updateTrades();

    @Unique
    private LockedTradeData fvtv$lockedTradeData;

    public VillagerMixin(EntityType<? extends AbstractVillager> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("HEAD"))
    private void fvtv$saveLockedTradeData(CompoundTag tag, CallbackInfo ci) {
        if (!fvtv$isEnabled() || this.fvtv$lockedTradeData == null) return;
        this.fvtv$lockedTradeData.save(tag);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void fvtv$readLockedTradeData(CompoundTag tag, CallbackInfo ci) {
        if (!fvtv$isEnabled()) return;
        this.fvtv$lockedTradeData = LockedTradeData.load(tag);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void fvtv$syncLockedTradeData(CallbackInfo ci) {
        if (!fvtv$isEnabled()) {
            this.fvtv$lockedTradeData = null;
            return;
        }
        if (fvtv$getOffersOrNull() == null) {
            this.fvtv$lockedTradeData = null;
            return;
        }
        if (this.fvtv$lockedTradeData != null) {
            this.fvtv$lockedTradeData.tick((Villager) (Object) this, this::fvtv$appendLockedOffer);
        }
    }

    @Inject(method = "updateTrades", at = @At("HEAD"), cancellable = true)
    private void fvtv$preventAdditionalTradesOnRankIncrease(CallbackInfo ci) {
        if (!fvtv$isEnabled()) return;
        MerchantOffers offers = fvtv$getOffersOrNull();
        if (offers == null || offers.isEmpty()) {
            this.fvtv$lockedTradeData = null;
            return;
        }
        if (fvtv$appendLockedOffer()) {
            ci.cancel();
        }
    }

    @Unique
    private boolean fvtv$appendLockedOffer() {
        MerchantOffers offers = fvtv$getOffersOrNull();
        if (offers == null || this.fvtv$lockedTradeData == null) return false;
        MerchantOffers dismissedTrades = this.fvtv$lockedTradeData.popTradeSet();
        if (dismissedTrades == null) return false;
        offers.addAll(dismissedTrades);
        return true;
    }

    @Override
    public void visibleTraders$setLockedTradeData(LockedTradeData data) {
        if (!fvtv$isEnabled()) return;
        this.fvtv$lockedTradeData = data;
    }

    @Override
    public Optional<LockedTradeData> visibleTraders$getLockedTradeData() {
        return fvtv$isEnabled() ? Optional.ofNullable(this.fvtv$lockedTradeData) : Optional.empty();
    }

    @Override
    public void visibleTrades$regenerateTrades() {
        if (!fvtv$isEnabled() || this.level().isClientSide) return;
        this.fvtv$lockedTradeData = new LockedTradeData((Villager) (Object) this);
    }

    @Override
    public int visibleTraders$getShiftedLevel() {
        int level = getVillagerData().getLevel();
        if (!fvtv$isEnabled()) return level;
        MerchantOffers offers = fvtv$getOffersOrNull();
        return offers == null ? level : level | (offers.size() << 8);
    }

    @Override
    public MerchantOffers visibleTraders$getCombinedOffers() {
        MerchantOffers combined = new MerchantOffers();
        MerchantOffers offers = fvtv$getOffersOrNull();
        if (offers != null) {
            combined.addAll(offers);
        }
        if (!fvtv$isEnabled()) return combined;
        if (!this.level().isClientSide) {
            if (this.fvtv$lockedTradeData == null) {
                visibleTrades$regenerateTrades();
            }
            if (this.fvtv$lockedTradeData != null) {
                combined.addAll(this.fvtv$lockedTradeData.buildLockedOffers());
            }
        }
        return combined;
    }

    @Override
    public void visibleTraders$updateTrades() {
        if (fvtv$isEnabled() && this.level() instanceof ServerLevel) {
            updateTrades();
        }
    }

    @Unique
    private MerchantOffers fvtv$getOffersOrNull() {
        return ((AbstractVillagerAccessor) this).fvtv$getOffers();
    }

    @Unique
    private boolean fvtv$isEnabled() {
        return Config.isEnabled();
    }
}

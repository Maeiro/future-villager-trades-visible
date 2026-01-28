package com.futurevillagertradesvisible.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.world.entity.npc.ClientSideMerchant;
import net.minecraft.world.item.trading.MerchantOffers;

import com.futurevillagertradesvisible.ducks.ClientSideMerchantDuck;

@Mixin(ClientSideMerchant.class)
public class ClientSideMerchantMixin implements ClientSideMerchantDuck {

    @Unique
    private MerchantOffers fvtv$clientUnlockedTrades;

    @Override
    public MerchantOffers visibleTraders$getClientUnlockedTrades() {
        return this.fvtv$clientUnlockedTrades;
    }

    @Override
    public void visibleTraders$setClientUnlockedTrades(MerchantOffers offers) {
        this.fvtv$clientUnlockedTrades = offers;
    }
}

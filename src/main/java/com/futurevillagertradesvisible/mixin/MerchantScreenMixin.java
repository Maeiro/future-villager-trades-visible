package com.futurevillagertradesvisible.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.MerchantOffers;

import com.futurevillagertradesvisible.Config;
import com.futurevillagertradesvisible.DebugLogger;
import com.futurevillagertradesvisible.ducks.MerchantMenuDuck;

@Mixin(MerchantScreen.class)
public abstract class MerchantScreenMixin extends AbstractContainerScreen<MerchantMenu> {

    @Shadow
    int scrollOff;

    @Unique
    private boolean fvtv$loggedScreenContext;

    public MerchantScreenMixin(MerchantMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void fvtv$updateButtonStates(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        try {
            if (!Config.isEnabled() || !(this.menu instanceof MerchantMenuDuck duck)) return;
            if (!this.fvtv$loggedScreenContext) {
                this.fvtv$loggedScreenContext = true;
                DebugLogger.info(
                        "MerchantScreen render started menuClass={} renderables={} scrollOff={} offersSize={}",
                        this.menu.getClass().getName(),
                        this.renderables.size(),
                        this.scrollOff,
                        this.menu.getOffers().size()
                );
            }
            int buttonIndex = 0;
            for (Renderable renderable : this.renderables) {
                if (renderable instanceof Button button && button.getWidth() == 88 && button.getHeight() == 20) {
                    int tradeIndex = buttonIndex + this.scrollOff;
                    button.active = duck.visibleTraders$shouldAllowTrade(tradeIndex);
                    buttonIndex++;
                }
            }
        } catch (RuntimeException e) {
            DebugLogger.error(
                    "MerchantScreenMixin#render(HEAD) failed menuClass={} renderables={} scrollOff={}",
                    e,
                    this.menu.getClass().getName(),
                    this.renderables.size(),
                    this.scrollOff
            );
            throw e;
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void fvtv$renderLockedTradesOverlay(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        try {
            if (!Config.isEnabled() || !(this.menu instanceof MerchantMenuDuck duck)) return;
            MerchantOffers offers = this.menu.getOffers();
            int total = offers.size();
            if (total == 0) return;

            int startX = this.leftPos + 5;
            int startY = this.topPos + 16 + 1;

            for (int index = 0; index < total; index++) {
                if (!fvtv$isRowVisible(index, total)) continue;
                if (duck.visibleTraders$shouldAllowTrade(index)) continue;
                int row = index - this.scrollOff;
                int y = startY + (row * 20);
                guiGraphics.fill(startX, y, startX + 88, y + 20, 0x88000000);
            }
        } catch (RuntimeException e) {
            DebugLogger.error(
                    "MerchantScreenMixin#render(TAIL) failed menuClass={} scrollOff={} offersSize={}",
                    e,
                    this.menu.getClass().getName(),
                    this.scrollOff,
                    this.menu.getOffers().size()
            );
            throw e;
        }
    }

    @Unique
    private boolean fvtv$isRowVisible(int index, int total) {
        return total <= 7 || (index >= this.scrollOff && index < 7 + this.scrollOff);
    }
}

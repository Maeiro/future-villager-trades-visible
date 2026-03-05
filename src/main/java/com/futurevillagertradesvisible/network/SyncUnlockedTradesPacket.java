package com.futurevillagertradesvisible.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.DistExecutor;

public record SyncUnlockedTradesPacket(int containerId, int unlockedCount) {

    public static void encode(SyncUnlockedTradesPacket message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.containerId());
        buffer.writeVarInt(message.unlockedCount());
    }

    public static SyncUnlockedTradesPacket decode(FriendlyByteBuf buffer) {
        return new SyncUnlockedTradesPacket(buffer.readVarInt(), buffer.readVarInt());
    }

    public static void handle(SyncUnlockedTradesPacket message, CustomPayloadEvent.Context context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ClientTradeSyncHandler.handleUnlockedTradeCount(message.containerId(), message.unlockedCount())
        );
    }
}


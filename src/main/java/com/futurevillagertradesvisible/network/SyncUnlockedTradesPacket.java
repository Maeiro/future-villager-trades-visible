package com.futurevillagertradesvisible.network;

import java.util.function.Supplier;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraft.network.FriendlyByteBuf;

public record SyncUnlockedTradesPacket(int containerId, int unlockedCount) {

    public static void encode(SyncUnlockedTradesPacket message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.containerId());
        buffer.writeVarInt(message.unlockedCount());
    }

    public static SyncUnlockedTradesPacket decode(FriendlyByteBuf buffer) {
        return new SyncUnlockedTradesPacket(buffer.readVarInt(), buffer.readVarInt());
    }

    public static void handle(SyncUnlockedTradesPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ClientTradeSyncHandler.handleUnlockedTradeCount(message.containerId(), message.unlockedCount())
        ));
        context.setPacketHandled(true);
    }
}

package com.futurevillagertradesvisible.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import com.futurevillagertradesvisible.FutureVillagerTradesVisible;

public final class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    private static int packetId = 0;

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(FutureVillagerTradesVisible.MODID, "network"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private NetworkHandler() {
    }

    public static void register() {
        CHANNEL.registerMessage(
                packetId++,
                SyncUnlockedTradesPacket.class,
                SyncUnlockedTradesPacket::encode,
                SyncUnlockedTradesPacket::decode,
                SyncUnlockedTradesPacket::handle,
                java.util.Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
    }

    public static void sendUnlockedCount(ServerPlayer player, int containerId, int unlockedCount) {
        CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new SyncUnlockedTradesPacket(containerId, unlockedCount)
        );
    }
}

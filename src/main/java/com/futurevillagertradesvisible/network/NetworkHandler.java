package com.futurevillagertradesvisible.network;

import java.util.Objects;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

import com.futurevillagertradesvisible.FutureVillagerTradesVisible;

public final class NetworkHandler {
    private static final int PROTOCOL_VERSION = 1;
    private static final ResourceLocation CHANNEL_ID = Objects.requireNonNull(
            ResourceLocation.tryParse(FutureVillagerTradesVisible.MODID + ":network"),
            "Invalid FVTV network channel id"
    );

    public static final SimpleChannel CHANNEL = ChannelBuilder
            .named(CHANNEL_ID)
            .networkProtocolVersion(PROTOCOL_VERSION)
            .clientAcceptedVersions(Channel.VersionTest.exact(PROTOCOL_VERSION))
            .serverAcceptedVersions(Channel.VersionTest.exact(PROTOCOL_VERSION))
            .simpleChannel();

    private static int packetId = 0;

    private NetworkHandler() {
    }

    public static void register() {
        CHANNEL.messageBuilder(SyncUnlockedTradesPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncUnlockedTradesPacket::encode)
                .decoder(SyncUnlockedTradesPacket::decode)
                .consumerMainThread(SyncUnlockedTradesPacket::handle)
                .add();
    }

    public static void sendUnlockedCount(ServerPlayer player, int containerId, int unlockedCount) {
        CHANNEL.send(new SyncUnlockedTradesPacket(containerId, unlockedCount), PacketDistributor.PLAYER.with(player));
    }
}


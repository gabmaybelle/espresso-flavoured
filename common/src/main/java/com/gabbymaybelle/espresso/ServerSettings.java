package com.gabbymaybelle.espresso;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * The server's settings that players' games need to agree on. Sent when a player joins, so on a
 * server everyone's bundles hold the same number of tools, whatever their own settings say.
 */
public record ServerSettings(int bundleUnstackables) implements CustomPacketPayload {
	public static final Type<ServerSettings> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Espresso.MOD_ID, "server_settings"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ServerSettings> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, ServerSettings::bundleUnstackables, ServerSettings::new);

	@Override
	public Type<ServerSettings> type() {
		return TYPE;
	}

	static void register() {
		// The message itself is registered by each loader (it needs the loader's networking).
		Platform.get().onPlayerJoin(player -> {
			if (Platform.get().canSend(player, TYPE)) {
				Platform.get().send(player, new ServerSettings(Config.bundleUnstackables));
			}
		});
	}

	/** Sends the current settings to everyone, after the host changes them in a LAN world. */
	public static void broadcast(MinecraftServer server) {
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			if (Platform.get().canSend(player, TYPE)) {
				Platform.get().send(player, new ServerSettings(Config.bundleUnstackables));
			}
		}
	}
}

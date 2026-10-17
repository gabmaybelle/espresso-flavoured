package com.gabbymaybelle.espresso;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Everything Espresso Flavoured Core asks of the mod loader: saved data, game events, networking,
 * creative tabs, loot tables and village jobs. The Fabric and NeoForge jars each set this first
 * thing, before any other Espresso class is used, so the rest of the mod is the same code on both.
 *
 * <p>The register methods are all called while the mod starts up; each loader hooks the handler into
 * its own events.
 */
public final class Platform {
	/**
	 * A value saved on a player or on a world (the overworld), kept the loader's own way: a Fabric
	 * data attachment (so worlds made before NeoForge keep theirs) or a NeoForge attachment. The
	 * holder is an entity or a {@link net.minecraft.server.level.ServerLevel}.
	 */
	public interface Data<T> {
		T getOrElse(Object holder, T fallback);

		/** The saved value, or a fresh one (stored) when there is none. */
		T getOrCreate(Object holder);

		boolean has(Object holder);

		void set(Object holder, T value);

		void remove(Object holder);
	}

	/** What a creative tab accepts. */
	public interface TabOutput {
		void accept(ItemLike item);
	}

	/** Right-clicking a block, on either side; {@link InteractionResult#PASS} lets the click carry on. */
	public interface UseBlock {
		InteractionResult use(Player player, Level level, InteractionHand hand, BlockHitResult hit);
	}

	/** Called as a loot table loads; {@code ops} reads JSON that names registry entries, and {@code add} puts another pool in the table. */
	public interface LootTableLoad {
		void modify(Identifier table, RegistryOps<JsonElement> ops, Consumer<LootPool> add);
	}

	/** What each loader answers. */
	public interface Hooks {
		/** The game's config folder. */
		Path configDir();

		/** Whether a mod with this id is installed. */
		boolean isModLoaded(String id);

		/**
		 * Saved data on a player or world. {@code initial} makes a fresh value (and is what an
		 * unset value reads as where a default is needed).
		 */
		<T> Data<T> data(String name, Codec<T> codec, Supplier<T> initial, boolean copyOnDeath);

		void onServerStarting(Consumer<MinecraftServer> handler);

		void onServerStarted(Consumer<MinecraftServer> handler);

		void onServerStopped(Consumer<MinecraftServer> handler);

		/** After the data packs have been (re)loaded on a running server. */
		void onDataReload(Consumer<MinecraftServer> handler);

		/** At the end of every server tick. */
		void onServerTick(Consumer<MinecraftServer> handler);

		void onUseBlock(UseBlock handler);

		void onPlayerJoin(Consumer<ServerPlayer> handler);

		/** Adds items to one of the vanilla creative tabs. */
		void onCreativeTab(ResourceKey<CreativeModeTab> tab, Consumer<TabOutput> handler);

		void onLootTableLoad(LootTableLoad handler);

		/** Whether this player's game understands this message (it has the mod too). */
		boolean canSend(ServerPlayer player, CustomPacketPayload.Type<?> type);

		void send(ServerPlayer player, CustomPacketPayload payload);

		/** A job site point of interest (the barista's espresso machine). Called while the content registers. */
		void registerPoi(Identifier id, int tickets, int searchDistance, Block block);
	}

	private static Hooks hooks;

	private Platform() {
	}

	public static void set(Hooks loaderHooks) {
		hooks = loaderHooks;
	}

	public static Hooks get() {
		return hooks;
	}

	/** Shorthand for the hooks' {@code data}. */
	public static <T> Data<T> data(String name, Codec<T> codec, Supplier<T> initial, boolean copyOnDeath) {
		return hooks.data(name, codec, initial, copyOnDeath);
	}
}

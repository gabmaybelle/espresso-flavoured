package com.gabbymaybelle.espresso.fabric;

import com.gabbymaybelle.espresso.Espresso;
import com.gabbymaybelle.espresso.Platform;
import com.gabbymaybelle.espresso.ServerSettings;
import com.gabbymaybelle.espresso.compat.TrinketsCompat;
import com.gabbymaybelle.espresso.content.CharmItem;
import com.gabbymaybelle.espresso.content.EspressoContent;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PoiHelper;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/** Fabric's way in: Fabric API's events, attachments, networking, item storage and built-in packs, passed on to the shared code. */
public class EspressoFabric implements ModInitializer {
	@Override
	public void onInitialize() {
		Platform.set(new Platform.Hooks() {
			@Override
			public Path configDir() {
				return FabricLoader.getInstance().getConfigDir();
			}

			@Override
			public boolean isModLoaded(String id) {
				return FabricLoader.getInstance().isModLoaded(id);
			}

			@Override
			public <T> Platform.Data<T> data(String name, Codec<T> codec, Supplier<T> initial, boolean copyOnDeath) {
				// The same ids as before NeoForge, so worlds made with the Fabric-only mod keep their data.
				AttachmentType<T> type = AttachmentRegistry.create(Identifier.fromNamespaceAndPath(Espresso.MOD_ID, name), builder -> {
					builder.persistent(codec).initializer(initial);
					if (copyOnDeath) {
						builder.copyOnDeath();
					}
				});
				return new Platform.Data<>() {
					@Override
					public T getOrElse(Object holder, T fallback) {
						return ((AttachmentTarget) holder).getAttachedOrElse(type, fallback);
					}

					@Override
					public T getOrCreate(Object holder) {
						return ((AttachmentTarget) holder).getAttachedOrCreate(type);
					}

					@Override
					public boolean has(Object holder) {
						return ((AttachmentTarget) holder).hasAttached(type);
					}

					@Override
					public void set(Object holder, T value) {
						((AttachmentTarget) holder).setAttached(type, value);
					}

					@Override
					public void remove(Object holder) {
						((AttachmentTarget) holder).removeAttached(type);
					}
				};
			}

			@Override
			public void onServerStarting(Consumer<MinecraftServer> handler) {
				ServerLifecycleEvents.SERVER_STARTING.register(handler::accept);
			}

			@Override
			public void onServerStarted(Consumer<MinecraftServer> handler) {
				ServerLifecycleEvents.SERVER_STARTED.register(handler::accept);
			}

			@Override
			public void onServerStopped(Consumer<MinecraftServer> handler) {
				ServerLifecycleEvents.SERVER_STOPPED.register(handler::accept);
			}

			@Override
			public void onDataReload(Consumer<MinecraftServer> handler) {
				ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resources, success) -> handler.accept(server));
			}

			@Override
			public void onServerTick(Consumer<MinecraftServer> handler) {
				ServerTickEvents.END_SERVER_TICK.register(handler::accept);
			}

			@Override
			public void onUseBlock(Platform.UseBlock handler) {
				UseBlockCallback.EVENT.register(handler::use);
			}

			@Override
			public void onPlayerJoin(Consumer<ServerPlayer> handler) {
				ServerPlayConnectionEvents.JOIN.register((connection, sender, server) -> handler.accept(connection.player));
			}

			@Override
			public void onCreativeTab(ResourceKey<CreativeModeTab> tab, Consumer<Platform.TabOutput> handler) {
				CreativeModeTabEvents.modifyOutputEvent(tab).register(output -> handler.accept(output::accept));
			}

			@Override
			public void onLootTableLoad(Platform.LootTableLoad handler) {
				LootTableEvents.MODIFY.register((key, table, source, registries) -> {
					if (source.isBuiltin()) {
						handler.modify(key.identifier(), registries.createSerializationContext(JsonOps.INSTANCE), table::pool);
					}
				});
			}

			@Override
			public boolean canSend(ServerPlayer player, CustomPacketPayload.Type<?> type) {
				return ServerPlayNetworking.canSend(player, type.id());
			}

			@Override
			public void send(ServerPlayer player, CustomPacketPayload payload) {
				ServerPlayNetworking.send(player, payload);
			}

			@Override
			public void registerPoi(Identifier id, int tickets, int searchDistance, Block block) {
				PoiHelper.register(id, tickets, searchDistance, block);
			}
		});

		Espresso.init();
		// Registers every block, item and effect (Fabric lets a mod do it at once).
		EspressoContent.load();
		PayloadTypeRegistry.clientboundPlay().register(ServerSettings.TYPE, ServerSettings.CODEC);
		ItemStorage.SIDED.registerForBlockEntity((canister, side) -> new CanisterStorage(canister), EspressoContent.CANISTER_ENTITY);
		// Wild coffee bushes in jungles and savannas (NeoForge does this with data-driven biome modifiers).
		BiomeModifications.addFeature(BiomeSelectors.tag(BiomeTags.IS_JUNGLE).or(BiomeSelectors.tag(BiomeTags.IS_SAVANNA)),
				GenerationStep.Decoration.VEGETAL_DECORATION,
				ResourceKey.<PlacedFeature>create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(Espresso.MOD_ID, "patch_coffee_bush")));

		// Charms go in Trinkets Updated's slots when it's installed (and on with a right-click).
		if (FabricLoader.getInstance().isModLoaded("trinkets_updated")) {
			CharmItem.useSlots(new TrinketsCompat());
			TrinketsCompat.equipOnUse(EspressoContent.CHARMS);
		}

		ModContainer mod = FabricLoader.getInstance().getModContainer(Espresso.MOD_ID).orElseThrow();
		for (Espresso.BuiltInPack pack : Espresso.builtInPacks()) {
			ResourceLoader.registerBuiltinPack(Identifier.fromNamespaceAndPath(Espresso.MOD_ID, pack.folder()), mod,
					Component.translatable(pack.nameKey()), pack.required() ? PackActivationType.ALWAYS_ENABLED : PackActivationType.DEFAULT_ENABLED);
		}
	}
}

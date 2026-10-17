package com.gabbymaybelle.espresso.neoforge;

import com.gabbymaybelle.espresso.Espresso;
import com.gabbymaybelle.espresso.Platform;
import com.gabbymaybelle.espresso.ServerSettings;
import com.gabbymaybelle.espresso.client.EspressoClient;
import com.gabbymaybelle.espresso.content.CharmItem;
import com.gabbymaybelle.espresso.content.EspressoContent;
import com.mojang.serialization.Codec;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import com.mojang.serialization.JsonOps;
import java.util.Optional;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper;

/**
 * NeoForge's way in: NeoForge's events, attachments, networking, item handlers and built-in packs,
 * passed on to the shared code. Runs on both sides; {@link EspressoNeoForgeClient} adds the client's part.
 */
@Mod(Espresso.MOD_ID)
public class EspressoNeoForge {
	private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Espresso.MOD_ID);
	private static final List<CreativeTabHandler> TAB_HANDLERS = new ArrayList<>();
	private static boolean contentLoaded;

	private record CreativeTabHandler(ResourceKey<CreativeModeTab> tab, Consumer<Platform.TabOutput> handler) {
	}

	public EspressoNeoForge(IEventBus modBus) {
		Platform.set(new Platform.Hooks() {
			@Override
			public Path configDir() {
				return FMLPaths.CONFIGDIR.get();
			}

			@Override
			public boolean isModLoaded(String id) {
				return ModList.get().isLoaded(id);
			}

			@Override
			public <T> Platform.Data<T> data(String name, Codec<T> codec, Supplier<T> initial, boolean copyOnDeath) {
				Supplier<AttachmentType<T>> type = ATTACHMENTS.register(name, () -> {
					AttachmentType.Builder<T> builder = AttachmentType.builder(initial).serialize(codec.fieldOf("value"));
					if (copyOnDeath) {
						builder.copyOnDeath();
					}
					return builder.build();
				});
				return new Platform.Data<>() {
					@Override
					public T getOrElse(Object holder, T fallback) {
						return ((IAttachmentHolder) holder).getExistingData(type.get()).orElse(fallback);
					}

					@Override
					public T getOrCreate(Object holder) {
						return ((IAttachmentHolder) holder).getData(type.get());
					}

					@Override
					public boolean has(Object holder) {
						return ((IAttachmentHolder) holder).hasData(type.get());
					}

					@Override
					public void set(Object holder, T value) {
						((IAttachmentHolder) holder).setData(type.get(), value);
					}

					@Override
					public void remove(Object holder) {
						((IAttachmentHolder) holder).removeData(type.get());
					}
				};
			}

			@Override
			public void onServerStarting(Consumer<MinecraftServer> handler) {
				NeoForge.EVENT_BUS.addListener((ServerAboutToStartEvent event) -> handler.accept(event.getServer()));
			}

			@Override
			public void onServerStarted(Consumer<MinecraftServer> handler) {
				NeoForge.EVENT_BUS.addListener((ServerStartedEvent event) -> handler.accept(event.getServer()));
			}

			@Override
			public void onServerStopped(Consumer<MinecraftServer> handler) {
				NeoForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> handler.accept(event.getServer()));
			}

			@Override
			public void onDataReload(Consumer<MinecraftServer> handler) {
				NeoForge.EVENT_BUS.addListener((OnDatapackSyncEvent event) -> handler.accept(event.getPlayerList().getServer()));
			}

			@Override
			public void onServerTick(Consumer<MinecraftServer> handler) {
				NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> handler.accept(event.getServer()));
			}

			@Override
			public void onUseBlock(Platform.UseBlock handler) {
				NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.RightClickBlock event) -> {
					var result = handler.use(event.getEntity(), event.getLevel(), event.getHand(), event.getHitVec());
					if (result != net.minecraft.world.InteractionResult.PASS) {
						event.setCancellationResult(result);
						event.setCanceled(true);
					}
				});
			}

			@Override
			public void onPlayerJoin(Consumer<ServerPlayer> handler) {
				NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
					if (event.getEntity() instanceof ServerPlayer player) {
						handler.accept(player);
					}
				});
			}

			@Override
			public void onCreativeTab(ResourceKey<CreativeModeTab> tab, Consumer<Platform.TabOutput> handler) {
				TAB_HANDLERS.add(new CreativeTabHandler(tab, handler));
			}

			@Override
			public void onLootTableLoad(Platform.LootTableLoad handler) {
				NeoForge.EVENT_BUS.addListener((LootTableLoadEvent event) -> {
					HolderGetter.Provider registries = event.getRegistries();
					// Reads the names of registry entries (items, loot functions...) from the registries the table is loading with.
					RegistryOps.RegistryInfoLookup lookup = new RegistryOps.RegistryInfoLookup() {
						@Override
						@SuppressWarnings("unchecked")
						public <E> Optional<HolderGetter<E>> lookup(ResourceKey<? extends Registry<? extends E>> registryKey) {
							return registries.lookup(registryKey).map(getter -> (HolderGetter<E>) getter);
						}
					};
					handler.modify(event.getName(), RegistryOps.create(JsonOps.INSTANCE, lookup), event.getTable()::addPool);
				});
			}

			@Override
			public boolean canSend(ServerPlayer player, CustomPacketPayload.Type<?> type) {
				return player.connection.hasChannel(type);
			}

			@Override
			public void send(ServerPlayer player, CustomPacketPayload payload) {
				PacketDistributor.sendToPlayer(player, payload);
			}

			@Override
			public void registerPoi(Identifier id, int tickets, int searchDistance, Block block) {
				// NeoForge notes the blocks' states when the type is added to the registry.
				net.minecraft.core.Registry.register(BuiltInRegistries.POINT_OF_INTEREST_TYPE, id,
						new PoiType(com.google.common.collect.ImmutableSet.copyOf(block.getStateDefinition().getPossibleStates()), tickets, searchDistance));
			}
		});

		Espresso.init();

		ATTACHMENTS.register(modBus);
		modBus.addListener(RegisterEvent.class, EspressoNeoForge::registerContent);
		modBus.addListener(EspressoNeoForge::payloads);
		modBus.addListener(EspressoNeoForge::packs);
		modBus.addListener(EspressoNeoForge::creativeTabs);
		modBus.addListener(EspressoNeoForge::capabilities);
		// Charms go in Curios' slots when it's installed (and on with a right-click).
		if (ModList.get().isLoaded("curios")) {
			modBus.addListener((FMLCommonSetupEvent event) -> event.enqueueWork(() -> {
				CharmItem.useSlots(new CuriosCompat());
				CuriosCompat.equipOnUse();
			}));
		}
	}

	/**
	 * NeoForge only lets a mod add blocks, items and the like while its registry events run, so the
	 * content class (whose fields register everything as it loads) is loaded here, once.
	 */
	private static void registerContent(RegisterEvent event) {
		if (!contentLoaded) {
			contentLoaded = true;
			EspressoContent.load();
		}
	}

	/** Optional, so a player without Espresso Flavoured Core can still join. */
	private static void payloads(RegisterPayloadHandlersEvent event) {
		event.registrar("1").optional().playToClient(ServerSettings.TYPE, ServerSettings.CODEC,
				(payload, context) -> context.enqueueWork(() -> EspressoClient.onServerSettings(payload)));
	}

	/** Recipes, advancements, the starter kit and trades; the title-screen look; and the longer days. */
	private static void packs(AddPackFindersEvent event) {
		for (Espresso.BuiltInPack pack : Espresso.builtInPacks()) {
			PackType type = pack.serverData() ? PackType.SERVER_DATA : PackType.CLIENT_RESOURCES;
			// The title-screen look can't be a switch-off-able default pack here, so it is always on.
			event.addPackFinders(Identifier.fromNamespaceAndPath(Espresso.MOD_ID, "resourcepacks/" + pack.folder()), type,
					Component.translatable(pack.nameKey()), PackSource.BUILT_IN, true, Pack.Position.TOP);
		}
	}

	private static void creativeTabs(BuildCreativeModeTabContentsEvent event) {
		for (CreativeTabHandler entry : TAB_HANDLERS) {
			if (event.getTabKey().equals(entry.tab())) {
				entry.handler().accept(event::accept);
			}
		}
	}

	private static void capabilities(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(Capabilities.Item.BLOCK, EspressoContent.CANISTER_ENTITY, (canister, side) -> new CanisterHandler(canister));
		event.registerBlockEntity(Capabilities.Item.BLOCK, EspressoContent.ESPRESSO_MACHINE_ENTITY, (machine, side) -> new WorldlyContainerWrapper(machine, side));
		event.registerBlockEntity(Capabilities.Item.BLOCK, EspressoContent.COFFEE_BAR_ENTITY, (bar, side) -> VanillaContainerWrapper.of(bar));
	}
}

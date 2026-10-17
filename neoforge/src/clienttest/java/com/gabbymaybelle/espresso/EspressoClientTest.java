package com.gabbymaybelle.espresso;

import static com.gabbymaybelle.espresso.ClientTest.check;
import static com.gabbymaybelle.espresso.ClientTest.createWorld;
import static com.gabbymaybelle.espresso.ClientTest.onServer;
import static com.gabbymaybelle.espresso.ClientTest.run;
import static com.gabbymaybelle.espresso.ClientTest.screenshot;
import static com.gabbymaybelle.espresso.ClientTest.waitTicks;
import static com.gabbymaybelle.espresso.ClientTest.waitUntil;

import com.gabbymaybelle.espresso.content.CanisterBlockEntity;
import com.gabbymaybelle.espresso.content.CharmItem;
import com.gabbymaybelle.espresso.content.EspressoContent;
import com.gabbymaybelle.espresso.content.EspressoMachineBlockEntity;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/**
 * The NeoForge jar's in-game test: the café basics, the same ones the Fabric game test
 * (fabric/src/gametest) plays through, plus what is NeoForge's own wiring: content registering in
 * the registry events, data in NeoForge attachments, the built-in packs, the biome modifier, loot
 * injection, item handlers for hoppers and pipes, and the rules reached through mixins (sleeping,
 * the Ember Glove). A failed check crashes the game, so the Gradle task fails.
 */
final class EspressoClientTest {
	private static volatile BlockPos at;

	private EspressoClientTest() {
	}

	static void steps() {
		createWorld("espresso-clienttest");

		onServer("the content and the packs", server -> {
			StringBuilder out = new StringBuilder();
			expect(out, BuiltInRegistries.ITEM.containsKey(id("espresso")), "the Espresso item is registered");
			expect(out, BuiltInRegistries.BLOCK.containsKey(id("espresso_machine")), "the espresso machine block is registered");
			expect(out, BuiltInRegistries.MOB_EFFECT.containsKey(id("drowsy")), "the Drowsy effect is registered");
			expect(out, BuiltInRegistries.ENTITY_TYPE.containsKey(id("seat")), "the seat entity is registered");
			expect(out, BuiltInRegistries.MENU.containsKey(id("espresso_machine")), "the machine's menu is registered");
			expect(out, BuiltInRegistries.VILLAGER_PROFESSION.containsKey(id("barista")), "the barista profession is registered");
			expect(out, server.getAdvancements().get(id("guide/root")) != null, "the built-in data pack's advancements load");
			expect(out, server.reloadableRegistries().getLootTable(net.minecraft.resources.ResourceKey.create(Registries.LOOT_TABLE, id("chests/spawn_kit"))) != LootTable.EMPTY,
					"the starter kit's loot table loads");
			ServerLevel level = server.overworld();
			// The espresso machine is the barista's workstation.
			expect(out, net.minecraft.world.entity.ai.village.poi.PoiTypes.forState(EspressoContent.ESPRESSO_MACHINE.defaultBlockState()).isPresent(),
					"the espresso machine is a job site");
			// The wild coffee bushes come from the data-driven biome modifier.
			Holder<net.minecraft.world.level.biome.Biome> jungle = server.registryAccess().lookupOrThrow(Registries.BIOME)
					.getOrThrow(net.minecraft.world.level.biome.Biomes.JUNGLE);
			boolean bushes = jungle.value().getGenerationSettings().features().stream().anyMatch(step -> step.stream()
					.anyMatch(feature -> feature.unwrapKey().map(key -> key.identifier().equals(id("patch_coffee_bush"))).orElse(false)));
			expect(out, bushes, "jungles get wild coffee bushes");
			// Spawn Gazebo is bundled and fills its starter chest with Espresso's kit.
			CompoundTag spawn = server.getCommandStorage().get(Identifier.fromNamespaceAndPath("spawn_gazebo", "spawn"));
			expect(out, spawn.getBooleanOr("chest", false), "a new world gets the gazebo with its starter chest");
			expect(out, (Espresso.MOD_ID + ":chests/spawn_kit").equals(spawn.getStringOr("table", "")),
					"the chest holds Espresso's starter kit (got " + spawn.getStringOr("table", "nothing") + ")");
			expect(out, spawn.getBooleanOr("claimed", false), "the first player to join claims the chest's kit");
			int radius = level.getGameRules().get(GameRules.RESPAWN_RADIUS);
			expect(out, radius == 0, "the spawn radius is 0 (got " + radius + ")");
			at = level.getRespawnData().pos().offset(40, 0, 40);
			return out.toString();
		});
		screenshot();

		onServer("loot from the village houses", server -> {
			// Jungle temples sometimes hold coffee: roll the chest many times until some turns up.
			ServerLevel level = server.overworld();
			LootTable table = server.reloadableRegistries().getLootTable(net.minecraft.resources.ResourceKey.create(Registries.LOOT_TABLE,
					Identifier.withDefaultNamespace("chests/jungle_temple")));
			boolean found = false;
			for (int i = 0; i < 600 && !found; i++) {
				LootParams params = new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, Vec3.ZERO).create(LootContextParamSets.CHEST);
				for (ItemStack stack : table.getRandomItems(params)) {
					found |= BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals(Espresso.MOD_ID);
				}
			}
			return found ? "" : "\n  coffee never turned up in 600 jungle temple chests";
		});

		onServer("the café's village house and the sleeping rule", server -> {
			StringBuilder out = new StringBuilder();
			var pool = server.registryAccess().lookupOrThrow(Registries.TEMPLATE_POOL)
					.getValue(Identifier.withDefaultNamespace("village/plains/houses"));
			boolean inPool = ((com.gabbymaybelle.espresso.mixin.StructureTemplatePoolAccessor) pool).espresso$templates().stream()
					.anyMatch(element -> element.toString().contains(Espresso.MOD_ID + ":village/plains/cafe"));
			expect(out, inPool, "the café is in the plains village houses pool");
			return out.toString();
		});

		// A machine on a coffee bar brews; one off a bar doesn't. Hoppers and pipes reach the canister.
		onServer("setting up the café", server -> {
			ServerLevel level = server.overworld();
			BlockPos bar = at;
			level.setBlockAndUpdate(bar.below(), Blocks.STONE.defaultBlockState());
			level.setBlockAndUpdate(bar, EspressoContent.COFFEE_BARS.getFirst().defaultBlockState());
			level.setBlockAndUpdate(bar.above(), EspressoContent.ESPRESSO_MACHINE.defaultBlockState());
			fill(level, bar.above(), new ItemStack(EspressoContent.ROASTED_COFFEE_BEANS, 3));
			BlockPos bare = bar.offset(3, 0, 0);
			level.setBlockAndUpdate(bare.below(), Blocks.STONE.defaultBlockState());
			level.setBlockAndUpdate(bare, EspressoContent.ESPRESSO_MACHINE.defaultBlockState());
			fill(level, bare, new ItemStack(EspressoContent.ROASTED_COFFEE_BEANS, 3));
			return "";
		});
		waitTicks(30);
		onServer("the machine on a coffee bar starts brewing", server -> machine(server, at.above()).brewTime() > 0 ? "" : "\n  not brewing");
		screenshot();
		waitUntil("three Espressos", EspressoMachineBlockEntity.BREW_TIME + 200, mc -> {
			MinecraftServer server = mc.getSingleplayerServer();
			// Asked on the server's own thread: it owns the world.
			return server.submit(() -> {
				EspressoMachineBlockEntity m = machine(server, at.above());
				return m.brewTime() == 0 && m.getItem(0).is(EspressoContent.ESPRESSO);
			}).join();
		});
		onServer("what the machine brewed", server -> {
			StringBuilder out = new StringBuilder();
			EspressoMachineBlockEntity m = machine(server, at.above());
			for (int slot = 0; slot < 3; slot++) {
				expect(out, m.getItem(slot).is(EspressoContent.ESPRESSO), "mug " + slot + " became an Espresso (got " + m.getItem(slot) + ")");
			}
			expect(out, m.getItem(EspressoMachineBlockEntity.GROUNDS_SLOT).getCount() == 3, "three shots left three grounds in the knock box");
			EspressoMachineBlockEntity off = machine(server, at.offset(3, 0, 0));
			expect(out, off.brewTime() == 0 && off.getItem(0).is(EspressoContent.MUG), "a machine off a coffee bar doesn't brew");
			return out.toString();
		});

		onServer("mugs, canisters, the Ember Glove and sleeping", server -> {
			StringBuilder out = new StringBuilder();
			ServerLevel level = server.overworld();
			ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
			player.setGameMode(GameType.SURVIVAL);

			// A mug: crouch and use it on a surface to set it down; use it again to pick it up.
			BlockPos table = at.offset(-2, 1, 0);
			level.setBlockAndUpdate(table.below(), Blocks.STONE.defaultBlockState());
			level.setBlockAndUpdate(table, Blocks.OAK_PLANKS.defaultBlockState());
			player.setShiftKeyDown(true);
			use(player, level, new ItemStack(EspressoContent.ESPRESSO), table);
			player.setShiftKeyDown(false);
			expect(out, level.getBlockState(table.above()).is(EspressoContent.PLACED_MUG), "crouch-using an Espresso sets the mug down");
			use(player, level, ItemStack.EMPTY, table.above());
			expect(out, level.getBlockState(table.above()).isAir(), "using the set-down mug picks it up");
			expect(out, player.getMainHandItem().is(EspressoContent.ESPRESSO), "and the same Espresso comes back (got " + player.getMainHandItem() + ")");
			player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);

			// A canister: put beans in with the hand, take them out through the item handler.
			BlockPos canister = at.offset(-2, 1, 2);
			level.setBlockAndUpdate(canister.below(), Blocks.STONE.defaultBlockState());
			level.setBlockAndUpdate(canister, EspressoContent.CANISTER_BLOCK.defaultBlockState());
			use(player, level, new ItemStack(EspressoContent.ROASTED_COFFEE_BEANS, 20), canister);
			expect(out, player.getMainHandItem().isEmpty(), "the canister takes the beans");
			CanisterBlockEntity canisterEntity = (CanisterBlockEntity) level.getBlockEntity(canister);
			expect(out, canisterEntity.amount() == 20, "and holds twenty (got " + canisterEntity.amount() + ")");
			var handler = level.getCapability(Capabilities.Item.BLOCK, canister, null);
			expect(out, handler != null, "the canister has an item handler for hoppers and pipes");
			if (handler != null) {
				try (Transaction transaction = Transaction.openRoot()) {
					int extracted = handler.extract(ItemResource.of(new ItemStack(EspressoContent.ROASTED_COFFEE_BEANS)), 5, transaction);
					expect(out, extracted == 5, "a pipe takes five beans out (got " + extracted + ")");
				}
				expect(out, canisterEntity.amount() == 20, "a pipe's rolled-back transaction leaves them in (got " + canisterEntity.amount() + ")");
				try (Transaction transaction = Transaction.openRoot()) {
					handler.extract(ItemResource.of(new ItemStack(EspressoContent.ROASTED_COFFEE_BEANS)), 5, transaction);
					handler.insert(ItemResource.of(new ItemStack(Items.DIAMOND_PICKAXE)), 1, transaction);
					transaction.commit();
				}
				expect(out, canisterEntity.amount() == 15, "a committed transaction takes them (got " + canisterEntity.amount() + ")");
				try (Transaction transaction = Transaction.openRoot()) {
					int inserted = handler.insert(ItemResource.of(new ItemStack(Items.DIAMOND_PICKAXE)), 1, transaction);
					expect(out, inserted == 0, "a canister takes only stackable items, and one kind at a time (took " + inserted + ")");
				}
			}

			// The Ember Glove, worn in the off-hand, smelts what a log drops; crouching keeps the log.
			BlockPos log = at.offset(-4, 1, 0);
			level.setBlockAndUpdate(log.below(), Blocks.STONE.defaultBlockState());
			player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(EspressoContent.EMBER_GLOVE));
			expect(out, CharmItem.slots() == null, "NeoForge charms are worn in the off-hand");
			ItemStack smelted = dropsFromLog(level, player, log, false);
			expect(out, smelted.is(Items.CHARCOAL), "the Ember Glove smelts a log's drop into charcoal (got " + smelted + ")");
			ItemStack kept = dropsFromLog(level, player, log, true);
			expect(out, kept.is(Items.OAK_LOG), "crouching keeps the log (got " + kept + ")");
			player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
			ItemStack plain = dropsFromLog(level, player, log, false);
			expect(out, plain.is(Items.OAK_LOG), "without the glove the log drops as a log (got " + plain + ")");

			// Your home: crouch and use a bed with an empty hand. Then lying down in it puts the "days away" count back to zero.
			BlockPos floor = at.offset(-6, 0, 0);
			level.setBlockAndUpdate(floor, Blocks.STONE.defaultBlockState());
			level.setBlockAndUpdate(floor.above(), Blocks.AIR.defaultBlockState());
			level.setBlockAndUpdate(floor.above().south(), Blocks.AIR.defaultBlockState());
			level.setBlockAndUpdate(floor.south(), Blocks.STONE.defaultBlockState());
			player.teleportTo(floor.getX() + 0.5, floor.getY() + 1, floor.getZ() - 1.5);
			use(player, level, new ItemStack(Items.BED.white()), floor);
			BlockPos bed = floor.above();
			expect(out, level.getBlockState(bed).getBlock() instanceof net.minecraft.world.level.block.BedBlock, "the bed was placed (got " + level.getBlockState(bed) + ")");
			expect(out, HomeBeds.isAtHome(player, level, bed.east(40)), "before a home is chosen, any bed is home");
			player.setShiftKeyDown(true);
			use(player, level, ItemStack.EMPTY, bed);
			player.setShiftKeyDown(false);
			expect(out, !HomeBeds.isAtHome(player, level, bed.east(40)), "crouch-using the bed makes it home; a bed 40 blocks away isn't");
			var sinceHome = sinceHomeRest();
			sinceHome.set(player, 1_000_000);
			player.startSleeping(bed);
			expect(out, sinceHome.getOrElse(player, -1) == 0, "lying down in the home bed starts the count again (got " + sinceHome.getOrElse(player, -1) + ")");
			player.stopSleeping();
			player.setGameMode(GameType.CREATIVE);
			return out.toString();
		});

		// Three days away from home makes you Drowsy, and coffee holds it off.
		onServer("setting up Drowsy", server -> {
			ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
			player.setGameMode(GameType.SURVIVAL);
			player.removeAllEffects();
			sinceHomeRest().set(player, 1_000_000);
			return "";
		});
		waitUntil("Drowsy", 100, mc -> mc.getSingleplayerServer().submit(
				() -> mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst().hasEffect(EspressoContent.DROWSY)).join());
		onServer("coffee holds Drowsy off", server -> {
			ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
			player.removeAllEffects();
			player.addEffect(new MobEffectInstance(EspressoContent.CAFFEINATED, 1200));
			return "";
		});
		waitTicks(45);
		onServer("being Caffeinated holds Drowsy off", server -> {
			ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
			player.setGameMode(GameType.CREATIVE);
			return player.hasEffect(EspressoContent.DROWSY) ? "\n  Drowsy came back" : "";
		});
		run(mc -> {
			// The server's settings message and the machine's screen are client things: check the client has them.
			check(net.minecraft.client.gui.screens.MenuScreens.getScreenFactory(EspressoContent.ESPRESSO_MACHINE_MENU).isPresent(),
					"the espresso machine has a screen");
			// The item tooltip event adds each café item's own lines under its name.
			List<net.minecraft.network.chat.Component> lines = new ItemStack(EspressoContent.BEAN_COIN).getTooltipLines(
					net.minecraft.world.item.Item.TooltipContext.of(mc.level), mc.player, net.minecraft.world.item.TooltipFlag.NORMAL);
			check(lines.stream().anyMatch(line -> line.getString().contains("One for each Espresso advancement")),
					"a Bean Coin's tooltip has its own line (got " + lines.stream().map(net.minecraft.network.chat.Component::getString).toList() + ")");
		});
		screenshot();
	}

	@SuppressWarnings("unchecked")
	private static Platform.Data<Integer> sinceHomeRest() {
		try {
			java.lang.reflect.Field field = Wellbeing.class.getDeclaredField("SINCE_HOME_REST");
			field.setAccessible(true);
			return (Platform.Data<Integer>) field.get(null);
		} catch (ReflectiveOperationException e) {
			throw new AssertionError(e);
		}
	}

	private static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(Espresso.MOD_ID, path);
	}

	private static void expect(StringBuilder failures, boolean ok, String what) {
		if (!ok) {
			failures.append("\n  ").append(what);
		}
	}

	private static void fill(ServerLevel level, BlockPos pos, ItemStack beans) {
		EspressoMachineBlockEntity machine = (EspressoMachineBlockEntity) level.getBlockEntity(pos);
		for (int slot = 0; slot < 3; slot++) {
			machine.setItem(slot, new ItemStack(EspressoContent.MUG));
		}
		machine.setItem(EspressoMachineBlockEntity.BEAN_SLOT, beans);
	}

	private static EspressoMachineBlockEntity machine(MinecraftServer server, BlockPos pos) {
		return (EspressoMachineBlockEntity) server.overworld().getBlockEntity(pos);
	}

	private static void use(ServerPlayer player, ServerLevel level, ItemStack stack, BlockPos pos) {
		player.setItemInHand(InteractionHand.MAIN_HAND, stack);
		player.gameMode.useItemOn(player, level, stack, InteractionHand.MAIN_HAND,
				new BlockHitResult(Vec3.atCenterOf(pos).add(0, 0.5, 0), Direction.UP, pos, false));
	}

	/** Breaks an oak log as the player (crouching or not) and returns what dropped. */
	private static ItemStack dropsFromLog(ServerLevel level, ServerPlayer player, BlockPos pos, boolean crouching) {
		level.setBlockAndUpdate(pos, Blocks.OAK_LOG.defaultBlockState());
		player.setShiftKeyDown(crouching);
		player.gameMode.destroyBlock(pos);
		player.setShiftKeyDown(false);
		List<ItemEntity> drops = level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(1));
		ItemStack dropped = drops.isEmpty() ? ItemStack.EMPTY : drops.getFirst().getItem().copy();
		drops.forEach(ItemEntity::discard);
		return dropped;
	}
}

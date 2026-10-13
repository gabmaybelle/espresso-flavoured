package com.gabbymaybelle.espresso;

import com.gabbymaybelle.espresso.client.EspressoSettingsScreen;
import com.gabbymaybelle.espresso.compat.TrinketsCompat;
import com.gabbymaybelle.espresso.content.CharmItem;
import com.gabbymaybelle.espresso.content.CoffeeBushBlock;
import com.gabbymaybelle.espresso.content.EspressoContent;
import com.gabbymaybelle.espresso.content.EspressoMachineBlockEntity;
import com.gabbymaybelle.espresso.content.Seat;
import com.gabbymaybelle.espresso.mixin.StructureTemplatePoolAccessor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Plays through Espresso Flavoured Core's main rules in a real game: the spawn gazebo and its
 * starter chest, brewing in the espresso machine, the Ristretto's effects, keeping the hotbar on
 * death, creepers sparing builds, roomier bundles and the settings screen. Run with
 * `./gradlew runClientGameTest`; screenshots land in build/run/clientGameTest/screenshots.
 */
public class EspressoClientTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext ctx) {
		settingsFile();
		settingsScreen(ctx);
		try (TestSingleplayerContext world = ctx.worldBuilder().create()) {
			world.getConnection().waitForChunksRender();
			TestServerContext server = world.getServer();
			server.runCommand("gamerule advance_time false");
			gazebo(ctx, server);
			BlockPos work = server.computeOnServer(s -> s.overworld().getRespawnData().pos().offset(24, -3, 0));
			brewing(ctx, server, work);
			ristretto(server);
			bundles(server);
			creepers(ctx, server, work.offset(0, 0, 12));
			BlockPos home = work.offset(12, 0, 0);
			safeHome(server, home);
			drowsiness(ctx, server, home);
			toughNights(ctx, server, work);
			blockMerchant(server, work);
			villageCafe(server);
			charms(ctx, server, work.offset(-12, 0, 0));
			cafeLife(ctx, server, work.offset(-12, 0, 12), home);
			keepOnDeath(ctx, server, work.offset(0, 0, -12));
		}
	}

	/**
	 * The settings file: a typo keeps the default instead of switching a setting off, case and
	 * spaces don't matter, a setting from a newer version survives, no temporary file is left
	 * behind, and a complete file isn't rewritten just by loading it (so notes added by hand stay).
	 */
	private static void settingsFile() {
		Path file = FabricLoader.getInstance().getConfigDir().resolve(Espresso.MOD_ID + ".properties");
		try {
			Files.writeString(file, "protectGazebo=ture\ncreeperGriefing= TRUE \nfutureSetting=hello\n");
			Config.load();
			check(Config.protectGazebo, "a typo (\"ture\") leaves Protect the Gazebo on, its default");
			check(Config.creeperGriefing, "\" TRUE \" reads as true");
			String written = Files.readString(file);
			check(written.contains("futureSetting=hello"), "a setting this version doesn't know is kept");
			check(written.contains("keepHotbarOnDeath=true"), "missing settings are written with their defaults");
			check(!Files.exists(file.resolveSibling(file.getFileName() + ".tmp")), "no temporary file is left behind");

			String complete = "# a note added by hand\n" + written;
			Files.writeString(file, complete);
			Config.load();
			check(Files.readString(file).equals(complete), "loading a complete file leaves it exactly as it was");
		} catch (IOException e) {
			throw new AssertionError("couldn't use the settings file", e);
		} finally {
			Config.creeperGriefing = false;
			Config.protectGazebo = true;
			Config.save();
		}
	}

	/** Mod Menu's Config button opens the settings, before any world exists. */
	private static void settingsScreen(ClientGameTestContext ctx) {
		ctx.setScreen(() -> new EspressoSettingsScreen(new TitleScreen()));
		ctx.waitForScreen(EspressoSettingsScreen.class);
		ctx.takeScreenshot("espresso_settings");
		ctx.setScreen(TitleScreen::new);
		ctx.waitForScreen(TitleScreen.class);
	}

	/** A new world gets the gazebo, the first player's starter kit is in its chest, and everyone starts inside it. */
	private static void gazebo(ClientGameTestContext ctx, TestServerContext server) {
		ctx.waitTicks(10);
		CompoundTag spawn = server.computeOnServer(s -> s.getCommandStorage().get(Identifier.fromNamespaceAndPath(Espresso.MOD_ID, "spawn")));
		check(spawn.getBooleanOr("chest", false), "a new world gets the gazebo with its starter chest");
		check(spawn.getBooleanOr("claimed", false), "the first player to join claims the chest's kit");
		int radius = server.computeOnServer(s -> s.overworld().getGameRules().get(GameRules.RESPAWN_RADIUS));
		check(radius == 0, "the spawn radius is 0 (got " + radius + ")");
		ctx.takeScreenshot("espresso_gazebo");
	}

	/**
	 * On a coffee bar, three mugs and three beans brew three Espressos and leave three grounds in
	 * the knock box; the Barista's Reserve brews a Ristretto; off a coffee bar nothing brews.
	 */
	private static void brewing(ClientGameTestContext ctx, TestServerContext server, BlockPos bar) {
		BlockPos machine = bar.above();
		BlockPos bare = bar.offset(3, 0, 0).above();
		server.runOnServer(s -> {
			ServerLevel level = s.overworld();
			level.setBlockAndUpdate(bar, EspressoContent.COFFEE_BARS.getFirst().defaultBlockState());
			level.setBlockAndUpdate(machine, EspressoContent.ESPRESSO_MACHINE.defaultBlockState());
			level.setBlockAndUpdate(bare.below(), Blocks.STONE.defaultBlockState());
			level.setBlockAndUpdate(bare, EspressoContent.ESPRESSO_MACHINE.defaultBlockState());
			fill(level, machine, new ItemStack(EspressoContent.ROASTED_COFFEE_BEANS, 3));
			fill(level, bare, new ItemStack(EspressoContent.ROASTED_COFFEE_BEANS, 3));
		});
		ctx.waitTicks(30);
		boolean steaming = server.computeOnServer(s -> machineAt(s, machine).brewTime() > 0);
		check(steaming, "the machine on a coffee bar starts brewing");
		ctx.takeScreenshot("espresso_machine_brewing");
		waitForServer(ctx, server, s -> machineAt(s, machine).brewTime() == 0
				&& machineAt(s, machine).getItem(0).is(EspressoContent.ESPRESSO), EspressoMachineBlockEntity.BREW_TIME + 100,
				"the machine on a coffee bar brews three Espressos");
		server.runOnServer(s -> {
			EspressoMachineBlockEntity m = machineAt(s, machine);
			for (int slot = 0; slot < 3; slot++) {
				check(m.getItem(slot).is(EspressoContent.ESPRESSO), "mug " + slot + " became an Espresso (got " + m.getItem(slot) + ")");
			}
			check(m.getItem(EspressoMachineBlockEntity.BEAN_SLOT).isEmpty(), "one bean was used per mug");
			check(m.getItem(EspressoMachineBlockEntity.GROUNDS_SLOT).is(EspressoContent.COFFEE_GROUNDS)
					&& m.getItem(EspressoMachineBlockEntity.GROUNDS_SLOT).getCount() == 3, "three shots left three grounds in the knock box");
			EspressoMachineBlockEntity off = machineAt(s, bare);
			check(off.brewTime() == 0 && off.getItem(0).is(EspressoContent.MUG), "a machine off a coffee bar doesn't brew");

			// The Barista's Reserve makes the Ristretto.
			for (int slot = 0; slot < EspressoMachineBlockEntity.SLOT_COUNT; slot++) {
				m.setItem(slot, ItemStack.EMPTY);
			}
			fill(s.overworld(), machine, new ItemStack(EspressoContent.BARISTAS_RESERVE, 3));
		});
		waitForServer(ctx, server, s -> machineAt(s, machine).getItem(0).is(EspressoContent.RISTRETTO), EspressoMachineBlockEntity.BREW_TIME + 100,
				"the Barista's Reserve brews a Ristretto");
	}

	/** One minute of Speed II and Haste II, the only ones in the mod, plus Caffeinated. */
	private static void ristretto(TestServerContext server) {
		server.runOnServer(s -> {
			ServerPlayer player = player(s);
			player.removeAllEffects();
			ItemStack mug = new ItemStack(EspressoContent.RISTRETTO);
			mug.finishUsingItem(s.overworld(), player);
			MobEffectInstance speed = player.getEffect(MobEffects.SPEED);
			MobEffectInstance haste = player.getEffect(MobEffects.HASTE);
			check(speed != null && speed.getAmplifier() == 1, "the Ristretto gives Speed II (got " + speed + ")");
			check(haste != null && haste.getAmplifier() == 1, "the Ristretto gives Haste II (got " + haste + ")");
			check(speed.getDuration() > 1100 && speed.getDuration() <= 1200, "for a minute (got " + speed.getDuration() + " ticks)");
			check(player.hasEffect(EspressoContent.CAFFEINATED), "and Caffeinated");
			player.removeAllEffects();
		});
	}

	/** Four tools fit in one bundle (vanilla: one), and a fifth doesn't. */
	private static void bundles(TestServerContext server) {
		server.runOnServer(s -> {
			BundleContents.Mutable bundle = new BundleContents.Mutable();
			int fitted = 0;
			for (int i = 0; i < 5; i++) {
				fitted += bundle.tryInsert(new ItemStack(Items.IRON_PICKAXE));
			}
			check(fitted == 4, "a bundle holds four tools by default (got " + fitted + ")");
		});
	}

	/** A creeper's explosion still happens, but the blocks round it stay. */
	private static void creepers(ClientGameTestContext ctx, TestServerContext server, BlockPos at) {
		server.runOnServer(s -> {
			ServerLevel level = s.overworld();
			for (BlockPos pos : BlockPos.betweenClosed(at.offset(-2, 0, -2), at.offset(2, 2, 2))) {
				boolean wall = Math.abs(pos.getX() - at.getX()) == 2 || Math.abs(pos.getZ() - at.getZ()) == 2;
				level.setBlockAndUpdate(pos, wall ? Blocks.OAK_PLANKS.defaultBlockState() : Blocks.AIR.defaultBlockState());
			}
		});
		server.runCommand("summon minecraft:creeper " + at.getX() + " " + at.getY() + " " + at.getZ() + " {Fuse:1s,ignited:1b,NoAI:1b}");
		ctx.waitTicks(20);
		server.runOnServer(s -> {
			ServerLevel level = s.overworld();
			int planks = 0;
			for (BlockPos pos : BlockPos.betweenClosed(at.offset(-2, 0, -2), at.offset(2, 2, 2))) {
				if (level.getBlockState(pos).is(Blocks.OAK_PLANKS)) {
					planks++;
				}
			}
			check(planks == 48, "the creeper's walls are all still there (" + planks + " of 48 planks)");
			check(level.getEntitiesOfClass(net.minecraft.world.entity.monster.Creeper.class, new AABB(at).inflate(4)).isEmpty(), "the creeper exploded");
		});
	}

	/**
	 * Crouching and using a bed with an empty hand makes it home: hostile mobs then can't spawn
	 * naturally inside the safe home radius (32 by default) but can outside it, and a broken bed
	 * protects nothing.
	 */
	private static void safeHome(TestServerContext server, BlockPos floor) {
		server.runOnServer(s -> {
			ServerLevel level = s.overworld();
			ServerPlayer player = player(s);
			level.setBlockAndUpdate(floor, Blocks.STONE.defaultBlockState());
			level.setBlockAndUpdate(floor.above(), Blocks.AIR.defaultBlockState());
			level.setBlockAndUpdate(floor.above().south(), Blocks.AIR.defaultBlockState());
			player.teleportTo(floor.getX() + 0.5, floor.getY() + 1, floor.getZ() - 1.5);
			ItemStack bed = new ItemStack(Items.BED.white());
			player.setItemInHand(InteractionHand.MAIN_HAND, bed);
			player.gameMode.useItemOn(player, level, bed, InteractionHand.MAIN_HAND,
					new BlockHitResult(Vec3.atCenterOf(floor).add(0, 0.5, 0), Direction.UP, floor, false));
			BlockPos bedPos = floor.above();
			check(level.getBlockState(bedPos).getBlock() instanceof BedBlock, "the bed was placed (got " + level.getBlockState(bedPos) + ")");
			check(!HomeBeds.isProtected(level, bedPos.east(10)), "nothing is protected before a home is chosen");

			player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
			player.setShiftKeyDown(true);
			player.gameMode.useItemOn(player, level, ItemStack.EMPTY, InteractionHand.MAIN_HAND,
					new BlockHitResult(Vec3.atCenterOf(bedPos), Direction.UP, bedPos, false));
			player.setShiftKeyDown(false);
			check(HomeBeds.isProtected(level, bedPos.east(20)), "crouch-using the bed makes it home: 20 blocks away is protected");
			check(!HomeBeds.isProtected(level, bedPos.east(40)), "40 blocks away is outside the safe home radius");
			check(HomeBeds.isAtHome(player, level, bedPos), "sleeping in that bed counts as sleeping at home");
		});
	}

	/**
	 * Three days without sleeping at home makes you Drowsy, coffee holds it off, and lying down in
	 * your home bed starts the count again.
	 */
	private static void drowsiness(ClientGameTestContext ctx, TestServerContext server, BlockPos floor) {
		server.runCommand("gamemode survival @a");
		server.runOnServer(s -> {
			ServerPlayer player = player(s);
			player.removeAllEffects();
			player.setAttached(sinceHomeRest(), 1_000_000);
		});
		waitForServer(ctx, server, s -> player(s).hasEffect(EspressoContent.DROWSY), 60, "three days away from home makes you Drowsy");

		server.runOnServer(s -> {
			ServerPlayer player = player(s);
			player.removeAllEffects();
			player.addEffect(new MobEffectInstance(EspressoContent.CAFFEINATED, 1200));
		});
		ctx.waitTicks(45);
		check(!server.computeOnServer(s -> player(s).hasEffect(EspressoContent.DROWSY)), "being Caffeinated holds Drowsy off");

		server.runOnServer(s -> {
			ServerPlayer player = player(s);
			player.removeAllEffects();
			player.startSleeping(floor.above());
			int since = player.getAttachedOrElse(sinceHomeRest(), -1);
			player.stopSleeping();
			check(since == 0, "lying down in the home bed starts the count again (got " + since + ")");
		});
		ctx.waitTicks(45);
		check(!server.computeOnServer(s -> player(s).hasEffect(EspressoContent.DROWSY)), "after sleeping at home there's no Drowsy");
		server.runCommand("gamemode creative @a");
	}

	@SuppressWarnings("unchecked")
	private static AttachmentType<Integer> sinceHomeRest() {
		try {
			Field field = Wellbeing.class.getDeclaredField("SINCE_HOME_REST");
			field.setAccessible(true);
			return (AttachmentType<Integer>) field.get(null);
		} catch (ReflectiveOperationException e) {
			throw new AssertionError(e);
		}
	}

	/** A hostile mob that spawns naturally at night has 20% more health; by day it doesn't. */
	private static void toughNights(ClientGameTestContext ctx, TestServerContext server, BlockPos at) {
		server.runCommand("time set 6000");
		ctx.waitTicks(3);
		float day = server.computeOnServer(s -> naturalZombieHealth(s.overworld(), at));
		// Espresso's longer day: 24000 ticks of daylight, then night, with midnight at 30000.
		server.runCommand("time set 30000");
		ctx.waitTicks(3);
		check(server.computeOnServer(s -> s.overworld().isDarkOutside()), "it's dark outside at tick 30000");
		float night = server.computeOnServer(s -> naturalZombieHealth(s.overworld(), at));
		server.runCommand("time set 6000");
		check(day == 20.0F, "a zombie spawning by day has its usual 20 health (got " + day + ")");
		check(night == 24.0F, "a zombie spawning at night has 20% more health (got " + night + ")");
	}

	private static float naturalZombieHealth(ServerLevel level, BlockPos at) {
		Zombie zombie = EntityTypes.ZOMBIE.create(level, EntitySpawnReason.NATURAL);
		zombie.snapTo(at.getX() + 0.5, at.getY() + 1, at.getZ() + 0.5);
		zombie.finalizeSpawn(level, level.getCurrentDifficultyAt(at), EntitySpawnReason.NATURAL, null);
		float health = zombie.getMaxHealth();
		zombie.discard();
		return health;
	}

	/** The wandering trader has extra building-block trades with the Block Merchant on, and always buys roasted beans. */
	private static void blockMerchant(TestServerContext server, BlockPos at) {
		server.runOnServer(s -> {
			ServerLevel level = s.overworld();
			int with = traderOffers(level, at, true);
			int without = traderOffers(level, at, false);
			Config.blockMerchant = true;
			check(with > without, "the Block Merchant adds trades (" + with + " with it, " + without + " without)");
		});
	}

	/** How many trades a new wandering trader offers; checks it buys roasted coffee beans either way. */
	private static int traderOffers(ServerLevel level, BlockPos at, boolean blockMerchant) {
		Config.blockMerchant = blockMerchant;
		WanderingTrader trader = EntityTypes.WANDERING_TRADER.create(level, EntitySpawnReason.COMMAND);
		trader.snapTo(at.getX() + 0.5, at.getY() + 1, at.getZ() + 0.5);
		int offers = trader.getOffers().size();
		boolean buysBeans = trader.getOffers().stream().anyMatch(offer -> offer.getCostA().is(EspressoContent.ROASTED_COFFEE_BEANS));
		trader.discard();
		check(buysBeans, "the wandering trader buys roasted coffee beans");
		return offers;
	}

	/** The café is one of the houses a plains village can build, and its structure file is there. */
	private static void villageCafe(TestServerContext server) {
		server.runOnServer(s -> {
			StructureTemplatePool pool = s.registryAccess().lookupOrThrow(Registries.TEMPLATE_POOL)
					.getValue(Identifier.withDefaultNamespace("village/plains/houses"));
			boolean inPool = ((StructureTemplatePoolAccessor) pool).espresso$templates().stream()
					.anyMatch(element -> element.toString().contains(Espresso.MOD_ID + ":village/plains/cafe"));
			check(inPool, "the café is in the plains village houses pool");
			check(s.getStructureTemplateManager().get(Identifier.fromNamespaceAndPath(Espresso.MOD_ID, "village/plains/cafe")).isPresent(),
					"the café's structure file loads");
		});
	}

	/**
	 * Each café charm's perk, worn the way this game wears them: in its Trinkets slot when Trinkets
	 * Updated is installed (`-PtestTrinkets`), otherwise in the off-hand. Each is checked on, then
	 * off again.
	 */
	private static void charms(ClientGameTestContext ctx, TestServerContext server, BlockPos at) {
		server.runCommand("gamemode survival @a");
		server.runOnServer(s -> {
			ServerPlayer player = player(s);
			player.getInventory().clearContent();
			player.removeAllEffects();
			player.teleportTo(at.getX() + 0.5, at.getY() + 1, at.getZ() + 0.5);
		});

		// The attribute perks, refreshed every ten ticks.
		expectAttribute(ctx, server, EspressoContent.LUCKY_BEAN, Attributes.LUCK, 1);
		expectAttribute(ctx, server, EspressoContent.READING_SOCKS, Attributes.STEP_HEIGHT, 0.5);
		expectAttribute(ctx, server, EspressoContent.CAFE_PARASOL, Attributes.SAFE_FALL_DISTANCE, 3);

		// Thermos: a drink's effects last a quarter longer (an Espresso's 90 seconds become 112.5).
		int thermos = server.computeOnServer(s -> {
			ServerPlayer player = player(s);
			wear(player, EspressoContent.THERMOS);
			player.removeAllEffects();
			new ItemStack(EspressoContent.ESPRESSO).finishUsingItem(s.overworld(), player);
			int duration = player.getEffect(MobEffects.SPEED).getDuration();
			takeOff(player);
			player.removeAllEffects();
			return duration;
		});
		check(thermos > 2200 && thermos <= 2250, "with a Thermos an Espresso's Speed lasts 2250 ticks (got " + thermos + ")");

		// Pocket Hearth: powder snow can't freeze you.
		server.runOnServer(s -> {
			ServerPlayer player = player(s);
			check(player.canFreeze(), "without a Pocket Hearth you can freeze");
			wear(player, EspressoContent.POCKET_HEARTH);
			check(!player.canFreeze(), "a Pocket Hearth stops you freezing");
			takeOff(player);
		});

		// Copper Tamper: 15% more damage to hostile mobs.
		float[] hits = server.computeOnServer(s -> {
			ServerPlayer player = player(s);
			float plain = hitZombie(s.overworld(), player, at);
			wear(player, EspressoContent.COPPER_TAMPER);
			float tamped = hitZombie(s.overworld(), player, at);
			takeOff(player);
			return new float[] { plain, tamped };
		});
		check(Math.abs(hits[1] - hits[0] * 1.15F) < 0.01F, "a Copper Tamper adds 15% damage (" + hits[0] + " plain, " + hits[1] + " tamped)");

		// Knit Beanie: Cozy heals away from home too.
		server.runOnServer(s -> {
			ServerPlayer player = player(s);
			player.removeAllEffects();
			wear(player, EspressoContent.KNIT_BEANIE);
			player.addEffect(new MobEffectInstance(EspressoContent.COZY, 600));
		});
		waitForServer(ctx, server, s -> player(s).hasEffect(MobEffects.REGENERATION), 60,
				"a Knit Beanie lets Cozy heal you away from home");
		server.runOnServer(s -> {
			takeOff(player(s));
			player(s).removeAllEffects();
		});

		// Ember Glove: a log broken by hand drops charcoal, unless you're crouching.
		server.runOnServer(s -> {
			ServerLevel level = s.overworld();
			ServerPlayer player = player(s);
			wear(player, EspressoContent.EMBER_GLOVE);
			check(dropsFromLog(level, player, at.offset(2, 1, 0), false).is(Items.CHARCOAL), "an Ember Glove turns a log into charcoal");
			check(dropsFromLog(level, player, at.offset(2, 1, 2), true).is(Items.OAK_LOG), "crouching keeps the log a log");
			takeOff(player);
			check(dropsFromLog(level, player, at.offset(2, 1, -2), false).is(Items.OAK_LOG), "without the glove a log is a log");
		});
		server.runCommand("gamemode creative @a");
	}

	/** Wearing the charm gives the attribute bonus; taking it off removes it again. */
	private static void expectAttribute(ClientGameTestContext ctx, TestServerContext server, net.minecraft.world.item.Item charm,
			Holder<Attribute> attribute, double bonus) {
		double base = server.computeOnServer(s -> player(s).getAttributeValue(attribute));
		server.runOnServer(s -> wear(player(s), charm));
		waitForServer(ctx, server, s -> Math.abs(player(s).getAttributeValue(attribute) - (base + bonus)) < 0.001, 40,
				charm + " gives " + bonus + " more " + attribute.getRegisteredName());
		server.runOnServer(s -> takeOff(player(s)));
		waitForServer(ctx, server, s -> Math.abs(player(s).getAttributeValue(attribute) - base) < 0.001, 40,
				"taking off " + charm + " takes the bonus away");
	}

	/** Puts a charm on: right-clicked into its slot with Trinkets, otherwise held in the off-hand. */
	private static void wear(ServerPlayer player, net.minecraft.world.item.Item charm) {
		ItemStack stack = new ItemStack(charm);
		if (CharmItem.TRINKETS) {
			player.setItemInHand(InteractionHand.MAIN_HAND, stack);
			player.gameMode.useItem(player, player.level(), stack, InteractionHand.MAIN_HAND);
			check(TrinketsCompat.isEquipped(player, charm), "right-clicking " + charm + " puts it in its Trinkets slot");
			player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		} else {
			player.setItemInHand(InteractionHand.OFF_HAND, stack);
		}
		check(CharmItem.isWorn(player, charm), charm + " counts as worn");
	}

	/** Takes every charm off again. */
	private static void takeOff(ServerPlayer player) {
		if (CharmItem.TRINKETS) {
			TrinketsCompat.takeCharms(player);
		}
		player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
	}

	/** How much health a zombie loses to a 10-point hit from the player. */
	private static float hitZombie(ServerLevel level, ServerPlayer player, BlockPos at) {
		Zombie zombie = EntityTypes.ZOMBIE.create(level, EntitySpawnReason.COMMAND);
		zombie.snapTo(at.getX() + 3.5, at.getY() + 1, at.getZ() + 0.5);
		level.addFreshEntity(zombie);
		float before = zombie.getHealth();
		zombie.hurtServer(level, player.damageSources().playerAttack(player), 10.0F);
		float lost = before - zombie.getHealth();
		zombie.discard();
		return lost;
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

	/**
	 * The everyday café things: sitting on stairs, growing and picking a coffee bush, setting a mug
	 * down and picking it up, a canister of beans, waking Well Rested after sleeping while Cozy,
	 * Bean Coins for advancements, and a master barista's coin trades.
	 */
	private static void cafeLife(ClientGameTestContext ctx, TestServerContext server, BlockPos at, BlockPos home) {
		server.runCommand("gamemode survival @a");
		server.runOnServer(s -> {
			ServerLevel level = s.overworld();
			ServerPlayer player = player(s);
			player.getInventory().clearContent();
			player.teleportTo(at.getX() + 0.5, at.getY() + 1, at.getZ() - 1.5);
			for (BlockPos pos : BlockPos.betweenClosed(at.offset(-3, 0, -3), at.offset(6, 0, 3))) {
				level.setBlockAndUpdate(pos, Blocks.GRASS_BLOCK.defaultBlockState());
				level.setBlockAndUpdate(pos.above(), Blocks.AIR.defaultBlockState());
				level.setBlockAndUpdate(pos.above(2), Blocks.AIR.defaultBlockState());
			}

			// Sitting: an upright stair with an empty hand, and crouching (standing up) ends it.
			BlockPos stair = at.above();
			level.setBlockAndUpdate(stair, Blocks.OAK_STAIRS.defaultBlockState());
			use(player, level, ItemStack.EMPTY, stair);
			check(player.getVehicle() instanceof Seat, "using stairs with an empty hand sits you down (riding " + player.getVehicle() + ")");
			Entity seat = player.getVehicle();
			player.stopRiding();
			seat.tick();
			check(!seat.isAlive(), "the seat goes away when you stand up");

			// A coffee bush: plant the cherries, bone meal it to full size, pick it.
			BlockPos bush = at.offset(3, 1, 0);
			use(player, level, new ItemStack(EspressoContent.COFFEE_CHERRIES), bush.below());
			check(level.getBlockState(bush).is(EspressoContent.COFFEE_BUSH), "coffee cherries plant a coffee bush");
			for (int i = 0; i < CoffeeBushBlock.MAX_AGE; i++) {
				BoneMealItem.growCrop(new ItemStack(Items.BONE_MEAL), level, bush);
			}
			check(level.getBlockState(bush).getValue(CoffeeBushBlock.AGE) == CoffeeBushBlock.MAX_AGE, "bone meal grows the bush to full size");
			check(level.getBlockState(bush.above()).is(EspressoContent.COFFEE_BUSH), "a grown bush is two blocks tall");
			use(player, level, ItemStack.EMPTY, bush);
			List<ItemEntity> picked = level.getEntitiesOfClass(ItemEntity.class, new AABB(bush).inflate(2),
					item -> item.getItem().is(EspressoContent.COFFEE_CHERRIES));
			int cherries = picked.stream().mapToInt(item -> item.getItem().getCount()).sum();
			picked.forEach(ItemEntity::discard);
			check(cherries >= 2 && cherries <= 3, "picking a full bush gives two or three cherries (got " + cherries + ")");
			check(level.getBlockState(bush).getValue(CoffeeBushBlock.AGE) == CoffeeBushBlock.TALL_AGE, "a picked bush goes back to growing");

			// A mug: crouch and use it on a surface to set it down; use it again to pick it up.
			BlockPos table = at.offset(-2, 1, 0);
			level.setBlockAndUpdate(table, Blocks.OAK_PLANKS.defaultBlockState());
			ItemStack mug = new ItemStack(EspressoContent.ESPRESSO);
			player.setShiftKeyDown(true);
			use(player, level, mug, table);
			player.setShiftKeyDown(false);
			check(level.getBlockState(table.above()).is(EspressoContent.PLACED_MUG), "crouch-using an Espresso sets the mug down");
			use(player, level, ItemStack.EMPTY, table.above());
			check(level.getBlockState(table.above()).isAir(), "using the set-down mug picks it up");
			check(player.getMainHandItem().is(EspressoContent.ESPRESSO), "and the same Espresso comes back (got " + player.getMainHandItem() + ")");
			player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);

			// A canister: put beans in, take them out again.
			BlockPos canister = at.offset(-2, 1, 2);
			level.setBlockAndUpdate(canister, EspressoContent.CANISTER_BLOCK.defaultBlockState());
			use(player, level, new ItemStack(EspressoContent.ROASTED_COFFEE_BEANS, 20), canister);
			check(player.getMainHandItem().isEmpty(), "the canister takes the beans");
			use(player, level, ItemStack.EMPTY, canister);
			check(player.getInventory().countItem(EspressoContent.ROASTED_COFFEE_BEANS) == 20, "an empty hand takes the beans back out ("
					+ player.getInventory().countItem(EspressoContent.ROASTED_COFFEE_BEANS) + ")");
		});
		// Stocked Up (the canister) is an Espresso advancement, so it pays a Bean Coin.
		waitForServer(ctx, server, s -> player(s).getInventory().countItem(EspressoContent.BEAN_COIN) >= 1, 60,
				"an Espresso advancement (Stocked Up) pays a Bean Coin");

		// Sleeping while Cozy wakes you Well Rested.
		server.runCommand("time set 30000");
		server.runOnServer(s -> {
			ServerPlayer player = player(s);
			player.addEffect(new MobEffectInstance(EspressoContent.COZY, 2400));
			player.startSleeping(home.above());
		});
		// Lying down this way doesn't count towards skipping the night, so wake up as morning would,
		// once the player has slept long enough for the game to count it (100 ticks).
		waitForServer(ctx, server, s -> player(s).isSleepingLongEnough(), 200, "the player sleeps long enough to count");
		server.runOnServer(s -> {
			player(s).stopSleepInBed(false, true);
			check(player(s).hasEffect(EspressoContent.WELL_RESTED), "sleeping the night through while Cozy wakes you Well Rested");
		});
		server.runCommand("time set 6000");

		// A master barista takes Bean Coins: the Golden Mug for 8, and three Barista's Reserve for 1.
		server.runOnServer(s -> {
			ServerLevel level = s.overworld();
			Villager barista = EntityTypes.VILLAGER.create(level, EntitySpawnReason.COMMAND);
			barista.snapTo(at.getX() + 0.5, at.getY() + 1, at.getZ() + 3.5);
			barista.setVillagerData(barista.getVillagerData().withProfession(s.registryAccess(), EspressoContent.BARISTA).withLevel(5));
			boolean goldenMug = barista.getOffers().stream().anyMatch(offer -> offer.getResult().is(EspressoContent.GOLDEN_MUG)
					&& offer.getCostA().is(EspressoContent.BEAN_COIN) && offer.getCostA().getCount() == 8);
			boolean reserve = barista.getOffers().stream().anyMatch(offer -> offer.getResult().is(EspressoContent.BARISTAS_RESERVE)
					&& offer.getResult().getCount() == 3 && offer.getCostA().is(EspressoContent.BEAN_COIN) && offer.getCostA().getCount() == 1);
			barista.discard();
			check(goldenMug, "a master barista sells the Golden Mug for 8 Bean Coins");
			check(reserve, "a master barista sells three Barista's Reserve for a Bean Coin");
		});
		server.runCommand("gamemode creative @a");
	}

	/** Right-clicks the top of a block with this item, as a player would. */
	private static void use(ServerPlayer player, ServerLevel level, ItemStack stack, BlockPos pos) {
		player.setItemInHand(InteractionHand.MAIN_HAND, stack);
		player.gameMode.useItemOn(player, level, stack, InteractionHand.MAIN_HAND,
				new BlockHitResult(Vec3.atCenterOf(pos).add(0, 0.5, 0), Direction.UP, pos, false));
	}

	/** The hotbar, armour and off-hand stay with you; the rest of the inventory drops where you fell. */
	private static void keepOnDeath(ClientGameTestContext ctx, TestServerContext server, BlockPos at) {
		server.runCommand("gamemode survival @a");
		server.runOnServer(s -> {
			ServerPlayer player = player(s);
			player.getInventory().clearContent();
			player.teleportTo(at.getX() + 0.5, at.getY() + 1, at.getZ() + 0.5);
			player.getInventory().setItem(0, new ItemStack(Items.DIAMOND_PICKAXE));
			player.getInventory().setItem(20, new ItemStack(Items.COBBLESTONE, 10));
			player.getInventory().setItem(40, new ItemStack(Items.SHIELD));
			if (CharmItem.TRINKETS) {
				// A charm worn in its Trinkets slot goes back into that slot after respawning.
				ItemStack bean = new ItemStack(EspressoContent.LUCKY_BEAN);
				player.setItemInHand(InteractionHand.MAIN_HAND, bean);
				player.gameMode.useItem(player, player.level(), bean, InteractionHand.MAIN_HAND);
				player.getInventory().setItem(0, new ItemStack(Items.DIAMOND_PICKAXE));
				check(TrinketsCompat.isEquipped(player, EspressoContent.LUCKY_BEAN), "the Lucky Bean is worn before dying");
			}
		});
		ctx.waitTicks(5);
		server.runCommand("kill @a");
		ctx.waitForScreen(DeathScreen.class);
		AssertionError whileDead = null;
		try {
			server.runOnServer(s -> {
			boolean dropped = !s.overworld().getEntitiesOfClass(ItemEntity.class, new AABB(at).inflate(4),
					item -> item.getItem().is(Items.COBBLESTONE)).isEmpty();
			check(dropped, "the cobblestone from the inventory dropped where the player fell");
			check(s.overworld().getEntitiesOfClass(ItemEntity.class, new AABB(at).inflate(4),
					item -> item.getItem().is(Items.DIAMOND_PICKAXE)).isEmpty(), "the hotbar's pickaxe didn't drop");
			});
		} catch (AssertionError e) {
			// Respawn first either way: a test world can't close while its player is dead.
			whileDead = e;
		}
		ctx.runOnClient(mc -> mc.player.respawn());
		if (whileDead != null) {
			ctx.waitForScreen(null);
			throw whileDead;
		}
		waitForServer(ctx, server, s -> player(s).isAlive() && player(s).getInventory().getItem(0).is(Items.DIAMOND_PICKAXE), 200,
				"the player respawns with the hotbar's pickaxe");
		server.runOnServer(s -> {
			ServerPlayer player = player(s);
			check(player.getInventory().getItem(40).is(Items.SHIELD), "the off-hand shield came back too");
			if (CharmItem.TRINKETS) {
				check(TrinketsCompat.isEquipped(player, EspressoContent.LUCKY_BEAN), "the worn Lucky Bean is back in its slot after respawning");
			}
			check(!player.getInventory().contains(new ItemStack(Items.COBBLESTONE)), "the rest of the inventory didn't come back");
		});
		ctx.waitForScreen(null);
		// The player's own game sees the kept items too, not just the server.
		ctx.waitFor(mc -> mc.player != null && mc.player.getInventory().getItem(0).is(Items.DIAMOND_PICKAXE)
				&& mc.player.getInventory().getItem(40).is(Items.SHIELD), 100);
		ctx.waitTicks(5);
		ctx.takeScreenshot("espresso_respawned");
	}

	private static void fill(ServerLevel level, BlockPos pos, ItemStack beans) {
		EspressoMachineBlockEntity machine = (EspressoMachineBlockEntity) level.getBlockEntity(pos);
		for (int slot = 0; slot < 3; slot++) {
			machine.setItem(slot, new ItemStack(EspressoContent.MUG));
		}
		machine.setItem(EspressoMachineBlockEntity.BEAN_SLOT, beans);
	}

	private static EspressoMachineBlockEntity machineAt(MinecraftServer server, BlockPos pos) {
		return (EspressoMachineBlockEntity) server.overworld().getBlockEntity(pos);
	}

	private static ServerPlayer player(MinecraftServer server) {
		return server.getPlayerList().getPlayers().getFirst();
	}

	/** Waits (up to {@code ticks}) until the condition holds on the server; {@code what} says what it's waiting for. */
	private static void waitForServer(ClientGameTestContext ctx, TestServerContext server, Function<MinecraftServer, Boolean> condition, int ticks, String what) {
		for (int i = 0; i < ticks; i++) {
			if (server.computeOnServer(condition::apply)) {
				return;
			}
			ctx.waitTick();
		}
		throw new AssertionError("Failed: " + what + " (waited " + ticks + " ticks)");
	}

	private static void check(boolean ok, String what) {
		if (!ok) {
			throw new AssertionError("Failed: " + what);
		}
	}
}

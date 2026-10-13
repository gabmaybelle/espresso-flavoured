package com.gabbymaybelle.espresso;

import com.gabbymaybelle.espresso.client.EspressoSettingsScreen;
import com.gabbymaybelle.espresso.content.EspressoContent;
import com.gabbymaybelle.espresso.content.EspressoMachineBlockEntity;
import java.util.function.Function;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.AABB;

/**
 * Plays through Espresso Flavoured Core's main rules in a real game: the spawn gazebo and its
 * starter chest, brewing in the espresso machine, the Ristretto's effects, keeping the hotbar on
 * death, creepers sparing builds, roomier bundles and the settings screen. Run with
 * `./gradlew runClientGameTest`; screenshots land in build/run/clientGameTest/screenshots.
 */
public class EspressoClientTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext ctx) {
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
			keepOnDeath(ctx, server, work.offset(0, 0, -12));
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
				&& machineAt(s, machine).getItem(0).is(EspressoContent.ESPRESSO), EspressoMachineBlockEntity.BREW_TIME + 100);
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
		waitForServer(ctx, server, s -> machineAt(s, machine).getItem(0).is(EspressoContent.RISTRETTO), EspressoMachineBlockEntity.BREW_TIME + 100);
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
		});
		ctx.waitTicks(5);
		server.runCommand("kill @a");
		ctx.waitForScreen(DeathScreen.class);
		server.runOnServer(s -> {
			boolean dropped = !s.overworld().getEntitiesOfClass(ItemEntity.class, new AABB(at).inflate(4),
					item -> item.getItem().is(Items.COBBLESTONE)).isEmpty();
			check(dropped, "the cobblestone from the inventory dropped where the player fell");
			check(s.overworld().getEntitiesOfClass(ItemEntity.class, new AABB(at).inflate(4),
					item -> item.getItem().is(Items.DIAMOND_PICKAXE)).isEmpty(), "the hotbar's pickaxe didn't drop");
		});
		ctx.runOnClient(mc -> mc.player.respawn());
		waitForServer(ctx, server, s -> player(s).isAlive() && player(s).getInventory().getItem(0).is(Items.DIAMOND_PICKAXE), 200);
		server.runOnServer(s -> {
			ServerPlayer player = player(s);
			check(player.getInventory().getItem(40).is(Items.SHIELD), "the off-hand shield came back too");
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

	/** Waits (up to {@code ticks}) until the condition holds on the server. */
	private static void waitForServer(ClientGameTestContext ctx, TestServerContext server, Function<MinecraftServer, Boolean> condition, int ticks) {
		for (int i = 0; i < ticks; i++) {
			if (server.computeOnServer(condition::apply)) {
				return;
			}
			ctx.waitTick();
		}
		throw new AssertionError("Failed: timed out after " + ticks + " ticks");
	}

	private static void check(boolean ok, String what) {
		if (!ok) {
			throw new AssertionError("Failed: " + what);
		}
	}
}

package com.gabbymaybelle.espresso;

import com.gabbymaybelle.espresso.content.CharmItem;
import com.gabbymaybelle.espresso.content.EspressoContent;
import com.mojang.serialization.Codec;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.timeline.Timelines;

/**
 * {@link #DAYS_AWAY} days away from your own bed (unless you're Caffeinated) make you Drowsy:
 * too tired to sprint, and slower to mine and walk. Only sleeping at home puts it right, so a
 * long trip ends with the walk back. Being Caffeinated holds Drowsy off while it lasts.
 * <p>
 * With the Caffeine Crash setting (off by default) it's harder: coffee only borrows time, and
 * when it wears off (or after one extra day at most) you crash into Drowsy II, and more coffee
 * won't help until you've slept at home; going those days without eating as well makes you
 * Hungry. No chat messages and no screen
 * effects: the effect icons say it, and the first time you turn Drowsy the hidden
 * "Bleary-Eyed" advancement pops up to say why.
 * <p>
 * Decaf is the cozy side: it gives Cozy, which heals you slowly near your home bed; sleeping
 * while Cozy wakes you Well Rested (two golden hearts); and with the crash on, a decaf after a
 * crash calms Drowsy II back to Drowsy I (you still need to sleep at home to clear it).
 */
public final class Wellbeing {
	private static final int CHECK_EVERY = 20;
	/** How many in-game days away from home (or without a meal) before Drowsy (or Hungry) sets in. */
	private static final int DAYS_AWAY = 3;
	private static final int WELL_RESTED_TICKS = 6000;
	/** For testing only: {@code -Despresso.testDayTicks=600} makes a "day" 30 seconds long. */
	private static final int TEST_DAY = Integer.getInteger("espresso.testDayTicks", 0);

	/** Ticks since the player last slept at home, counted while they're online. */
	private static final AttachmentType<Integer> SINCE_HOME_REST = counter("time_since_home_rest");
	/** Ticks since the player last ate, counted while they're online. */
	private static final AttachmentType<Integer> SINCE_MEAL = counter("time_since_meal");
	/** 0 = rested, 1 = holding off Drowsy with coffee, 2 = crashed, 3 = crashed but calmed by a decaf. Cleared by sleeping at home. */
	private static final AttachmentType<Integer> CAFFEINE = counter("caffeine_state");
	private static final int BORROWING = 1, CRASHED = 2, CALMED = 3;
	/** Players who lay down while Cozy; they wake up Well Rested if they sleep the night through. */
	private static final Set<UUID> COZY_SLEEPERS = new HashSet<>();

	/**
	 * One in-game day in ticks, read from the overworld's day timeline when the server starts or
	 * reloads its data: 36000 with Espresso's longer days, 24000 in vanilla, or whatever another
	 * mod's or datapack's timeline says.
	 */
	private static int dayTicks = 24000;

	private Wellbeing() {
	}

	private static AttachmentType<Integer> counter(String name) {
		return AttachmentRegistry.create(Identifier.fromNamespaceAndPath(Espresso.MOD_ID, name),
				builder -> builder.persistent(Codec.INT).initializer(() -> 0));
	}

	private static int oneDay() {
		return TEST_DAY > 0 ? TEST_DAY : dayTicks;
	}

	private static int daysAway() {
		return DAYS_AWAY * oneDay();
	}

	/** The overworld day's length from the server's timeline data (vanilla's 24000 if it can't be read). */
	private static int readDayTicks(MinecraftServer server) {
		try {
			return server.registryAccess().lookupOrThrow(Registries.TIMELINE).get(Timelines.OVERWORLD_DAY)
					.map(holder -> holder.value().periodTicks().orElse(24000)).orElse(24000);
		} catch (RuntimeException e) {
			Espresso.LOGGER.warn("Espresso Flavoured: couldn't read the day length; assuming 24000 ticks", e);
			return 24000;
		}
	}

	public static void ate(ServerPlayer player) {
		player.setAttached(SINCE_MEAL, 0);
	}

	/** A decaf after a caffeine crash softens Drowsy II to Drowsy I. */
	public static void calm(ServerPlayer player) {
		if (player.getAttachedOrElse(CAFFEINE, 0) == CRASHED) {
			player.setAttached(CAFFEINE, CALMED);
		}
	}

	static void register() {
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			dayTicks = readDayTicks(server);
			Espresso.LOGGER.info("Espresso Flavoured: a day is {} ticks, so Drowsy comes after {} ticks away from home", oneDay(), daysAway());
		});
		ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resources, success) -> dayTicks = readDayTicks(server));
		if (TEST_DAY > 0) {
			Espresso.LOGGER.warn("Espresso Flavoured: test mode, wellbeing days are {} ticks long", TEST_DAY);
		}
		// Lying down at home counts as rest, the same moment vanilla resets its own sleep timer.
		EntitySleepEvents.START_SLEEPING.register((entity, bed) -> {
			if (entity instanceof ServerPlayer player && player.level() instanceof ServerLevel level
					&& HomeBeds.isAtHome(player, level, bed)) {
				player.setAttached(SINCE_HOME_REST, 0);
				player.removeAttached(CAFFEINE);
			}
			if (entity instanceof ServerPlayer player && player.hasEffect(EspressoContent.COZY)) {
				COZY_SLEEPERS.add(player.getUUID());
			}
		});
		EntitySleepEvents.STOP_SLEEPING.register((entity, bed) -> {
			if (entity instanceof ServerPlayer player && COZY_SLEEPERS.remove(player.getUUID()) && player.isSleepingLongEnough()) {
				player.addEffect(new MobEffectInstance(EspressoContent.WELL_RESTED, WELL_RESTED_TICKS, 0));
				EspressoAdvancements.award(player, "guide/sweet_dreams", "rested");
			}
		});

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTickCount() % CHECK_EVERY != 0) {
				return;
			}
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				if (!player.isAlive()) {
					continue;
				}
				int sinceRest = tick(player, SINCE_HOME_REST);
				int sinceMeal = tick(player, SINCE_MEAL);
				// Cozy heals gently near home (or anywhere in a Knit Beanie): a short Regeneration, topped up as it runs out.
				if (player.hasEffect(EspressoContent.COZY) && (HomeBeds.isNearHome(player) || CharmItem.isWorn(player, EspressoContent.KNIT_BEANIE))) {
					MobEffectInstance regen = player.getEffect(MobEffects.REGENERATION);
					if (regen == null || regen.getDuration() <= CHECK_EVERY) {
						player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0, true, false, true));
					}
				}
				if (!Config.drowsiness || player.isSpectator() || player.isCreative()) {
					continue;
				}
				boolean tooLongAway = sinceRest >= daysAway();
				int caffeine = player.getAttachedOrElse(CAFFEINE, 0);
				// With the crash turned off, a crash from before goes back to plain Drowsy.
				if (!Config.caffeineCrash && (caffeine == CRASHED || caffeine == CALMED)) {
					caffeine = 0;
					player.removeAttached(CAFFEINE);
				}
				if (tooLongAway && caffeine != CRASHED && caffeine != CALMED) {
					boolean caffeinated = player.hasEffect(EspressoContent.CAFFEINATED);
					if (caffeinated && (!Config.caffeineCrash || sinceRest < daysAway() + oneDay())) {
						caffeine = BORROWING;
					} else if (caffeine == BORROWING && !Config.caffeineCrash) {
						caffeine = 0;
					} else if (caffeine == BORROWING) {
						caffeine = CRASHED;
					}
					player.setAttached(CAFFEINE, caffeine);
				}
				// Refreshed every second; each fades a moment after its cause is gone.
				int drowsy = caffeine == CRASHED ? 1 : caffeine == CALMED || (tooLongAway && caffeine != BORROWING) ? 0 : -1;
				if (drowsy >= 0) {
					player.addEffect(new MobEffectInstance(EspressoContent.DROWSY, 60, drowsy, true, false, true));
					// The first time, a hidden advancement says why (it's not under guide/, so it pays no Bean Coin).
					EspressoAdvancements.award(player, "tips/bleary_eyed", "drowsy");
				}
				if (Config.caffeineCrash && tooLongAway && sinceMeal >= daysAway()) {
					player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 60, 0, true, false, true));
				}
			}
		});
	}

	private static int tick(ServerPlayer player, AttachmentType<Integer> counter) {
		int value = Math.min(player.getAttachedOrCreate(counter) + CHECK_EVERY, Integer.MAX_VALUE / 2);
		player.setAttached(counter, value);
		return value;
	}
}

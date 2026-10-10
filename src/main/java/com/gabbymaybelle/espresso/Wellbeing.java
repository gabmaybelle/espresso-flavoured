package com.gabbymaybelle.espresso;

import com.gabbymaybelle.espresso.content.EspressoContent;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * Two days away from your own bed (unless you're Caffeinated) makes you Drowsy: too tired
 * to sprint, and slower to mine and walk. Only sleeping at home puts it right, so a long
 * mining trip ends with the walk back. Coffee only borrows time: once you're past two days,
 * being Caffeinated holds Drowsy off, but when the coffee wears off (or after one extra day
 * at most) you crash into Drowsy II, and more coffee won't help until you've slept at home.
 * Going those two days without eating as well makes you Hungry. No messages and no screen
 * effects; the effect icons say it.
 */
public final class Wellbeing {
	private static final int CHECK_EVERY = 20;

	/** Ticks since the player last slept at home, counted while they're online. */
	private static final AttachmentType<Integer> SINCE_HOME_REST = counter("time_since_home_rest");
	/** 0 = rested, 1 = holding off Drowsy with coffee, 2 = crashed. Cleared by sleeping at home. */
	private static final AttachmentType<Integer> CAFFEINE = counter("caffeine_state");
	private static final int BORROWING = 1, CRASHED = 2;
	/** Ticks since the player last ate, counted while they're online. */
	private static final AttachmentType<Integer> SINCE_MEAL = counter("time_since_meal");

	private Wellbeing() {
	}

	private static AttachmentType<Integer> counter(String name) {
		return AttachmentRegistry.create(Identifier.fromNamespaceAndPath(Espresso.MOD_ID, name),
				builder -> builder.persistent(Codec.INT).initializer(() -> 0));
	}

	/** For testing only: {@code -Despresso.testDayTicks=600} makes a "day" 30 seconds long. */
	private static final int TEST_DAY = Integer.getInteger("espresso.testDayTicks", 0);

	/** One in-game day: 36000 ticks with longer days, 24000 without. */
	private static int oneDay() {
		if (TEST_DAY > 0) {
			return TEST_DAY;
		}
		return Config.longerDays ? 36000 : 24000;
	}

	private static int twoDays() {
		return 2 * oneDay();
	}

	public static void ate(ServerPlayer player) {
		player.setAttached(SINCE_MEAL, 0);
	}

	static void register() {
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
				if (!Config.drowsiness || player.isSpectator() || player.isCreative()) {
					continue;
				}
				boolean upTwoDays = sinceRest >= twoDays();
				int caffeine = player.getAttachedOrElse(CAFFEINE, 0);
				if (upTwoDays && caffeine != CRASHED) {
					boolean caffeinated = player.hasEffect(EspressoContent.CAFFEINATED);
					if (caffeinated && sinceRest < twoDays() + oneDay()) {
						caffeine = BORROWING;
					} else if (caffeine == BORROWING) {
						caffeine = CRASHED;
					}
					player.setAttached(CAFFEINE, caffeine);
				}
				// Refreshed every second; each fades a moment after its cause is gone.
				if (caffeine == CRASHED) {
					player.addEffect(new MobEffectInstance(EspressoContent.DROWSY, 60, 1, true, false, true));
				} else if (upTwoDays && caffeine != BORROWING) {
					player.addEffect(new MobEffectInstance(EspressoContent.DROWSY, 60, 0, true, false, true));
				}
				if (upTwoDays && sinceMeal >= twoDays()) {
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

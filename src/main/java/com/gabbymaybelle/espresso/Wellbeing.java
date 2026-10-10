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
 * mining trip ends with the walk back. Going those two days without eating as well makes
 * you Hungry. No messages and no screen effects; the effect icons say it.
 */
public final class Wellbeing {
	private static final int CHECK_EVERY = 20;

	/** Ticks since the player last slept at home, counted while they're online. */
	private static final AttachmentType<Integer> SINCE_HOME_REST = counter("time_since_home_rest");
	/** Ticks since the player last ate, counted while they're online. */
	private static final AttachmentType<Integer> SINCE_MEAL = counter("time_since_meal");

	private Wellbeing() {
	}

	private static AttachmentType<Integer> counter(String name) {
		return AttachmentRegistry.create(Identifier.fromNamespaceAndPath(Espresso.MOD_ID, name),
				builder -> builder.persistent(Codec.INT).initializer(() -> 0));
	}

	/** Two in-game days: 72000 ticks with longer days, 48000 without. */
	private static int twoDays() {
		return Config.longerDays ? 72000 : 48000;
	}

	public static void ate(ServerPlayer player) {
		player.setAttached(SINCE_MEAL, 0);
	}

	static void register() {
		// Lying down at home counts as rest, the same moment vanilla resets its own sleep timer.
		EntitySleepEvents.START_SLEEPING.register((entity, bed) -> {
			if (entity instanceof ServerPlayer player && player.level() instanceof ServerLevel level
					&& HomeBeds.isAtHome(player, level, bed)) {
				player.setAttached(SINCE_HOME_REST, 0);
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
				// Refreshed every second; each fades a moment after its cause is gone.
				if (upTwoDays && !player.hasEffect(EspressoContent.CAFFEINATED)) {
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

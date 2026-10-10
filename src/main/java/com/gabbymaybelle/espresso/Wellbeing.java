package com.gabbymaybelle.espresso;

import com.gabbymaybelle.espresso.content.EspressoContent;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * Staying up for two days without sleeping (unless you're Caffeinated) makes you Drowsy.
 * Staying up for two days without eating as well makes you Hungry. Both last until you
 * sleep (or eat). No messages and no screen effects; the effect icons say it.
 */
public final class Wellbeing {
	private static final int CHECK_EVERY = 20;

	/** Ticks since the player last ate, counted while they're online, like the time-since-rest stat. */
	private static final AttachmentType<Integer> SINCE_MEAL = AttachmentRegistry.create(
			Identifier.fromNamespaceAndPath(Espresso.MOD_ID, "time_since_meal"),
			builder -> builder.persistent(Codec.INT).initializer(() -> 0));

	private Wellbeing() {
	}

	/** Two in-game days: 72000 ticks with longer days, 48000 without. */
	private static int twoDays() {
		return Config.longerDays ? 72000 : 48000;
	}

	public static void ate(ServerPlayer player) {
		player.setAttached(SINCE_MEAL, 0);
	}

	static void register() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTickCount() % CHECK_EVERY != 0) {
				return;
			}
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				if (!player.isAlive()) {
					continue;
				}
				int sinceMeal = Math.min(player.getAttachedOrCreate(SINCE_MEAL) + CHECK_EVERY, Integer.MAX_VALUE / 2);
				player.setAttached(SINCE_MEAL, sinceMeal);
				if (!Config.drowsiness || player.isSpectator() || player.isCreative()) {
					continue;
				}
				boolean upTwoDays = player.getStats().getValue(Stats.CUSTOM.get(Stats.TIME_SINCE_REST)) >= twoDays();
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
}

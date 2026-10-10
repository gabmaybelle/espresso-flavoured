package com.gabbymaybelle.espresso;

import com.gabbymaybelle.espresso.content.EspressoContent;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffectInstance;

/**
 * Drowsy: players who haven't slept in two days (unless they're Caffeinated), who are
 * down to 3 drumsticks of food, or who are down to 3 hearts get the Drowsy effect until
 * they sleep, eat or heal. No messages; the effect, its icon and the narrower view say it.
 */
public final class Wellbeing {
	/** Two Espresso Flavoured days (36000 ticks each). */
	private static final int TIRED_AFTER = 72000;
	private static final int HUNGRY_AT = 6;
	private static final float WORN_OUT_AT = 6.0F;

	private Wellbeing() {
	}

	static void register() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (!Config.drowsiness || server.getTickCount() % 20 != 0) {
				return;
			}
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				if (player.isSpectator() || player.isCreative() || !player.isAlive()) {
					continue;
				}
				boolean tired = player.getStats().getValue(Stats.CUSTOM.get(Stats.TIME_SINCE_REST)) >= TIRED_AFTER
						&& !player.hasEffect(EspressoContent.CAFFEINATED);
				boolean hungry = player.getFoodData().getFoodLevel() <= HUNGRY_AT;
				boolean wornOut = player.getHealth() <= WORN_OUT_AT;
				if (tired || hungry || wornOut) {
					// Refreshed every second; fades a moment after the cause is gone.
					player.addEffect(new MobEffectInstance(EspressoContent.DROWSY, 60, 0, true, false, true));
				}
			}
		});
	}
}

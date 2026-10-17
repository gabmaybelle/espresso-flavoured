package com.gabbymaybelle.espresso;

import net.minecraft.server.level.ServerLevel;

/**
 * Short, sharp nights: while a level with a sky is dark, the hostile mob cap is a quarter
 * higher and naturally spawned hostile mobs get 20% more health and damage. Each spawn round
 * notes whether its own level is having a night (see NaturalSpawnerMixin), so nothing is
 * shared between levels or threads.
 */
public final class Nights {
	public static final double BUFF = 0.2;
	public static final int CAP_PERCENT = 125;

	private Nights() {
	}

	/** True when tough nights are on and this level has a sky and it's dark outside. */
	public static boolean isToughNight(ServerLevel level) {
		return Config.toughNights && level.dimensionType().hasSkyLight() && level.isDarkOutside();
	}

	/** A spawn round that remembers whether its level was having a night when it began. */
	public interface NightSpawnState {
		void espresso$setNight(boolean night);
	}
}

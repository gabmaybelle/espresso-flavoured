package com.gabbymaybelle.espresso;

/**
 * Short, sharp nights: while a level with a sky is dark, the hostile mob cap is a quarter
 * higher and naturally spawned hostile mobs get 20% more health and damage. Set at the
 * start of each level's chunk tick, which is when natural spawning happens.
 */
public final class Nights {
	public static final double BUFF = 0.2;
	public static final int CAP_PERCENT = 125;

	/** True while the level being ticked has a sky and it's dark outside. */
	public static boolean spawningAtNight = false;

	private Nights() {
	}
}

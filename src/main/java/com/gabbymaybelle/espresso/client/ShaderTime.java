package com.gabbymaybelle.espresso.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.timeline.Timeline;
import net.minecraft.world.timeline.Timelines;

/**
 * The time of day as a normal 24000-tick day, for shaders (see IrisWorldTimeMixin), whenever the
 * day isn't vanilla's length, whoever changed it: Espresso's longer days, or another mod's or
 * datapack's timeline. Espresso's own day keeps its stretched daylight; any other length is scaled
 * evenly.
 */
public final class ShaderTime {
	/** Espresso's day: 24000 ticks of daylight (vanilla's 12000 at half speed), then 12000 of night. */
	private static final int LONG_DAY = 36000, LONG_DAYLIGHT = 24000;

	private ShaderTime() {
	}

	/** The tick of a vanilla-length day (0 = 6:00 AM), or -1 on a vanilla-length day or where there's no day. */
	public static int vanillaDayTime() {
		ClientLevel level = Minecraft.getInstance().level;
		Timeline timeline = level == null ? null : overworldDay(level);
		if (timeline == null || timeline.periodTicks().get() == 24000) {
			return -1;
		}
		int period = timeline.periodTicks().get();
		return (int) toVanilla(timeline.getCurrentTicks(level.clockManager()), period) % 24000;
	}

	/** The overworld day timeline, if this dimension follows it. */
	private static Timeline overworldDay(ClientLevel level) {
		Timeline timeline = level.registryAccess().lookupOrThrow(Registries.TIMELINE)
				.get(Timelines.OVERWORLD_DAY).map(holder -> holder.value()).orElse(null);
		if (timeline == null || timeline.periodTicks().isEmpty()
				|| level.dimensionType().timelines().stream().noneMatch(h -> h.value() == timeline)) {
			return null;
		}
		return timeline;
	}

	/** A tick of the day, back on vanilla's even 24000-tick day, where tick 0 is 6:00 AM. */
	private static double toVanilla(long ticks, int period) {
		return period == LONG_DAY
				? (ticks <= LONG_DAYLIGHT ? ticks / 2.0 : 12000 + (ticks - LONG_DAYLIGHT))
				: ticks * 24000.0 / period;
	}
}

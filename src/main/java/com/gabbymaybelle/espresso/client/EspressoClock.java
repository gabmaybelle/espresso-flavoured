package com.gabbymaybelle.espresso.client;

import com.gabbymaybelle.espresso.Config;
import com.gabbymaybelle.espresso.Espresso;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.timeline.Timeline;
import net.minecraft.world.timeline.Timelines;

/**
 * A day counter and clock in the top-right corner ("Day 4 · 2:35 PM"). It reads the overworld
 * day, so it's right with Espresso's longer days, where daylight runs at half speed (vanilla
 * clocks would say it's evening at what's really mid-afternoon). Hidden in the Nether and the
 * End, and with the "clock" setting off.
 */
public final class EspressoClock {
	private static final int MARGIN = 4;
	/** Height of a row of vanilla effect icons, including the gap below it. */
	private static final int EFFECT_ROW = 26;
	private static final int WHITE = 0xFFFFFFFF;
	private static final int GOLD = 0xFFFFAA00;
	/** Gold for the hour before night (6 to 7 PM). */
	private static final int DUSK = 18 * 60, NIGHT = 19 * 60;
	/** Espresso's day: 24000 ticks of daylight (vanilla's 12000 at half speed), then 12000 of night. */
	private static final int LONG_DAY = 36000, LONG_DAYLIGHT = 24000;

	private EspressoClock() {
	}

	static void register() {
		HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(Espresso.MOD_ID, "clock"), EspressoClock::extract);
	}

	private static void extract(GuiGraphicsExtractor graphics, DeltaTracker delta) {
		Minecraft mc = Minecraft.getInstance();
		if (!Config.clock || mc.level == null || mc.getDebugOverlay().showDebugScreen()) {
			return;
		}
		int[] dayAndMinute = dayAndMinute(mc.level);
		if (dayAndMinute == null) {
			return;
		}
		int minute = dayAndMinute[1];
		int hour24 = minute / 60;
		int hour12 = hour24 % 12 == 0 ? 12 : hour24 % 12;
		String text = String.format("Day %d · %d:%02d %s", dayAndMinute[0], hour12, minute % 60, hour24 < 12 ? "AM" : "PM");
		int colour = minute >= DUSK && minute < NIGHT ? GOLD : WHITE;
		graphics.text(mc.font, text, graphics.guiWidth() - mc.font.width(text) - MARGIN, clockY(mc), colour, true);
	}

	/**
	 * The time of day as a normal 24000-tick day (0 = 6:00 AM) whenever the day isn't vanilla's length,
	 * whoever changed it (Espresso's longer days, or another mod's or datapack's timeline), for shaders
	 * (see IrisWorldTimeMixin). Espresso's own day keeps its stretched daylight; any other length is
	 * scaled evenly. -1 on a vanilla-length day, or where there's no day.
	 */
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

	/** The day number (from 1) and the minute of the day (0 = midnight), or null where there's no day. */
	private static int[] dayAndMinute(ClientLevel level) {
		Timeline timeline = overworldDay(level);
		if (timeline == null) {
			return null;
		}
		double vanilla = toVanilla(timeline.getCurrentTicks(level.clockManager()), timeline.periodTicks().get());
		int minute = ((int) (vanilla * 1440 / 24000) + 6 * 60) % (24 * 60);
		return new int[] { timeline.getPeriodCount(level.clockManager()) + 1, minute };
	}

	/** Below the effect icons, which vanilla draws in the same corner. */
	private static int clockY(Minecraft mc) {
		Screen screen = mc.gui.screen();
		if (mc.player == null || (screen != null && screen.showsActiveEffects())) {
			return MARGIN;
		}
		boolean beneficial = false;
		boolean harmful = false;
		for (MobEffectInstance effect : mc.player.getActiveEffects()) {
			if (effect.showIcon()) {
				if (effect.getEffect().value().isBeneficial()) {
					beneficial = true;
				} else {
					harmful = true;
				}
			}
		}
		int top = mc.isDemo() ? 16 : 1;
		return harmful ? top + EFFECT_ROW * 2 + MARGIN : beneficial ? top + EFFECT_ROW + MARGIN : MARGIN;
	}
}

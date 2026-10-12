package com.gabbymaybelle.espresso.client;

import com.gabbymaybelle.espresso.Config;
import com.gabbymaybelle.espresso.Espresso;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.timeline.Timeline;
import net.minecraft.world.timeline.Timelines;

/**
 * A day counter and clock in the top-right corner ("Day 4 · 2:35 PM"). It reads the overworld
 * day, so it's right with Espresso's longer days, where daylight runs at half speed (vanilla
 * clocks would say it's evening at what's really mid-afternoon). Hidden in the Nether and the
 * End, and with the "clock" setting off.
 * <p>
 * At 6:00 PM, an hour before night, "The sun's getting real low" shows above the hotbar for a few
 * seconds (the "duskWarning" setting). It only shows when dusk arrives as time passes, not when the
 * time jumps forward from sleeping or /time set.
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
	/** Larger forward steps than this (in game minutes) are sleeping or /time set, not dusk arriving. */
	private static final int MAX_STEP_MINUTES = 10;
	/** How long the dusk message stays up, in client ticks (20 a second), including a one-second fade. */
	private static final int DUSK_TICKS = 8 * 20, FADE_TICKS = 20;
	/** Vanilla draws action-bar text this many GUI pixels above the bottom of the screen. */
	private static final int ACTION_BAR_OFFSET = 72;

	/** The minute of the day at the last client tick, or -1 where there's no day. */
	private static int lastMinute = -1;
	private static int duskTicksLeft = 0;

	private EspressoClock() {
	}

	static void register() {
		HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(Espresso.MOD_ID, "clock"), EspressoClock::extract);
		ClientTickEvents.END_CLIENT_TICK.register(EspressoClock::tick);
	}

	private static void extract(GuiGraphicsExtractor graphics, DeltaTracker delta) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || mc.getDebugOverlay().showDebugScreen()) {
			return;
		}
		if (duskTicksLeft > 0) {
			Component dusk = Component.translatable("hud.espresso_flavoured.dusk");
			int alpha = Math.max(8, Math.min(255, duskTicksLeft * 255 / FADE_TICKS));
			graphics.text(mc.font, dusk, (graphics.guiWidth() - mc.font.width(dusk)) / 2, graphics.guiHeight() - ACTION_BAR_OFFSET,
					(alpha << 24) | (GOLD & 0xFFFFFF), true);
		}
		if (!Config.clock) {
			return;
		}
		int[] dayAndMinute = dayAndMinute(mc.level);
		if (dayAndMinute == null) {
			return;
		}
		int minute = dayAndMinute[1];
		int hour24 = minute / 60;
		int hour12 = hour24 % 12 == 0 ? 12 : hour24 % 12;
		Component text = Component.translatable("hud.espresso_flavoured.clock", dayAndMinute[0], hour12, String.format("%02d", minute % 60),
				Component.translatable(hour24 < 12 ? "hud.espresso_flavoured.am" : "hud.espresso_flavoured.pm"));
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

	/** Starts the dusk message when the clock passes 6:00 PM, and counts it down. */
	private static void tick(Minecraft mc) {
		int[] dayAndMinute = mc.level == null || mc.player == null ? null : dayAndMinute(mc.level);
		if (dayAndMinute == null) {
			lastMinute = -1;
			duskTicksLeft = 0;
			return;
		}
		if (duskTicksLeft > 0 && !mc.isPaused()) {
			duskTicksLeft--;
		}
		int now = dayAndMinute[1];
		if (Config.duskWarning && lastMinute >= 0 && lastMinute < DUSK && now >= DUSK && now - lastMinute <= MAX_STEP_MINUTES) {
			duskTicksLeft = DUSK_TICKS;
		}
		lastMinute = now;
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

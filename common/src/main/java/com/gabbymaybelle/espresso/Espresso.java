package com.gabbymaybelle.espresso;

import com.gabbymaybelle.espresso.content.ContentEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Espresso Flavoured Core. Each loader's entry sets {@link Platform} first, then calls
 * {@link #init()}, registers the content ({@link com.gabbymaybelle.espresso.content.EspressoContent})
 * the way its loader wants, and adds the built-in packs listed in {@link #BUILT_IN_PACKS}.
 */
public final class Espresso {
	public static final String MOD_ID = "espresso_flavoured";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	/** A built-in pack folder under {@code resourcepacks/}: server data or a resource pack, and whether it is always on (otherwise it can be switched off). */
	public record BuiltInPack(String folder, String nameKey, boolean serverData, boolean required) {
	}

	private Espresso() {
	}

	/**
	 * Reads the settings and hooks up the rules and events. This must not touch
	 * {@code EspressoContent}'s fields: NeoForge only lets a mod register blocks and items while
	 * its registry events run, which is after this.
	 */
	public static void init() {
		Config.load();
		HomeBeds.register();
		ContentEvents.register();
		Wellbeing.register();
		KeepOnDeath.register();
		ServerSettings.register();
		LOGGER.info("Espresso Flavoured: ready (longer days {}, drowsiness {}, caffeine crash {})", Config.longerDays, Config.drowsiness, Config.caffeineCrash);
	}

	/**
	 * The built-in packs, in order: recipes, advancements, the starter kit, the field guide and the
	 * trades (always on); the title-screen look (on unless switched off, on Fabric); and 20-minute
	 * days and 10-minute nights, when the setting is on.
	 */
	public static java.util.List<BuiltInPack> builtInPacks() {
		java.util.List<BuiltInPack> packs = new java.util.ArrayList<>();
		packs.add(new BuiltInPack("espresso_data", "pack.espresso_flavoured.data", true, true));
		packs.add(new BuiltInPack("espresso_look", "pack.espresso_flavoured.look", false, false));
		if (Config.longerDays) {
			packs.add(new BuiltInPack("espresso_long_days", "pack.espresso_flavoured.long_days", true, true));
		}
		return packs;
	}
}

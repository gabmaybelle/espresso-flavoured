package com.gabbymaybelle.espresso;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

/**
 * Settings in {@code config/espresso_flavoured.properties}. Missing keys get their defaults, and
 * the file is only written when it's missing a setting (so players can see what exists) or the
 * settings screen changes one; a value that can't be read keeps its default and says so in the
 * log. Settings this version doesn't know are kept. Read at startup; the settings screen changes
 * them as you play (only longerDays needs a restart).
 */
public final class Config {
	private static final Path FILE = Platform.get().configDir().resolve(Espresso.MOD_ID + ".properties");
	/** Written to the file, so a later version can tell an older file apart if it ever needs to change one. */
	private static final int CONFIG_VERSION = 2;
	/** Every setting the file should have; the file is written when one is missing. */
	private static final List<String> KEYS = List.of("blockMerchant", "bundleUnstackables", "longerDays",
			"regenNeedsSaturation", "toughNights", "drowsiness", "caffeineCrash", "coffeeBarRequired", "sugarCaneHeight", "configVersion");
	/** The sliders' limits on the settings screen; the file is held to the same ones. */
	public static final int MAX_BUNDLE_UNSTACKABLES = 8;
	public static final int MIN_SUGAR_CANE_HEIGHT = 3;
	public static final int MAX_SUGAR_CANE_HEIGHT = 8;
	/** The file as it was read, so settings from a newer version (or added by hand) survive a save. */
	private static Properties loaded = new Properties();

	/** The wandering trader also offers building blocks from far-off biomes. */
	public static boolean blockMerchant = true;
	/** How many tools, armour pieces and other non-stacking items fit in one bundle. 1 is vanilla. */
	public static int bundleUnstackables = 4;
	/** The server's bundleUnstackables while playing on one (0 = not on a server, use our own). Not a setting: set by ServerSettings. */
	public static int serverBundleUnstackables = 0;
	/** 20-minute days and 10-minute nights (vanilla: 10 and 10). */
	public static boolean longerDays = true;
	/** Slow natural healing only happens while you have saturation (you've eaten recently). */
	public static boolean regenNeedsSaturation = true;
	/** Nights bring a few more hostile mobs, with 20% more health and damage. */
	public static boolean toughNights = true;
	/** Drowsy after three days without sleeping at home (any bed within 32 blocks of your home bed); Hungry too if you haven't eaten in that time. */
	public static boolean drowsiness = true;
	/** After three days away, coffee only holds Drowsy off for a while, then you crash. Off: coffee holds it off for as long as you keep drinking. */
	public static boolean caffeineCrash = false;
	/** The espresso machine only brews on a coffee bar. */
	public static boolean coffeeBarRequired = true;
	/** How tall sugar cane grows (vanilla: 3). */
	public static int sugarCaneHeight = 5;
	// The gazebo at spawn and its protection are Spawn Gazebo's (bundled): its settings are in config/spawn_gazebo.properties.
	// Builds that stay built, phantoms, a monster-free radius and keeping your hotbar on death were settings until 0.3.0;
	// they are Cozy Core's game rules now. Their old keys in the file are ignored (and kept, like any unknown key).

	private Config() {
	}

	/** Tools per bundle right now: the server's setting when on a server, otherwise this game's. */
	public static int bundleUnstackables() {
		return serverBundleUnstackables > 0 ? serverBundleUnstackables : bundleUnstackables;
	}

	static void load() {
		Properties props = new Properties();
		if (Files.exists(FILE)) {
			try (Reader reader = Files.newBufferedReader(FILE)) {
				props.load(reader);
			} catch (IOException e) {
				Espresso.LOGGER.warn("Espresso Flavoured: couldn't read {}, using defaults", FILE, e);
			}
		}
		blockMerchant = bool(props, "blockMerchant", blockMerchant);
		bundleUnstackables = Math.max(1, Math.min(MAX_BUNDLE_UNSTACKABLES, integer(props, "bundleUnstackables", bundleUnstackables)));
		longerDays = bool(props, "longerDays", longerDays);
		regenNeedsSaturation = bool(props, "regenNeedsSaturation", regenNeedsSaturation);
		toughNights = bool(props, "toughNights", toughNights);
		drowsiness = bool(props, "drowsiness", drowsiness);
		// Version 2 made the caffeine crash (and Hungry with it) an opt-in, so older files get it off once.
		caffeineCrash = integer(props, "configVersion", CONFIG_VERSION) < 2 ? false : bool(props, "caffeineCrash", caffeineCrash);
		coffeeBarRequired = bool(props, "coffeeBarRequired", coffeeBarRequired);
		sugarCaneHeight = Math.max(MIN_SUGAR_CANE_HEIGHT, Math.min(MAX_SUGAR_CANE_HEIGHT, integer(props, "sugarCaneHeight", sugarCaneHeight)));
		loaded = props;
		// Only write when there's something to add: a new file, a new setting, or an older version.
		if (!KEYS.stream().allMatch(props::containsKey) || integer(props, "configVersion", CONFIG_VERSION) < CONFIG_VERSION) {
			save();
		}
	}

	/** Writes every setting back to the file (called after loading and from the settings screen). */
	public static void save() {
		Properties props = new Properties();
		props.putAll(loaded);
		props.setProperty("blockMerchant", Boolean.toString(blockMerchant));
		props.setProperty("bundleUnstackables", Integer.toString(bundleUnstackables));
		props.setProperty("longerDays", Boolean.toString(longerDays));
		props.setProperty("regenNeedsSaturation", Boolean.toString(regenNeedsSaturation));
		props.setProperty("toughNights", Boolean.toString(toughNights));
		props.setProperty("drowsiness", Boolean.toString(drowsiness));
		props.setProperty("caffeineCrash", Boolean.toString(caffeineCrash));
		props.setProperty("coffeeBarRequired", Boolean.toString(coffeeBarRequired));
		props.setProperty("sugarCaneHeight", Integer.toString(sugarCaneHeight));
		props.setProperty("configVersion", Integer.toString(CONFIG_VERSION));
		// Written to a temporary file first and then swapped in, so a crash part-way through never
		// leaves a half-written or empty settings file.
		Path temp = FILE.resolveSibling(FILE.getFileName() + ".tmp");
		try (Writer writer = Files.newBufferedWriter(temp)) {
			props.store(writer, "Espresso Flavoured (also editable in game: Mod Menu > Espresso Flavoured > Config)\n"
					+ "blockMerchant: the wandering trader also sells building blocks\n"
					+ "bundleUnstackables: how many tools or armour pieces fit in one bundle (1 = vanilla, up to 8)\n"
					+ "longerDays: 20-minute days and 10-minute nights\n"
					+ "(the gazebo at spawn is Spawn Gazebo's, bundled with Espresso: its settings are in spawn_gazebo.properties)\n"
					+ "regenNeedsSaturation: slow healing only while you've eaten recently\n"
					+ "toughNights: a few more, slightly tougher monsters at night\n"
					+ "drowsiness: Drowsy after three days without sleeping at home (crouch + use a bed with an empty hand to set it; any bed within 32 blocks counts); coffee holds it off while it lasts\n"
					+ "caffeineCrash: a harder Drowsy: coffee only holds it off for a day and then you crash, and three days without eating as well makes you Hungry\n"
					+ "coffeeBarRequired: the espresso machine only brews on top of a coffee bar\n"
					+ "sugarCaneHeight: how tall sugar cane grows (3, as in vanilla, to 8)\n"
					+ "longerDays needs a restart; everything else applies straight away\n"
					+ "(builds that stay built, phantoms, a monster-free home and keeping your hotbar on death are Cozy Core's game rules now)");
		} catch (IOException e) {
			Espresso.LOGGER.warn("Espresso Flavoured: couldn't write {}", FILE, e);
			return;
		}
		try {
			replace(temp, FILE);
		} catch (IOException e) {
			Espresso.LOGGER.warn("Espresso Flavoured: couldn't write {}", FILE, e);
		}
	}

	/** Moves {@code from} over {@code to} in one step where the file system allows it. */
	static void replace(Path from, Path to) throws IOException {
		try {
			Files.move(from, to, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
		} catch (AtomicMoveNotSupportedException e) {
			Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
		}
	}

	private static boolean bool(Properties props, String key, boolean fallback) {
		String value = props.getProperty(key);
		if (value == null) {
			return fallback;
		}
		String v = value.trim().toLowerCase(Locale.ROOT);
		if (v.equals("true") || v.equals("false")) {
			return v.equals("true");
		}
		// A typo shouldn't quietly switch a setting off: keep the default and say so.
		Espresso.LOGGER.warn("Espresso Flavoured: {} should be true or false, not \"{}\"; using {}", key, value.trim(), fallback);
		return fallback;
	}

	private static int integer(Properties props, String key, int fallback) {
		String value = props.getProperty(key);
		if (value == null) {
			return fallback;
		}
		try {
			return Math.max(0, Integer.parseInt(value.trim()));
		} catch (NumberFormatException e) {
			Espresso.LOGGER.warn("Espresso Flavoured: {} should be a whole number, not \"{}\"; using {}", key, value.trim(), fallback);
			return fallback;
		}
	}
}

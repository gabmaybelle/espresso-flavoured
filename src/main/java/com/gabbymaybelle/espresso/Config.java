package com.gabbymaybelle.espresso;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Settings in {@code config/espresso.properties}. Missing keys get their defaults and
 * the file is rewritten with every key, so players can see what exists. Read at startup;
 * the settings screen changes them as you play (only longerDays needs a restart).
 */
public final class Config {
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve(Espresso.MOD_ID + ".properties");
	/** Bumped when an old file needs a one-off change on load (see {@link #load}). */
	private static final int CONFIG_VERSION = 2;

	/** Creeper explosions break blocks (vanilla). Off: they still hurt, but spare builds. */
	public static boolean creeperGriefing = false;
	/** Endermen pick up and move blocks (vanilla). */
	public static boolean endermanGriefing = false;
	/** Phantoms spawn for players who haven't slept (vanilla). */
	public static boolean phantoms = false;
	/** Hostile mobs don't spawn naturally within this many blocks of a player's home bed. 0 turns it off. */
	public static int safeHomeRadius = 32;
	/** The wandering trader also offers building blocks from far-off biomes. */
	public static boolean blockMerchant = true;
	/** How many tools, armour pieces and other non-stacking items fit in one bundle. 1 is vanilla. */
	public static int bundleUnstackables = 4;
	/** The server's bundleUnstackables while playing on one (0 = not on a server, use our own). */
	public static int serverBundleUnstackables = 0;

	/** Tools per bundle right now: the server's setting when on a server, otherwise this game's. */
	public static int bundleUnstackables() {
		return serverBundleUnstackables > 0 ? serverBundleUnstackables : bundleUnstackables;
	}
	/** 20-minute days and 10-minute nights (vanilla: 10 and 10). */
	public static boolean longerDays = true;
	/** A day counter and clock in the top-right corner (client side). */
	public static boolean clock = true;
	/** Keep the hotbar, armour and off-hand on death; everything else drops. */
	public static boolean keepHotbarOnDeath = true;
	/** Slow natural healing only happens while you have saturation (you've eaten recently). */
	public static boolean regenNeedsSaturation = true;
	/** Nights bring a few more hostile mobs, with 20% more health and damage. */
	public static boolean toughNights = true;
	/** Drowsy after two days without sleeping at home; Hungry too if you haven't eaten in that time. */
	public static boolean drowsiness = true;
	/** After two days away, coffee only holds Drowsy off for a while, then you crash. Off: coffee holds it off for as long as you keep drinking. */
	public static boolean caffeineCrash = true;
	/** The espresso machine only brews on a coffee bar. */
	public static boolean coffeeBarRequired = true;
	/** How tall sugar cane grows (vanilla: 3). */
	public static int sugarCaneHeight = 5;
	/**
	 * Lusher rivers and ponds in new land: seagrass and kelp, plants and flowers on the banks, moss and mud
	 * at the waterline, moss carpet and azaleas, muddy banks where there's clay, and more firefly bushes and
	 * lily pads. Read at startup.
	 */
	public static boolean lusherRivers = true;
	/** A little stone gazebo at the world spawn of every new world. Read when a world is created. */
	public static boolean spawnGazebo = true;

	private Config() {
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
		creeperGriefing = bool(props, "creeperGriefing", creeperGriefing);
		endermanGriefing = bool(props, "endermanGriefing", endermanGriefing);
		phantoms = bool(props, "phantoms", phantoms);
		safeHomeRadius = integer(props, "safeHomeRadius", safeHomeRadius);
		blockMerchant = bool(props, "blockMerchant", blockMerchant);
		bundleUnstackables = Math.max(1, integer(props, "bundleUnstackables", bundleUnstackables));
		longerDays = bool(props, "longerDays", longerDays);
		clock = bool(props, "clock", clock);
		keepHotbarOnDeath = bool(props, "keepHotbarOnDeath", keepHotbarOnDeath);
		regenNeedsSaturation = bool(props, "regenNeedsSaturation", regenNeedsSaturation);
		toughNights = bool(props, "toughNights", toughNights);
		drowsiness = bool(props, "drowsiness", drowsiness);
		caffeineCrash = bool(props, "caffeineCrash", caffeineCrash);
		coffeeBarRequired = bool(props, "coffeeBarRequired", coffeeBarRequired);
		sugarCaneHeight = Math.max(1, Math.min(16, integer(props, "sugarCaneHeight", sugarCaneHeight)));
		lusherRivers = bool(props, "lusherRivers", lusherRivers);
		spawnGazebo = bool(props, "spawnGazebo", spawnGazebo);
		// Files from before 0.3.0 have lusherRivers switched off whenever Terralith was installed (the old
		// default, not a choice). Lush shores are part of the look now, so turn it on once; it can be
		// turned off again any time.
		if (integer(props, "configVersion", 1) < CONFIG_VERSION) {
			lusherRivers = true;
		}
		save();
	}

	/** Writes every setting back to the file (called after loading and from the settings screen). */
	public static void save() {
		Properties props = new Properties();
		props.setProperty("creeperGriefing", Boolean.toString(creeperGriefing));
		props.setProperty("endermanGriefing", Boolean.toString(endermanGriefing));
		props.setProperty("phantoms", Boolean.toString(phantoms));
		props.setProperty("safeHomeRadius", Integer.toString(safeHomeRadius));
		props.setProperty("blockMerchant", Boolean.toString(blockMerchant));
		props.setProperty("bundleUnstackables", Integer.toString(bundleUnstackables));
		props.setProperty("longerDays", Boolean.toString(longerDays));
		props.setProperty("clock", Boolean.toString(clock));
		props.setProperty("keepHotbarOnDeath", Boolean.toString(keepHotbarOnDeath));
		props.setProperty("regenNeedsSaturation", Boolean.toString(regenNeedsSaturation));
		props.setProperty("toughNights", Boolean.toString(toughNights));
		props.setProperty("drowsiness", Boolean.toString(drowsiness));
		props.setProperty("caffeineCrash", Boolean.toString(caffeineCrash));
		props.setProperty("coffeeBarRequired", Boolean.toString(coffeeBarRequired));
		props.setProperty("sugarCaneHeight", Integer.toString(sugarCaneHeight));
		props.setProperty("lusherRivers", Boolean.toString(lusherRivers));
		props.setProperty("spawnGazebo", Boolean.toString(spawnGazebo));
		props.setProperty("configVersion", Integer.toString(CONFIG_VERSION));
		try (Writer writer = Files.newBufferedWriter(FILE)) {
			props.store(writer, "Espresso Flavoured (also editable in game: Mod Menu > Espresso Flavoured > Config)\n"
					+ "creeperGriefing: creeper explosions break blocks (vanilla behaviour)\n"
					+ "endermanGriefing: endermen pick up blocks (vanilla behaviour)\n"
					+ "phantoms: phantoms spawn for players who haven't slept (vanilla behaviour)\n"
					+ "safeHomeRadius: no natural hostile spawns within this many blocks of your home bed (crouch + use a bed with an empty hand; 0 = off)\n"
					+ "blockMerchant: the wandering trader also sells building blocks\n"
					+ "bundleUnstackables: how many tools or armour pieces fit in one bundle (1 = vanilla)\n"
					+ "longerDays: 20-minute days and 10-minute nights\n"
					+ "clock: a day counter and clock in the top-right corner\n"
					+ "lusherRivers: seagrass, bank plants, flowers, moss and mud along rivers and ponds in new land\n"
					+ "spawnGazebo: a little stone gazebo at the world spawn of new worlds\n"
					+ "keepHotbarOnDeath: keep your hotbar, armour and off-hand when you die\n"
					+ "regenNeedsSaturation: slow healing only while you've eaten recently\n"
					+ "toughNights: a few more, slightly tougher monsters at night\n"
					+ "drowsiness: Drowsy after two days without sleeping at home (any bed within your safe home radius), and Hungry if you haven't eaten in that time either\n"
					+ "caffeineCrash: past two days away, coffee holds off Drowsy for a while and then you crash (off: it holds it off as long as you keep drinking)\n"
					+ "coffeeBarRequired: the espresso machine only brews on top of a coffee bar\n"
					+ "sugarCaneHeight: how tall sugar cane grows (vanilla 3)\n"
					+ "longerDays needs a restart; everything else applies straight away");
		} catch (IOException e) {
			Espresso.LOGGER.warn("Espresso Flavoured: couldn't write {}", FILE, e);
		}
	}

	private static boolean bool(Properties props, String key, boolean fallback) {
		String value = props.getProperty(key);
		return value == null ? fallback : Boolean.parseBoolean(value.trim());
	}

	private static int integer(Properties props, String key, int fallback) {
		String value = props.getProperty(key);
		if (value == null) {
			return fallback;
		}
		try {
			return Math.max(0, Integer.parseInt(value.trim()));
		} catch (NumberFormatException e) {
			return fallback;
		}
	}
}

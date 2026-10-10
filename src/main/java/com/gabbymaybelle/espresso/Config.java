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
 * the file is rewritten with every key, so players can see what exists. Read once at
 * startup; changes need a restart.
 */
public final class Config {
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve(Espresso.MOD_ID + ".properties");

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

		props.setProperty("creeperGriefing", Boolean.toString(creeperGriefing));
		props.setProperty("endermanGriefing", Boolean.toString(endermanGriefing));
		props.setProperty("phantoms", Boolean.toString(phantoms));
		props.setProperty("safeHomeRadius", Integer.toString(safeHomeRadius));
		props.setProperty("blockMerchant", Boolean.toString(blockMerchant));
		props.setProperty("bundleUnstackables", Integer.toString(bundleUnstackables));
		try (Writer writer = Files.newBufferedWriter(FILE)) {
			props.store(writer, "Espresso Flavoured (restart the game after editing)\n"
					+ "creeperGriefing: creeper explosions break blocks (vanilla behaviour)\n"
					+ "endermanGriefing: endermen pick up blocks (vanilla behaviour)\n"
					+ "phantoms: phantoms spawn for players who haven't slept (vanilla behaviour)\n"
					+ "safeHomeRadius: no natural hostile spawns within this many blocks of your home bed (crouch + use a bed with an empty hand; 0 = off)\n"
					+ "blockMerchant: the wandering trader also sells building blocks\n"
					+ "bundleUnstackables: how many tools or armour pieces fit in one bundle (1 = vanilla)");
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

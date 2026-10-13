package com.gabbymaybelle.espresso.client;

import com.gabbymaybelle.espresso.Config;
import com.gabbymaybelle.espresso.ServerSettings;
import com.gabbymaybelle.espresso.content.GazeboGuard;
import com.gabbymaybelle.espresso.content.SpawnGazebo;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;

/**
 * Espresso Flavoured's settings, opened from Mod Menu. Built from vanilla option widgets
 * so it looks like the game's own options. Settings apply to worlds you host; on someone
 * else's server, the server's config file decides, so those settings are greyed out there
 * and only the client-side ones (the clock and the dusk warning) can be changed.
 */
public class EspressoSettingsScreen extends OptionsSubScreen {
	/** Settings the server decides, greyed out while playing on one. */
	private final List<OptionInstance<?>> serverSide = new ArrayList<>();

	public EspressoSettingsScreen(Screen parent) {
		super(parent, Minecraft.getInstance().options, Component.translatable("espresso_flavoured.options.title"));
	}

	/** True while connected to a server that isn't this game's own. */
	private static boolean onServer() {
		Minecraft mc = Minecraft.getInstance();
		return mc.level != null && mc.getSingleplayerServer() == null;
	}

	@Override
	protected void addOptions() {
		boolean onServer = onServer();
		if (onServer) {
			list.addHeader(Component.translatable("espresso_flavoured.options.on_server"));
		}
		list.addHeader(Component.translatable("espresso_flavoured.options.section.home"));
		list.addSmall(
				server(radius("safeHomeRadius", Config.safeHomeRadius, 0, 64, v -> Config.safeHomeRadius = v)),
				server(toggle("keepHotbarOnDeath", Config.keepHotbarOnDeath, v -> Config.keepHotbarOnDeath = v)),
				server(toggle("creeperGriefing", Config.creeperGriefing, v -> Config.creeperGriefing = v)),
				server(toggle("endermanGriefing", Config.endermanGriefing, v -> Config.endermanGriefing = v)));

		list.addHeader(Component.translatable("espresso_flavoured.options.section.days"));
		list.addSmall(
				server(toggle("longerDays", Config.longerDays, v -> Config.longerDays = v)),
				server(toggle("toughNights", Config.toughNights, v -> Config.toughNights = v)),
				server(toggle("phantoms", Config.phantoms, v -> Config.phantoms = v)),
				toggle("clock", Config.clock, v -> Config.clock = v),
				choice("clockPosition", Config.ClockPosition.values(), Config.clockPosition, v -> Config.clockPosition = v),
				toggle("duskWarning", Config.duskWarning, v -> Config.duskWarning = v));

		list.addHeader(Component.translatable("espresso_flavoured.options.section.wellbeing"));
		list.addSmall(
				server(toggle("regenNeedsSaturation", Config.regenNeedsSaturation, v -> Config.regenNeedsSaturation = v)),
				server(toggle("drowsiness", Config.drowsiness, v -> Config.drowsiness = v)),
				server(toggle("caffeineCrash", Config.caffeineCrash, v -> Config.caffeineCrash = v)));

		list.addHeader(Component.translatable("espresso_flavoured.options.section.coffee"));
		list.addSmall(
				server(toggle("coffeeBarRequired", Config.coffeeBarRequired, v -> Config.coffeeBarRequired = v)),
				server(radius("sugarCaneHeight", Config.sugarCaneHeight, 3, 8, v -> Config.sugarCaneHeight = v)));

		list.addHeader(Component.translatable("espresso_flavoured.options.section.world"));
		if (SpawnGazebo.STANDALONE) {
			list.addHeader(Component.translatable("espresso_flavoured.options.spawn_gazebo_installed"));
		}
		OptionInstance<Boolean> spawnGazebo = server(toggle("spawnGazebo", Config.spawnGazebo, v -> Config.spawnGazebo = v));
		OptionInstance<Boolean> protectGazebo = server(toggle("protectGazebo", Config.protectGazebo, v -> Config.protectGazebo = v));
		list.addSmall(
				server(toggle("lusherRivers", Config.lusherRivers, v -> Config.lusherRivers = v)),
				spawnGazebo, protectGazebo);
		if (SpawnGazebo.STANDALONE) {
			// The standalone mod's settings decide; these two do nothing while it's installed.
			for (OptionInstance<?> option : List.of(spawnGazebo, protectGazebo)) {
				AbstractWidget widget = list.findOption(option);
				if (widget != null) {
					widget.active = false;
				}
			}
		}

		list.addHeader(Component.translatable("espresso_flavoured.options.section.storage"));
		list.addSmall(
				server(radius("bundleUnstackables", Config.bundleUnstackables, 1, 8, v -> Config.bundleUnstackables = v)),
				server(toggle("blockMerchant", Config.blockMerchant, v -> Config.blockMerchant = v)));

		if (onServer) {
			for (OptionInstance<?> option : serverSide) {
				AbstractWidget widget = list.findOption(option);
				if (widget != null) {
					widget.active = false;
				}
			}
		}
	}

	/** Marks a setting as the server's, so it's greyed out while playing on one. */
	private <T> OptionInstance<T> server(OptionInstance<T> option) {
		serverSide.add(option);
		return option;
	}

	@Override
	public void removed() {
		super.removed();
		Config.save();
		IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
		if (server != null) {
			// Hosting a LAN world: guests use the host's bundle setting, and hear whether the gazebo is protected.
			server.execute(() -> {
				ServerSettings.broadcast(server);
				GazeboGuard.broadcast(server);
			});
		}
	}

	private static OptionInstance<Boolean> toggle(String key, boolean value, Consumer<Boolean> set) {
		return OptionInstance.createBoolean("espresso_flavoured.options." + key,
				OptionInstance.cachedConstantTooltip(Component.translatable("espresso_flavoured.options." + key + ".tooltip")),
				value, set::accept);
	}

	/** A button that cycles through an enum's values, each named "espresso_flavoured.options.<key>.<value>". */
	private static <E extends Enum<E>> OptionInstance<E> choice(String key, E[] values, E value, Consumer<E> set) {
		String prefix = "espresso_flavoured.options." + key;
		return new OptionInstance<>(prefix,
				OptionInstance.cachedConstantTooltip(Component.translatable(prefix + ".tooltip")),
				// The button adds the caption itself, so this is just the value.
				(caption, v) -> Component.translatable(prefix + "." + v.name().toLowerCase(Locale.ROOT)),
				new OptionInstance.Enum<>(List.of(values), Codec.INT.xmap(i -> values[i], Enum::ordinal)),
				value, set::accept);
	}

	private static OptionInstance<Integer> radius(String key, int value, int min, int max, Consumer<Integer> set) {
		return new OptionInstance<>("espresso_flavoured.options." + key,
				OptionInstance.cachedConstantTooltip(Component.translatable("espresso_flavoured.options." + key + ".tooltip")),
				(caption, v) -> Component.translatable("options.generic_value", caption,
						v == 0 ? Component.translatable("options.off") : Component.literal(Integer.toString(v))),
				new OptionInstance.IntRange(min, max), value, set::accept);
	}
}

package com.gabbymaybelle.espresso.client;

import com.gabbymaybelle.espresso.Config;
import com.gabbymaybelle.espresso.ServerSettings;
import com.gabbymaybelle.spawngazebo.client.SettingsScreen;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;

/**
 * Espresso Flavoured's settings, opened from Mod Menu. Built from vanilla option widgets
 * so it looks like the game's own options. Settings apply to worlds you host; on someone
 * else's server, the server's config file decides, so they're greyed out there.
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
				server(radius("safeHomeRadius", Config.safeHomeRadius, 0, Config.MAX_SAFE_HOME_RADIUS, v -> Config.safeHomeRadius = v)),
				server(toggle("keepHotbarOnDeath", Config.keepHotbarOnDeath, v -> Config.keepHotbarOnDeath = v)),
				server(toggle("creeperGriefing", Config.creeperGriefing, v -> Config.creeperGriefing = v)),
				server(toggle("endermanGriefing", Config.endermanGriefing, v -> Config.endermanGriefing = v)));

		list.addHeader(Component.translatable("espresso_flavoured.options.section.days"));
		list.addSmall(
				server(toggle("longerDays", Config.longerDays, v -> Config.longerDays = v)),
				server(toggle("toughNights", Config.toughNights, v -> Config.toughNights = v)),
				server(toggle("phantoms", Config.phantoms, v -> Config.phantoms = v)));

		list.addHeader(Component.translatable("espresso_flavoured.options.section.wellbeing"));
		list.addSmall(
				server(toggle("regenNeedsSaturation", Config.regenNeedsSaturation, v -> Config.regenNeedsSaturation = v)),
				server(toggle("drowsiness", Config.drowsiness, v -> Config.drowsiness = v)),
				server(toggle("caffeineCrash", Config.caffeineCrash, v -> Config.caffeineCrash = v)));

		list.addHeader(Component.translatable("espresso_flavoured.options.section.coffee"));
		list.addSmall(
				server(toggle("coffeeBarRequired", Config.coffeeBarRequired, v -> Config.coffeeBarRequired = v)),
				server(slider("sugarCaneHeight", Config.sugarCaneHeight, Config.MIN_SUGAR_CANE_HEIGHT, Config.MAX_SUGAR_CANE_HEIGHT, v -> Config.sugarCaneHeight = v)));

		list.addHeader(Component.translatable("espresso_flavoured.options.section.world"));
		// The gazebo at spawn is Spawn Gazebo's (bundled), with its own settings screen.
		list.addBig(Button.builder(Component.translatable("espresso_flavoured.options.gazeboSettings"),
						button -> minecraft.gui.setScreen(new SettingsScreen(this)))
				.tooltip(Tooltip.create(Component.translatable("espresso_flavoured.options.gazeboSettings.tooltip")))
				.build());

		list.addHeader(Component.translatable("espresso_flavoured.options.section.storage"));
		list.addSmall(
				server(slider("bundleUnstackables", Config.bundleUnstackables, 1, Config.MAX_BUNDLE_UNSTACKABLES, v -> Config.bundleUnstackables = v)),
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
			// Hosting a LAN world: guests use the host's bundle setting.
			server.execute(() -> ServerSettings.broadcast(server));
		}
	}

	private static OptionInstance<Boolean> toggle(String key, boolean value, Consumer<Boolean> set) {
		return OptionInstance.createBoolean("espresso_flavoured.options." + key,
				OptionInstance.cachedConstantTooltip(Component.translatable("espresso_flavoured.options." + key + ".tooltip")),
				value, set::accept);
	}

	/** A slider for a count. */
	private static OptionInstance<Integer> slider(String key, int value, int min, int max, Consumer<Integer> set) {
		return new OptionInstance<>("espresso_flavoured.options." + key,
				OptionInstance.cachedConstantTooltip(Component.translatable("espresso_flavoured.options." + key + ".tooltip")),
				(caption, v) -> Component.translatable("options.generic_value", caption, Component.literal(Integer.toString(v))),
				new OptionInstance.IntRange(min, max), value, set::accept);
	}

	/** A slider for a radius, where 0 reads "Off". */
	private static OptionInstance<Integer> radius(String key, int value, int min, int max, Consumer<Integer> set) {
		return new OptionInstance<>("espresso_flavoured.options." + key,
				OptionInstance.cachedConstantTooltip(Component.translatable("espresso_flavoured.options." + key + ".tooltip")),
				(caption, v) -> Component.translatable("options.generic_value", caption,
						v == 0 ? Component.translatable("options.off") : Component.literal(Integer.toString(v))),
				new OptionInstance.IntRange(min, max), value, set::accept);
	}
}

package com.gabbymaybelle.espresso.client;

import com.gabbymaybelle.espresso.Config;
import com.gabbymaybelle.espresso.ServerSettings;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;

/**
 * Espresso Flavoured's settings, opened from Mod Menu. Built from vanilla option widgets
 * so it looks like the game's own options. Settings apply to worlds you host; on a
 * server, the server's config file decides.
 */
public class EspressoSettingsScreen extends OptionsSubScreen {
	public EspressoSettingsScreen(Screen parent) {
		super(parent, Minecraft.getInstance().options, Component.translatable("espresso.options.title"));
	}

	@Override
	protected void addOptions() {
		list.addHeader(Component.translatable("espresso.options.section.home"));
		list.addSmall(
				radius("safeHomeRadius", Config.safeHomeRadius, 0, 64, v -> Config.safeHomeRadius = v),
				toggle("keepHotbarOnDeath", Config.keepHotbarOnDeath, v -> Config.keepHotbarOnDeath = v),
				toggle("creeperGriefing", Config.creeperGriefing, v -> Config.creeperGriefing = v),
				toggle("endermanGriefing", Config.endermanGriefing, v -> Config.endermanGriefing = v));

		list.addHeader(Component.translatable("espresso.options.section.days"));
		list.addSmall(
				toggle("longerDays", Config.longerDays, v -> Config.longerDays = v),
				toggle("toughNights", Config.toughNights, v -> Config.toughNights = v),
				toggle("phantoms", Config.phantoms, v -> Config.phantoms = v));

		list.addHeader(Component.translatable("espresso.options.section.wellbeing"));
		list.addSmall(
				toggle("regenNeedsSaturation", Config.regenNeedsSaturation, v -> Config.regenNeedsSaturation = v),
				toggle("drowsiness", Config.drowsiness, v -> Config.drowsiness = v),
				toggle("caffeineCrash", Config.caffeineCrash, v -> Config.caffeineCrash = v));

		list.addHeader(Component.translatable("espresso.options.section.coffee"));
		list.addSmall(
				toggle("coffeeBarRequired", Config.coffeeBarRequired, v -> Config.coffeeBarRequired = v),
				radius("sugarCaneHeight", Config.sugarCaneHeight, 3, 8, v -> Config.sugarCaneHeight = v));

		list.addHeader(Component.translatable("espresso.options.section.storage"));
		list.addSmall(
				radius("bundleUnstackables", Config.bundleUnstackables, 1, 8, v -> Config.bundleUnstackables = v),
				toggle("blockMerchant", Config.blockMerchant, v -> Config.blockMerchant = v));
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
		return OptionInstance.createBoolean("espresso.options." + key,
				OptionInstance.cachedConstantTooltip(Component.translatable("espresso.options." + key + ".tooltip")),
				value, set::accept);
	}

	private static OptionInstance<Integer> radius(String key, int value, int min, int max, Consumer<Integer> set) {
		return new OptionInstance<>("espresso.options." + key,
				OptionInstance.cachedConstantTooltip(Component.translatable("espresso.options." + key + ".tooltip")),
				(caption, v) -> Component.translatable("options.generic_value", caption,
						v == 0 ? Component.translatable("options.off") : Component.literal(Integer.toString(v))),
				new OptionInstance.IntRange(min, max), value, set::accept);
	}
}

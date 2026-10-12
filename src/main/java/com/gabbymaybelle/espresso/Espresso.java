package com.gabbymaybelle.espresso;

import com.gabbymaybelle.espresso.content.EspressoContent;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Espresso implements ModInitializer {
	public static final String MOD_ID = "espresso_flavoured";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		Config.load();
		HomeBeds.register();
		EspressoContent.register();
		Wellbeing.register();
		KeepOnDeath.register();
		ServerSettings.register();

		ModContainer mod = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow();
		// Recipes, advancements, the starter kit, the field guide and the block merchant's trades.
		ResourceLoader.registerBuiltinPack(Identifier.fromNamespaceAndPath(MOD_ID, "espresso_data"), mod,
				Component.translatable("pack.espresso_flavoured.data"), PackActivationType.ALWAYS_ENABLED);
		// "Espresso Flavoured" under the title-screen logo, and splash texts; can be switched off in the Resource Packs screen.
		ResourceLoader.registerBuiltinPack(Identifier.fromNamespaceAndPath(MOD_ID, "espresso_look"), mod,
				Component.translatable("pack.espresso_flavoured.look"), PackActivationType.DEFAULT_ENABLED);
		// 20-minute days and 10-minute nights.
		if (Config.longerDays) {
			ResourceLoader.registerBuiltinPack(Identifier.fromNamespaceAndPath(MOD_ID, "espresso_long_days"), mod,
					Component.translatable("pack.espresso_flavoured.long_days"), PackActivationType.ALWAYS_ENABLED);
		}
		LOGGER.info("Espresso Flavoured: ready (safe home radius {}, creeper griefing {}, enderman griefing {}, phantoms {})",
				Config.safeHomeRadius, Config.creeperGriefing, Config.endermanGriefing, Config.phantoms);
	}
}

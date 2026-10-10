package com.gabbymaybelle.espresso.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/** Adds a Config button for Espresso Flavoured in Mod Menu. */
public class ModMenuIntegration implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return EspressoSettingsScreen::new;
	}
}

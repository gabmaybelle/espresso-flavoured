package com.gabbymaybelle.espresso.client;

import com.gabbymaybelle.espresso.content.EspressoContent;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;

/** Client setup: the espresso machine's screen. */
public class EspressoClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		MenuScreens.register(EspressoContent.ESPRESSO_MACHINE_MENU, EspressoMachineScreen::new);
	}
}

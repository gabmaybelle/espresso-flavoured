package com.gabbymaybelle.espresso.fabric;

import com.gabbymaybelle.espresso.ServerSettings;
import com.gabbymaybelle.espresso.client.EspressoClient;
import com.gabbymaybelle.espresso.client.EspressoMachineScreen;
import com.gabbymaybelle.espresso.content.EspressoContent;
import com.gabbymaybelle.espresso.client.EspressoTooltips;
import java.util.List;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.gui.screens.MenuScreens;

/** The client's part on Fabric: screens and renderers, tints, tooltips and the server's settings. */
public class EspressoFabricClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EspressoClient.setup();
		MenuScreens.register(EspressoContent.ESPRESSO_MACHINE_MENU, EspressoMachineScreen::new);
		EspressoClient.blockTints().forEach((block, tint) -> BlockColorRegistry.register(List.<BlockTintSource>of(tint), block));
		ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> EspressoTooltips.add(stack, lines));
		ClientPlayNetworking.registerGlobalReceiver(ServerSettings.TYPE, (settings, context) -> EspressoClient.onServerSettings(settings));
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> EspressoClient.onDisconnect());
	}
}

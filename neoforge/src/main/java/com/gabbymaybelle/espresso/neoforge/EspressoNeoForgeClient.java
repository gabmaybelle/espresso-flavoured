package com.gabbymaybelle.espresso.neoforge;

import com.gabbymaybelle.espresso.Espresso;
import com.gabbymaybelle.espresso.client.EspressoClient;
import com.gabbymaybelle.espresso.client.EspressoMachineScreen;
import com.gabbymaybelle.espresso.client.EspressoSettingsScreen;
import com.gabbymaybelle.espresso.client.EspressoTooltips;
import com.gabbymaybelle.espresso.content.EspressoContent;
import java.util.List;
import net.minecraft.client.color.block.BlockTintSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/** The client's part on NeoForge: screens and renderers, tints, tooltips, the Mods screen's Config button and the server's settings. */
@Mod(value = Espresso.MOD_ID, dist = Dist.CLIENT)
public class EspressoNeoForgeClient {
	public EspressoNeoForgeClient(IEventBus modBus, ModContainer container) {
		container.registerExtensionPoint(IConfigScreenFactory.class, (mod, parent) -> new EspressoSettingsScreen(parent));
		modBus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(EspressoClient::setup));
		modBus.addListener((RegisterMenuScreensEvent event) -> event.register(EspressoContent.ESPRESSO_MACHINE_MENU, EspressoMachineScreen::new));
		modBus.addListener((RegisterColorHandlersEvent.BlockTintSources event) ->
				EspressoClient.blockTints().forEach((block, tint) -> event.register(List.<BlockTintSource>of(tint), block)));
		NeoForge.EVENT_BUS.addListener((ItemTooltipEvent event) -> EspressoTooltips.add(event.getItemStack(), event.getToolTip()));
		NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> EspressoClient.onDisconnect());
	}
}

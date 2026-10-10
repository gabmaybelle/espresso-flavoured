package com.gabbymaybelle.espresso.client;

import com.gabbymaybelle.espresso.Config;
import com.gabbymaybelle.espresso.Espresso;
import com.gabbymaybelle.espresso.content.EspressoContent;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

/** Drowsy also darkens the edges of the screen, like eyes struggling to stay open. */
public class EspressoClient implements ClientModInitializer {
	private static final int BANDS = 24;

	@Override
	public void onInitializeClient() {
		// Just above vanilla's full-screen overlays (pumpkin, frost), below the crosshair and hotbar.
		HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS,
				Identifier.fromNamespaceAndPath(Espresso.MOD_ID, "drowsy_vignette"), EspressoClient::drowsyVignette);
	}

	private static void drowsyVignette(GuiGraphicsExtractor graphics, DeltaTracker delta) {
		Minecraft mc = Minecraft.getInstance();
		if (!Config.drowsyVisuals || mc.player == null || !mc.player.hasEffect(EspressoContent.DROWSY)) {
			return;
		}
		int w = graphics.guiWidth(), h = graphics.guiHeight();
		int depthX = w / 6, depthY = h / 5;
		// Bands that get darker towards the edges; deeper at the top and bottom, like eyelids.
		for (int i = 0; i < BANDS; i++) {
			int alpha = (int) (150 * Math.pow(1.0 - (double) i / BANDS, 2));
			int color = alpha << 24 | 0x0a0610;
			int dy = depthY * i / BANDS, dyNext = depthY * (i + 1) / BANDS;
			int dx = depthX * i / BANDS, dxNext = depthX * (i + 1) / BANDS;
			graphics.fill(0, dy, w, dyNext, color);
			graphics.fill(0, h - dyNext, w, h - dy, color);
			graphics.fill(dx, dyNext, dxNext, h - dyNext, color);
			graphics.fill(w - dxNext, dyNext, w - dx, h - dyNext, color);
		}
	}
}

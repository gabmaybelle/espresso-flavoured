package com.gabbymaybelle.espresso.client;

import com.gabbymaybelle.espresso.Espresso;
import com.gabbymaybelle.espresso.content.EspressoMachineBlockEntity;
import com.gabbymaybelle.espresso.content.EspressoMachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** The espresso machine's screen: beans on top, three bottles below, and a coffee drip that fills as it brews. */
public class EspressoMachineScreen extends AbstractContainerScreen<EspressoMachineMenu> {
	private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Espresso.MOD_ID, "textures/gui/container/espresso_machine.png");
	private static final int DRIP_X = 83, DRIP_Y = 36, DRIP_W = 9, DRIP_H = 20;

	public EspressoMachineScreen(EspressoMachineMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
	}

	@Override
	protected void init() {
		super.init();
		titleLabelX = (imageWidth - font.width(title)) / 2;
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		super.extractLabels(graphics, mouseX, mouseY);
		if (!menu.isOnCoffeeBar()) {
			Component warning = Component.translatable("container.espresso.espresso_machine.needs_bar");
			// Right-aligned on the inventory label's line.
			graphics.text(font, warning, imageWidth - 8 - font.width(warning), inventoryLabelY, 0xFFB03A2E, false);
		}
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractBackground(graphics, mouseX, mouseY, partialTick);
		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
		int brewTime = menu.getBrewTime();
		if (brewTime > 0) {
			int filled = Math.round(DRIP_H * (1.0F - (float) brewTime / EspressoMachineBlockEntity.BREW_TIME));
			if (filled > 0) {
				graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos + DRIP_X, topPos + DRIP_Y, 176, 0, DRIP_W, filled, 256, 256);
			}
		}
	}
}

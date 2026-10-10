package com.gabbymaybelle.espresso.client;

import com.gabbymaybelle.espresso.content.EspressoContent;
import com.gabbymaybelle.espresso.content.Mugs;
import com.gabbymaybelle.espresso.content.PlacedMugBlock;
import java.util.List;
import java.util.Set;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

/** Client setup: the espresso machine's screen, and placed mugs tinted to their colour. */
public class EspressoClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		MenuScreens.register(EspressoContent.ESPRESSO_MACHINE_MENU, EspressoMachineScreen::new);
		BlockColorRegistry.register(List.of(new BlockTintSource() {
			@Override
			public int color(BlockState state) {
				return Mugs.tint(state.getValue(PlacedMugBlock.COLOR));
			}

			@Override
			public Set<Property<?>> relevantProperties() {
				return Set.of(PlacedMugBlock.COLOR);
			}
		}), EspressoContent.PLACED_MUG);
	}
}

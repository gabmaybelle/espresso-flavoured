package com.gabbymaybelle.espresso.client;

import com.gabbymaybelle.espresso.content.CanisterBlock;
import com.gabbymaybelle.espresso.content.EspressoContent;
import com.gabbymaybelle.espresso.content.Mugs;
import com.gabbymaybelle.espresso.content.PlacedMugBlock;
import java.util.List;
import java.util.Set;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

/** Client setup: the espresso machine's screen, canister labels, and mugs and canisters tinted to their colour. */
public class EspressoClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		MenuScreens.register(EspressoContent.ESPRESSO_MACHINE_MENU, EspressoMachineScreen::new);
		BlockEntityRenderers.register(EspressoContent.CANISTER_ENTITY, CanisterRenderer::new);
		tintByColor(EspressoContent.PLACED_MUG, PlacedMugBlock.COLOR);
		tintByColor(EspressoContent.CANISTER_BLOCK, CanisterBlock.COLOR);
	}

	/** Tints a block's model (tintindex 0) by its colour property: cream, one of the 16 dyes, or gold. */
	private static void tintByColor(Block block, IntegerProperty color) {
		BlockColorRegistry.register(List.of(new BlockTintSource() {
			@Override
			public int color(BlockState state) {
				return Mugs.tint(state.getValue(color));
			}

			@Override
			public Set<Property<?>> relevantProperties() {
				return Set.of(color);
			}
		}), block);
	}
}

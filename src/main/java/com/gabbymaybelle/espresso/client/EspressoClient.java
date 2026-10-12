package com.gabbymaybelle.espresso.client;

import com.gabbymaybelle.espresso.content.CanisterBlock;
import com.gabbymaybelle.espresso.content.EspressoContent;
import com.gabbymaybelle.espresso.content.GazeboGuard;
import com.gabbymaybelle.espresso.content.Mugs;
import com.gabbymaybelle.espresso.content.PlacedMugBlock;
import com.gabbymaybelle.espresso.Config;
import com.gabbymaybelle.espresso.ServerSettings;
import java.util.List;
import java.util.Set;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.NoopRenderer;
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
		EntityRenderers.register(EspressoContent.SEAT, NoopRenderer::new);
		EspressoClock.register();
		EspressoTooltips.register();
		tintByColor(EspressoContent.PLACED_MUG, PlacedMugBlock.COLOR);
		tintByColor(EspressoContent.CANISTER_BLOCK, CanisterBlock.COLOR);
		// The water lily's pad: the same green as a lily pad in the world.
		BlockColorRegistry.register(List.of(new BlockTintSource() {
			@Override
			public int color(BlockState state) {
				return 0xFF208030;
			}

			@Override
			public Set<Property<?>> relevantProperties() {
				return Set.of();
			}
		}), EspressoContent.WATER_LILY);
		// On someone else's server, use their bundle setting so bundles match for everyone.
		ClientPlayNetworking.registerGlobalReceiver(ServerSettings.TYPE, (settings, context) -> {
			if (!context.client().hasSingleplayerServer()) {
				Config.serverBundleUnstackables = settings.bundleUnstackables();
			}
		});
		// Where the spawn gazebo is (while it's protected), so a placement there is refused on this side too.
		ClientPlayNetworking.registerGlobalReceiver(GazeboGuard.Box.TYPE, (payload, context) ->
				GazeboGuard.setClientBox(payload.box().orElse(null)));
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			Config.serverBundleUnstackables = 0;
			GazeboGuard.setClientBox(null);
		});
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

package com.gabbymaybelle.espresso.client;

import com.gabbymaybelle.espresso.Config;
import com.gabbymaybelle.espresso.ServerSettings;
import com.gabbymaybelle.espresso.content.CanisterBlock;
import com.gabbymaybelle.espresso.content.EspressoContent;
import com.gabbymaybelle.espresso.content.Mugs;
import com.gabbymaybelle.espresso.content.PlacedMugBlock;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

/**
 * Client setup shared by both loaders: the espresso machine's screen, canister labels, and the
 * colours mugs and canisters are tinted. Each loader's client entry calls these at the right time
 * and wires {@link #tooltips} and the settings message to its own events.
 */
public final class EspressoClient {
	private EspressoClient() {
	}

	/** Renderers, once the game's client is ready. */
	public static void setup() {
		BlockEntityRenderers.register(EspressoContent.CANISTER_ENTITY, CanisterRenderer::new);
		EntityRenderers.register(EspressoContent.SEAT, NoopRenderer::new);
	}

	/** Mugs and canisters are tinted (tintindex 0) by their colour property: cream, one of the 16 dyes, or gold. */
	public static Map<Block, BlockTintSource> blockTints() {
		return Map.of(
				EspressoContent.PLACED_MUG, tintByColor(PlacedMugBlock.COLOR),
				EspressoContent.CANISTER_BLOCK, tintByColor(CanisterBlock.COLOR));
	}

	private static BlockTintSource tintByColor(IntegerProperty color) {
		return new BlockTintSource() {
			@Override
			public int color(BlockState state) {
				return Mugs.tint(state.getValue(color));
			}

			@Override
			public Set<Property<?>> relevantProperties() {
				return Set.of(color);
			}
		};
	}

	/** On someone else's server, use their bundle setting so bundles match for everyone. */
	public static void onServerSettings(ServerSettings settings) {
		if (!Minecraft.getInstance().hasSingleplayerServer()) {
			Config.serverBundleUnstackables = settings.bundleUnstackables();
		}
	}

	public static void onDisconnect() {
		Config.serverBundleUnstackables = 0;
	}
}

package com.gabbymaybelle.espresso.client;

import com.gabbymaybelle.espresso.content.CanisterBlock;
import com.gabbymaybelle.espresso.content.CanisterBlockEntity;
import com.gabbymaybelle.espresso.content.JarBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/** Draws a canister's contents on its front like a label, or a glass jar's contents inside the glass. */
public class CanisterRenderer implements BlockEntityRenderer<CanisterBlockEntity, CanisterRenderer.State> {
	private final ItemModelResolver itemModelResolver;

	public CanisterRenderer(BlockEntityRendererProvider.Context context) {
		itemModelResolver = context.itemModelResolver();
	}

	public static class State extends BlockEntityRenderState {
		final ItemStackRenderState label = new ItemStackRenderState();
		Direction facing = Direction.NORTH;
		boolean jar;
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(CanisterBlockEntity canister, State state, float partialTick, Vec3 cameraPos,
			ModelFeatureRenderer.CrumblingOverlay crumbling) {
		BlockEntityRenderer.super.extractRenderState(canister, state, partialTick, cameraPos, crumbling);
		state.facing = canister.getBlockState().getValue(CanisterBlock.FACING);
		state.jar = canister.getBlockState().getBlock() instanceof JarBlock;
		ItemStack label = canister.labelItem();
		itemModelResolver.updateForTopItem(state.label, label, ItemDisplayContext.FIXED, canister.getLevel(), null,
				(int) canister.getBlockPos().asLong());
	}

	@Override
	public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
		if (state.label.isEmpty()) {
			return;
		}
		pose.pushPose();
		// Centre of the block, turned so the label faces out of the canister's front.
		pose.translate(0.5F, 0.0F, 0.5F);
		pose.rotateDegrees(Axis.YP, -state.facing.toYRot());
		if (state.jar) {
			// Inside the glass, a little below the middle.
			pose.translate(0.0F, 0.3F, 0.0F);
			pose.scale(0.4F, 0.4F, 0.4F);
		} else {
			// Just in front of the front face (the canister is 8 px wide), halfway up.
			pose.translate(0.0F, 0.33F, 0.255F);
			pose.scale(0.28F, 0.28F, 0.28F);
		}
		state.label.submit(pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
		pose.popPose();
	}
}

package com.gabbymaybelle.espresso.content;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.LilyPadBlock;
import net.minecraft.world.level.gameevent.GameEvent;

/**
 * A water lily: a lily pad with a cactus flower blooming on it, right on the water. It behaves like a
 * lily pad (sits on water, boats break it). Bone meal makes a lily pad bloom into one, and a few grow
 * among the waterside lily pads in new land.
 */
public class WaterLilyBlock extends LilyPadBlock {
	public WaterLilyBlock(Properties properties) {
		super(properties);
	}

	static void register() {
		UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
			ItemStack stack = player.getItemInHand(hand);
			BlockPos pos = hit.getBlockPos();
			if (!stack.is(Items.BONE_MEAL) || !level.getBlockState(pos).is(Blocks.LILY_PAD)
					|| !player.mayUseItemAt(pos, hit.getDirection(), stack)) {
				return InteractionResult.PASS;
			}
			if (level instanceof ServerLevel serverLevel) {
				serverLevel.setBlock(pos, EspressoContent.WATER_LILY.defaultBlockState(), Block.UPDATE_ALL);
				stack.consume(1, player);
				serverLevel.levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, pos, 8); // bone meal's sparkles and sound
				serverLevel.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, EspressoContent.WATER_LILY.defaultBlockState()));
			}
			return InteractionResult.SUCCESS;
		});
	}
}

package com.gabbymaybelle.espresso.content;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

/**
 * A coffee bush: grows through four stages like a sweet berry bush and is harvested by
 * hand for coffee cherries, but has no thorns and doesn't slow you down.
 */
public class CoffeeBushBlock extends SweetBerryBushBlock {
	public CoffeeBushBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
		return new ItemStack(EspressoContent.COFFEE_CHERRIES);
	}

	@Override
	protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects, boolean isPrecise) {
		// No thorns and no slowdown.
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		int age = state.getValue(AGE);
		if (age < 2) {
			return InteractionResult.PASS;
		}
		if (level instanceof ServerLevel serverLevel) {
			int count = 1 + level.getRandom().nextInt(2) + (age == MAX_AGE ? 1 : 0);
			Block.popResource(serverLevel, pos, new ItemStack(EspressoContent.COFFEE_CHERRIES, count));
			level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 0.9F + level.getRandom().nextFloat() * 0.3F);
			BlockState picked = state.setValue(AGE, 1);
			level.setBlock(pos, picked, Block.UPDATE_CLIENTS);
			level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, picked));
		}
		return InteractionResult.SUCCESS;
	}
}

package com.gabbymaybelle.espresso.content;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Used coffee grounds, from the espresso machine's knock box. Like the gardener's trick, they feed
 * crops, stems and berry and coffee bushes: a gentler bone meal, with a one-in-two chance of a
 * growth step. They also go in the composter.
 */
public class CoffeeGroundsItem extends Item {
	private static final float GROW_CHANCE = 0.5F;

	public CoffeeGroundsItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		BlockState state = level.getBlockState(pos);
		if (!(state.getBlock() instanceof CropBlock || state.getBlock() instanceof StemBlock
				|| state.getBlock() instanceof SweetBerryBushBlock || state.getBlock() instanceof CoffeeBushBlock)
				|| !(state.getBlock() instanceof BonemealableBlock plant)
				|| !plant.isValidBonemealTarget(level, pos, state, BonemealSource.INTERACTION)) {
			return InteractionResult.PASS;
		}
		if (level instanceof ServerLevel serverLevel) {
			if (level.getRandom().nextFloat() < GROW_CHANCE
					&& plant.isBonemealSuccess(level, level.getRandom(), pos, state, BonemealSource.INTERACTION)) {
				plant.performBonemeal(serverLevel, level.getRandom(), pos, state, BonemealSource.INTERACTION);
			}
			BoneMealItem.addGrowthParticles(level, pos, 6);
			context.getItemInHand().consume(1, context.getPlayer());
		}
		return InteractionResult.SUCCESS;
	}
}

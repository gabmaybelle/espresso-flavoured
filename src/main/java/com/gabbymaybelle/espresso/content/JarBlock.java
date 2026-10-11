package com.gabbymaybelle.espresso.content;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A glass jar: a canister you can see into. Same storage and handling as {@link CanisterBlock};
 * what's inside shows through the glass instead of on a label. Full of honey bottles, it's a
 * honey jar.
 */
public class JarBlock extends CanisterBlock {
	private static final Map<Direction, VoxelShape> SHAPES = Shapes.rotateHorizontal(Shapes.or(
			Block.box(4, 0, 4, 12, 11, 12), Block.box(5, 11, 5, 11, 13, 11)));

	public JarBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected Item item() {
		return EspressoContent.GLASS_JAR;
	}

	/** A glass lid: a little clink. */
	@Override
	protected SoundEvent lidSound() {
		return SoundEvents.GLASS_HIT;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPES.get(state.getValue(FACING));
	}
}

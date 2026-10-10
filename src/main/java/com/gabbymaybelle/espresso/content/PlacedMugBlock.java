package com.gabbymaybelle.espresso.content;

import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A mug set down on a surface, empty or full. It shows its contents and (the nearest dye
 * shade of) its colour; the exact mug item is kept in {@link PlacedMugBlockEntity} and
 * comes back when it's picked up (use it) or broken.
 */
public class PlacedMugBlock extends HorizontalDirectionalBlock implements EntityBlock {
	public enum Contents implements StringRepresentable {
		EMPTY("empty"), ESPRESSO("espresso"), LATTE("latte"), MOCHA("mocha");

		private final String name;

		Contents(String name) {
			this.name = name;
		}

		@Override
		public String getSerializedName() {
			return name;
		}
	}

	public static final EnumProperty<Contents> CONTENTS = EnumProperty.create("contents", Contents.class);
	/** 0 = undyed, 1..16 = the nearest dye colour (id + 1). */
	public static final IntegerProperty COLOR = IntegerProperty.create("color", 0, 16);
	/** Mug and handle, handle to the east when facing north; rotated for the other sides. */
	private static final Map<Direction, VoxelShape> SHAPES = Shapes.rotateHorizontal(Shapes.or(
			Block.box(5, 0, 5, 11, 6, 11), Block.box(11, 1, 7, 13, 5, 9)));

	public PlacedMugBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH)
				.setValue(CONTENTS, Contents.EMPTY).setValue(COLOR, 0));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, CONTENTS, COLOR);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPES.get(state.getValue(FACING));
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new PlacedMugBlockEntity(pos, state);
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		return Block.canSupportCenter(level, pos.below(), Direction.UP);
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
			Direction direction, BlockPos neighbourPos, BlockState neighbour, RandomSource random) {
		if (direction == Direction.DOWN && !canSurvive(state, level, pos)) {
			return Blocks.AIR.defaultBlockState();
		}
		return super.updateShape(state, level, ticks, pos, direction, neighbourPos, neighbour, random);
	}

	/** Picks the mug back up. */
	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!level.isClientSide()) {
			ItemStack mug = level.getBlockEntity(pos) instanceof PlacedMugBlockEntity entity ? entity.getMug() : ItemStack.EMPTY;
			level.removeBlock(pos, false);
			if (!mug.isEmpty()) {
				Mugs.giveBack(player, InteractionHand.MAIN_HAND, mug);
			}
			level.playSound(null, pos, SoundEvents.DECORATED_POT_HIT, SoundSource.BLOCKS, 1.0F, 1.2F);
			level.gameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Context.of(player, state));
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
		BlockEntity entity = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
		if (entity instanceof PlacedMugBlockEntity mug && !mug.getMug().isEmpty()) {
			return List.of(mug.getMug());
		}
		return List.of();
	}

	@Override
	protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
		return level.getBlockEntity(pos) instanceof PlacedMugBlockEntity mug && !mug.getMug().isEmpty()
				? mug.getMug() : new ItemStack(EspressoContent.MUG);
	}
}

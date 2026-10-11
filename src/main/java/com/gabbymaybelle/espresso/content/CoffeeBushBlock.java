package com.gabbymaybelle.espresso.content;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.core.Direction;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A coffee bush. It starts as a short seedling and grows into a two-block shrub, like a lilac,
 * whose cherries turn from yellow to red. Pick them by hand from either half; the bush stays.
 * No thorns, and it doesn't slow you down.
 *
 * <p>Stages: 0 seedling, 1 young (both short); 2 grown, 3 turning, 4 ripe (all two blocks tall).
 * A grown bush that has lost its top (or one from before bushes grew tall) regrows it when it
 * next ticks.
 */
public class CoffeeBushBlock extends DoublePlantBlock implements BonemealableBlock {
	public static final int MAX_AGE = 4;
	/** From this stage the bush is two blocks tall. */
	public static final int TALL_AGE = 2;
	public static final IntegerProperty AGE = BlockStateProperties.AGE_4;
	private static final VoxelShape SEEDLING = Block.box(3, 0, 3, 13, 8, 13);
	private static final VoxelShape BUSH = Block.box(1, 0, 1, 15, 16, 15);
	private static final VoxelShape TOP = Block.box(1, 0, 1, 15, 14, 15);

	public CoffeeBushBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(HALF, DoubleBlockHalf.LOWER).setValue(AGE, 0));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(AGE);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
			return TOP;
		}
		return state.getValue(AGE) == 0 ? SEEDLING : BUSH;
	}

	@Override
	protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
		return new ItemStack(EspressoContent.COFFEE_CHERRIES);
	}

	@Override
	protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects, boolean isPrecise) {
		// No thorns and no slowdown.
	}

	// ------------------------------------------------------------ placing and staying put

	/** Planted as a seedling, which needs no room above. */
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState();
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		// Unlike a tall flower, no top half yet.
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
			BlockState below = level.getBlockState(pos.below());
			return below.is(this) && below.getValue(HALF) == DoubleBlockHalf.LOWER && below.getValue(AGE) >= TALL_AGE;
		}
		BlockPos ground = pos.below();
		return mayPlaceOn(level.getBlockState(ground), level, ground);
	}

	/** The top goes when the bottom does; the bottom stays without its top (it regrows). */
	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
			BlockPos neighborPos, BlockState neighborState, RandomSource random) {
		return state.canSurvive(level, pos) ? state : Blocks.AIR.defaultBlockState();
	}

	/** Each half drops its own loot (only the bottom has any), since a young bush has no top. */
	@Override
	public void playerDestroy(ServerLevel level, ServerPlayer player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack tool) {
		player.awardStat(Stats.BLOCK_MINED.get(this));
		player.causeFoodExhaustion(0.005F);
		dropResources(state, level, pos, blockEntity, player, tool);
	}

	// ------------------------------------------------------------ growing

	@Override
	protected boolean isRandomlyTicking(BlockState state) {
		return state.getValue(HALF) == DoubleBlockHalf.LOWER;
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		int age = state.getValue(AGE);
		if (age >= TALL_AGE && !hasTop(level, pos)) {
			if (level.isEmptyBlock(pos.above())) {
				setAge(level, pos, state, age);
			}
			return;
		}
		if (age < MAX_AGE && random.nextInt(5) == 0 && level.getRawBrightness(pos.above(), 0) >= 9) {
			grow(level, pos, state);
		}
	}

	private boolean hasTop(LevelReader level, BlockPos lower) {
		BlockState above = level.getBlockState(lower.above());
		return above.is(this) && above.getValue(HALF) == DoubleBlockHalf.UPPER;
	}

	/** One stage on, if there's room to grow tall. */
	private void grow(ServerLevel level, BlockPos lower, BlockState state) {
		int next = state.getValue(AGE) + 1;
		if (next == TALL_AGE && !level.isEmptyBlock(lower.above())) {
			return;
		}
		setAge(level, lower, state, next);
		level.gameEvent(GameEvent.BLOCK_CHANGE, lower, GameEvent.Context.of(state));
	}

	/** Sets both halves to a stage, adding the top half once the bush is tall. */
	private void setAge(Level level, BlockPos lower, BlockState state, int age) {
		level.setBlock(lower, state.setValue(AGE, age), Block.UPDATE_ALL);
		if (age >= TALL_AGE) {
			level.setBlock(lower.above(), state.setValue(AGE, age).setValue(HALF, DoubleBlockHalf.UPPER), Block.UPDATE_ALL);
		}
	}

	private static BlockPos lowerOf(BlockState state, BlockPos pos) {
		return state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
	}

	@Override
	public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, BonemealSource source) {
		BlockPos lower = lowerOf(state, pos);
		int age = state.getValue(AGE);
		return age < MAX_AGE && (age + 1 != TALL_AGE || level.isEmptyBlock(lower.above()));
	}

	@Override
	public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
		return true;
	}

	@Override
	public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
		BlockPos lower = lowerOf(state, pos);
		grow(level, lower, level.getBlockState(lower));
	}

	// ------------------------------------------------------------ picking

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		int age = state.getValue(AGE);
		if (age <= TALL_AGE) {
			return InteractionResult.PASS;
		}
		if (level instanceof ServerLevel serverLevel) {
			BlockPos lower = lowerOf(state, pos);
			int count = 1 + level.getRandom().nextInt(2) + (age == MAX_AGE ? 1 : 0);
			Block.popResource(serverLevel, pos, new ItemStack(EspressoContent.COFFEE_CHERRIES, count));
			level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 0.9F + level.getRandom().nextFloat() * 0.3F);
			BlockState lowerState = level.getBlockState(lower);
			setAge(level, lower, lowerState, TALL_AGE);
			level.gameEvent(GameEvent.BLOCK_CHANGE, lower, GameEvent.Context.of(player, lowerState));
		}
		return InteractionResult.SUCCESS;
	}
}

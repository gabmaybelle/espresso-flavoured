package com.gabbymaybelle.espresso.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A clay canister for a counter or shelf: holds up to four stacks of one stackable item and
 * shows it on a label. Use an item on it to put that item in, use it with an empty hand to
 * take a stack out (crouch to take just one). Dyeable like mugs; the exact colour is kept
 * with the block and comes back when it's broken.
 */
public class CanisterBlock extends HorizontalDirectionalBlock implements EntityBlock {
	/** 0 = undyed, 1..16 = the nearest dye colour (id + 1), as for mugs. */
	public static final IntegerProperty COLOR = IntegerProperty.create("color", 0, 16);
	private static final Map<Direction, VoxelShape> SHAPES = Shapes.rotateHorizontal(Shapes.or(
			Block.box(4, 0, 4, 12, 10, 12), Block.box(4.5, 10, 4.5, 11.5, 12, 11.5)));

	public CanisterBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(COLOR, 0));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, COLOR);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite())
				.setValue(COLOR, Mugs.colorIndex(context.getItemInHand()));
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPES.get(state.getValue(FACING));
	}

	/** The item this block is placed from and drops as. */
	protected Item item() {
		return EspressoContent.CANISTER;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new CanisterBlockEntity(pos, state);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		if (level.getBlockEntity(pos) instanceof CanisterBlockEntity canister) {
			canister.color = stack.get(DataComponents.DYED_COLOR);
		}
	}

	/** Puts the held item in. */
	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
			InteractionHand hand, BlockHitResult hit) {
		if (stack.isEmpty() || !(level.getBlockEntity(pos) instanceof CanisterBlockEntity canister)) {
			return InteractionResult.TRY_WITH_EMPTY_HAND;
		}
		ItemVariant variant = ItemVariant.of(stack);
		try (Transaction transaction = Transaction.openOuter()) {
			long inserted = canister.storage.insert(variant, stack.getCount(), transaction);
			if (inserted == 0) {
				return InteractionResult.TRY_WITH_EMPTY_HAND;
			}
			if (level.isClientSide()) {
				return InteractionResult.SUCCESS;
			}
			transaction.commit();
			if (!player.getAbilities().instabuild) {
				stack.shrink((int) inserted);
			}
		}
		level.playSound(null, pos, SoundEvents.DECORATED_POT_INSERT, SoundSource.BLOCKS, 1.0F, 1.1F);
		return InteractionResult.SUCCESS;
	}

	/** Takes a stack out, or one item when crouching. */
	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!(level.getBlockEntity(pos) instanceof CanisterBlockEntity canister) || canister.storage.amount == 0) {
			return InteractionResult.PASS;
		}
		if (!level.isClientSide()) {
			ItemVariant variant = canister.storage.variant;
			int want = player.isShiftKeyDown() ? 1 : variant.toStack().getMaxStackSize();
			try (Transaction transaction = Transaction.openOuter()) {
				long taken = canister.storage.extract(variant, want, transaction);
				transaction.commit();
				Mugs.giveBack(player, InteractionHand.MAIN_HAND, variant.toStack((int) taken));
			}
			level.playSound(null, pos, SoundEvents.DECORATED_POT_HIT, SoundSource.BLOCKS, 1.0F, 1.2F);
		}
		return InteractionResult.SUCCESS;
	}

	/** The canister itself (in its exact colour) plus everything that was in it. */
	@Override
	protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
		List<ItemStack> drops = new ArrayList<>();
		ItemStack canisterItem = new ItemStack(item());
		if (params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof CanisterBlockEntity canister) {
			if (canister.color != null) {
				canisterItem.set(DataComponents.DYED_COLOR, canister.color);
			}
			drops.add(canisterItem);
			drops.addAll(canister.contents());
		} else {
			drops.add(canisterItem);
		}
		return drops;
	}

	@Override
	protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
		ItemStack stack = new ItemStack(item());
		if (level.getBlockEntity(pos) instanceof CanisterBlockEntity canister && canister.color != null) {
			stack.set(DataComponents.DYED_COLOR, canister.color);
		}
		return stack;
	}

	@Override
	protected boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
		return level.getBlockEntity(pos) instanceof CanisterBlockEntity canister ? canister.comparatorSignal() : 0;
	}
}

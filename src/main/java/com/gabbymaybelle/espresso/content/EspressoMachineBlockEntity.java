package com.gabbymaybelle.espresso.content;

import com.gabbymaybelle.espresso.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * The espresso machine's insides: three mug slots and a bean slot, like a brewing stand. It
 * only brews while it sits on a coffee bar.
 * While there are beans and at least one empty mug, it brews for ten seconds, then fills
 * each mug with an Espresso (keeping its colour), one bean per mug. Hoppers put beans in
 * from above, mugs in from the sides, and take Espresso out from below. Each shot of coffee leaves
 * its used grounds in the knock box (cocoa leaves none); hoppers below take those out too, and the
 * machine waits while the knock box is full.
 */
public class EspressoMachineBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
	public static final int BEAN_SLOT = 3;
	public static final int GROUNDS_SLOT = 4;
	public static final int SLOT_COUNT = 5;
	public static final int BREW_TIME = 200;
	private static final int[] SLOTS_FOR_UP = { BEAN_SLOT };
	private static final int[] SLOTS_FOR_SIDES = { 0, 1, 2 };
	private static final int[] SLOTS_FOR_DOWN = { 0, 1, 2, GROUNDS_SLOT };

	private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
	/** Ticks left on the current brew; 0 when idle. */
	private int brewTime;
	private final ContainerData data = new ContainerData() {
		@Override
		public int get(int index) {
			return index == 0 ? brewTime : (onCoffeeBar() ? 1 : 0);
		}

		@Override
		public void set(int index, int value) {
			if (index == 0) {
				brewTime = value;
			}
		}

		@Override
		public int getCount() {
			return 2;
		}
	};

	public EspressoMachineBlockEntity(BlockPos pos, BlockState state) {
		super(EspressoContent.ESPRESSO_MACHINE_ENTITY, pos, state);
	}

	public static void serverTick(ServerLevel level, BlockPos pos, BlockState state, EspressoMachineBlockEntity machine) {
		boolean canBrew = machine.canBrew();
		if (machine.brewTime > 0) {
			if (!canBrew) {
				machine.brewTime = 0;
			} else if (--machine.brewTime == 0) {
				machine.brew(level, pos, state);
			}
			machine.setChanged();
		} else if (canBrew) {
			machine.brewTime = BREW_TIME;
			machine.setChanged();
		}
		boolean hasBeans = !machine.items.get(BEAN_SLOT).isEmpty();
		if (state.getValue(EspressoMachineBlock.HAS_BEANS) != hasBeans) {
			level.setBlock(pos, state.setValue(EspressoMachineBlock.HAS_BEANS, hasBeans), Block.UPDATE_CLIENTS);
		}
	}

	/** The machine only works on a coffee bar (unless that's turned off in the settings). */
	/** Ticks left on the current brew; 0 when idle. */
	public int brewTime() {
		return brewTime;
	}

	public boolean onCoffeeBar() {
		return !Config.coffeeBarRequired
				|| level != null && level.getBlockState(worldPosition.below()).getBlock() instanceof CoffeeBarBlock;
	}

	private boolean canBrew() {
		ItemStack beans = items.get(BEAN_SLOT);
		if (beans.isEmpty() || !onCoffeeBar()) {
			return false;
		}
		int shots = 0;
		for (int i = 0; i < BEAN_SLOT; i++) {
			if (Mugs.isEmptyMug(items.get(i))) {
				shots++;
			}
		}
		shots = Math.min(shots, beans.getCount());
		// Waits until the knock box has room for this brew's grounds (cocoa leaves none).
		ItemStack grounds = items.get(GROUNDS_SLOT);
		return shots > 0 && (beans.is(EspressoContent.COCOA_MIX) || grounds.getCount() + shots <= EspressoContent.COFFEE_GROUNDS.getDefaultMaxStackSize());
	}

	private void brew(ServerLevel level, BlockPos pos, BlockState state) {
		ItemStack beans = items.get(BEAN_SLOT);
		Item drink = beans.is(EspressoContent.BARISTAS_RESERVE) ? EspressoContent.RISTRETTO
				: beans.is(EspressoContent.COCOA_MIX) ? EspressoContent.HOT_CHOCOLATE : EspressoContent.ESPRESSO;
		boolean decaf = beans.is(EspressoContent.DECAF_BEANS);
		boolean leavesGrounds = drink != EspressoContent.HOT_CHOCOLATE;
		int shots = 0;
		for (int i = 0; i < BEAN_SLOT && !beans.isEmpty(); i++) {
			if (Mugs.isEmptyMug(items.get(i))) {
				items.set(i, Mugs.fill(items.get(i), drink, decaf));
				beans.shrink(1);
				shots++;
			}
		}
		if (leavesGrounds) {
			ItemStack grounds = items.get(GROUNDS_SLOT);
			if (grounds.isEmpty()) {
				items.set(GROUNDS_SLOT, new ItemStack(EspressoContent.COFFEE_GROUNDS, shots));
			} else {
				grounds.setCount(Math.min(grounds.getMaxStackSize(), grounds.getCount() + shots));
			}
		}
		level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.4F, 1.6F);
		level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 0.8F, 1.2F);
		// A puff of steam from the brew head.
		Direction front = state.getValue(EspressoMachineBlock.FACING);
		level.sendParticles(ParticleTypes.WHITE_SMOKE, pos.getX() + 0.5 + front.getStepX() * 0.3,
				pos.getY() + 0.75, pos.getZ() + 0.5 + front.getStepZ() * 0.3, 6, 0.08, 0.05, 0.08, 0.01);
	}

	/** What goes in the top slot: roasted or decaf beans, the Barista's Reserve, or cocoa mix. */
	public static boolean isBeans(ItemStack stack) {
		return stack.is(EspressoContent.ROASTED_COFFEE_BEANS) || stack.is(EspressoContent.DECAF_BEANS)
				|| stack.is(EspressoContent.BARISTAS_RESERVE) || stack.is(EspressoContent.COCOA_MIX);
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("container.espresso.espresso_machine");
	}

	@Override
	public int getContainerSize() {
		return SLOT_COUNT;
	}

	@Override
	protected NonNullList<ItemStack> getItems() {
		return items;
	}

	@Override
	protected void setItems(NonNullList<ItemStack> items) {
		this.items = items;
	}

	@Override
	protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
		return new EspressoMachineMenu(containerId, inventory, this, data);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
		ContainerHelper.loadAllItems(input, items);
		brewTime = input.getIntOr("BrewTime", 0);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		ContainerHelper.saveAllItems(output, items);
		output.putInt("BrewTime", brewTime);
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		if (slot == BEAN_SLOT) {
			return isBeans(stack);
		}
		if (slot == GROUNDS_SLOT) {
			return false;
		}
		return Mugs.isEmptyMug(stack) && items.get(slot).isEmpty();
	}

	@Override
	public int[] getSlotsForFace(Direction side) {
		return side == Direction.UP ? SLOTS_FOR_UP : side == Direction.DOWN ? SLOTS_FOR_DOWN : SLOTS_FOR_SIDES;
	}

	@Override
	public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) {
		return canPlaceItem(slot, stack);
	}

	@Override
	public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
		// Finished drinks and used grounds come out; empty mugs stay in to be filled.
		return side == Direction.DOWN && (slot == GROUNDS_SLOT || slot < BEAN_SLOT && !Mugs.isEmptyMug(stack));
	}
}

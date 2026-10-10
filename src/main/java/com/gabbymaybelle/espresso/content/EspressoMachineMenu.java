package com.gabbymaybelle.espresso.content;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** The espresso machine's menu: three bottle slots below, the bean slot on top, laid out like a brewing stand. */
public class EspressoMachineMenu extends AbstractContainerMenu {
	private static final int INV_START = EspressoMachineBlockEntity.SLOT_COUNT;
	private static final int INV_END = INV_START + 36;

	private final Container machine;
	private final ContainerData data;

	/** Client side: the server fills these in. */
	public EspressoMachineMenu(int containerId, Inventory inventory) {
		this(containerId, inventory, new SimpleContainer(EspressoMachineBlockEntity.SLOT_COUNT), new SimpleContainerData(1));
	}

	public EspressoMachineMenu(int containerId, Inventory inventory, Container machine, ContainerData data) {
		super(EspressoContent.ESPRESSO_MACHINE_MENU, containerId);
		checkContainerSize(machine, EspressoMachineBlockEntity.SLOT_COUNT);
		checkContainerDataCount(data, 1);
		this.machine = machine;
		this.data = data;
		addSlot(new BottleSlot(machine, 0, 56, 51));
		addSlot(new BottleSlot(machine, 1, 79, 58));
		addSlot(new BottleSlot(machine, 2, 102, 51));
		addSlot(new Slot(machine, EspressoMachineBlockEntity.BEAN_SLOT, 79, 17) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return stack.is(EspressoContent.ROASTED_COFFEE_BEANS);
			}
		});
		addDataSlots(data);
		addStandardInventorySlots(inventory, 8, 84);
	}

	/** Ticks left on the current brew, 0 when idle. */
	public int getBrewTime() {
		return data.get(0);
	}

	@Override
	public boolean stillValid(Player player) {
		return machine.stillValid(player);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		Slot slot = slots.get(index);
		if (!slot.hasItem()) {
			return ItemStack.EMPTY;
		}
		ItemStack stack = slot.getItem();
		ItemStack original = stack.copy();
		if (index < INV_START) {
			// Out of the machine, into the inventory.
			if (!moveItemStackTo(stack, INV_START, INV_END, true)) {
				return ItemStack.EMPTY;
			}
		} else if (stack.is(EspressoContent.ROASTED_COFFEE_BEANS)) {
			if (!moveItemStackTo(stack, EspressoMachineBlockEntity.BEAN_SLOT, INV_START, false)) {
				return ItemStack.EMPTY;
			}
		} else if (stack.is(Items.GLASS_BOTTLE)) {
			if (!moveItemStackTo(stack, 0, EspressoMachineBlockEntity.BEAN_SLOT, false)) {
				return ItemStack.EMPTY;
			}
		} else if (index < INV_END - 9) {
			// Main inventory to hotbar, and back.
			if (!moveItemStackTo(stack, INV_END - 9, INV_END, false)) {
				return ItemStack.EMPTY;
			}
		} else if (!moveItemStackTo(stack, INV_START, INV_END - 9, false)) {
			return ItemStack.EMPTY;
		}
		if (stack.isEmpty()) {
			slot.setByPlayer(ItemStack.EMPTY);
		} else {
			slot.setChanged();
		}
		if (stack.getCount() == original.getCount()) {
			return ItemStack.EMPTY;
		}
		slot.onTake(player, stack);
		return original;
	}

	/** Holds one glass bottle (or the Espresso it becomes). */
	private static class BottleSlot extends Slot {
		BottleSlot(Container container, int slot, int x, int y) {
			super(container, slot, x, y);
		}

		@Override
		public boolean mayPlace(ItemStack stack) {
			return stack.is(Items.GLASS_BOTTLE);
		}

		@Override
		public int getMaxStackSize() {
			return 1;
		}
	}
}

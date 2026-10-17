package com.gabbymaybelle.espresso.content;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * The cupboard inside a coffee bar: two rows of storage, a little less than a chest (one recipe
 * makes two bars). An ordinary container: hoppers fill and empty it, and its contents spill out
 * when the bar is broken.
 */
public class CoffeeBarBlockEntity extends BaseContainerBlockEntity {
	public static final int ROWS = 2;
	public static final int SLOT_COUNT = ROWS * 9;

	private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);

	public CoffeeBarBlockEntity(BlockPos pos, BlockState state) {
		super(EspressoContent.COFFEE_BAR_ENTITY, pos, state);
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("container.espresso_flavoured.coffee_bar");
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
		return new ChestMenu(MenuType.GENERIC_9x2, containerId, inventory, this, ROWS);
	}

	/** The cupboard doors: a soft wooden creak, like a barrel. */
	@Override
	public void startOpen(ContainerUser user) {
		doorSound(SoundEvents.BARREL_OPEN);
	}

	@Override
	public void stopOpen(ContainerUser user) {
		doorSound(SoundEvents.BARREL_CLOSE);
	}

	private void doorSound(SoundEvent sound) {
		if (level != null && !level.isClientSide() && !isRemoved()) {
			level.playSound(null, worldPosition, sound, SoundSource.BLOCKS, 0.5F, level.getRandom().nextFloat() * 0.1F + 1.0F);
		}
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
		ContainerHelper.loadAllItems(input, items);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		ContainerHelper.saveAllItems(output, items);
	}
}

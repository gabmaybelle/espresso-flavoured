package com.gabbymaybelle.espresso.content;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Remembers exactly which mug was set down (its colour, drink and name), so it comes back unchanged. */
public class PlacedMugBlockEntity extends BlockEntity {
	private ItemStack mug = ItemStack.EMPTY;

	public PlacedMugBlockEntity(BlockPos pos, BlockState state) {
		super(EspressoContent.PLACED_MUG_ENTITY, pos, state);
	}

	public ItemStack getMug() {
		return mug.copy();
	}

	public void setMug(ItemStack mug) {
		this.mug = mug.copy();
		setChanged();
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		mug = input.read("Mug", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.store("Mug", ItemStack.OPTIONAL_CODEC, mug);
	}
}

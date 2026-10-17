package com.gabbymaybelle.espresso.content;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Remembers exactly which mug was set down (its colour, drink and name), so it comes back unchanged.
 * Shared with players too, so the game (and Jade) can tell which drink it is.
 */
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
		if (level != null) {
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
		}
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

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		return saveCustomOnly(registries);
	}
}

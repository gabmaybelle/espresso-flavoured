package com.gabbymaybelle.espresso.content;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * A canister's contents: one kind of stackable item, up to four stacks of it. Hoppers use
 * {@link #storage} through Fabric's item storage, and the client is kept up to date so the
 * label on the front shows what's inside.
 */
public class CanisterBlockEntity extends BlockEntity {
	public static final int STACKS = 4;

	public final SingleVariantStorage<ItemVariant> storage = new SingleVariantStorage<>() {
		@Override
		protected ItemVariant getBlankVariant() {
			return ItemVariant.blank();
		}

		@Override
		protected long getCapacity(ItemVariant variant) {
			return (long) STACKS * (variant.isBlank() ? 64 : variant.toStack().getMaxStackSize());
		}

		@Override
		protected boolean canInsert(ItemVariant variant) {
			return variant.toStack().getMaxStackSize() > 1;
		}

		@Override
		protected void onFinalCommit() {
			changed();
		}
	};

	/** The canister's exact dye colour, or null when undyed. */
	DyedItemColor color;

	public CanisterBlockEntity(BlockPos pos, BlockState state) {
		super(EspressoContent.CANISTER_ENTITY, pos, state);
	}

	/** What's inside, as a single item for the label (empty when the canister is empty). */
	public ItemStack labelItem() {
		return storage.isResourceBlank() || storage.amount == 0 ? ItemStack.EMPTY : storage.variant.toStack(1);
	}

	/** Everything inside, as stacks, for when the canister is broken. */
	public List<ItemStack> contents() {
		List<ItemStack> stacks = new ArrayList<>();
		long left = storage.amount;
		while (left > 0 && !storage.isResourceBlank()) {
			int count = (int) Math.min(left, storage.variant.toStack().getMaxStackSize());
			stacks.add(storage.variant.toStack(count));
			left -= count;
		}
		return stacks;
	}

	public int comparatorSignal() {
		long capacity = storage.getCapacity();
		return storage.amount == 0 || capacity == 0 ? 0 : 1 + (int) (14 * storage.amount / capacity);
	}

	void changed() {
		if (storage.amount == 0) {
			storage.variant = ItemVariant.blank();
		}
		setChanged();
		if (level != null) {
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
			level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
		}
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		SingleVariantStorage.readValue(storage, ItemVariant.CODEC, ItemVariant::blank, input);
		color = input.read("Color", DyedItemColor.CODEC).orElse(null);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		SingleVariantStorage.writeValue(storage, ItemVariant.CODEC, output);
		if (color != null) {
			output.store("Color", DyedItemColor.CODEC, color);
		}
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

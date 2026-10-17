package com.gabbymaybelle.espresso.content;

import java.util.ArrayList;
import java.util.List;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * A canister's contents: one kind of stackable item, up to four stacks of it. Hoppers and pipes get
 * at it through the loader's item storage (see the Fabric and NeoForge jars), which call
 * {@link #insert} and {@link #extract}; the client is kept up to date so the label on the front
 * shows what's inside.
 */
public class CanisterBlockEntity extends BlockEntity {
	public static final int STACKS = 4;
	/**
	 * The item and its components, saved as {@code {item, components}} (the same shape Fabric's item
	 * storage used, so canisters in older worlds keep their contents).
	 */
	private static final Codec<ItemStack> VARIANT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("item").forGetter(ItemStack::typeHolder),
			DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(ItemStack::getComponentsPatch)
	).apply(instance, (item, components) -> new ItemStack(item, 1, components)));

	/** What's inside, as a single item; empty when the canister is empty. */
	private ItemStack variant = ItemStack.EMPTY;
	private long amount;

	/** The canister's exact dye colour, or null when undyed. */
	DyedItemColor color;

	public CanisterBlockEntity(BlockPos pos, BlockState state) {
		super(EspressoContent.CANISTER_ENTITY, pos, state);
	}

	public long amount() {
		return amount;
	}

	/** One of the item inside, or empty. */
	public ItemStack variant() {
		return variant;
	}

	/** How many fit: four stacks of whatever is inside (of 64, when empty). */
	public long capacity(ItemStack item) {
		return (long) STACKS * (item.isEmpty() ? 64 : item.getMaxStackSize());
	}

	/** Whether this item can go in right now: stackable, and the same as what's inside (anything, when empty). */
	public boolean accepts(ItemStack stack) {
		return !stack.isEmpty() && stack.getMaxStackSize() > 1 && (variant.isEmpty() || ItemStack.isSameItemSameComponents(variant, stack));
	}

	/** Puts in as much of the stack as fits (without changing it) and returns how many; with {@code simulate}, only says. */
	public int insert(ItemStack stack, int count, boolean simulate) {
		if (!accepts(stack)) {
			return 0;
		}
		long room = capacity(stack) - amount;
		int inserted = (int) Math.max(0, Math.min(count, room));
		if (inserted > 0 && !simulate) {
			setContents(variant.isEmpty() ? stack.copyWithCount(1) : variant, amount + inserted);
		}
		return inserted;
	}

	/** Takes up to {@code count} of {@code item} out and returns how many; with {@code simulate}, only says. */
	public int extract(ItemStack item, int count, boolean simulate) {
		if (variant.isEmpty() || !ItemStack.isSameItemSameComponents(variant, item)) {
			return 0;
		}
		int taken = (int) Math.min(count, amount);
		if (taken > 0 && !simulate) {
			setContents(variant, amount - taken);
		}
		return taken;
	}

	/** Replaces the contents without telling anyone; the loaders' storage uses it to roll a transaction back. */
	public void restoreContents(ItemStack item, long count) {
		variant = item;
		amount = count;
	}

	private void setContents(ItemStack item, long count) {
		restoreContents(item, count);
		changed();
	}

	/** What's inside, as a single item for the label (empty when the canister is empty). */
	public ItemStack labelItem() {
		return amount == 0 ? ItemStack.EMPTY : variant;
	}

	/** Everything inside, as stacks, for when the canister is broken. */
	public List<ItemStack> contents() {
		List<ItemStack> stacks = new ArrayList<>();
		long left = amount;
		while (left > 0 && !variant.isEmpty()) {
			int count = (int) Math.min(left, variant.getMaxStackSize());
			stacks.add(variant.copyWithCount(count));
			left -= count;
		}
		return stacks;
	}

	public int comparatorSignal() {
		long capacity = capacity(variant);
		return amount == 0 || capacity == 0 ? 0 : 1 + (int) (14 * amount / capacity);
	}

	/** Saves, syncs to the client and updates comparators; the loaders call it when a transaction commits. */
	public void changed() {
		if (amount == 0) {
			variant = ItemStack.EMPTY;
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
		variant = input.read("variant", VARIANT_CODEC).filter(item -> !item.isEmpty()).orElse(ItemStack.EMPTY);
		amount = variant.isEmpty() ? 0 : input.getLongOr("amount", 0L);
		color = input.read("Color", DyedItemColor.CODEC).orElse(null);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.store("variant", VARIANT_CODEC, variant.isEmpty() ? new ItemStack(Items.AIR) : variant);
		output.putLong("amount", amount);
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

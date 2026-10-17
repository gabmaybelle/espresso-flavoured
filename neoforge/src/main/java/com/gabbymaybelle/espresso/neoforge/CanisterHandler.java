package com.gabbymaybelle.espresso.neoforge;

import com.gabbymaybelle.espresso.content.CanisterBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/** A canister as a NeoForge item handler, so hoppers and pipes can fill and empty it. */
final class CanisterHandler implements ResourceHandler<ItemResource> {
	private record Contents(ItemStack item, long amount) {
	}

	private final CanisterBlockEntity canister;
	private final SnapshotJournal<Contents> journal = new SnapshotJournal<>() {
		@Override
		protected Contents createSnapshot() {
			return new Contents(canister.variant(), canister.amount());
		}

		@Override
		protected void revertToSnapshot(Contents snapshot) {
			canister.restoreContents(snapshot.item(), snapshot.amount());
		}

		@Override
		protected void onRootCommit(Contents originalState) {
			canister.changed();
		}
	};

	CanisterHandler(CanisterBlockEntity canister) {
		this.canister = canister;
	}

	@Override
	public int size() {
		return 1;
	}

	@Override
	public ItemResource getResource(int index) {
		return canister.variant().isEmpty() ? ItemResource.EMPTY : ItemResource.of(canister.variant());
	}

	@Override
	public long getAmountAsLong(int index) {
		return canister.amount();
	}

	@Override
	public long getCapacityAsLong(int index, ItemResource resource) {
		return canister.capacity(resource.isEmpty() ? canister.variant() : resource.toStack());
	}

	@Override
	public boolean isValid(int index, ItemResource resource) {
		return canister.accepts(resource.toStack());
	}

	@Override
	public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
		ItemStack stack = resource.toStack();
		int inserted = canister.insert(stack, amount, true);
		if (inserted > 0) {
			journal.updateSnapshots(transaction);
			canister.restoreContents(canister.variant().isEmpty() ? stack.copyWithCount(1) : canister.variant(), canister.amount() + inserted);
		}
		return inserted;
	}

	@Override
	public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
		int taken = canister.extract(resource.toStack(), amount, true);
		if (taken > 0) {
			journal.updateSnapshots(transaction);
			canister.restoreContents(canister.variant(), canister.amount() - taken);
		}
		return taken;
	}
}

package com.gabbymaybelle.espresso.fabric;

import com.gabbymaybelle.espresso.content.CanisterBlockEntity;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.world.item.ItemStack;

/** A canister as Fabric item storage, so hoppers and pipes can fill and empty it. */
final class CanisterStorage implements Storage<ItemVariant> {
	private record Contents(ItemStack item, long amount) {
	}

	private final CanisterBlockEntity canister;
	private final SnapshotParticipant<Contents> journal = new SnapshotParticipant<>() {
		@Override
		protected Contents createSnapshot() {
			return new Contents(canister.variant(), canister.amount());
		}

		@Override
		protected void readSnapshot(Contents snapshot) {
			canister.restoreContents(snapshot.item(), snapshot.amount());
		}

		@Override
		protected void onFinalCommit() {
			canister.changed();
		}
	};

	CanisterStorage(CanisterBlockEntity canister) {
		this.canister = canister;
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		ItemStack stack = resource.toStack();
		int inserted = canister.insert(stack, (int) Math.min(maxAmount, Integer.MAX_VALUE), true);
		if (inserted > 0) {
			journal.updateSnapshots(transaction);
			canister.restoreContents(canister.variant().isEmpty() ? stack.copyWithCount(1) : canister.variant(), canister.amount() + inserted);
		}
		return inserted;
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		int taken = canister.extract(resource.toStack(), (int) Math.min(maxAmount, Integer.MAX_VALUE), true);
		if (taken > 0) {
			journal.updateSnapshots(transaction);
			canister.restoreContents(canister.variant(), canister.amount() - taken);
		}
		return taken;
	}

	@Override
	public Iterator<StorageView<ItemVariant>> iterator() {
		return List.<StorageView<ItemVariant>>of(new StorageView<>() {
			@Override
			public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
				return CanisterStorage.this.extract(resource, maxAmount, transaction);
			}

			@Override
			public boolean isResourceBlank() {
				return canister.amount() == 0 || canister.variant().isEmpty();
			}

			@Override
			public ItemVariant getResource() {
				return canister.variant().isEmpty() ? ItemVariant.blank() : ItemVariant.of(canister.variant());
			}

			@Override
			public long getAmount() {
				return canister.amount();
			}

			@Override
			public long getCapacity() {
				return canister.capacity(canister.variant());
			}
		}).iterator();
	}
}

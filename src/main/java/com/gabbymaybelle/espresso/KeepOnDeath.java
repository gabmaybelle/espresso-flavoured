package com.gabbymaybelle.espresso;

import com.gabbymaybelle.espresso.compat.TrinketsCompat;
import com.gabbymaybelle.espresso.content.CharmItem;
import com.mojang.serialization.Codec;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/**
 * On death a player keeps their hotbar, armour and off-hand. Everything else drops as
 * usual (into a grave, with a graves mod), so there's still a trip back for resources.
 * Kept items are saved with the player until they respawn, so a crash or log-out while
 * dead loses nothing.
 */
public final class KeepOnDeath {
	private static final AttachmentType<Map<String, ItemStack>> KEPT = AttachmentRegistry.create(
			Identifier.fromNamespaceAndPath(Espresso.MOD_ID, "kept_items"),
			builder -> builder.persistent(Codec.unboundedMap(Codec.STRING, ItemStack.CODEC)).copyOnDeath());

	/** Hotbar 0..8, then feet, legs, chest, head (36..39) and the off-hand (40). */
	private static final int[] KEPT_SLOTS = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 36, 37, 38, 39, 40 };

	private KeepOnDeath() {
	}

	static void register() {
		// The kept items are copied onto the respawned player after the respawn events run,
		// so they're handed back on the next ticks instead (this also covers logging out
		// while dead and coming back later).
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTickCount() % 10 != 0) {
				return;
			}
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				if (player.isAlive() && player.hasAttached(KEPT)) {
					restore(player);
				}
			}
		});
	}

	private static void restore(ServerPlayer player) {
		Map<String, ItemStack> kept = player.removeAttached(KEPT);
		if (kept == null) {
			return;
		}
		Inventory inventory = player.getInventory();
		for (Map.Entry<String, ItemStack> entry : kept.entrySet()) {
			// Keys are the slot, plus "#n" for items from a second death while these were waiting.
			String key = entry.getKey().split("#")[0];
			ItemStack stack = entry.getValue();
			if (TrinketsCompat.isCharmKey(key)) {
				// A worn charm goes back in its trinket slot; into the inventory if that slot's gone.
				if (!(CharmItem.TRINKETS && TrinketsCompat.putBack(player, key, stack)) && !inventory.add(stack)) {
					player.spawnAtLocation(player.level(), stack);
				}
				continue;
			}
			int slot = Integer.parseInt(key);
			if (inventory.getItem(slot).isEmpty()) {
				inventory.setItem(slot, stack);
			} else if (!inventory.add(stack)) {
				player.spawnAtLocation(player.level(), stack);
			}
		}
	}

	/** Called just before a player's equipment drops on death. */
	public static void stashKeptItems(Player player) {
		if (!Config.keepHotbarOnDeath || !(player instanceof ServerPlayer)) {
			return;
		}
		Inventory inventory = player.getInventory();
		// Anything still waiting from a death moments ago stays in the list.
		Map<String, ItemStack> kept = new HashMap<>(player.getAttachedOrElse(KEPT, Map.of()));
		for (int slot : KEPT_SLOTS) {
			ItemStack stack = inventory.getItem(slot);
			// Curse of Vanishing still vanishes.
			if (stack.isEmpty() || EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)) {
				continue;
			}
			String key = Integer.toString(slot);
			for (int n = 1; kept.containsKey(key); n++) {
				key = slot + "#" + n;
			}
			kept.put(key, stack.copy());
			inventory.setItem(slot, ItemStack.EMPTY);
		}
		// Worn café charms stay too (with Trinkets; without it they're ordinary inventory items).
		if (CharmItem.TRINKETS) {
			for (Map.Entry<String, ItemStack> charm : TrinketsCompat.takeCharms(player).entrySet()) {
				String key = charm.getKey();
				for (int n = 1; kept.containsKey(key); n++) {
					key = charm.getKey() + "#" + n;
				}
				kept.put(key, charm.getValue());
			}
		}
		if (!kept.isEmpty()) {
			player.setAttached(KEPT, kept);
		}
	}
}

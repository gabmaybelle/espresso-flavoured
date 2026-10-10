package com.gabbymaybelle.espresso;

import com.mojang.serialization.Codec;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
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
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			Map<String, ItemStack> kept = newPlayer.getAttached(KEPT);
			if (kept == null) {
				return;
			}
			Inventory inventory = newPlayer.getInventory();
			for (Map.Entry<String, ItemStack> entry : kept.entrySet()) {
				int slot = Integer.parseInt(entry.getKey());
				if (inventory.getItem(slot).isEmpty()) {
					inventory.setItem(slot, entry.getValue());
				} else if (!inventory.add(entry.getValue())) {
					newPlayer.spawnAtLocation(newPlayer.level(), entry.getValue());
				}
			}
			newPlayer.removeAttached(KEPT);
		});
	}

	/** Called just before a player's equipment drops on death. */
	public static void stashKeptItems(Player player) {
		if (!Config.keepHotbarOnDeath || !(player instanceof ServerPlayer)) {
			return;
		}
		Inventory inventory = player.getInventory();
		Map<String, ItemStack> kept = new HashMap<>();
		for (int slot : KEPT_SLOTS) {
			ItemStack stack = inventory.getItem(slot);
			// Curse of Vanishing still vanishes.
			if (stack.isEmpty() || EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)) {
				continue;
			}
			kept.put(Integer.toString(slot), stack.copy());
			inventory.setItem(slot, ItemStack.EMPTY);
		}
		if (!kept.isEmpty()) {
			player.setAttached(KEPT, kept);
		}
	}
}

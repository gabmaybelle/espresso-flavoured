package com.gabbymaybelle.espresso;

import com.gabbymaybelle.espresso.compat.TrinketsCompat;
import com.gabbymaybelle.espresso.content.CharmItem;
import com.mojang.serialization.Codec;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * The café charms a player is wearing (in Trinkets slots) stay with them on death and go back
 * in their slots after respawning. Kept items are saved with the player until they respawn, so
 * a crash or log-out while dead loses nothing. Without Trinkets a charm is an ordinary item in
 * the off-hand, and follows whatever rule the world has for those.
 * <p>
 * Until 0.3.0 this also kept the hotbar, armour and off-hand; that is Cozy Core's
 * {@code keep_hotbar_on_death} game rule now. Items a 0.3.0 player left here when they died
 * (keyed by their slot number) are still handed back.
 */
public final class KeepOnDeath {
	private static final AttachmentType<Map<String, ItemStack>> KEPT = AttachmentRegistry.create(
			Identifier.fromNamespaceAndPath(Espresso.MOD_ID, "kept_items"),
			builder -> builder.persistent(Codec.unboundedMap(Codec.STRING, ItemStack.CODEC)).copyOnDeath());

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
			// A hotbar, armour or off-hand slot, from a death in 0.3.0 or earlier.
			int slot = Integer.parseInt(key);
			if (inventory.getItem(slot).isEmpty()) {
				inventory.setItem(slot, stack);
			} else if (!inventory.add(stack)) {
				player.spawnAtLocation(player.level(), stack);
			}
		}
	}

	/** Called just before a player's equipment drops on death: worn charms are set aside. */
	public static void stashKeptItems(Player player) {
		if (!CharmItem.TRINKETS || !(player instanceof ServerPlayer)) {
			return;
		}
		// Anything still waiting from a death moments ago stays in the list.
		Map<String, ItemStack> kept = new HashMap<>(player.getAttachedOrElse(KEPT, Map.of()));
		for (Map.Entry<String, ItemStack> charm : TrinketsCompat.takeCharms(player).entrySet()) {
			String key = charm.getKey();
			for (int n = 1; kept.containsKey(key); n++) {
				key = charm.getKey() + "#" + n;
			}
			kept.put(key, charm.getValue());
		}
		if (!kept.isEmpty()) {
			player.setAttached(KEPT, kept);
		}
	}
}

package com.gabbymaybelle.espresso.compat;

import java.util.Map;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Where charms can be worn besides the off-hand: Trinkets Updated's slots (Fabric only). The Fabric
 * jar sets one when that mod is installed (see {@code CharmItem.useSlots}); on NeoForge, and on
 * Fabric without it, charms work from the off-hand.
 */
public interface CharmSlots {
	/** Keys for charms set aside on death start with this: the slot they were worn in, so they go back there. */
	String KEY = "trinket:";

	static boolean isCharmKey(String key) {
		return key.startsWith(KEY);
	}

	boolean isEquipped(LivingEntity entity, Item item);

	/**
	 * Takes every worn charm out of its slot (unless it carries Curse of Vanishing), keyed by the
	 * slot, so KeepOnDeath can hand them back after respawn. Called before the slots drop them.
	 */
	Map<String, ItemStack> takeCharms(LivingEntity entity);

	/** Puts a charm back in the slot it came from, if that slot is still there and empty. */
	boolean putBack(LivingEntity entity, String key, ItemStack stack);
}

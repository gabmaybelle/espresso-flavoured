package com.gabbymaybelle.espresso.compat;

import com.gabbymaybelle.espresso.content.CharmItem;
import eu.pb4.trinkets.api.TrinketAttachment;
import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.TrinketSlotReference;
import eu.pb4.trinkets.api.TrinketsApi;
import eu.pb4.trinkets.api.component.TrinketDataComponents;
import eu.pb4.trinkets.api.component.TrinketEquippable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/** Trinkets Updated, kept in its own class so the mod loads without it. Only called when it's installed. */
public final class TrinketsCompat {
	/** Keys for charms set aside on death: the slot they were worn in, so they go back there. */
	private static final String KEY = "trinket:";

	private TrinketsCompat() {
	}

	public static boolean isEquipped(LivingEntity entity, Item item) {
		TrinketAttachment attachment = TrinketsApi.getAttachment(entity);
		return attachment != null && attachment.isEquipped(item);
	}

	/**
	 * Takes every worn charm out of its slot (unless it carries Curse of Vanishing), keyed by the
	 * slot, so KeepOnDeath can hand them back after respawn. Called before Trinkets drops them.
	 */
	public static Map<String, ItemStack> takeCharms(LivingEntity entity) {
		Map<String, ItemStack> taken = new HashMap<>();
		TrinketAttachment attachment = TrinketsApi.getAttachment(entity);
		if (attachment == null) {
			return taken;
		}
		attachment.forEach((slot, stack) -> {
			if (stack.getItem() instanceof CharmItem && !EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)) {
				TrinketSlotReference ref = slot.reference();
				taken.put(KEY + ref.slot() + "/" + ref.index(), stack.copy());
				slot.set(ItemStack.EMPTY);
			}
		});
		return taken;
	}

	public static boolean isCharmKey(String key) {
		return key.startsWith(KEY);
	}

	/** Puts a charm back in the slot it came from, if that slot is still there and empty. */
	public static boolean putBack(LivingEntity entity, String key, ItemStack stack) {
		TrinketAttachment attachment = TrinketsApi.getAttachment(entity);
		int slash = key.lastIndexOf('/');
		if (attachment == null || slash < 0) {
			return false;
		}
		try {
			TrinketSlotAccess slot = attachment.getSlotAccess(key.substring(KEY.length(), slash), Integer.parseInt(key.substring(slash + 1)));
			return slot != null && slot.isValid() && slot.get().isEmpty() && slot.set(stack);
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/** Charms go on with a right-click, like armour. */
	public static void equipOnUse(Collection<Item> charms) {
		DefaultItemComponentEvents.MODIFY.register(context -> context.modify(charms, (components, item) ->
				components.set(TrinketDataComponents.EQUIPMENT, TrinketEquippable.DEFAULT.withEquipOnInteract(true)
						.withEquipSound(SoundEvents.ARMOR_EQUIP_LEATHER))));
	}
}

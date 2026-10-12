package com.gabbymaybelle.espresso.compat;

import eu.pb4.trinkets.api.TrinketAttachment;
import eu.pb4.trinkets.api.TrinketsApi;
import eu.pb4.trinkets.api.component.TrinketDataComponents;
import eu.pb4.trinkets.api.component.TrinketEquippable;
import java.util.Collection;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;

/** Trinkets Updated, kept in its own class so the mod loads without it. Only called when it's installed. */
public final class TrinketsCompat {
	private TrinketsCompat() {
	}

	public static boolean isEquipped(LivingEntity entity, Item item) {
		TrinketAttachment attachment = TrinketsApi.getAttachment(entity);
		return attachment != null && attachment.isEquipped(item);
	}

	/** Charms go on with a right-click, like armour. */
	public static void equipOnUse(Collection<Item> charms) {
		DefaultItemComponentEvents.MODIFY.register(context -> context.modify(charms, (components, item) ->
				components.set(TrinketDataComponents.EQUIPMENT, TrinketEquippable.DEFAULT.withEquipOnInteract(true)
						.withEquipSound(SoundEvents.ARMOR_EQUIP_LEATHER))));
	}
}

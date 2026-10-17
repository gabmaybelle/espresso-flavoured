package com.gabbymaybelle.espresso.neoforge;

import com.gabbymaybelle.espresso.compat.CharmSlots;
import com.gabbymaybelle.espresso.content.CharmItem;
import com.gabbymaybelle.espresso.content.EspressoContent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

/**
 * Curios, the NeoForge counterpart of Trinkets: worn charms go in its slots. Kept in its own class
 * so the mod loads without it; only used when Curios is installed. Which slot a charm fits is data
 * (the tags in data/curios/tags/item).
 */
final class CuriosCompat implements CharmSlots {
	@Override
	public boolean isEquipped(LivingEntity entity, Item item) {
		ICuriosItemHandler inventory = CuriosApi.getCuriosInventoryOrNull(entity);
		// Not findFirstCurio(item): that answers from a cache kept for the rest of the tick, which is stale the moment a charm moves.
		return inventory != null && inventory.findFirstCurio(stack -> stack.getItem() == item).isPresent();
	}

	@Override
	public Map<String, ItemStack> takeCharms(LivingEntity entity) {
		Map<String, ItemStack> taken = new HashMap<>();
		ICuriosItemHandler inventory = CuriosApi.getCuriosInventoryOrNull(entity);
		if (inventory == null) {
			return taken;
		}
		List<SlotResult> worn = inventory.findCurios(stack -> stack.getItem() instanceof CharmItem);
		for (SlotResult result : worn) {
			ItemStack stack = result.stack();
			if (EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)) {
				continue;
			}
			SlotContext slot = result.slotContext();
			taken.put(KEY + slot.identifier() + "/" + slot.index(), stack.copy());
			inventory.setEquippedCurio(slot.identifier(), slot.index(), ItemStack.EMPTY);
		}
		return taken;
	}

	@Override
	public boolean putBack(LivingEntity entity, String key, ItemStack stack) {
		ICuriosItemHandler inventory = CuriosApi.getCuriosInventoryOrNull(entity);
		int slash = key.lastIndexOf('/');
		if (inventory == null || slash < 0) {
			return false;
		}
		try {
			String identifier = key.substring(KEY.length(), slash);
			int index = Integer.parseInt(key.substring(slash + 1));
			return inventory.getStacksHandler(identifier).map(handler -> {
				if (index >= handler.getSlots() || !handler.getStacks().getStackInSlot(index).isEmpty()
						|| !CuriosApi.isStackValid(new SlotContext(identifier, entity, index, false, true), stack)) {
					return false;
				}
				handler.getStacks().setStackInSlot(index, stack);
				return true;
			}).orElse(false);
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/** Charms go on with a right-click, like armour. */
	static void equipOnUse() {
		ICurioItem rightClick = new ICurioItem() {
			@Override
			public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
				return true;
			}
		};
		EspressoContent.CHARMS.forEach(charm -> CuriosApi.registerCurio(charm, rightClick));
	}
}

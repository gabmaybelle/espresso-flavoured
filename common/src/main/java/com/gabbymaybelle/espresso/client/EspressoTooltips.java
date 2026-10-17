package com.gabbymaybelle.espresso.client;

import com.gabbymaybelle.espresso.content.TooltipLines;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Puts each {@link TooltipLines} item's own lines straight under its name, as vanilla does for its items. */
public final class EspressoTooltips {
	private EspressoTooltips() {
	}

	/** Each loader calls this from its item tooltip event. */
	public static void add(ItemStack stack, List<Component> lines) {
		// An empty list is a hidden tooltip; leave it hidden.
		if (lines.isEmpty() || !(stack.getItem() instanceof TooltipLines item)) {
			return;
		}
		List<Component> extra = new ArrayList<>();
		item.addTooltipLines(stack, extra::add);
		lines.addAll(1, extra);
	}
}

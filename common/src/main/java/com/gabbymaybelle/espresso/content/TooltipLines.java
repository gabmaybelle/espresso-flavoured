package com.gabbymaybelle.espresso.content;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * An item with its own lines under its name in the tooltip. The client adds them (see
 * EspressoTooltips) through Fabric's tooltip event, in the place vanilla's deprecated
 * appendHoverText used to put them.
 */
public interface TooltipLines {
	void addTooltipLines(ItemStack stack, Consumer<Component> tooltip);
}

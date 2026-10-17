package com.gabbymaybelle.espresso.mixin;

import com.gabbymaybelle.espresso.content.CharmItem;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import java.util.function.Consumer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;

/** Lets the Ember Glove smelt what a loot table hands out (see {@link CharmItem#emberGlove}). */
@Mixin(LootTable.class)
public abstract class EmberGloveLootMixin {
	@WrapMethod(method = "getRandomItemsRaw(Lnet/minecraft/world/level/storage/loot/LootContext;Ljava/util/function/Consumer;)V")
	private void espresso$emberGlove(LootContext context, Consumer<ItemStack> out, Operation<Void> original) {
		original.call(context, CharmItem.emberGlove(context, out));
	}
}

package com.gabbymaybelle.espresso.mixin;

import com.gabbymaybelle.espresso.Config;
import com.mojang.serialization.DataResult;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.component.BundleContents;
import org.apache.commons.lang3.math.Fraction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Vanilla weighs an item in a bundle as 1 / its stack size, so a single tool or piece
 * of armour fills a whole bundle. Here items that don't stack take 1 / bundleUnstackables
 * of a bundle instead (a quarter by default). Bundles inside bundles and filled bee nests
 * keep their vanilla weight. On a server, the server's setting is used by everyone.
 */
@Mixin(BundleContents.class)
public abstract class BundleContentsMixin {
	@Inject(method = "getWeight", at = @At("HEAD"), cancellable = true)
	private static void espresso$lighterUnstackables(ItemInstance item, CallbackInfoReturnable<DataResult<Fraction>> cir) {
		int perBundle = Config.bundleUnstackables();
		if (perBundle > 1 && item.getMaxStackSize() == 1
				&& item.get(DataComponents.BUNDLE_CONTENTS) == null && item.get(DataComponents.BEES) == null) {
			cir.setReturnValue(DataResult.success(Fraction.getFraction(1, perBundle)));
		}
	}
}

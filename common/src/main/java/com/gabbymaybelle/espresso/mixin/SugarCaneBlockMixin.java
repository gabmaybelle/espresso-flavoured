package com.gabbymaybelle.espresso.mixin;

import com.gabbymaybelle.espresso.Config;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.level.block.SugarCaneBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Sugar cane grows {@link Config#sugarCaneHeight} blocks tall (vanilla: 3), so a few plants keep you in sugar. */
@Mixin(SugarCaneBlock.class)
public abstract class SugarCaneBlockMixin {
	@ModifyExpressionValue(method = "randomTick", at = @At(value = "CONSTANT", args = "intValue=3"))
	private int espresso$caneHeight(int vanilla) {
		return Config.sugarCaneHeight;
	}
}

package com.gabbymaybelle.espresso.mixin;

import com.gabbymaybelle.espresso.Config;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Vanilla slowly heals you whenever your hunger bar is at 18 or more. Here that slow
 * healing also needs some saturation, which only comes from eating recently, so healing
 * is a reward for keeping fed rather than something a full-looking bar gives for free.
 */
@Mixin(FoodData.class)
public abstract class FoodDataMixin {
	@Shadow
	private float saturationLevel;

	@ModifyConstant(method = "tick", constant = @Constant(intValue = 18))
	private int espresso$healOnlyWhenFed(int original) {
		return Config.regenNeedsSaturation && saturationLevel <= 0.0F ? Integer.MAX_VALUE : original;
	}
}

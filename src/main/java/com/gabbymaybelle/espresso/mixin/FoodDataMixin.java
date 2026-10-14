package com.gabbymaybelle.espresso.mixin;

import com.gabbymaybelle.espresso.Config;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Vanilla slowly heals you whenever your hunger bar is at 18 or more. Here that slow
 * healing also needs some saturation, which only comes from eating recently, so healing
 * is a reward for keeping fed rather than something a full-looking bar gives for free.
 * A completely full bar still counts as fed, though: with no saturation and a full bar you
 * couldn't eat (the game refuses food at 20) and couldn't heal, and nothing but walking
 * would get you out of that. Healing from a full bar costs hunger as it does in vanilla,
 * so a notch soon frees up to eat.
 */
@Mixin(FoodData.class)
public abstract class FoodDataMixin {
	/** The hunger bar's maximum, at which the game won't let you eat. */
	@Unique
	private static final int FULL = 20;

	@Shadow
	private int foodLevel;
	@Shadow
	private float saturationLevel;

	@ModifyExpressionValue(method = "tick", at = @At(value = "CONSTANT", args = "intValue=18"))
	private int espresso$healOnlyWhenFed(int original) {
		boolean unfed = saturationLevel <= 0.0F && foodLevel < FULL;
		return Config.regenNeedsSaturation && unfed ? Integer.MAX_VALUE : original;
	}
}

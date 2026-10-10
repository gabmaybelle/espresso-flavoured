package com.gabbymaybelle.espresso.mixin;

import com.gabbymaybelle.espresso.Wellbeing;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Eating anything with food value counts as a meal for Wellbeing's hunger clock. */
@Mixin(FoodProperties.class)
public abstract class FoodPropertiesMixin {
	@Inject(method = "onConsume", at = @At("HEAD"))
	private void espresso$ate(Level level, LivingEntity entity, ItemStack stack, Consumable consumable, CallbackInfo ci) {
		if (entity instanceof ServerPlayer player) {
			Wellbeing.ate(player);
		}
	}
}

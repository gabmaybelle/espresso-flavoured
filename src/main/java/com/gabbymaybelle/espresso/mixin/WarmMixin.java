package com.gabbymaybelle.espresso.mixin;

import com.gabbymaybelle.espresso.content.EspressoContent;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Warm (from a pumpkin spice latte): powder snow can't freeze you. */
@Mixin(LivingEntity.class)
public abstract class WarmMixin {
	@Inject(method = "canFreeze", at = @At("HEAD"), cancellable = true)
	private void espresso$warm(CallbackInfoReturnable<Boolean> cir) {
		if (((LivingEntity) (Object) this).hasEffect(EspressoContent.WARM)) {
			cir.setReturnValue(false);
		}
	}
}

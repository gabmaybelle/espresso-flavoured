package com.gabbymaybelle.espresso.mixin;

import com.gabbymaybelle.espresso.Nights;
import net.minecraft.world.entity.MobCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** A quarter more hostile mobs can be around at once at night. */
@Mixin(MobCategory.class)
public abstract class MobCategoryMixin {
	@Inject(method = "getMaxInstancesPerChunk", at = @At("RETURN"), cancellable = true)
	private void espresso$moreAtNight(CallbackInfoReturnable<Integer> cir) {
		if (Nights.spawningAtNight && (Object) this == MobCategory.MONSTER) {
			cir.setReturnValue(cir.getReturnValue() * Nights.CAP_PERCENT / 100);
		}
	}
}

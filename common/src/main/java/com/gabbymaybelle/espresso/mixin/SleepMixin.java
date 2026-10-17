package com.gabbymaybelle.espresso.mixin;

import com.gabbymaybelle.espresso.Wellbeing;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Tells {@link Wellbeing} when someone lies down in a bed and when they get up again. (Fabric API
 * has events for this and NeoForge doesn't, so both loaders use this.)
 */
@Mixin(LivingEntity.class)
public abstract class SleepMixin {
	@Inject(method = "startSleeping", at = @At("RETURN"))
	private void espresso$startedSleeping(BlockPos bed, CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValueZ()) {
			Wellbeing.startedSleeping((LivingEntity) (Object) this, bed);
		}
	}

	@Inject(method = "stopSleeping", at = @At("HEAD"))
	private void espresso$stoppedSleeping(CallbackInfo ci) {
		LivingEntity self = (LivingEntity) (Object) this;
		self.getSleepingPos().ifPresent(bed -> Wellbeing.stoppedSleeping(self, bed));
	}
}

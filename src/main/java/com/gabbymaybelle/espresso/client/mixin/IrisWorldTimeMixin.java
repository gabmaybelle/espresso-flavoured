package com.gabbymaybelle.espresso.client.mixin;

import com.gabbymaybelle.espresso.client.EspressoClock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Shaders (through Iris) read the time of day as the world clock modulo 24000, so with Espresso's
 * longer days their sky ran ahead: sunset colours at noon, night by mid-afternoon. The same happens
 * with any other mod's or datapack's day length. This hands them the time of day as a normal
 * 24000-tick day instead. Does nothing without Iris, or on a vanilla-length day.
 */
@Pseudo
@Mixin(targets = "net.irisshaders.iris.uniforms.WorldTimeUniforms", remap = false)
public abstract class IrisWorldTimeMixin {
	@Inject(method = "getWorldDayTime", at = @At("RETURN"), cancellable = true, require = 0, remap = false)
	private static void espresso$longerDays(CallbackInfoReturnable<Integer> cir) {
		int vanilla = EspressoClock.vanillaDayTime();
		if (vanilla >= 0) {
			cir.setReturnValue(vanilla);
		}
	}
}

package com.gabbymaybelle.espresso.client.mixin;

import com.gabbymaybelle.espresso.content.EspressoContent;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Drowsy narrows the field of view a little, like heavy eyelids. */
@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin {
	@Inject(method = "getFieldOfViewModifier", at = @At("RETURN"), cancellable = true)
	private void espresso$drowsyView(boolean firstPerson, float partialTick, CallbackInfoReturnable<Float> cir) {
		if (((AbstractClientPlayer) (Object) this).hasEffect(EspressoContent.DROWSY)) {
			cir.setReturnValue(cir.getReturnValue() * 0.88F);
		}
	}
}

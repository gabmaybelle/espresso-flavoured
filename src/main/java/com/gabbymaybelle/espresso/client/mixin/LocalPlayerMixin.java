package com.gabbymaybelle.espresso.client.mixin;

import com.gabbymaybelle.espresso.content.EspressoContent;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Too tired to sprint: Drowsy stops you starting a sprint and ends one already going. */
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
	@Inject(method = "isSprintingPossible", at = @At("HEAD"), cancellable = true)
	private void espresso$tooDrowsyToSprint(boolean allowedInShallowWater, CallbackInfoReturnable<Boolean> cir) {
		if (((LocalPlayer) (Object) this).hasEffect(EspressoContent.DROWSY)) {
			cir.setReturnValue(false);
		}
	}
}

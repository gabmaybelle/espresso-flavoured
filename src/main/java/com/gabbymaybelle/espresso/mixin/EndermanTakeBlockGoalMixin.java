package com.gabbymaybelle.espresso.mixin;

import com.gabbymaybelle.espresso.Config;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Endermen no longer pick up blocks. */
@Mixin(targets = "net.minecraft.world.entity.monster.Enderman$EndermanTakeBlockGoal")
public abstract class EndermanTakeBlockGoalMixin {
	@Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
	private void espresso$leaveBlocksAlone(CallbackInfoReturnable<Boolean> cir) {
		if (!Config.endermanGriefing) {
			cir.setReturnValue(false);
		}
	}
}

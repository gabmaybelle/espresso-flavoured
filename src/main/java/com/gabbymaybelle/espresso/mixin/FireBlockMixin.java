package com.gabbymaybelle.espresso.mixin;

import com.gabbymaybelle.espresso.content.GazeboGuard;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Fire can't burn away the spawn gazebo's fences, gates and benches. */
@Mixin(FireBlock.class)
public abstract class FireBlockMixin {
	@Inject(method = "checkBurnOut", at = @At("HEAD"), cancellable = true)
	private void espresso$spareGazebo(Level level, BlockPos pos, int chance, RandomSource random, int age, CallbackInfo ci) {
		if (GazeboGuard.guards(level, pos)) {
			ci.cancel();
		}
	}
}

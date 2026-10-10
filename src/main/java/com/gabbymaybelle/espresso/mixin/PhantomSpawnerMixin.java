package com.gabbymaybelle.espresso.mixin;

import com.gabbymaybelle.espresso.Config;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** No phantoms, so sleeping stays optional. */
@Mixin(PhantomSpawner.class)
public abstract class PhantomSpawnerMixin {
	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private void espresso$noPhantoms(ServerLevel level, boolean spawnEnemies, CallbackInfo ci) {
		if (!Config.phantoms) {
			ci.cancel();
		}
	}
}

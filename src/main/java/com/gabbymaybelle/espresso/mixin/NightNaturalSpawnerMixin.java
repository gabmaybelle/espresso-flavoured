package com.gabbymaybelle.espresso.mixin;

import com.gabbymaybelle.espresso.Nights;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LocalMobCapCalculator;
import net.minecraft.world.level.NaturalSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Tough nights: each spawn round is told whether its level is having a night, for the mob cap
 * (see SpawnStateMixin). If that mixin didn't apply (another mod changed the same code), this
 * does nothing rather than fail.
 */
@Mixin(NaturalSpawner.class)
public abstract class NightNaturalSpawnerMixin {
	@Inject(method = "createState", at = @At("RETURN"))
	private static void espresso$noteNight(int spawnableChunkCount, ServerLevel level, NaturalSpawner.ChunkGetter chunks,
			LocalMobCapCalculator localCaps, CallbackInfoReturnable<NaturalSpawner.SpawnState> cir) {
		if (cir.getReturnValue() instanceof Nights.NightSpawnState state) {
			state.espresso$setNight(Nights.isToughNight(level));
		}
	}
}

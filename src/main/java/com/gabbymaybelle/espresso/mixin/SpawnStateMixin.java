package com.gabbymaybelle.espresso.mixin;

import com.gabbymaybelle.espresso.Nights;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/** A quarter more hostile mobs can be around at once while this spawn round's level is having a night. */
@Mixin(NaturalSpawner.SpawnState.class)
public abstract class SpawnStateMixin implements Nights.NightSpawnState {
	@Unique
	private boolean espresso$night;

	@Override
	public void espresso$setNight(boolean night) {
		espresso$night = night;
	}

	@ModifyExpressionValue(method = "canSpawnForCategoryGlobal",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/MobCategory;getMaxInstancesPerChunk()I"))
	private int espresso$moreAtNight(int cap, @Local(argsOnly = true) MobCategory category) {
		return espresso$night && category == MobCategory.MONSTER ? cap * Nights.CAP_PERCENT / 100 : cap;
	}
}

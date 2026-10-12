package com.gabbymaybelle.espresso.mixin;

import com.gabbymaybelle.espresso.HomeBeds;
import com.gabbymaybelle.espresso.Nights;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.LocalMobCapCalculator;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The safe home radius (no natural hostile spawns near a home bed), and tough nights: each spawn
 * round is told whether its level is having a night, for the mob cap (see SpawnStateMixin).
 */
@Mixin(NaturalSpawner.class)
public abstract class NaturalSpawnerMixin {
	@Inject(method = "isValidSpawnPostitionForType", at = @At("HEAD"), cancellable = true)
	private static void espresso$safeHome(ServerLevel level, MobCategory category, StructureManager structures,
			ChunkGenerator generator, MobSpawnSettings.SpawnerData spawner, BlockPos.MutableBlockPos pos,
			double distanceToPlayer, CallbackInfoReturnable<Boolean> cir) {
		if (category == MobCategory.MONSTER && HomeBeds.isProtected(level, pos)) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "createState", at = @At("RETURN"))
	private static void espresso$noteNight(int spawnableChunkCount, ServerLevel level, NaturalSpawner.ChunkGetter chunks,
			LocalMobCapCalculator localCaps, CallbackInfoReturnable<NaturalSpawner.SpawnState> cir) {
		((Nights.NightSpawnState) cir.getReturnValue()).espresso$setNight(Nights.isToughNight(level));
	}
}

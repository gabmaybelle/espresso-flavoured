package com.gabbymaybelle.espresso.mixin;

import com.gabbymaybelle.espresso.Config;
import com.gabbymaybelle.espresso.Nights;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Records whether the level about to spawn mobs is having a night. */
@Mixin(ServerChunkCache.class)
public abstract class ServerChunkCacheMixin {
	@Shadow
	@Final
	private ServerLevel level;

	@Inject(method = "tickChunks(Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("HEAD"))
	private void espresso$checkNight(ProfilerFiller profiler, CallbackInfo ci) {
		Nights.spawningAtNight = Config.toughNights && level.dimensionType().hasSkyLight() && level.isDarkOutside();
	}
}

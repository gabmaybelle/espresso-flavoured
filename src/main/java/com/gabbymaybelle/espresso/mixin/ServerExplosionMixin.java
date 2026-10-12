package com.gabbymaybelle.espresso.mixin;

import com.gabbymaybelle.espresso.content.GazeboGuard;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ServerExplosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Explosions of any kind (TNT, fireballs, modded blasts) leave the spawn gazebo standing. This hooks
 * the step that breaks the blocks rather than the one that finds them, which Lithium replaces.
 */
@Mixin(ServerExplosion.class)
public abstract class ServerExplosionMixin {
	@Inject(method = "interactWithBlocks", at = @At("HEAD"))
	private void espresso$spareGazebo(List<BlockPos> blocks, CallbackInfo ci) {
		ServerExplosion explosion = (ServerExplosion) (Object) this;
		blocks.removeIf(pos -> GazeboGuard.guards(explosion.level(), pos));
	}
}

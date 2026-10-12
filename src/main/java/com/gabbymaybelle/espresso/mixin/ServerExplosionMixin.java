package com.gabbymaybelle.espresso.mixin;

import com.gabbymaybelle.espresso.Config;
import com.gabbymaybelle.espresso.Espresso;
import com.gabbymaybelle.espresso.content.GazeboGuard;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.ServerExplosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Explosions of any kind (TNT, fireballs, modded blasts) leave the spawn gazebo standing, and with
 * creeper griefing off, no creeper's explosion breaks blocks: vanilla's (also handled in
 * CreeperMixin), any mod's creeper built on it, and any mob listed in the
 * {@code espresso_flavoured:spares_blocks} entity tag, for creeper-like mobs that aren't. This hooks
 * the step that breaks the blocks rather than the one that finds them, which Lithium replaces.
 */
@Mixin(ServerExplosion.class)
public abstract class ServerExplosionMixin {
	@Unique
	private static final TagKey<EntityType<?>> ESPRESSO$SPARES_BLOCKS = TagKey.create(Registries.ENTITY_TYPE,
			Identifier.fromNamespaceAndPath(Espresso.MOD_ID, "spares_blocks"));

	@Inject(method = "interactWithBlocks", at = @At("HEAD"))
	private void espresso$spareBlocks(List<BlockPos> blocks, CallbackInfo ci) {
		ServerExplosion explosion = (ServerExplosion) (Object) this;
		Entity source = explosion.getDirectSourceEntity();
		if (!Config.creeperGriefing && source != null && (source instanceof Creeper || source.typeHolder().is(ESPRESSO$SPARES_BLOCKS))) {
			blocks.clear();
			return;
		}
		blocks.removeIf(pos -> GazeboGuard.guards(explosion.level(), pos));
	}
}

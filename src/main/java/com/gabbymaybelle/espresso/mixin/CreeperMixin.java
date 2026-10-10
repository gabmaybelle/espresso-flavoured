package com.gabbymaybelle.espresso.mixin;

import com.gabbymaybelle.espresso.Config;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** Creeper explosions still hurt players and mobs, but no longer break blocks. */
@Mixin(Creeper.class)
public abstract class CreeperMixin {
	@ModifyArg(method = "explodeCreeper", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerLevel;explode(Lnet/minecraft/world/entity/Entity;DDDFLnet/minecraft/world/level/Level$ExplosionInteraction;)V"),
			index = 5)
	private Level.ExplosionInteraction espresso$spareBlocks(Level.ExplosionInteraction interaction) {
		return Config.creeperGriefing ? interaction : Level.ExplosionInteraction.NONE;
	}
}

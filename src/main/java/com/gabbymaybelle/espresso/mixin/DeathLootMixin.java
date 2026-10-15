package com.gabbymaybelle.espresso.mixin;

import com.gabbymaybelle.espresso.KeepOnDeath;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Set aside the worn café charms at the very start of a player's death drops, before anything
 * else handles the inventory (Trinkets and graves mods such as Universal Graves take over the
 * equipment drop later in this method).
 */
@Mixin(LivingEntity.class)
public abstract class DeathLootMixin {
	@Inject(method = "dropAllDeathLoot", at = @At("HEAD"))
	private void espresso$keepHotbarAndArmour(ServerLevel level, DamageSource source, CallbackInfo ci) {
		if ((Object) this instanceof Player player) {
			KeepOnDeath.stashKeptItems(player);
		}
	}
}

package com.gabbymaybelle.espresso.mixin;

import com.gabbymaybelle.espresso.content.CharmItem;
import com.gabbymaybelle.espresso.content.EspressoContent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** A Copper Tamper: 15% more damage to hostile mobs (and only them), from hits and from your arrows. */
@Mixin(LivingEntity.class)
public abstract class TamperMixin {
	@ModifyVariable(method = "hurtServer", at = @At("HEAD"), argsOnly = true)
	private float espresso$tamper(float amount, ServerLevel level, DamageSource source) {
		if (this instanceof Enemy && source.getEntity() instanceof Player player && CharmItem.isWorn(player, EspressoContent.COPPER_TAMPER)) {
			return amount * 1.15F;
		}
		return amount;
	}
}

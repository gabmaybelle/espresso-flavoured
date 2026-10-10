package com.gabbymaybelle.espresso.mixin;

import com.gabbymaybelle.espresso.Espresso;
import com.gabbymaybelle.espresso.Nights;
import net.minecraft.resources.Identifier;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Hostile mobs that spawn naturally at night get 20% more health and attack damage. */
@Mixin(Mob.class)
public abstract class MobNightMixin {
	@Unique
	private static final Identifier ESPRESSO$NIGHT = Identifier.fromNamespaceAndPath(Espresso.MOD_ID, "night_spawn");

	@Inject(method = "finalizeSpawn", at = @At("RETURN"))
	private void espresso$toughAtNight(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
			SpawnGroupData data, CallbackInfoReturnable<SpawnGroupData> cir) {
		Mob mob = (Mob) (Object) this;
		if (reason != EntitySpawnReason.NATURAL || !(mob instanceof Enemy) || !Nights.spawningAtNight) {
			return;
		}
		espresso$buff(mob.getAttribute(Attributes.MAX_HEALTH));
		espresso$buff(mob.getAttribute(Attributes.ATTACK_DAMAGE));
		mob.setHealth(mob.getMaxHealth());
	}

	@Unique
	private static void espresso$buff(AttributeInstance attribute) {
		if (attribute != null && !attribute.hasModifier(ESPRESSO$NIGHT)) {
			attribute.addPermanentModifier(new AttributeModifier(ESPRESSO$NIGHT, Nights.BUFF, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
		}
	}
}

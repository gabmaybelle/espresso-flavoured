package com.gabbymaybelle.espresso.content;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * Well Rested: after a cosy night's sleep, two golden hearts that last until they're used up
 * (like Absorption) or the effect runs out.
 */
public class WellRestedEffect extends MobEffect {
	static final float HEARTS = 4.0F;

	WellRestedEffect(MobEffectCategory category, int color) {
		super(category, color);
	}

	@Override
	public void onEffectStarted(LivingEntity entity, int amplifier) {
		super.onEffectStarted(entity, amplifier);
		entity.setAbsorptionAmount(Math.max(entity.getAbsorptionAmount(), HEARTS * (amplifier + 1)));
	}

	@Override
	public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
		// Ends once the golden hearts are gone.
		return entity.getAbsorptionAmount() > 0.0F;
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		return true;
	}
}

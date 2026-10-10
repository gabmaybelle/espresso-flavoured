package com.gabbymaybelle.espresso.content;

import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** A coffee drink. Drunk from the Golden Mug, its pick-me-up lasts half as long again. */
public class CoffeeItem extends Item {
	private static final List<Holder<MobEffect>> PERKS = List.of(MobEffects.SPEED, MobEffects.HASTE, EspressoContent.CAFFEINATED);

	public CoffeeItem(Properties properties) {
		super(properties);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
		boolean golden = Mugs.isGolden(stack);
		ItemStack result = super.finishUsingItem(stack, level, entity);
		if (golden && !level.isClientSide()) {
			for (Holder<MobEffect> perk : PERKS) {
				MobEffectInstance effect = entity.getEffect(perk);
				if (effect != null) {
					entity.addEffect(new MobEffectInstance(perk, effect.getDuration() * 3 / 2, effect.getAmplifier(),
							effect.isAmbient(), effect.isVisible(), effect.showIcon()));
				}
			}
		}
		return result;
	}
}

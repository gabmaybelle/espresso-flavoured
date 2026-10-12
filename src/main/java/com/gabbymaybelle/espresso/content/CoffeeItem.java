package com.gabbymaybelle.espresso.content;

import com.gabbymaybelle.espresso.Wellbeing;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

/**
 * A coffee drink. Drunk from the Golden Mug, its pick-me-up (or Cozy) lasts half as long again.
 * A decaf one gives Cozy in place of the caffeine but keeps the drink's own extras (a decaf
 * pumpkin spice latte still makes you Warm), and anything Cozy calms a caffeine crash. A Thermos
 * (a café charm) makes all of a drink's effects last a quarter longer.
 */
public class CoffeeItem extends Item {
	/** The caffeine: what decaf leaves out. */
	private static final List<Holder<MobEffect>> CAFFEINE = List.of(MobEffects.SPEED, MobEffects.HASTE, EspressoContent.CAFFEINATED);
	/** What the Golden Mug stretches. */
	private static final List<Holder<MobEffect>> PERKS = List.of(MobEffects.SPEED, MobEffects.HASTE, EspressoContent.CAFFEINATED, EspressoContent.COZY);
	private static final int COZY_TICKS = 3600;

	public CoffeeItem(Properties properties) {
		super(properties);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
		boolean golden = Mugs.isGolden(stack);
		if (stack.getOrDefault(EspressoContent.DECAF, false)) {
			// Worked out from this drink's own effects, so mixing a decaf espresso into a latte
			// doesn't carry the espresso's effects along.
			stack.set(DataComponents.CONSUMABLE, decaf(components().get(DataComponents.CONSUMABLE)));
		}
		List<MobEffectInstance> effects = drinkEffects(stack);
		boolean thermos = CharmItem.isWorn(entity, EspressoContent.THERMOS);
		ItemStack result = super.finishUsingItem(stack, level, entity);
		if (entity instanceof ServerPlayer player && effects.stream().anyMatch(effect -> effect.getEffect() == EspressoContent.COZY)) {
			Wellbeing.calm(player);
		}
		if ((golden || thermos) && !level.isClientSide()) {
			// The Golden Mug makes the pick-me-up (or Cozy) last half as long again, and a Thermos makes
			// everything the drink gives last a quarter longer; together they multiply. addEffect keeps
			// anything longer you already had.
			for (MobEffectInstance effect : effects) {
				double stretch = (golden && PERKS.contains(effect.getEffect()) ? 1.5 : 1) * (thermos ? 1.25 : 1);
				if (stretch > 1) {
					entity.addEffect(new MobEffectInstance(effect.getEffect(), (int) (effect.getDuration() * stretch), effect.getAmplifier(),
							effect.isAmbient(), effect.isVisible(), effect.showIcon()));
				}
			}
		}
		return result;
	}

	/** A drink's effects with the caffeine swapped for Cozy. */
	private static Consumable decaf(Consumable drink) {
		List<ConsumeEffect> effects = new ArrayList<>();
		for (ConsumeEffect effect : drink.onConsumeEffects()) {
			if (effect instanceof ApplyStatusEffectsConsumeEffect apply) {
				List<MobEffectInstance> kept = apply.effects().stream().filter(e -> !CAFFEINE.contains(e.getEffect())).toList();
				if (!kept.isEmpty()) {
					effects.add(new ApplyStatusEffectsConsumeEffect(kept, apply.probability()));
				}
			} else {
				effects.add(effect);
			}
		}
		effects.add(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(EspressoContent.COZY, COZY_TICKS, 0)));
		return new Consumable(drink.consumeSeconds(), drink.animation(), drink.sound(), drink.hasConsumeParticles(), effects);
	}

	/** Every effect this drink gives, as it would give them. */
	private static List<MobEffectInstance> drinkEffects(ItemStack stack) {
		List<MobEffectInstance> effects = new ArrayList<>();
		Consumable consumable = stack.get(DataComponents.CONSUMABLE);
		if (consumable != null) {
			for (ConsumeEffect effect : consumable.onConsumeEffects()) {
				if (effect instanceof ApplyStatusEffectsConsumeEffect apply) {
					effects.addAll(apply.effects());
				}
			}
		}
		return effects;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
		if (stack.getOrDefault(EspressoContent.DECAF, false)) {
			tooltip.accept(Component.translatable("item.espresso.decaf").withStyle(ChatFormatting.GRAY));
		}
	}
}

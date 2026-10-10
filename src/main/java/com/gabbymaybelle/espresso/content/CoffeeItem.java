package com.gabbymaybelle.espresso.content;

import com.gabbymaybelle.espresso.Wellbeing;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

/**
 * A coffee drink. Drunk from the Golden Mug, its pick-me-up lasts half as long again. A decaf
 * one gives Cozy instead, and calms a caffeine crash.
 */
public class CoffeeItem extends Item {
	private static final List<Holder<MobEffect>> PERKS = List.of(MobEffects.SPEED, MobEffects.HASTE, EspressoContent.CAFFEINATED);

	public CoffeeItem(Properties properties) {
		super(properties);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
		boolean golden = Mugs.isGolden(stack);
		boolean decaf = stack.getOrDefault(EspressoContent.DECAF, false);
		ItemStack result = super.finishUsingItem(stack, level, entity);
		if (decaf && entity instanceof ServerPlayer player) {
			Wellbeing.calm(player);
		}
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

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
		if (stack.getOrDefault(EspressoContent.DECAF, false)) {
			tooltip.accept(Component.translatable("item.espresso.decaf").withStyle(ChatFormatting.GRAY));
		}
	}
}

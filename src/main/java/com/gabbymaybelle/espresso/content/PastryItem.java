package com.gabbymaybelle.espresso.content;

import com.gabbymaybelle.espresso.EspressoAdvancements;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * A café pastry. Eaten with a coffee on the go (while Caffeinated) or a decaf or cocoa (while
 * Cozy), it's a pairing: a little extra saturation and a puff of hearts.
 */
public class PastryItem extends Item {
	/** Extra saturation for a pairing, on top of the pastry's own. */
	private static final float PAIRING_SATURATION = 3.0F;

	public PastryItem(Properties properties) {
		super(properties);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
		boolean paired = entity.hasEffect(EspressoContent.CAFFEINATED) || entity.hasEffect(EspressoContent.COZY);
		ItemStack result = super.finishUsingItem(stack, level, entity);
		if (paired && entity instanceof ServerPlayer player && level instanceof ServerLevel serverLevel) {
			player.getFoodData().setSaturation(Math.min(player.getFoodData().getFoodLevel(),
					player.getFoodData().getSaturationLevel() + PAIRING_SATURATION));
			serverLevel.sendParticles(ParticleTypes.HEART, player.getX(), player.getEyeY() + 0.3, player.getZ(), 3, 0.3, 0.2, 0.3, 0.0);
			level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.4F, 1.3F);
			EspressoAdvancements.award(player, "guide/perfect_pairing", "paired");
		}
		return result;
	}
}

package com.gabbymaybelle.espresso.mixin;

import com.gabbymaybelle.espresso.EspressoAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Tells EspressoAdvancements about each finished trade, with both what was paid and what was got. */
@Mixin(AbstractVillager.class)
public abstract class MerchantTradeMixin {
	@Inject(method = "notifyTrade", at = @At("TAIL"))
	private void espresso$traded(MerchantOffer offer, CallbackInfo ci) {
		AbstractVillager merchant = (AbstractVillager) (Object) this;
		if (merchant.getTradingPlayer() instanceof ServerPlayer player) {
			EspressoAdvancements.traded(player, merchant, offer);
		}
	}
}

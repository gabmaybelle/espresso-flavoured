package com.gabbymaybelle.espresso.mixin;

import com.gabbymaybelle.espresso.Config;
import com.gabbymaybelle.espresso.Espresso;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The block merchant: after its usual trades, the wandering trader also offers a few
 * building blocks from the {@code espresso:wandering_trader/building_blocks} trade set.
 */
@Mixin(WanderingTrader.class)
public abstract class WanderingTraderMixin extends AbstractVillager {
	@Unique
	private static final ResourceKey<TradeSet> ESPRESSO$BUILDING_BLOCKS = ResourceKey.create(Registries.TRADE_SET,
			Identifier.fromNamespaceAndPath(Espresso.MOD_ID, "wandering_trader/building_blocks"));

	private WanderingTraderMixin(EntityType<? extends AbstractVillager> type, Level level) {
		super(type, level);
	}

	@Inject(method = "updateTrades", at = @At("TAIL"))
	private void espresso$addBuildingBlocks(ServerLevel level, CallbackInfo ci) {
		if (Config.blockMerchant) {
			addOffersFromTradeSet(level, getOffers(), ESPRESSO$BUILDING_BLOCKS);
		}
	}
}

package com.gabbymaybelle.espresso;

import com.gabbymaybelle.espresso.content.EspressoContent;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.item.trading.MerchantOffer;

/**
 * Awards the Espresso advancements that the game can't notice on its own (their criteria are
 * {@code minecraft:impossible}): setting a home, sleeping Well Rested, a pastry pairing, sitting
 * down, filling a canister, and the trades vanilla's trade trigger can't tell apart.
 */
public final class EspressoAdvancements {
	private EspressoAdvancements() {
	}

	/** Grants one criterion of {@code espresso:<path>}, if the advancement is loaded. */
	public static void award(ServerPlayer player, String path, String criterion) {
		AdvancementHolder advancement = player.level().getServer().getAdvancements()
				.get(Identifier.fromNamespaceAndPath(Espresso.MOD_ID, path));
		if (advancement != null) {
			player.getAdvancements().award(advancement, criterion);
		}
	}

	/**
	 * After a trade: Fair Trade for selling roasted beans to the wandering trader (vanilla's trigger
	 * only sees what you got, and the trader pays emeralds for plenty of things), and Your Usual for
	 * any trade with a barista.
	 */
	public static void traded(ServerPlayer player, AbstractVillager merchant, MerchantOffer offer) {
		if (merchant instanceof WanderingTrader && offer.getCostA().is(EspressoContent.ROASTED_COFFEE_BEANS)) {
			award(player, "guide/fair_trade", "sold");
		}
		if (merchant instanceof Villager villager && villager.getVillagerData().profession().is(EspressoContent.BARISTA)) {
			award(player, "guide/your_usual", "traded");
		}
	}
}

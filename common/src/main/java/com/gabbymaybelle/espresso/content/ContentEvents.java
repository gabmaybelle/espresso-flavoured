package com.gabbymaybelle.espresso.content;

import com.gabbymaybelle.espresso.Platform;
import net.minecraft.world.item.CreativeModeTabs;

/**
 * Hooks up what the content needs once the game is running: the rules in each content class, and
 * the creative tabs. Kept apart from {@link EspressoContent} so it can run at startup without
 * loading (and so registering) the content early.
 */
public final class ContentEvents {
	private ContentEvents() {
	}

	public static void register() {
		Mugs.register();
		BeanCoin.register();
		CharmItem.register();
		Seat.register();
		CoffeeLoot.register();
		VillageCafe.register();
		Platform.Hooks hooks = Platform.get();
		hooks.onCreativeTab(CreativeModeTabs.NATURAL_BLOCKS, output -> output.accept(EspressoContent.COFFEE_CHERRIES));
		hooks.onCreativeTab(CreativeModeTabs.FUNCTIONAL_BLOCKS, output -> {
			output.accept(EspressoContent.ESPRESSO_MACHINE_ITEM);
			output.accept(EspressoContent.CANISTER);
			output.accept(EspressoContent.GLASS_JAR);
			EspressoContent.COFFEE_BAR_ITEMS.forEach(output::accept);
		});
		hooks.onCreativeTab(CreativeModeTabs.TOOLS_AND_UTILITIES, output -> EspressoContent.CHARMS.forEach(output::accept));
		hooks.onCreativeTab(CreativeModeTabs.INGREDIENTS, output -> {
			output.accept(EspressoContent.UNFIRED_MUG);
			output.accept(EspressoContent.UNFIRED_CANISTER);
			output.accept(EspressoContent.BARISTAS_RESERVE);
			output.accept(EspressoContent.DECAF_BEANS);
			output.accept(EspressoContent.CINNAMON);
			output.accept(EspressoContent.PUMPKIN_SPICE);
			output.accept(EspressoContent.COCOA_MIX);
			output.accept(EspressoContent.CARAMEL);
			output.accept(EspressoContent.COFFEE_GROUNDS);
			output.accept(EspressoContent.VANILLA_SYRUP);
		});
		hooks.onCreativeTab(CreativeModeTabs.FOOD_AND_DRINKS, output -> {
			output.accept(EspressoContent.MUG);
			output.accept(EspressoContent.GOLDEN_MUG);
			output.accept(EspressoContent.BEAN_COIN);
			output.accept(EspressoContent.ROASTED_COFFEE_BEANS);
			output.accept(EspressoContent.ESPRESSO);
			output.accept(EspressoContent.LATTE);
			output.accept(EspressoContent.MOCHA_LATTE);
			output.accept(EspressoContent.VANILLA_LATTE);
			output.accept(EspressoContent.RISTRETTO);
			output.accept(EspressoContent.PUMPKIN_SPICE_LATTE);
			output.accept(EspressoContent.ICED_COFFEE);
			output.accept(EspressoContent.NIGHT_SHIFT);
			output.accept(EspressoContent.HOT_CHOCOLATE);
			output.accept(EspressoContent.HONEY_LATTE);
			output.accept(EspressoContent.CARAMEL_MACCHIATO);
			output.accept(EspressoContent.COLD_BREW);
			output.accept(EspressoContent.CROISSANT);
			output.accept(EspressoContent.BISCOTTI);
			output.accept(EspressoContent.MUFFIN);
		});
	}
}

package com.gabbymaybelle.espresso.content;

import com.gabbymaybelle.espresso.Espresso;
import java.util.List;
import java.util.function.Function;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

/** Espresso Flavoured's blocks, items and effects. */
public final class EspressoContent {
	private EspressoContent() {
	}

	private static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(Espresso.MOD_ID, path);
	}

	// ---------------------------------------------------------------- effects
	/** Heavy eyelids: slower mining and walking, and (client side) no sprinting. */
	public static final Holder<MobEffect> DROWSY = Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, id("drowsy"),
			new EspressoEffect(MobEffectCategory.HARMFUL, 0x5a4a6e)
					.addAttributeModifier(Attributes.BLOCK_BREAK_SPEED, id("drowsy_mining"), -0.3, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
					.addAttributeModifier(Attributes.MOVEMENT_SPEED, id("drowsy_walking"), -0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
	/** From coffee: holds off the drowsiness of staying up too long. */
	public static final Holder<MobEffect> CAFFEINATED = Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, id("caffeinated"),
			new EspressoEffect(MobEffectCategory.BENEFICIAL, 0xc4884e));

	// ---------------------------------------------------------------- blocks
	public static final Block COFFEE_BUSH = registerBlock("coffee_bush", CoffeeBushBlock::new,
			BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).randomTicks().noCollision()
					.sound(SoundType.SWEET_BERRY_BUSH).pushReaction(PushReaction.POPPED));

	// ---------------------------------------------------------------- items
	public static final Item COFFEE_CHERRIES = registerItem("coffee_cherries",
			props -> new BlockItem(COFFEE_BUSH, props), new Item.Properties().useItemDescriptionPrefix());
	public static final Item ROASTED_COFFEE_BEANS = registerItem("roasted_coffee_beans", Item::new, new Item.Properties());
	public static final Item ESPRESSO = registerItem("espresso", Item::new, drink(1800, null));
	public static final Item LATTE = registerItem("latte", Item::new, drink(3600, new FoodProperties(2, 0.2F, true)));
	public static final Item MOCHA_LATTE = registerItem("mocha_latte", Item::new, drink(3600, new FoodProperties(4, 0.4F, true)));

	/** A coffee drink: Speed I, Haste I and Caffeinated for {@code ticks}, served in a glass bottle. */
	private static Item.Properties drink(int ticks, FoodProperties food) {
		Consumable consumable = Consumables.defaultDrink()
				.onConsume(new ApplyStatusEffectsConsumeEffect(List.of(
						new MobEffectInstance(MobEffects.SPEED, ticks, 0),
						new MobEffectInstance(MobEffects.HASTE, ticks, 0),
						new MobEffectInstance(CAFFEINATED, ticks, 0))))
				.build();
		Item.Properties props = new Item.Properties().stacksTo(16).usingConvertsTo(Items.GLASS_BOTTLE);
		return food == null ? props.component(DataComponents.CONSUMABLE, consumable) : props.food(food, consumable);
	}

	private static Block registerBlock(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties props) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, factory.apply(props.setId(key)));
	}

	private static Item registerItem(String name, Function<Item.Properties, Item> factory, Item.Properties props) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id(name));
		return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(props.setId(key)));
	}

	/** Loads the class (registering everything above) and hooks up creative tabs and worldgen. */
	public static void register() {
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.NATURAL_BLOCKS).register(output -> output.accept(COFFEE_CHERRIES));
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FOOD_AND_DRINKS).register(output -> {
			output.accept(ROASTED_COFFEE_BEANS);
			output.accept(ESPRESSO);
			output.accept(LATTE);
			output.accept(MOCHA_LATTE);
		});
		// Wild coffee bushes in jungles and savannas.
		BiomeModifications.addFeature(BiomeSelectors.tag(BiomeTags.IS_JUNGLE).or(BiomeSelectors.tag(BiomeTags.IS_SAVANNA)),
				GenerationStep.Decoration.VEGETAL_DECORATION,
				ResourceKey.create(Registries.PLACED_FEATURE, id("patch_coffee_bush")));
	}

	/** MobEffect's constructor is protected. */
	private static final class EspressoEffect extends MobEffect {
		EspressoEffect(MobEffectCategory category, int color) {
			super(category, color);
		}
	}
}

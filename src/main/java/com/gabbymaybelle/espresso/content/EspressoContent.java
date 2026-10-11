package com.gabbymaybelle.espresso.content;

import com.gabbymaybelle.espresso.Espresso;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import java.util.function.Predicate;
import net.minecraft.world.level.biome.Biomes;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PoiHelper;
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
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
	/** From coffee: holds off Drowsy for a while, at the price of a crash when it wears off (see Wellbeing). */
	public static final Holder<MobEffect> CAFFEINATED = Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, id("caffeinated"),
			new EspressoEffect(MobEffectCategory.BENEFICIAL, 0xc4884e));
	/** From decaf: heals you slowly near home, and sleeping with it wakes you Well Rested (see Wellbeing). */
	public static final Holder<MobEffect> COZY = Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, id("cozy"),
			new EspressoEffect(MobEffectCategory.BENEFICIAL, 0xe8a87c));
	/** From a pumpkin spice latte: powder snow can't freeze you (see WarmMixin). */
	public static final Holder<MobEffect> WARM = Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, id("warm"),
			new EspressoEffect(MobEffectCategory.BENEFICIAL, 0xe07b39));
	/** Two golden hearts after a cozy night's sleep. */
	public static final Holder<MobEffect> WELL_RESTED = Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, id("well_rested"),
			new WellRestedEffect(MobEffectCategory.BENEFICIAL, 0xf4d06f)
					.addAttributeModifier(Attributes.MAX_ABSORPTION, id("well_rested_hearts"), WellRestedEffect.HEARTS, AttributeModifier.Operation.ADD_VALUE));

	// ---------------------------------------------------------------- blocks
	public static final Block COFFEE_BUSH = registerBlock("coffee_bush", CoffeeBushBlock::new,
			BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).randomTicks().noCollision()
					.sound(SoundType.SWEET_BERRY_BUSH).pushReaction(PushReaction.POPPED));
	public static final Block ESPRESSO_MACHINE = registerBlock("espresso_machine", EspressoMachineBlock::new,
			BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5F).requiresCorrectToolForDrops()
					.sound(SoundType.METAL).noOcclusion());
	public static final BlockEntityType<EspressoMachineBlockEntity> ESPRESSO_MACHINE_ENTITY = Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE, id("espresso_machine"),
			new BlockEntityType<>(EspressoMachineBlockEntity::new, Set.of(ESPRESSO_MACHINE)));
	public static final MenuType<EspressoMachineMenu> ESPRESSO_MACHINE_MENU = Registry.register(
			BuiltInRegistries.MENU, id("espresso_machine"), new MenuType<>(EspressoMachineMenu::new, FeatureFlags.VANILLA_SET));
	public static final Block CANISTER_BLOCK = registerBlock("canister", CanisterBlock::new,
			BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_WHITE).strength(0.5F)
					.sound(SoundType.DECORATED_POT).noOcclusion().pushReaction(PushReaction.IMMOVEABLE));
	public static final Block GLASS_JAR_BLOCK = registerBlock("glass_jar", JarBlock::new,
			BlockBehaviour.Properties.of().mapColor(MapColor.NONE).strength(0.3F)
					.sound(SoundType.GLASS).noOcclusion().pushReaction(PushReaction.IMMOVEABLE));
	public static final BlockEntityType<CanisterBlockEntity> CANISTER_ENTITY = Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE, id("canister"), new BlockEntityType<>(CanisterBlockEntity::new, Set.of(CANISTER_BLOCK, GLASS_JAR_BLOCK)));

	// ---------------------------------------------------------------- the barista
	/** The espresso machine is the barista's workstation. */
	public static final ResourceKey<PoiType> BARISTA_POI = ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, id("barista"));
	static {
		PoiHelper.register(BARISTA_POI.identifier(), 1, 1, ESPRESSO_MACHINE);
	}
	public static final ResourceKey<VillagerProfession> BARISTA = ResourceKey.create(Registries.VILLAGER_PROFESSION, id("barista"));
	static {
		Int2ObjectMap<ResourceKey<TradeSet>> trades = new Int2ObjectOpenHashMap<>();
		for (int level = 1; level <= 5; level++) {
			trades.put(level, ResourceKey.create(Registries.TRADE_SET, id("barista/level_" + level)));
		}
		Registry.register(BuiltInRegistries.VILLAGER_PROFESSION, BARISTA, new VillagerProfession(
				Component.translatable("entity.espresso.villager.barista"),
				poi -> poi.is(BARISTA_POI), poi -> poi.is(BARISTA_POI),
				ImmutableSet.of(), ImmutableSet.of(), SoundEvents.BREWING_STAND_BREW, trades));
	}
	/** Every wood the coffee bar comes in. */
	public static final List<String> WOODS = List.of("oak", "spruce", "birch", "jungle", "acacia", "dark_oak", "mangrove",
			"cherry", "pale_oak", "poplar", "bamboo", "crimson", "warped");
	/** Coffee bars by wood, in {@link #WOODS} order. */
	public static final List<Block> COFFEE_BARS = WOODS.stream().map(wood -> registerBlock(wood + "_coffee_bar", CoffeeBarBlock::new,
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5F)
					.sound(wood.equals("crimson") || wood.equals("warped") ? SoundType.NETHER_WOOD
							: wood.equals("bamboo") ? SoundType.BAMBOO_WOOD : wood.equals("cherry") ? SoundType.CHERRY_WOOD : SoundType.WOOD))).toList();
	/** A mug set down on a surface (placed by crouch-using a mug; see {@link Mugs}). */
	public static final Block PLACED_MUG = registerBlock("placed_mug", PlacedMugBlock::new,
			BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_WHITE).strength(0.3F)
					.sound(SoundType.DECORATED_POT).noOcclusion().pushReaction(PushReaction.POPPED));
	public static final BlockEntityType<PlacedMugBlockEntity> PLACED_MUG_ENTITY = Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE, id("placed_mug"), new BlockEntityType<>(PlacedMugBlockEntity::new, Set.of(PLACED_MUG)));

	// ---------------------------------------------------------------- items
	public static final Item COFFEE_CHERRIES = registerItem("coffee_cherries",
			props -> new BlockItem(COFFEE_BUSH, props), new Item.Properties().useItemDescriptionPrefix());
	public static final Item ESPRESSO_MACHINE_ITEM = registerItem("espresso_machine",
			props -> new BlockItem(ESPRESSO_MACHINE, props), new Item.Properties().useBlockDescriptionPrefix());
	/** Loyalty card stamps, a bitmask over {@link LoyaltyCard#STAMPS}. */
	public static final DataComponentType<Integer> STAMPS = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, id("stamps"),
			DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT).build());
	/** Marks a drink as decaf; it carries from Espresso to Latte and on, like the mug's colour. */
	public static final DataComponentType<Boolean> DECAF = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, id("decaf"),
			DataComponentType.<Boolean>builder().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());
	/** Roasted beans washed in water; the espresso machine brews them into decaf. */
	public static final Item DECAF_BEANS = registerItem("decaf_coffee_beans", Item::new, new Item.Properties());
	/** Bark from stripping jungle and acacia logs (see tools/generate-strip.js); a spice for the pumpkin spice latte. */
	public static final Item CINNAMON = registerItem("cinnamon", Item::new, new Item.Properties());
	/** Pumpkin, sugar and cinnamon, for a pumpkin spice latte. */
	public static final Item PUMPKIN_SPICE = registerItem("pumpkin_spice", Item::new, new Item.Properties());
	/** Cocoa, sugar and milk; the espresso machine brews it into hot chocolate. */
	public static final Item COCOA_MIX = registerItem("cocoa_mix", Item::new, new Item.Properties());
	public static final Item UNFIRED_CANISTER = registerItem("unfired_canister", Item::new, new Item.Properties());
	public static final Item CANISTER = registerItem("canister", props -> new BlockItem(CANISTER_BLOCK, props),
			new Item.Properties().useBlockDescriptionPrefix());
	public static final List<Item> COFFEE_BAR_ITEMS = COFFEE_BARS.stream().map(bar -> registerItem(
			BuiltInRegistries.BLOCK.getKey(bar).getPath(), props -> new BlockItem(bar, props), new Item.Properties().useBlockDescriptionPrefix())).toList();
	public static final Item GLASS_JAR = registerItem("glass_jar", props -> new BlockItem(GLASS_JAR_BLOCK, props),
			new Item.Properties().useBlockDescriptionPrefix());
	/** Rare beans from a master barista; the espresso machine brews them into Ristretto. */
	public static final Item BARISTAS_RESERVE = registerItem("baristas_reserve", Item::new, new Item.Properties().rarity(Rarity.RARE));
	public static final Item UNFIRED_MUG = registerItem("unfired_mug", Item::new, new Item.Properties());
	public static final Item MUG = registerItem("mug", Item::new, new Item.Properties().stacksTo(16));
	/** The loyalty card's reward: can't be dyed, shines, and coffee drunk from it lasts longer. */
	public static final Item GOLDEN_MUG = registerItem("golden_mug", Item::new, new Item.Properties().stacksTo(1)
			.component(DataComponents.DYED_COLOR, new DyedItemColor(Mugs.GOLD & 0xFFFFFF))
			.component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true));
	public static final Item LOYALTY_CARD = registerItem("loyalty_card", LoyaltyCard::new, new Item.Properties().stacksTo(1));
	public static final Item VANILLA_SYRUP = registerItem("vanilla_syrup", Item::new, new Item.Properties());
	public static final Item ROASTED_COFFEE_BEANS = registerItem("roasted_coffee_beans", Item::new, new Item.Properties());
	public static final Item ESPRESSO = registerItem("espresso", CoffeeItem::new, drink(1800, null));
	public static final Item LATTE = registerItem("latte", CoffeeItem::new, drink(3600, new FoodProperties(2, 0.2F, true)));
	public static final Item MOCHA_LATTE = registerItem("mocha_latte", CoffeeItem::new, drink(3600, new FoodProperties(4, 0.4F, true)));
	/** A short, strong shot from the Barista's Reserve: the only Speed II and Haste II. */
	public static final Item RISTRETTO = registerItem("ristretto", CoffeeItem::new, drink(1200, 1, null).rarity(Rarity.RARE));
	/** Autumn in a mug: the usual pick-me-up, and Warm (powder snow can't freeze you). */
	public static final Item PUMPKIN_SPICE_LATTE = registerItem("pumpkin_spice_latte", CoffeeItem::new,
			drink(3600, 0, new FoodProperties(3, 0.3F, true), new MobEffectInstance(WARM, 3600, 0)));
	/** For hot days and the Nether: thirty seconds of Fire Resistance with the coffee. */
	public static final Item ICED_COFFEE = registerItem("iced_coffee", CoffeeItem::new,
			drink(1800, 0, null, new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600, 0)));
	/** A glow berry cold brew for late nights: three minutes of Night Vision with the coffee. */
	public static final Item NIGHT_SHIFT = registerItem("night_shift", CoffeeItem::new,
			drink(1800, 0, null, new MobEffectInstance(MobEffects.NIGHT_VISION, 3600, 0)));
	/** No coffee at all: a warm cocoa that makes you Cozy, like decaf. */
	public static final Item HOT_CHOCOLATE = registerItem("hot_chocolate", CoffeeItem::new, new Item.Properties().stacksTo(16)
			.usingConvertsTo(MUG).food(new FoodProperties(4, 0.4F, true), decafConsumable()));
	/** The house special: lasts longest and fills you up the most. */
	public static final Item VANILLA_LATTE = registerItem("vanilla_latte", CoffeeItem::new, drink(4800, new FoodProperties(4, 0.6F, true)));

	/** A coffee drink: Speed I, Haste I and Caffeinated for {@code ticks}, served in a mug (which you get back). */
	private static Item.Properties drink(int ticks, FoodProperties food) {
		return drink(ticks, 0, food);
	}

	/** What a decaf drink does instead of the caffeine: three minutes of Cozy. */
	public static Consumable decafConsumable() {
		return Consumables.defaultDrink().onConsume(new ApplyStatusEffectsConsumeEffect(List.of(new MobEffectInstance(COZY, 3600, 0)))).build();
	}

	/** As above, with Speed and Haste at the given amplifier (Caffeinated is always I), plus any extra perks. */
	private static Item.Properties drink(int ticks, int amplifier, FoodProperties food, MobEffectInstance... perks) {
		List<MobEffectInstance> effects = new ArrayList<>(List.of(
				new MobEffectInstance(MobEffects.SPEED, ticks, amplifier),
				new MobEffectInstance(MobEffects.HASTE, ticks, amplifier),
				new MobEffectInstance(CAFFEINATED, ticks, 0)));
		effects.addAll(List.of(perks));
		Consumable consumable = Consumables.defaultDrink().onConsume(new ApplyStatusEffectsConsumeEffect(effects)).build();
		Item.Properties props = new Item.Properties().stacksTo(16).usingConvertsTo(MUG);
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
		Mugs.register();
		LoyaltyCard.register();
		CoffeeLoot.register();
		VillageCafe.register();
		ItemStorage.SIDED.registerForBlockEntity((canister, side) -> canister.storage, CANISTER_ENTITY);
		// A hopper under a coffee bar takes finished drinks out of the espresso machine on top.
		ItemStorage.SIDED.registerForBlocks((level, pos, state, entity, side) ->
				side == Direction.DOWN && level.getBlockEntity(pos.above()) instanceof EspressoMachineBlockEntity machine
						? ContainerStorage.of(machine, Direction.DOWN) : null,
				COFFEE_BARS.toArray(Block[]::new));
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.NATURAL_BLOCKS).register(output -> output.accept(COFFEE_CHERRIES));
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(output -> {
			output.accept(ESPRESSO_MACHINE_ITEM);
			output.accept(CANISTER);
			output.accept(GLASS_JAR);
			COFFEE_BAR_ITEMS.forEach(output::accept);
		});
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(output -> {
			output.accept(UNFIRED_MUG);
			output.accept(UNFIRED_CANISTER);
			output.accept(BARISTAS_RESERVE);
			output.accept(DECAF_BEANS);
			output.accept(CINNAMON);
			output.accept(PUMPKIN_SPICE);
			output.accept(COCOA_MIX);
			output.accept(VANILLA_SYRUP);
		});
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FOOD_AND_DRINKS).register(output -> {
			output.accept(MUG);
			output.accept(GOLDEN_MUG);
			output.accept(LOYALTY_CARD);
			output.accept(ROASTED_COFFEE_BEANS);
			output.accept(ESPRESSO);
			output.accept(LATTE);
			output.accept(MOCHA_LATTE);
			output.accept(VANILLA_LATTE);
			output.accept(RISTRETTO);
			output.accept(PUMPKIN_SPICE_LATTE);
			output.accept(ICED_COFFEE);
			output.accept(NIGHT_SHIFT);
			output.accept(HOT_CHOCOLATE);
		});
		// Wild coffee bushes in jungles and savannas.
		BiomeModifications.addFeature(BiomeSelectors.tag(BiomeTags.IS_JUNGLE).or(BiomeSelectors.tag(BiomeTags.IS_SAVANNA)),
				GenerationStep.Decoration.VEGETAL_DECORATION,
				ResourceKey.create(Registries.PLACED_FEATURE, id("patch_coffee_bush")));
		// By the water: a few more firefly bushes in temperate biomes, and lily pads on rivers and ponds.
		Predicate<BiomeSelectionContext> temperate = BiomeSelectors.tag(BiomeTags.IS_FOREST).or(BiomeSelectors.tag(BiomeTags.IS_TAIGA))
				.or(BiomeSelectors.tag(BiomeTags.IS_RIVER)).or(BiomeSelectors.tag(ConventionalBiomeTags.IS_PLAINS))
				.or(BiomeSelectors.includeByKey(Biomes.MEADOW, Biomes.CHERRY_GROVE))
				.and(BiomeSelectors.tag(ConventionalBiomeTags.IS_SNOWY).negate());
		BiomeModifications.addFeature(temperate, GenerationStep.Decoration.VEGETAL_DECORATION,
				ResourceKey.create(Registries.PLACED_FEATURE, id("waterside_firefly_bush")));
		BiomeModifications.addFeature(temperate, GenerationStep.Decoration.VEGETAL_DECORATION,
				ResourceKey.create(Registries.PLACED_FEATURE, id("waterside_lily_pads")));
		// Lusher rivers (not frozen ones): seagrass, kelp and clay in the water; plants, flowers, moss, mud
		// and the odd mossy boulder at the waterline. A river's banks are usually in the neighbouring biome,
		// so the bank features also go to the temperate land around (they only grow right by water).
		// See tools/generate-rivers.js.
		Predicate<BiomeSelectionContext> rivers = BiomeSelectors.tag(BiomeTags.IS_RIVER)
				.and(BiomeSelectors.tag(ConventionalBiomeTags.IS_SNOWY).negate())
				.and(context -> !context.getBiomeKey().equals(Biomes.FROZEN_RIVER));
		for (String feature : List.of("river_seagrass", "river_kelp", "river_clay")) {
			BiomeModifications.addFeature(rivers, GenerationStep.Decoration.VEGETAL_DECORATION, ResourceKey.create(Registries.PLACED_FEATURE, id(feature)));
		}
		for (String feature : List.of("waterline_moss", "waterline_mud", "waterline_rock")) {
			BiomeModifications.addFeature(rivers.or(temperate), GenerationStep.Decoration.LOCAL_MODIFICATIONS, ResourceKey.create(Registries.PLACED_FEATURE, id(feature)));
		}
		for (String feature : List.of("river_bank_plants", "river_bank_flowers")) {
			BiomeModifications.addFeature(rivers.or(temperate), GenerationStep.Decoration.VEGETAL_DECORATION, ResourceKey.create(Registries.PLACED_FEATURE, id(feature)));
		}
	}

	/** MobEffect's constructor is protected. */
	private static final class EspressoEffect extends MobEffect {
		EspressoEffect(MobEffectCategory category, int color) {
			super(category, color);
		}
	}
}

// Writes the espresso_data built-in data pack (recipes, advancements, starter kit,
// field guide and block-merchant trades). Edit the lists below and re-run:
//   node tools/generate-data.js
const fs = require('fs'), path = require('path');
const ROOT = path.join(__dirname, '..', 'src', 'main', 'resources', 'resourcepacks', 'espresso_data');
const out = (rel, obj) => {
	const f = path.join(ROOT, ...rel.split('/'));
	fs.mkdirSync(path.dirname(f), { recursive: true });
	fs.writeFileSync(f, typeof obj === 'string' ? obj : JSON.stringify(obj, null, 2) + '\n');
};
fs.rmSync(ROOT, { recursive: true, force: true });

out('pack.mcmeta', { pack: { description: 'Espresso Flavoured: recipes, advancements, starter kit and trades', min_format: 121, max_format: 121 } });

// ---------------------------------------------------------------- recipes
out('data/espresso/recipe/shulker_box_overworld.json', {
	type: 'minecraft:crafting_shaped', category: 'misc',
	key: { I: 'minecraft:iron_ingot', P: 'minecraft:ender_pearl', A: 'minecraft:amethyst_shard', C: 'minecraft:chest' },
	pattern: ['IPI', 'ACA', 'IPI'],
	result: { id: 'minecraft:shulker_box' },
});
out('data/espresso/recipe/bundle_from_wool.json', {
	type: 'minecraft:crafting_shaped', category: 'equipment',
	key: { '-': 'minecraft:string', '#': '#minecraft:wool' }, pattern: ['-', '#'], result: { id: 'minecraft:bundle' },
});
out('data/espresso/recipe/bundle_from_rabbit_hide.json', {
	type: 'minecraft:crafting_shaped', category: 'equipment',
	key: { '-': 'minecraft:string', '#': 'minecraft:rabbit_hide' }, pattern: ['-', '#'], result: { id: 'minecraft:bundle' },
});

// ---------------------------------------------------------------- coffee
for (const [type, suffix, time] of [['minecraft:smelting', '', 200], ['minecraft:smoking', '_from_smoking', 100], ['minecraft:campfire_cooking', '_from_campfire_cooking', 600]]) {
	out(`data/espresso/recipe/roasted_coffee_beans${suffix}.json`, {
		type, category: 'food', cookingtime: time, experience: 0.35,
		ingredient: 'espresso:coffee_cherries', result: { id: 'espresso:roasted_coffee_beans' },
	});
}
// Espresso is only brewed in the espresso machine (beans on top, mugs below).
out('data/espresso/recipe/espresso_machine.json', {
	type: 'minecraft:crafting_shaped', category: 'misc',
	pattern: ['III', 'IBI', 'SSS'],
	key: { I: 'minecraft:iron_ingot', B: 'minecraft:bucket', S: 'minecraft:smooth_stone' },
	result: { id: 'espresso:espresso_machine' },
});
out('data/espresso/loot_table/blocks/espresso_machine.json', {
	type: 'minecraft:block',
	pools: [{ rolls: 1, entries: [{ type: 'minecraft:item', name: 'espresso:espresso_machine' }],
		condition: { type: 'minecraft:survives_explosion' } }],
});
out('data/minecraft/tags/block/mineable/pickaxe.json', { values: ['espresso:espresso_machine'] });
// Mugs: shape clay, fire it, dye it like leather. Drinks keep the mug they're in.
out('data/espresso/recipe/unfired_mug.json', {
	type: 'minecraft:crafting_shaped', category: 'misc',
	pattern: ['C C', 'CCC'], key: { C: 'minecraft:clay_ball' },
	result: { id: 'espresso:unfired_mug', count: 2 },
});
out('data/espresso/recipe/mug.json', {
	type: 'minecraft:smelting', category: 'misc', cookingtime: 200, experience: 0.1,
	ingredient: 'espresso:unfired_mug', result: { id: 'espresso:mug' },
});
out('data/espresso/recipe/mug_dyed.json', {
	type: 'minecraft:crafting_dye', group: 'dyed_mug', target: 'espresso:mug', dye: '#minecraft:dyes', result: { id: 'espresso:mug' },
});
// Transmute keeps the mug's colour (and the mug you get back) from espresso to latte to mocha.
out('data/espresso/recipe/latte.json', {
	type: 'minecraft:crafting_transmute', category: 'misc',
	input: 'espresso:espresso', material: 'minecraft:milk_bucket', result: { id: 'espresso:latte' },
});
out('data/espresso/recipe/mocha_latte.json', {
	type: 'minecraft:crafting_transmute', category: 'misc',
	input: 'espresso:latte', material: 'minecraft:cocoa_beans', result: { id: 'espresso:mocha_latte' },
});
// Decaf: wash roasted beans in water (the bucket comes back empty).
out('data/espresso/recipe/decaf_coffee_beans.json', {
	type: 'minecraft:crafting_shapeless', category: 'misc',
	ingredients: ['espresso:roasted_coffee_beans', 'espresso:roasted_coffee_beans', 'espresso:roasted_coffee_beans', 'espresso:roasted_coffee_beans', 'minecraft:water_bucket'],
	result: { id: 'espresso:decaf_coffee_beans', count: 4 },
});
// The house special: honey and sugar make vanilla syrup, which turns a latte into a vanilla latte.
out('data/espresso/recipe/vanilla_syrup.json', {
	type: 'minecraft:crafting_shapeless', category: 'misc',
	ingredients: ['minecraft:honey_bottle', 'minecraft:sugar'],
	result: { id: 'espresso:vanilla_syrup', count: 2 },
});
out('data/espresso/recipe/vanilla_latte.json', {
	type: 'minecraft:crafting_transmute', category: 'misc',
	input: 'espresso:latte', material: 'espresso:vanilla_syrup', result: { id: 'espresso:vanilla_latte' },
});
// Cinnamon: stripping a jungle or acacia log sometimes drops some (one in three); the axe
// transformer that rolls this loot is written by tools/generate-strip.js.
out('data/espresso/loot_table/strip/cinnamon.json', {
	type: 'minecraft:block_interact',
	pools: [{ rolls: 1, condition: { type: 'minecraft:random_chance', chance: 0.33 },
		entries: [{ type: 'minecraft:item', name: 'espresso:cinnamon' }] }],
	random_sequence: 'espresso:strip/cinnamon',
});
// More drinks: pumpkin spice (pumpkin, sugar and cinnamon) for a seasonal latte, ice for iced coffee,
// glow berries for a night shift, and cocoa mix that the machine brews into hot chocolate.
out('data/espresso/recipe/pumpkin_spice.json', {
	type: 'minecraft:crafting_shapeless', category: 'misc',
	ingredients: ['minecraft:pumpkin', 'minecraft:sugar', 'espresso:cinnamon'], result: { id: 'espresso:pumpkin_spice', count: 4 },
});
out('data/espresso/recipe/cocoa_mix.json', {
	type: 'minecraft:crafting_shapeless', category: 'misc',
	ingredients: ['minecraft:cocoa_beans', 'minecraft:sugar', 'minecraft:milk_bucket'], result: { id: 'espresso:cocoa_mix', count: 4 },
});
for (const [name, input, material] of [['pumpkin_spice_latte', 'espresso:latte', 'espresso:pumpkin_spice'],
	['iced_coffee', 'espresso:espresso', 'minecraft:ice'], ['night_shift', 'espresso:espresso', 'minecraft:glow_berries'],
	['honey_latte', 'espresso:latte', 'minecraft:honey_bottle'], ['caramel_macchiato', 'espresso:latte', 'espresso:caramel'],
	['cold_brew', 'espresso:espresso', 'minecraft:packed_ice']]) {
	out(`data/espresso/recipe/${name}.json`, {
		type: 'minecraft:crafting_transmute', category: 'misc', input, material, result: { id: `espresso:${name}` },
	});
}
out('data/espresso/tags/item/coffee.json', { values: ['espresso:espresso', 'espresso:latte', 'espresso:mocha_latte', 'espresso:vanilla_latte',
	'espresso:pumpkin_spice_latte', 'espresso:iced_coffee', 'espresso:night_shift', 'espresso:honey_latte', 'espresso:caramel_macchiato',
	'espresso:cold_brew'] });
// Pastries: a croissant (flaky with butter, so milk), biscotti (twice-baked, small) and a berry muffin.
out('data/espresso/recipe/croissant.json', {
	type: 'minecraft:crafting_shapeless', category: 'misc',
	ingredients: ['minecraft:wheat', 'minecraft:wheat', 'minecraft:wheat', '#minecraft:eggs', 'minecraft:milk_bucket'], result: { id: 'espresso:croissant', count: 4 },
});
out('data/espresso/recipe/biscotti.json', {
	type: 'minecraft:crafting_shapeless', category: 'misc',
	ingredients: ['minecraft:wheat', 'minecraft:wheat', 'minecraft:sugar', '#minecraft:eggs'], result: { id: 'espresso:biscotti', count: 6 },
});
out('data/espresso/recipe/muffin.json', {
	type: 'minecraft:crafting_shapeless', category: 'misc',
	ingredients: ['minecraft:wheat', 'minecraft:sugar', '#minecraft:eggs', 'minecraft:sweet_berries'], result: { id: 'espresso:muffin', count: 2 },
});
// Caramel: sugar, smelted (or quicker in a smoker).
out('data/espresso/recipe/caramel.json', {
	type: 'minecraft:smelting', category: 'food', cookingtime: 200, experience: 0.1, ingredient: 'minecraft:sugar', result: { id: 'espresso:caramel' },
});
out('data/espresso/recipe/caramel_from_smoking.json', {
	type: 'minecraft:smoking', category: 'food', cookingtime: 100, experience: 0.1, ingredient: 'minecraft:sugar', result: { id: 'espresso:caramel' },
});

// Canisters: clay, fired, dyeable, like mugs.
out('data/espresso/recipe/unfired_canister.json', {
	type: 'minecraft:crafting_shaped', category: 'misc',
	pattern: ['C C', 'C C', 'CCC'], key: { C: 'minecraft:clay_ball' },
	result: { id: 'espresso:unfired_canister' },
});
out('data/espresso/recipe/canister.json', {
	type: 'minecraft:smelting', category: 'misc', cookingtime: 200, experience: 0.1,
	ingredient: 'espresso:unfired_canister', result: { id: 'espresso:canister' },
});
out('data/espresso/recipe/canister_dyed.json', {
	type: 'minecraft:crafting_dye', group: 'dyed_canister', target: 'espresso:canister', dye: '#minecraft:dyes', result: { id: 'espresso:canister' },
});

// Breaking a bush with cherries on it drops them, like a sweet berry bush.
const bushDrop = (age, min, max) => ({
	condition: { type: 'minecraft:match_block', blocks: 'espresso:coffee_bush', state: { age: String(age), half: 'lower' } },
	entries: [{ type: 'minecraft:item', name: 'espresso:coffee_cherries' }],
	modifier: [{ type: 'minecraft:set_count', count: { type: 'minecraft:uniform', min, max } }],
	rolls: 1,
});
out('data/espresso/loot_table/blocks/coffee_bush.json', {
	type: 'minecraft:block', modifier: { type: 'minecraft:explosion_decay' },
	pools: [bushDrop(4, 2, 3), bushDrop(3, 1, 2)],
});

// Wild coffee bushes (added to jungles and savannas by the mod), grown tall and ripe.
out('data/espresso/worldgen/feature/coffee_bush.json', {
	type: 'minecraft:simple_block', to_place: { id: 'espresso:coffee_bush', properties: { age: '4', half: 'lower' } },
});
out('data/espresso/worldgen/placed_feature/patch_coffee_bush.json', {
	feature: 'espresso:coffee_bush',
	placement: [
		{ type: 'minecraft:rarity_filter', chance: 12 },
		{ type: 'minecraft:in_square' },
		{ type: 'minecraft:heightmap', heightmap: 'WORLD_SURFACE_WG' },
		{ type: 'minecraft:biome' },
		{ type: 'minecraft:count', count: 48 },
		{ type: 'minecraft:offset',
			x: { type: 'minecraft:trapezoid', max: 6, min: -6, plateau: 0 },
			y: { type: 'minecraft:trapezoid', max: 3, min: -3, plateau: 0 },
			z: { type: 'minecraft:trapezoid', max: 6, min: -6, plateau: 0 } },
		{ type: 'minecraft:block_predicate_filter', predicate: { type: 'minecraft:all_of', predicates: [
			{ type: 'minecraft:matching_block_tag', tag: 'minecraft:air' },
			{ type: 'minecraft:matching_blocks', blocks: 'minecraft:grass_block', offset: [0, -1, 0] },
		] } },
	],
});

// The wandering trader always carries coffee cherries (see fair trade below), so anyone can
// start a coffee patch, however far they are from a jungle.
out('data/espresso/villager_trade/wandering_trader/coffee_cherries.json', {
	gives: { count: 3, id: 'espresso:coffee_cherries' }, max_uses: 8, reputation_discount: 0.05, wants: { id: 'minecraft:emerald' },
});

// ---------------------------------------------------------------- block merchant
// [item, how many for one emerald (or for `price` emeralds), price]
const BLOCKS = [
	['terracotta', 16], ['red_sand', 16], ['red_sandstone', 12], ['sandstone', 16],
	['mud_bricks', 16], ['packed_mud', 16], ['calcite', 12], ['tuff', 16],
	['dripstone_block', 12], ['mossy_cobblestone', 12], ['moss_block', 8], ['snow_block', 16],
	['packed_ice', 8], ['coarse_dirt', 16], ['podzol', 8], ['mycelium', 4],
	['cobbled_deepslate', 16], ['bamboo_block', 8], ['cherry_planks', 16], ['pale_oak_planks', 16],
	['prismarine', 8, 2], ['blackstone', 12, 2], ['quartz_block', 4, 2],
];
const tradeIds = [];
for (const [item, count, price = 1] of BLOCKS) {
	tradeIds.push(`espresso:wandering_trader/${item}`);
	out(`data/espresso/villager_trade/wandering_trader/${item}.json`, {
		gives: { count, id: `minecraft:${item}` },
		max_uses: 8,
		reputation_discount: 0.05,
		wants: price > 1 ? { count: price, id: 'minecraft:emerald' } : { id: 'minecraft:emerald' },
	});
}
out('data/espresso/tags/villager_trade/wandering_trader/building_blocks.json', { values: tradeIds });
out('data/espresso/trade_set/wandering_trader/building_blocks.json', {
	amount: 3,
	random_sequence: 'espresso:trade_set/wandering_trader/building_blocks',
	trades: '#espresso:wandering_trader/building_blocks',
});

// Hard-to-find decoration: plants and trimmings from biomes you may not have reached yet.
// [item, how many, emeralds]
const DECOR = [
	['cherry_sapling', 2, 1], ['pale_oak_sapling', 2, 1], ['mangrove_propagule', 2, 1], ['flowering_azalea', 2, 1],
	['spore_blossom', 1, 2], ['pitcher_plant', 1, 2], ['torchflower', 1, 2], ['open_eyeblossom', 2, 1],
	['pink_petals', 4, 1], ['wildflowers', 4, 1], ['blue_orchid', 4, 1], ['cactus_flower', 4, 1],
	['firefly_bush', 2, 1], ['small_dripleaf', 2, 1], ['pale_hanging_moss', 4, 1], ['leaf_litter', 8, 1],
	['glow_berries', 4, 1], ['sea_pickle', 4, 1], ['lily_pad', 4, 1], ['candle', 4, 1],
];
const decorIds = [];
for (const [item, count, price] of DECOR) {
	decorIds.push(`espresso:wandering_trader/decor/${item}`);
	out(`data/espresso/villager_trade/wandering_trader/decor/${item}.json`, {
		gives: { count, id: `minecraft:${item}` },
		max_uses: 6,
		reputation_discount: 0.05,
		wants: price > 1 ? { count: price, id: 'minecraft:emerald' } : { id: 'minecraft:emerald' },
	});
}
out('data/espresso/tags/villager_trade/wandering_trader/decor.json', { values: decorIds });
// Everything the block merchant sells, for House Blend.
out('data/espresso/tags/item/block_merchant.json', { values: [...BLOCKS, ...DECOR].map(([item]) => `minecraft:${item}`) });
out('data/espresso/trade_set/wandering_trader/decor.json', {
	amount: 2,
	random_sequence: 'espresso:trade_set/wandering_trader/decor',
	trades: '#espresso:wandering_trader/decor',
});

// Fair trade: the trader always sells coffee cherries to plant, and always buys your roasted
// beans back at a fair price.
out('data/espresso/villager_trade/wandering_trader/fair_trade_beans.json', {
	gives: { id: 'minecraft:emerald' }, max_uses: 16, reputation_discount: 0, wants: { count: 4, id: 'espresso:roasted_coffee_beans' },
});
out('data/espresso/tags/villager_trade/wandering_trader/fair_trade.json', {
	values: ['espresso:wandering_trader/coffee_cherries', 'espresso:wandering_trader/fair_trade_beans'],
});
out('data/espresso/trade_set/wandering_trader/fair_trade.json', {
	amount: 2,
	random_sequence: 'espresso:trade_set/wandering_trader/fair_trade',
	trades: '#espresso:wandering_trader/fair_trade',
});

// ---------------------------------------------------------------- the barista villager
// The espresso machine is a job site, so unemployed villagers can claim it.
out('data/minecraft/tags/point_of_interest_type/acquirable_job_site.json', { values: ['espresso:barista'] });
// Trades by level: [name, wants (id, count), gives (id, count), max uses, xp]. Two of each level are offered.
const E = 'minecraft:emerald';
const BARISTA_TRADES = {
	1: [
		['beans_emerald', ['espresso:roasted_coffee_beans', 10], [E, 1], 16, 2],
		['sugar_emerald', ['minecraft:sugar', 20], [E, 1], 16, 2],
		['emerald_cherries', [E, 1], ['espresso:coffee_cherries', 3], 12, 1],
		['emerald_mug', [E, 1], ['espresso:mug', 2], 12, 1],
		['emerald_biscotti', [E, 1], ['espresso:biscotti', 6], 12, 1],
		// The café bakes too: it buys flour and eggs, and sells bread.
		['wheat_emerald', ['minecraft:wheat', 20], [E, 1], 16, 2],
		['egg_emerald', ['minecraft:egg', 12], [E, 1], 16, 2],
		['emerald_bread', [E, 1], ['minecraft:bread', 6], 16, 1],
	],
	2: [
		['milk_emerald', ['minecraft:milk_bucket', 1], [E, 1], 12, 10],
		['cocoa_emerald', ['minecraft:cocoa_beans', 10], [E, 1], 16, 10],
		['emerald_espresso', [E, 2], ['espresso:espresso', 1], 12, 5],
		['emerald_canister', [E, 2], ['espresso:canister', 1], 12, 5],
		['emerald_croissant', [E, 1], ['espresso:croissant', 3], 12, 5],
		['emerald_muffin', [E, 1], ['espresso:muffin', 2], 12, 5],
		['emerald_cookie', [E, 1], ['minecraft:cookie', 8], 16, 5],
		['emerald_lantern', [E, 1], ['minecraft:lantern', 1], 12, 5],
	],
	3: [
		['honey_emerald', ['minecraft:honey_bottle', 4], [E, 1], 12, 20],
		['emerald_latte', [E, 3], ['espresso:latte', 1], 12, 10],
		['emerald_mocha', [E, 4], ['espresso:mocha_latte', 1], 12, 10],
		['emerald_glass_jar', [E, 2], ['espresso:glass_jar', 1], 12, 10],
		['sugar_cane_emerald', ['minecraft:sugar_cane', 24], [E, 1], 16, 20],
		['emerald_pumpkin_pie', [E, 1], ['minecraft:pumpkin_pie', 4], 12, 10],
		['emerald_candle', [E, 1], ['minecraft:candle', 4], 12, 10],
		['emerald_flower_pot', [E, 1], ['minecraft:flower_pot', 3], 12, 10],
	],
	4: [
		['glow_berries_emerald', ['minecraft:glow_berries', 12], [E, 1], 12, 30],
		['emerald_vanilla_latte', [E, 5], ['espresso:vanilla_latte', 1], 12, 15],
		['emerald_name_tag', [E, 20], ['minecraft:name_tag', 1], 6, 15],
		['emerald_experience_bottle', [E, 3], ['minecraft:experience_bottle', 1], 12, 15],
		['emerald_cake', [E, 3], ['minecraft:cake', 1], 12, 15],
		['emerald_painting', [E, 1], ['minecraft:painting', 3], 12, 15],
	],
	5: [
		// Something for the café's corner: the music box version of Creator.
		['emerald_music_disc', [E, 12], ['minecraft:music_disc_creator_music_box', 1], 2, 30],
		['emerald_baristas_reserve', [E, 8], ['espresso:baristas_reserve', 1], 6, 30],
		['emerald_golden_apple', [E, 10], ['minecraft:golden_apple', 1], 4, 30],
		// Bean Coins (one for each Espresso advancement) buy the Golden Mug and a couple of treats.
		['bean_coins_golden_mug', ['espresso:bean_coin', 8], ['espresso:golden_mug', 1], 1, 0],
		['bean_coin_baristas_reserve', ['espresso:bean_coin', 1], ['espresso:baristas_reserve', 3], 4, 0],
		['bean_coins_experience_bottles', ['espresso:bean_coin', 2], ['minecraft:experience_bottle', 16], 4, 0],
	],
};
// A master shows every trade, so the Bean Coin ones are always there.
const SHOW_ALL = new Set(['5']);
const item = ([id, count]) => (count > 1 ? { count, id } : { id });
for (const [level, trades] of Object.entries(BARISTA_TRADES)) {
	const ids = [];
	for (const [name, wants, gives, maxUses, xp] of trades) {
		ids.push(`espresso:barista/${level}/${name}`);
		out(`data/espresso/villager_trade/barista/${level}/${name}.json`, {
			// Coin prices stay put; everything else gets cheaper as the barista likes you more.
			gives: item(gives), max_uses: maxUses, reputation_discount: wants[0] === 'espresso:bean_coin' ? 0 : 0.05, wants: item(wants),
			...(xp ? { xp } : {}),
		});
	}
	out(`data/espresso/tags/villager_trade/barista/level_${level}.json`, { values: ids });
	out(`data/espresso/trade_set/barista/level_${level}.json`, {
		amount: SHOW_ALL.has(level) ? trades.length : 2, random_sequence: `espresso:trade_set/barista/level_${level}`, trades: `#espresso:barista/level_${level}`,
	});
}

// Coffee bars: a smooth stone counter on a plank cabinet, one for every wood.
const COFFEE_BARS = ['oak', 'spruce', 'birch', 'jungle', 'acacia', 'dark_oak', 'mangrove', 'cherry', 'pale_oak', 'poplar', 'bamboo', 'crimson', 'warped'];
for (const wood of COFFEE_BARS) {
	out(`data/espresso/recipe/${wood}_coffee_bar.json`, {
		type: 'minecraft:crafting_shaped', category: 'building', group: 'coffee_bar',
		pattern: ['SSS', 'P P', 'PPP'], key: { S: 'minecraft:smooth_stone_slab', P: `minecraft:${wood}_planks` },
		result: { id: `espresso:${wood}_coffee_bar`, count: 2 },
	});
	out(`data/espresso/loot_table/blocks/${wood}_coffee_bar.json`, {
		type: 'minecraft:block',
		pools: [{ rolls: 1, entries: [{ type: 'minecraft:item', name: `espresso:${wood}_coffee_bar` }], condition: { type: 'minecraft:survives_explosion' } }],
	});
}
out('data/minecraft/tags/block/mineable/axe.json', { values: COFFEE_BARS.map(w => `espresso:${w}_coffee_bar`) });

// ---------------------------------------------------------------- recipe book
// Like vanilla, recipes show up in the recipe book once you pick up something they use.
const unlock = (name, items, recipes) => {
	const criteria = Object.fromEntries(items.map((item, i) => [`has_item_${i}`, {
		trigger: 'minecraft:inventory_changed', conditions: { items: [{ items: item }] },
	}]));
	criteria.has_the_recipe = { trigger: 'minecraft:recipe_unlocked', conditions: { recipes: `espresso:${recipes[0]}` } };
	out(`data/espresso/advancement/recipes/${name}.json`, {
		parent: 'minecraft:recipes/root',
		criteria,
		requirements: [Object.keys(criteria)],
		rewards: { recipes: recipes.map(r => `espresso:${r}`) },
	});
};
unlock('roasted_coffee_beans', ['espresso:coffee_cherries'],
	['roasted_coffee_beans', 'roasted_coffee_beans_from_smoking', 'roasted_coffee_beans_from_campfire_cooking']);
unlock('espresso_machine', ['espresso:roasted_coffee_beans'], ['espresso_machine', 'decaf_coffee_beans']);
unlock('coffee_bars', ['minecraft:smooth_stone_slab', 'espresso:espresso_machine'], COFFEE_BARS.map(w => `${w}_coffee_bar`));
unlock('clay_pottery', ['minecraft:clay_ball'], ['unfired_mug', 'unfired_canister']);
unlock('mug', ['espresso:unfired_mug'], ['mug']);
unlock('canister', ['espresso:unfired_canister'], ['canister']);
unlock('mug_dyed', ['espresso:mug'], ['mug_dyed']);
unlock('canister_dyed', ['espresso:canister'], ['canister_dyed']);
unlock('latte', ['espresso:espresso'], ['latte', 'iced_coffee', 'night_shift', 'cold_brew']);
unlock('latte_drinks', ['espresso:latte'], ['mocha_latte', 'vanilla_latte', 'pumpkin_spice_latte', 'honey_latte', 'caramel_macchiato']);
unlock('caramel', ['minecraft:sugar'], ['caramel', 'caramel_from_smoking']);
unlock('pastries', ['minecraft:wheat', 'minecraft:sugar'], ['croissant', 'biscotti', 'muffin']);
unlock('pumpkin_spice', ['minecraft:pumpkin', 'espresso:cinnamon'], ['pumpkin_spice']);
unlock('cocoa_mix', ['minecraft:cocoa_beans'], ['cocoa_mix']);
unlock('vanilla_syrup', ['minecraft:honey_bottle', 'minecraft:sugar'], ['vanilla_syrup']);
unlock('glass_jar', ['minecraft:glass'], ['glass_jar']);
unlock('shulker_box_overworld', ['minecraft:ender_pearl', 'minecraft:amethyst_shard'], ['shulker_box_overworld']);
unlock('bundles', ['#minecraft:wool', 'minecraft:rabbit_hide'], ['bundle_from_wool', 'bundle_from_rabbit_hide']);

// Glass jars: glass with a wooden lid.
out('data/espresso/recipe/glass_jar.json', {
	type: 'minecraft:crafting_shaped', category: 'misc',
	pattern: [' S ', 'G G', 'GGG'], key: { S: '#minecraft:wooden_slabs', G: 'minecraft:glass' },
	result: { id: 'espresso:glass_jar', count: 2 },
});

// ---------------------------------------------------------------- field guide
const PAGES = [
	'Espresso Flavoured\nField Notes\n\nWelcome! This world is a little kinder to builders, so you can spend less time worrying and more time making something you love.\n\n- Gabby',
	'Your Safe Haven\n\nCrouch and use a bed with an empty hand to make it your home. Monsters won\'t spawn within 32 blocks of it.\n\nSleeping elsewhere won\'t move it. Break the bed and the protection goes. Only a night at home leaves you truly rested.',
	'Builds Stay Built\n\nCreepers still bite, but their blasts no longer break blocks.\n\nEndermen leave your walls alone, and phantoms never visit, so rest whenever you like.',
	'Coffee Corner\n\nRoast coffee cherries, then brew them in an espresso machine on a coffee bar. Mugs are fired clay.\n\nAway two days? Only your own bed leaves you rested. Decaf helps you sleep well.',
	'If You Fall\n\nPlaying the Espresso Flavoured pack? Your things wait for you in a grave where you fell. Head back, open it, and carry on.\n\nHappy building!',
];
out('data/espresso/loot_table/field_guide.json', {
	type: 'minecraft:command',
	pools: [{ rolls: 1, entries: [{ type: 'minecraft:item', name: 'minecraft:written_book', modifier: [{
		type: 'minecraft:set_components',
		components: { 'minecraft:written_book_content': { title: 'Field Notes', author: 'Gabby', pages: PAGES, resolved: true } },
	}] }] }],
});
out('data/espresso/loot_table/starter_bundle.json', {
	type: 'minecraft:command',
	pools: [{ rolls: 1, entries: [{ type: 'minecraft:item', name: 'minecraft:bundle', modifier: [{
		type: 'minecraft:set_components',
		components: { 'minecraft:bundle_contents': [
			{ id: 'minecraft:torch', count: 16 },
			{ id: 'minecraft:oak_sapling', count: 4 },
			{ id: 'minecraft:bread', count: 6 },
		] },
	}] }] }],
});
out('data/espresso/function/starter_kit.mcfunction', [
	'# A warm welcome on a player\'s first join: the field guide, a bundle of essentials and a bed.',
	'loot give @s loot espresso:field_guide',
	'loot give @s loot espresso:starter_bundle',
	'give @s minecraft:white_bed',
	'advancement grant @s only espresso:guide/field_notes',
	'',
].join('\n'));

// ---------------------------------------------------------------- advancements
const adv = (id, parent, icon, title, description, criteria, extra = {}, rewards) => out(`data/espresso/advancement/${id}.json`, {
	...(parent ? { parent: `espresso:${parent}` } : {}),
	criteria,
	display: { icon: { id: icon }, title, description, ...extra },
	requirements: Object.keys(criteria).map(k => [k]),
	...(rewards ? { rewards } : {}),
});
adv('guide/root', null, 'minecraft:cocoa_beans', 'Espresso Flavoured', 'A calmer, more productive survival world',
	{ joined: { trigger: 'minecraft:tick' } },
	{ background: 'minecraft:block/stripped_dark_oak_log', announce_to_chat: false, show_toast: false });
adv('guide/field_notes', 'guide/root', 'minecraft:written_book', 'Field Notes', 'Get your copy of the Espresso Flavoured field guide',
	{ received: { trigger: 'minecraft:impossible' } });
adv('guide/first_shot', 'guide/root', 'minecraft:oak_door', 'First Shot', 'Place a door. Every home starts somewhere',
	{ door: { trigger: 'minecraft:placed_block', conditions: { location: { type: 'minecraft:match_block', blocks: '#minecraft:doors' } } } });
adv('guide/safe_haven', 'guide/first_shot', 'minecraft:white_bed', 'Safe Haven', 'Crouch and use a bed with an empty hand to make it your home',
	{ home: { trigger: 'minecraft:impossible' } });
adv('guide/pocket_shot', 'guide/root', 'minecraft:bundle', 'Pocket Shot', 'Get a bundle',
	{ bundle: { trigger: 'minecraft:inventory_changed', conditions: { items: [{ items: 'minecraft:bundle' }] } } });
adv('guide/refill', 'guide/pocket_shot', 'minecraft:shulker_box', 'Refill', 'Craft a shulker box without leaving the Overworld',
	{ crafted: { trigger: 'minecraft:recipe_crafted', conditions: { recipes: 'espresso:shulker_box_overworld' } } },
	{ frame: 'goal' });
adv('guide/house_blend', 'guide/root', 'minecraft:terracotta', 'House Blend', 'Buy building blocks or decorations from the wandering trader',
	{ traded: { trigger: 'minecraft:villager_trade', conditions: {
		villager: { type: 'minecraft:entity_properties', entity: 'this', predicate: { 'minecraft:entity_type': 'minecraft:wandering_trader' } },
		item: { items: '#espresso:block_merchant' } } } });

// Café life: sitting down, rest spots out in the wild, and village cafés.
adv('guide/take_a_seat', 'guide/root', 'minecraft:oak_stairs', 'Take a Seat', 'Sit down on a bench or chair',
	{ sat: { trigger: 'minecraft:impossible' } });
adv('guide/scenic_route', 'guide/take_a_seat', 'minecraft:lantern', 'Scenic Route', 'Stop at a rest spot out in the wild',
	{ visited: { trigger: 'minecraft:location', conditions: {
		player: { type: 'minecraft:entity_properties', entity: 'this', predicate: { location: { structures: 'espresso:rest_spot' } } } } } });
adv('guide/local_favourite', 'guide/take_a_seat', 'minecraft:chest', 'Local Favourite', 'Find a village café and open its supply chest',
	{ found: { trigger: 'minecraft:player_generates_container_loot', conditions: { loot_tables: ['espresso:chests/village_cafe'] } } });

adv('guide/barista', 'guide/root', 'espresso:espresso_machine', 'Barista', 'Craft an espresso machine',
	{ crafted: { trigger: 'minecraft:recipe_crafted', conditions: { recipes: 'espresso:espresso_machine' } } });
adv('guide/counter_culture', 'guide/barista', 'espresso:oak_coffee_bar', 'Counter Culture', 'Place a coffee bar',
	{ placed: { trigger: 'minecraft:placed_block', conditions: { location: { type: 'minecraft:match_block', blocks: COFFEE_BARS.map(w => `espresso:${w}_coffee_bar`) } } } });
adv('guide/stocked_up', 'guide/counter_culture', 'espresso:glass_jar', 'Stocked Up', 'Put something in a canister or glass jar',
	{ stocked: { trigger: 'minecraft:impossible' } });
adv('guide/your_usual', 'guide/barista', 'minecraft:emerald', 'Your Usual', 'Trade with a barista',
	{ traded: { trigger: 'minecraft:impossible' } });
adv('guide/mug_shot', 'guide/barista', 'espresso:mug', 'Mug Shot', 'Dye a mug',
	{ dyed: { trigger: 'minecraft:recipe_crafted', conditions: { recipes: 'espresso:mug_dyed' } } });
adv('guide/first_sip', 'guide/barista', 'espresso:espresso', 'First Sip', 'Drink your first coffee',
	{ sipped: { trigger: 'minecraft:consume_item', conditions: { item: { items: '#espresso:coffee' } } } });
adv('guide/sweet_dreams', 'guide/first_sip', 'minecraft:white_bed', 'Sweet Dreams', 'Sleep after a decaf and wake up Well Rested',
	{ rested: { trigger: 'minecraft:impossible' } });
adv('guide/perfect_pairing', 'guide/first_sip', 'espresso:croissant', 'Perfect Pairing', 'Eat a pastry with your coffee (or cocoa)',
	{ paired: { trigger: 'minecraft:impossible' } });
adv('guide/house_special', 'guide/first_sip', 'espresso:vanilla_latte', 'House Special', 'Make a vanilla latte',
	{ made: { trigger: 'minecraft:recipe_crafted', conditions: { recipes: 'espresso:vanilla_latte' } } });
adv('guide/full_card', 'guide/house_special', 'espresso:golden_mug', 'On the House', 'Buy the Golden Mug from a master barista with Bean Coins',
	{ redeemed: { trigger: 'minecraft:villager_trade', conditions: { item: { items: 'espresso:golden_mug' } } } }, { frame: 'challenge' });
adv('guide/fair_trade', 'guide/first_sip', 'espresso:roasted_coffee_beans', 'Fair Trade', 'Sell roasted coffee beans to the wandering trader',
	// Awarded by EspressoAdvancements.traded: the vanilla trigger only sees what you got, not what you sold.
	{ sold: { trigger: 'minecraft:impossible' } });

// Gives the starter kit once, the first time a player joins.
out('data/espresso/advancement/starter_kit.json', {
	criteria: { joined: { trigger: 'minecraft:tick' } },
	requirements: [['joined']],
	rewards: { function: 'espresso:starter_kit' },
});

// ---------------------------------------------------------------- coffee out in the world
// A loot entry: name, weight, and a count range.
const loot = (name, weight, min = 1, max = min) => ({
	type: 'minecraft:item', name, weight,
	...(max > 1 ? { modifier: { type: 'minecraft:set_count', count: { type: 'minecraft:uniform', min, max } } } : {}),
});
const nothing = (weight) => ({ type: 'minecraft:empty', weight });
// The village café's chest: café supplies.
out('data/espresso/loot_table/chests/village_cafe.json', {
	type: 'minecraft:chest',
	pools: [{ rolls: { type: 'minecraft:uniform', min: 3, max: 6 }, entries: [
		loot('espresso:coffee_cherries', 10, 2, 6), loot('espresso:roasted_coffee_beans', 8, 2, 5), loot('minecraft:sugar', 6, 2, 5),
		loot('espresso:mug', 4), loot('espresso:decaf_coffee_beans', 3, 1, 3), loot('espresso:cinnamon', 3, 1, 3), loot('espresso:cocoa_mix', 3, 1, 2),
		loot('espresso:vanilla_syrup', 2), loot('minecraft:milk_bucket', 2), loot('minecraft:emerald', 2, 1, 2),
		loot('espresso:croissant', 4, 1, 3), loot('espresso:muffin', 3, 1, 2), loot('espresso:biscotti', 3, 2, 6), loot('espresso:caramel', 2, 1, 3),
	] }],
	random_sequence: 'espresso:chests/village_cafe',
});
// Pools added to vanilla chests (by CoffeeLoot.java), so coffee turns up while exploring.
// Kept outside the data pack: they're merged into the vanilla tables, not loaded as tables.
const villageCoffee = [{ rolls: 1, entries: [loot('espresso:coffee_cherries', 2, 1, 4), loot('espresso:roasted_coffee_beans', 1, 1, 3), nothing(4)] }];
const inject = {
	...Object.fromEntries(['plains', 'desert', 'savanna', 'taiga', 'snowy'].map(b => [`minecraft:chests/village/village_${b}_house`, villageCoffee])),
	'minecraft:chests/jungle_temple': [{ rolls: 1, entries: [loot('espresso:coffee_cherries', 4, 3, 8), loot('espresso:roasted_coffee_beans', 2, 2, 5),
		loot('espresso:baristas_reserve', 1), nothing(3)] }],
	'minecraft:chests/shipwreck_supply': [{ rolls: 1, entries: [loot('espresso:roasted_coffee_beans', 2, 2, 6), loot('espresso:coffee_cherries', 1, 2, 5), nothing(4)] }],
};
fs.writeFileSync(path.join(__dirname, '..', 'src', 'main', 'resources', 'espresso', 'loot_inject.json'), JSON.stringify(inject, null, 2) + '\n');

// The village café buildings.
require('./generate-cafe.js');
// Rest spots in the wild, and waterside plants.
require('./generate-rest-spots.js');
// Lusher rivers.
require('./generate-rivers.js');

console.log('espresso_data written to', ROOT);

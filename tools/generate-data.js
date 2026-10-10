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
		conditions: [{ condition: 'minecraft:survives_explosion' }] }],
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
out('data/espresso/tags/item/coffee.json', { values: ['espresso:espresso', 'espresso:latte', 'espresso:mocha_latte', 'espresso:vanilla_latte'] });

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

// The loyalty card comes with the Barista advancement; a full card buys the Golden Mug.
out('data/espresso/loot_table/loyalty_card.json', {
	type: 'minecraft:command',
	pools: [{ rolls: 1, entries: [{ type: 'minecraft:item', name: 'espresso:loyalty_card' }] }],
});

// Breaking a bush with cherries on it drops them, like a sweet berry bush.
const bushDrop = (age, min, max) => ({
	condition: { type: 'minecraft:match_block', blocks: 'espresso:coffee_bush', state: { age: String(age) } },
	entries: [{ type: 'minecraft:item', name: 'espresso:coffee_cherries' }],
	modifier: [{ type: 'minecraft:set_count', count: { type: 'minecraft:uniform', min, max } }],
	rolls: 1,
});
out('data/espresso/loot_table/blocks/coffee_bush.json', {
	type: 'minecraft:block', modifier: { type: 'minecraft:explosion_decay' },
	pools: [bushDrop(3, 2, 3), bushDrop(2, 1, 2)],
});

// Wild coffee bushes (added to jungles and savannas by the mod).
out('data/espresso/worldgen/feature/coffee_bush.json', {
	type: 'minecraft:simple_block', to_place: { id: 'espresso:coffee_bush', properties: { age: '3' } },
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
out('data/espresso/villager_trade/wandering_trader/golden_mug.json', {
	gives: { id: 'espresso:golden_mug' }, max_uses: 1, reputation_discount: 0,
	wants: { id: 'espresso:loyalty_card', components: { 'espresso:stamps': 31 } },
});
out('data/espresso/tags/villager_trade/wandering_trader/fair_trade.json', {
	values: ['espresso:wandering_trader/coffee_cherries', 'espresso:wandering_trader/fair_trade_beans', 'espresso:wandering_trader/golden_mug'],
});
out('data/espresso/trade_set/wandering_trader/fair_trade.json', {
	amount: 3,
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
	],
	2: [
		['milk_emerald', ['minecraft:milk_bucket', 1], [E, 1], 12, 10],
		['cocoa_emerald', ['minecraft:cocoa_beans', 10], [E, 1], 16, 10],
		['emerald_espresso', [E, 2], ['espresso:espresso', 1], 12, 5],
		['emerald_canister', [E, 2], ['espresso:canister', 1], 12, 5],
	],
	3: [
		['honey_emerald', ['minecraft:honey_bottle', 4], [E, 1], 12, 20],
		['emerald_latte', [E, 3], ['espresso:latte', 1], 12, 10],
		['emerald_mocha', [E, 4], ['espresso:mocha_latte', 1], 12, 10],
		['emerald_glass_jar', [E, 2], ['espresso:glass_jar', 1], 12, 10],
	],
	4: [
		['glow_berries_emerald', ['minecraft:glow_berries', 12], [E, 1], 12, 30],
		['emerald_vanilla_latte', [E, 5], ['espresso:vanilla_latte', 1], 12, 15],
		['emerald_name_tag', [E, 20], ['minecraft:name_tag', 1], 6, 15],
		['emerald_experience_bottle', [E, 3], ['minecraft:experience_bottle', 1], 12, 15],
	],
	5: [
		['emerald_baristas_reserve', [E, 8], ['espresso:baristas_reserve', 1], 6, 30],
		['emerald_golden_apple', [E, 10], ['minecraft:golden_apple', 1], 4, 30],
	],
};
const item = ([id, count]) => (count > 1 ? { count, id } : { id });
for (const [level, trades] of Object.entries(BARISTA_TRADES)) {
	const ids = [];
	for (const [name, wants, gives, maxUses, xp] of trades) {
		ids.push(`espresso:barista/${level}/${name}`);
		out(`data/espresso/villager_trade/barista/${level}/${name}.json`, {
			gives: item(gives), max_uses: maxUses, reputation_discount: 0.05, wants: item(wants), xp,
		});
	}
	out(`data/espresso/tags/villager_trade/barista/level_${level}.json`, { values: ids });
	out(`data/espresso/trade_set/barista/level_${level}.json`, {
		amount: 2, random_sequence: `espresso:trade_set/barista/level_${level}`, trades: `#espresso:barista/level_${level}`,
	});
}

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
adv('guide/house_blend', 'guide/root', 'minecraft:terracotta', 'House Blend', 'Buy building blocks from the wandering trader',
	{ traded: { trigger: 'minecraft:villager_trade', conditions: { villager: { type: 'minecraft:entity_properties', entity: 'this', predicate: { 'minecraft:entity_type': 'minecraft:wandering_trader' } } } } });

adv('guide/barista', 'guide/root', 'espresso:espresso_machine', 'Barista', 'Craft an espresso machine (and get a loyalty card)',
	{ crafted: { trigger: 'minecraft:recipe_crafted', conditions: { recipes: 'espresso:espresso_machine' } } },
	{}, { loot: ['espresso:loyalty_card'] });
adv('guide/mug_shot', 'guide/barista', 'espresso:mug', 'Mug Shot', 'Dye a mug',
	{ dyed: { trigger: 'minecraft:recipe_crafted', conditions: { recipes: 'espresso:mug_dyed' } } });
adv('guide/first_sip', 'guide/barista', 'espresso:espresso', 'First Sip', 'Drink a coffee you brewed yourself',
	{ sipped: { trigger: 'minecraft:consume_item', conditions: { item: { items: '#espresso:coffee' } } } });
adv('guide/sweet_dreams', 'guide/first_sip', 'minecraft:white_bed', 'Sweet Dreams', 'Sleep after a decaf and wake up Well Rested',
	{ rested: { trigger: 'minecraft:impossible' } });
adv('guide/house_special', 'guide/first_sip', 'espresso:vanilla_latte', 'House Special', 'Make a vanilla latte',
	{ made: { trigger: 'minecraft:recipe_crafted', conditions: { recipes: 'espresso:vanilla_latte' } } });
adv('guide/full_card', 'guide/house_special', 'espresso:golden_mug', 'On the House', 'Trade a full loyalty card for the Golden Mug',
	{ redeemed: { trigger: 'minecraft:villager_trade', conditions: { item: { items: 'espresso:golden_mug' } } } }, { frame: 'challenge' });
adv('guide/fair_trade', 'guide/first_sip', 'espresso:roasted_coffee_beans', 'Fair Trade', 'Sell roasted coffee beans to the wandering trader',
	{ sold: { trigger: 'minecraft:villager_trade', conditions: {
		villager: { type: 'minecraft:entity_properties', entity: 'this', predicate: { 'minecraft:entity_type': 'minecraft:wandering_trader' } },
		item: { items: 'minecraft:emerald' } } } });

// Gives the starter kit once, the first time a player joins.
out('data/espresso/advancement/starter_kit.json', {
	criteria: { joined: { trigger: 'minecraft:tick' } },
	requirements: [['joined']],
	rewards: { function: 'espresso:starter_kit' },
});

console.log('espresso_data written to', ROOT);

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
// Espresso is only brewed in the espresso machine (beans in, glass bottle for a shot).
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
out('data/espresso/recipe/latte.json', {
	type: 'minecraft:crafting_shapeless', category: 'misc',
	ingredients: ['espresso:espresso', 'espresso:espresso', 'espresso:espresso', 'minecraft:milk_bucket'],
	result: { id: 'espresso:latte', count: 3 },
});
out('data/espresso/recipe/mocha_latte.json', {
	type: 'minecraft:crafting_shapeless', category: 'misc',
	ingredients: ['espresso:latte', 'minecraft:cocoa_beans'],
	result: { id: 'espresso:mocha_latte' },
});
out('data/espresso/tags/item/coffee.json', { values: ['espresso:espresso', 'espresso:latte', 'espresso:mocha_latte'] });

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
out('data/espresso/tags/villager_trade/wandering_trader/fair_trade.json', {
	values: ['espresso:wandering_trader/coffee_cherries', 'espresso:wandering_trader/fair_trade_beans'],
});
out('data/espresso/trade_set/wandering_trader/fair_trade.json', {
	amount: 2,
	random_sequence: 'espresso:trade_set/wandering_trader/fair_trade',
	trades: '#espresso:wandering_trader/fair_trade',
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
const adv = (id, parent, icon, title, description, criteria, extra = {}) => out(`data/espresso/advancement/${id}.json`, {
	...(parent ? { parent: `espresso:${parent}` } : {}),
	criteria,
	display: { icon: { id: icon }, title, description, ...extra },
	requirements: Object.keys(criteria).map(k => [k]),
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

adv('guide/barista', 'guide/root', 'espresso:espresso_machine', 'Barista', 'Craft an espresso machine',
	{ crafted: { trigger: 'minecraft:recipe_crafted', conditions: { recipes: 'espresso:espresso_machine' } } });
adv('guide/first_sip', 'guide/barista', 'espresso:espresso', 'First Sip', 'Drink a coffee you brewed yourself',
	{ sipped: { trigger: 'minecraft:consume_item', conditions: { item: { items: '#espresso:coffee' } } } });
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

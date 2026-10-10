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

// ---------------------------------------------------------------- field guide
const PAGES = [
	'Espresso Flavoured\nField Notes\n\nWelcome! This world is a little kinder to builders, so you can spend less time worrying and more time making something you love.\n\n- Gabby',
	'Your Safe Haven\n\nCrouch and use a bed with an empty hand to make it your home. Monsters won\'t spawn within 32 blocks of it.\n\nSleeping elsewhere won\'t move it. Break the bed and the protection goes.',
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

// Gives the starter kit once, the first time a player joins.
out('data/espresso/advancement/starter_kit.json', {
	criteria: { joined: { trigger: 'minecraft:tick' } },
	requirements: [['joined']],
	rewards: { function: 'espresso:starter_kit' },
});

console.log('espresso_data written to', ROOT);

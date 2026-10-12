// Writes the wild rest spots (a bench, a lamp post and a little planting, found now and then
// in meadows, plains and forests) and the waterside plants (more firefly bushes by water, lily
// pads on rivers and ponds), into the espresso_data pack. The mod adds the plants to biomes.
// Run by generate-data.js (which clears the pack first).
const fs = require('fs'), path = require('path');
const { Structure, stairs, fence } = require('./structure.js');
const DATA = path.join(__dirname, '..', 'src', 'main', 'resources', 'resourcepacks', 'espresso_data', 'data');
const out = (rel, obj) => {
	const f = path.join(DATA, ...rel.split('/'));
	fs.mkdirSync(path.dirname(f), { recursive: true });
	fs.writeFileSync(f, Buffer.isBuffer(obj) ? obj : JSON.stringify(obj, null, 2) + '\n');
};

const WOODS = ['oak', 'birch', 'spruce'];
const GRASS = 'minecraft:grass_block[snowy=false]';
const AIR = 'minecraft:air';

// A rounded patch of lawn, cleared of tall grass above, so each spot sits neatly in the land.
function lawn(spot) {
	for (let x = 0; x <= 8; x++) for (let z = 0; z <= 8; z++) {
		if ((x - 4) ** 2 + (z - 4) ** 2 > 20) continue;
		spot.set(x, 0, z, GRASS);
		for (let y = 1; y <= 7; y++) spot.set(x, y, z, AIR);
	}
}
// Three stairs with trapdoor armrests. The bench's back is on the `back` side.
function bench(spot, x0, z, back, wood) {
	for (let x = x0; x < x0 + 3; x++) spot.set(x, 1, z, stairs(`minecraft:${wood}`, back));
	const arm = (facing) => `minecraft:${wood}_trapdoor[facing=${facing},half=bottom,open=true,powered=false,waterlogged=false]`;
	spot.set(x0 - 1, 1, z, arm('west'));
	spot.set(x0 + 3, 1, z, arm('east'));
}
// A lamp post: three fence posts, an arm reaching towards `toward`, and a lantern hanging from it.
function lampPost(spot, x, z, toward, wood) {
	for (let y = 1; y <= 3; y++) spot.set(x, y, z, fence(`minecraft:${wood}`));
	const [dx, dz] = { east: [1, 0], west: [-1, 0], north: [0, -1], south: [0, 1] }[toward];
	const back = { east: 'west', west: 'east', north: 'south', south: 'north' }[toward];
	spot.set(x, 4, z, fence(`minecraft:${wood}`, [toward]));
	spot.set(x + dx, 4, z + dz, fence(`minecraft:${wood}`, [back]));
	spot.set(x + dx, 3, z + dz, 'minecraft:lantern[hanging=true,waterlogged=false]');
}

// By a pond: a bench looking over a little pond with lily pads, cane and a firefly bush.
function pondSpot(wood) {
	const spot = new Structure([9, 8, 9]);
	lawn(spot);
	for (const [x, z] of [[3, 1], [4, 1], [5, 1], [2, 2], [3, 2], [4, 2], [5, 2], [6, 2], [3, 3], [4, 3], [5, 3]]) {
		spot.set(x, 0, z, 'minecraft:water[level=0]');
	}
	spot.set(3, 1, 1, 'minecraft:lily_pad');
	spot.set(5, 1, 3, 'minecraft:lily_pad');
	spot.set(2, 1, 1, 'minecraft:sugar_cane[age=0]');
	spot.set(2, 2, 1, 'minecraft:sugar_cane[age=0]');
	spot.set(7, 1, 2, 'minecraft:firefly_bush');
	spot.set(1, 1, 3, 'minecraft:firefly_bush');
	bench(spot, 3, 5, 'south', wood); // looking north, over the water
	lampPost(spot, 7, 6, 'west', wood);
	spot.set(1, 1, 5, 'minecraft:poppy');
	spot.set(2, 1, 6, 'minecraft:oxeye_daisy');
	spot.set(7, 1, 4, 'minecraft:cornflower');
	spot.set(6, 1, 7, 'minecraft:allium');
	return spot.toNbt();
}

// Under a tree: a bench in its shade, a lamp post beside it and flowers round the trunk.
function treeSpot(wood) {
	const spot = new Structure([9, 9, 9]);
	lawn(spot);
	const leaves = `minecraft:${wood}_leaves[distance=1,persistent=false,waterlogged=false]`;
	const [tx, tz] = [4, 3];
	for (let y = 4; y <= 7; y++) {
		const r = y <= 5 ? 2 : 1;
		for (let x = tx - r; x <= tx + r; x++) for (let z = tz - r; z <= tz + r; z++) {
			const corner = Math.abs(x - tx) === r && Math.abs(z - tz) === r;
			if (corner && (r === 2 || y === 7)) continue;
			spot.set(x, y, z, leaves);
		}
	}
	for (let y = 1; y <= 5; y++) spot.set(tx, y, tz, `minecraft:${wood}_log[axis=y]`);
	bench(spot, 3, 5, 'north', wood); // its back to the tree
	lampPost(spot, 7, 5, 'west', wood);
	spot.set(3, 1, 3, 'minecraft:dandelion');
	spot.set(5, 1, 2, 'minecraft:poppy');
	spot.set(5, 1, 3, 'minecraft:azure_bluet');
	spot.set(1, 1, 6, 'minecraft:oxeye_daisy');
	return spot.toNbt();
}

const elements = [];
for (const wood of WOODS) {
	for (const [name, build] of [['pond', pondSpot], ['tree', treeSpot]]) {
		out(`espresso_flavoured/structure/rest_spot/${name}_${wood}.nbt`, build(wood));
		elements.push({ weight: 1, element: { element_type: 'minecraft:single_pool_element', location: `espresso_flavoured:rest_spot/${name}_${wood}`,
			processors: 'minecraft:empty', projection: 'rigid' } });
	}
}
out('espresso_flavoured/worldgen/template_pool/rest_spot.json', { fallback: 'minecraft:empty', elements });
out('espresso_flavoured/worldgen/structure/rest_spot.json', {
	type: 'minecraft:jigsaw', biomes: '#espresso_flavoured:has_structure/rest_spot', step: 'surface_structures',
	terrain_adaptation: 'beard_thin', spawn_overrides: {}, start_pool: 'espresso_flavoured:rest_spot', size: 1,
	// The lawn replaces the top layer of ground.
	start_height: { absolute: -1 }, project_start_to_heightmap: 'WORLD_SURFACE_WG', max_distance_from_center: 16, use_expansion_hack: false,
});
// About one per 18 x 18 chunks, roughly every 300 blocks (where the biome suits).
out('espresso_flavoured/worldgen/structure_set/rest_spots.json', {
	structures: [{ structure: 'espresso_flavoured:rest_spot', weight: 1 }],
	placement: { type: 'minecraft:random_spread', spacing: 18, separation: 6, salt: 20261010 },
});
out('espresso_flavoured/tags/worldgen/biome/has_structure/rest_spot.json', { values: [
	'minecraft:plains', 'minecraft:sunflower_plains', 'minecraft:meadow', 'minecraft:forest', 'minecraft:flower_forest',
	'minecraft:birch_forest', 'minecraft:old_growth_birch_forest', 'minecraft:cherry_grove', 'minecraft:taiga',
	{ id: '#c:is_plains', required: false }, { id: '#c:is_forest', required: false },
	// Terralith's gentle land that isn't tagged as plains or forest.
	...['shrubland', 'lush_valley', 'valley_clearing', 'blooming_plateau', 'shield_clearing', 'shield', 'forested_highlands']
		.map(b => ({ id: `terralith:${b}`, required: false })),
] });

// Waterside plants. Firefly bushes beside water (on top of vanilla's few), and lily pads.
const nearWater = { type: 'minecraft:any_of', predicates: [[1, 0], [-1, 0], [0, 1], [0, -1]].map(([x, z]) => (
	{ type: 'minecraft:matching_fluids', fluids: ['minecraft:water', 'minecraft:flowing_water'], offset: [x, -1, z] })) };
const scatter = (r) => ({ type: 'minecraft:offset',
	x: { type: 'minecraft:trapezoid', max: r, min: -r, plateau: 0 },
	y: { type: 'minecraft:trapezoid', max: 3, min: -3, plateau: 0 },
	z: { type: 'minecraft:trapezoid', max: r, min: -r, plateau: 0 } });
out('espresso_flavoured/worldgen/placed_feature/waterside_firefly_bush.json', {
	feature: 'minecraft:firefly_bush',
	placement: [
		{ type: 'minecraft:count', count: 3 },
		{ type: 'minecraft:in_square' },
		{ type: 'minecraft:heightmap', heightmap: 'MOTION_BLOCKING_NO_LEAVES' },
		{ type: 'minecraft:biome' },
		{ type: 'minecraft:block_predicate_filter', predicate: { type: 'minecraft:all_of', predicates: [
			{ type: 'minecraft:matching_block_tag', tag: 'minecraft:air' },
			{ type: 'minecraft:would_survive', state: 'minecraft:firefly_bush' },
			nearWater,
		] } },
		{ type: 'minecraft:count', count: 12 },
		scatter(4),
		{ type: 'minecraft:block_predicate_filter', predicate: { type: 'minecraft:all_of', predicates: [
			{ type: 'minecraft:matching_block_tag', tag: 'minecraft:air' },
			{ type: 'minecraft:would_survive', state: 'minecraft:firefly_bush' },
		] } },
	],
});
out('espresso_flavoured/worldgen/placed_feature/waterside_lily_pads.json', {
	feature: 'minecraft:waterlily',
	// A small cluster in about one chunk in three, so the water stays easy to get about on
	// (ten tries in every chunk carpeted lakes and rivers).
	placement: [
		{ type: 'minecraft:rarity_filter', chance: 3 },
		{ type: 'minecraft:in_square' },
		{ type: 'minecraft:heightmap', heightmap: 'WORLD_SURFACE_WG' },
		{ type: 'minecraft:biome' },
		{ type: 'minecraft:count', count: 4 },
		scatter(4),
		{ type: 'minecraft:block_predicate_filter', predicate: { type: 'minecraft:matching_block_tag', tag: 'minecraft:air' } },
	],
});
// Water lilies (lily pads in bloom), rarer still: about one for every four or five lily pads.
out('espresso_flavoured/worldgen/feature/waterside_water_lily.json', { type: 'minecraft:simple_block', to_place: { id: 'espresso_flavoured:water_lily' } });
out('espresso_flavoured/worldgen/placed_feature/waterside_water_lilies.json', {
	feature: 'espresso_flavoured:waterside_water_lily',
	placement: [
		{ type: 'minecraft:rarity_filter', chance: 6 },
		{ type: 'minecraft:in_square' },
		{ type: 'minecraft:heightmap', heightmap: 'WORLD_SURFACE_WG' },
		{ type: 'minecraft:biome' },
		{ type: 'minecraft:count', count: 2 },
		scatter(4),
		{ type: 'minecraft:block_predicate_filter', predicate: { type: 'minecraft:all_of', predicates: [
			{ type: 'minecraft:matching_block_tag', tag: 'minecraft:air' },
			{ type: 'minecraft:would_survive', state: 'espresso_flavoured:water_lily' },
		] } },
	],
});
console.log('rest spots and waterside plants written');

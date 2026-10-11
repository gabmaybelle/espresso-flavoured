// Writes the lusher rivers into the espresso_data pack: thicker seagrass and some kelp in the
// deeper stretches, ferns, grasses and bushes along the banks, drifts of flowers by the water,
// and at the waterline a little moss, clay, mud and the odd mossy boulder. Everything keeps to
// the water's edge, so clearing land nearby isn't any harder. The mod adds the underwater ones to
// rivers, and the bank ones to rivers and the land around them (a river's banks are usually in the
// neighbouring biome), which also dresses the edges of lakes and ponds.
// Run by generate-data.js (which clears the pack first).
const fs = require('fs'), path = require('path');
const DATA = path.join(__dirname, '..', 'src', 'main', 'resources', 'resourcepacks', 'espresso_data', 'data', 'espresso', 'worldgen');
const out = (rel, obj) => {
	const f = path.join(DATA, ...rel.split('/'));
	fs.mkdirSync(path.dirname(f), { recursive: true });
	fs.writeFileSync(f, JSON.stringify(obj, null, 2) + '\n');
};

const water = (offset) => ({ type: 'minecraft:matching_fluids', fluids: ['minecraft:water', 'minecraft:flowing_water'], ...(offset ? { offset } : {}) });
// The ground just below is beside water at the same height: a low bank, right at the waterline.
const atWaterline = { type: 'minecraft:any_of', predicates: [[1, 0], [-1, 0], [0, 1], [0, -1]].map(([x, z]) => water([x, -1, z])) };
// Or a block above it: the usual riverbank, for plants and flowers.
const byWater = { type: 'minecraft:any_of', predicates: [[1, 0], [-1, 0], [0, 1], [0, -1]].flatMap(([x, z]) => [water([x, -1, z]), water([x, -2, z])]) };
// Open ground: air, or short grass and the like (vanilla grass already covers most banks).
const air = { type: 'minecraft:replaceable' };
const survives = (state) => ({ type: 'minecraft:would_survive', state });
const filter = (...predicates) => ({ type: 'minecraft:block_predicate_filter', predicate: predicates.length === 1 ? predicates[0] : { type: 'minecraft:all_of', predicates } });
const scatter = (r, y = 2) => ({ type: 'minecraft:offset',
	x: { type: 'minecraft:trapezoid', max: r, min: -r, plateau: 0 },
	y: { type: 'minecraft:trapezoid', max: y, min: -y, plateau: 0 },
	z: { type: 'minecraft:trapezoid', max: r, min: -r, plateau: 0 } });
const surface = { type: 'minecraft:heightmap', heightmap: 'MOTION_BLOCKING_NO_LEAVES' };
const riverbed = { type: 'minecraft:heightmap', heightmap: 'OCEAN_FLOOR' };
const inBiome = { type: 'minecraft:biome' };
const square = { type: 'minecraft:in_square' };
const count = (n) => ({ type: 'minecraft:count', count: n });
const rarity = (chance) => ({ type: 'minecraft:rarity_filter', chance });
// A clump along the bank: find a spot at the waterline, then scatter around it.
const bankClump = (state, spots, clump, r) => [count(spots), square, surface, inBiome, filter(air, survives(state), byWater), count(clump), scatter(r), filter(air, survives(state))];

// ---- 1. Under the water
out('placed_feature/river_seagrass.json', {
	feature: 'minecraft:seagrass_tall',
	placement: [square, count(32), scatter(7, 0), riverbed, filter({ type: 'minecraft:matching_blocks', blocks: 'minecraft:water' }), inBiome],
});
out('placed_feature/river_kelp.json', {
	feature: 'minecraft:kelp',
	placement: [rarity(2), count(6), square, riverbed, filter(
		{ type: 'minecraft:matching_blocks', blocks: 'minecraft:water' },
		{ type: 'minecraft:matching_blocks', blocks: 'minecraft:water', offset: [0, 3, 0] }, // only where it's deep
		{ type: 'minecraft:has_sturdy_face', direction: 'up', offset: [0, -1, 0] },
		{ type: 'minecraft:not', predicate: { type: 'minecraft:matching_block_tag', offset: [0, -1, 0], tag: 'minecraft:cannot_support_kelp' } },
	), inBiome],
});

// ---- 2. Along the banks: grasses, ferns and bushes
out('feature/river_bank_plants.json', {
	type: 'minecraft:simple_block',
	to_place: { type: 'minecraft:weighted', entries: [
		{ data: 'minecraft:short_grass', weight: 4 }, { data: { id: 'minecraft:tall_grass', properties: { half: 'lower' } }, weight: 3 },
		{ data: 'minecraft:fern', weight: 3 }, { data: { id: 'minecraft:large_fern', properties: { half: 'lower' } }, weight: 1 },
		{ data: 'minecraft:bush', weight: 2 },
	] },
});
out('placed_feature/river_bank_plants.json', { feature: 'espresso:river_bank_plants', placement: bankClump('minecraft:short_grass', 6, 14, 3) });

// ---- 3. Drifts of flowers by the water
out('feature/river_bank_flowers.json', {
	type: 'minecraft:simple_block',
	to_place: { type: 'minecraft:weighted', entries: [
		{ data: 'minecraft:cornflower', weight: 3 }, { data: 'minecraft:azure_bluet', weight: 3 }, { data: 'minecraft:oxeye_daisy', weight: 2 },
	] },
});
out('placed_feature/river_bank_flowers.json', { feature: 'espresso:river_bank_flowers', placement: bankClump('minecraft:cornflower', 4, 10, 3) });

// ---- 5. At the waterline: moss, clay, mud and the odd mossy boulder
const disk = (block, min, max, targets) => ({
	type: 'minecraft:disk', half_height: 1, radius: { type: 'minecraft:uniform', min_inclusive: min, max_inclusive: max },
	state_provider: { id: block }, target: { type: 'minecraft:matching_blocks', blocks: targets },
});
out('feature/waterline_moss.json', disk('minecraft:moss_block', 1, 2, ['minecraft:grass_block', 'minecraft:dirt']));
out('placed_feature/waterline_moss.json', { feature: 'espresso:waterline_moss', placement: [rarity(2), count(2), square, surface, inBiome, filter(air, atWaterline)] });
// Mud only on low, earthy banks right at the water, never on sand or gravel.
out('feature/waterline_mud.json', disk('minecraft:mud', 1, 3, ['minecraft:grass_block', 'minecraft:dirt', 'minecraft:coarse_dirt']));
out('placed_feature/waterline_mud.json', { feature: 'espresso:waterline_mud', placement: [rarity(3), square, surface, inBiome, filter(air, atWaterline)] });
out('placed_feature/river_clay.json', {
	feature: 'minecraft:disk_clay',
	placement: [count(2), square, { type: 'minecraft:heightmap', heightmap: 'OCEAN_FLOOR_WG' }, filter(water()), inBiome],
});
out('placed_feature/waterline_rock.json', {
	feature: 'minecraft:forest_rock',
	placement: [rarity(6), square, { type: 'minecraft:heightmap', heightmap: 'OCEAN_FLOOR' }, inBiome, filter(air, atWaterline)],
});

console.log('lusher rivers written');
module.exports = {
	underwater: ['river_seagrass', 'river_kelp', 'river_clay'],
	banks: ['river_bank_plants', 'river_bank_flowers', 'waterline_moss', 'waterline_mud', 'waterline_rock'],
};

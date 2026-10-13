// Writes the lusher rivers into the espresso_data pack: thicker seagrass and some kelp in the
// deeper stretches, ferns, grasses and bushes along the banks, drifts of flowers by the water,
// and at the waterline a little moss, clay, mud and the odd mossy boulder. Everything keeps to
// the water's edge, so clearing land nearby isn't any harder. The mod adds the underwater ones to
// rivers, and the bank ones to rivers and the land around them (a river's banks are usually in the
// neighbouring biome), which also dresses the edges of lakes and ponds.
// Run by generate-data.js (which clears the pack first).
const fs = require('fs'), path = require('path');
const DATA = path.join(__dirname, '..', 'src', 'main', 'resources', 'resourcepacks', 'espresso_data', 'data', 'espresso_flavoured', 'worldgen');
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
// Open ground: air, or short grass and the like (vanilla grass already covers most banks). Never
// water, though it's replaceable too: a plant set into a river cell is flooded again as soon as the
// chunk goes live, and drops as an item.
const air = { type: 'minecraft:all_of', predicates: [{ type: 'minecraft:replaceable' }, { type: 'minecraft:not', predicate: water() }] };
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
out('placed_feature/river_bank_plants.json', { feature: 'espresso_flavoured:river_bank_plants', placement: bankClump('minecraft:short_grass', 6, 14, 3) });

// ---- 3. Drifts of flowers by the water
out('feature/river_bank_flowers.json', {
	type: 'minecraft:simple_block',
	to_place: { type: 'minecraft:weighted', entries: [
		{ data: 'minecraft:cornflower', weight: 3 }, { data: 'minecraft:azure_bluet', weight: 3 }, { data: 'minecraft:oxeye_daisy', weight: 2 },
	] },
});
out('placed_feature/river_bank_flowers.json', { feature: 'espresso_flavoured:river_bank_flowers', placement: bankClump('minecraft:cornflower', 4, 10, 3) });

// ---- 4. Mossy banks: moss carpet with the odd azalea bush (flowering or not), like a lush cave
// come up for air. Placed after the waterline moss below, so carpets often sit on the moss.
out('feature/river_bank_moss.json', {
	type: 'minecraft:simple_block',
	to_place: { type: 'minecraft:weighted', entries: [
		{ data: 'minecraft:moss_carpet', weight: 5 }, { data: 'minecraft:azalea', weight: 3 }, { data: 'minecraft:flowering_azalea', weight: 2 },
	] },
});
// Carpet would happily sit on water, so only on solid ground.
const onGround = { type: 'minecraft:has_sturdy_face', direction: 'up', offset: [0, -1, 0] };
// Plenty of tries for a spot by the water (most land elsewhere), and generous clumps that reach a
// little wider and higher up the bank.
// Not right at the water's lip either: water at the same height creeps sideways into a carpet's
// cell (carpets don't hold it back) and breaks it.
const notAtLip = { type: 'minecraft:not', predicate: { type: 'minecraft:any_of', predicates: [[1, 0], [-1, 0], [0, 1], [0, -1]].map(([x, z]) => water([x, 0, z])) } };
// Only every third chunk or so gets the moss, so rivers are mostly grass and flowers with the odd
// lush green stretch, rather than mossy the whole way along.
out('placed_feature/river_bank_moss.json', { feature: 'espresso_flavoured:river_bank_moss', placement: [
	rarity(3), count(12), square, surface, inBiome, filter(air, survives('minecraft:moss_carpet'), byWater, notAtLip),
	count(10), scatter(4, 3), filter(air, survives('minecraft:moss_carpet'), onGround, notAtLip)] });

// ---- 4b. Lush shores, like a lush cave come up to the light: now and then vanilla's own moss patch
// (a carpet of moss sprouting moss carpet, grass and azaleas) or a little clay pool of big and small
// dripleaf, right at the water's edge.
// A few tries per chunk: each only takes if it lands right at the waterline.
out('placed_feature/lush_shore_moss.json', { feature: 'minecraft:moss_patch', placement: [rarity(3), count(6), square, surface, inBiome, filter(air, atWaterline)] });
out('placed_feature/lush_shore_pool.json', { feature: 'minecraft:clay_pool_with_dripleaves', placement: [count(3), square, surface, inBiome, filter(air, atWaterline)] });

// ---- 5. At the waterline: moss, clay, mud and the odd mossy boulder
const disk = (block, min, max, targets) => ({
	type: 'minecraft:disk', half_height: 1, radius: { type: 'minecraft:uniform', min_inclusive: min, max_inclusive: max },
	state_provider: { id: block }, target: { type: 'minecraft:matching_blocks', blocks: targets },
});
out('feature/waterline_moss.json', disk('minecraft:moss_block', 1, 3, ['minecraft:grass_block', 'minecraft:dirt']));
out('placed_feature/waterline_moss.json', { feature: 'espresso_flavoured:waterline_moss', placement: [count(5), square, surface, inBiome, filter(air, atWaterline)] });
// Muddy banks, only where there's clay in the water nearby, and only on earthy banks (never sand or
// gravel). Three patches fade from the water outward, each painted over the last: a wide fringe of
// coarse and rooted dirt among the grass, then patchy mud with packed mud and rooted dirt, then soft
// mud right at the waterline (and the riverbed beside it). They run after the clay is placed.
const nearClay = { type: 'minecraft:any_of', predicates: [-4, -3, -2, -1, 0, 1, 2, 3, 4].flatMap(x => [-4, -3, -2, -1, 0, 1, 2, 3, 4]
	.filter(z => x * x + z * z <= 16).flatMap(z => [-2, -3, -4].map(y => ({ type: 'minecraft:matching_blocks', blocks: 'minecraft:clay', offset: [x, y, z] })))) };
const mixDisk = (entries, min, max, targets) => ({
	...disk('minecraft:mud', min, max, targets),
	state_provider: { type: 'minecraft:weighted', entries: entries.map(([data, weight]) => ({ data, weight })) },
});
const earthy = ['minecraft:grass_block', 'minecraft:dirt', 'minecraft:coarse_dirt'];
const muddy = [...earthy, 'minecraft:rooted_dirt', 'minecraft:mud', 'minecraft:packed_mud', 'minecraft:moss_block'];
const muddyBank = (name, feature) => {
	out(`feature/${name}.json`, feature);
	out(`placed_feature/${name}.json`, { feature: `espresso_flavoured:${name}`, placement: [count(6), square, surface, inBiome, filter(air, atWaterline, nearClay)] });
};
muddyBank('muddy_bank_fringe', mixDisk([['minecraft:grass_block', 4], ['minecraft:coarse_dirt', 2], ['minecraft:rooted_dirt', 2]], 3, 5, earthy));
muddyBank('muddy_bank_mix', mixDisk([['minecraft:mud', 4], ['minecraft:packed_mud', 1], ['minecraft:rooted_dirt', 2]], 2, 3, muddy));
muddyBank('muddy_bank_edge', disk('minecraft:mud', 1, 2, muddy));
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
	banks: ['muddy_bank_fringe', 'muddy_bank_mix', 'muddy_bank_edge', 'lush_shore_moss', 'lush_shore_pool', 'river_bank_plants', 'river_bank_flowers', 'river_bank_moss',
		'waterline_moss', 'waterline_rock'],
};

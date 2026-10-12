// Writes the spawn gazebo: a little stone pavilion that SpawnGazebo.java builds at the world
// spawn of every new world. Mostly stone (bricks, tuff, moss) with fence posts holding the roof
// up; a spore blossom hangs in the middle and glow berries light it. One file per wood (the mod
// picks the wood growing around spawn) and per palette (grey stone, or sandstone in dry biomes).
// Run by generate-data.js (which clears the pack first).
const fs = require('fs'), path = require('path');
const { Structure, stairs, fence } = require('./structure.js');
const DATA = path.join(__dirname, '..', 'src', 'main', 'resources', 'resourcepacks', 'espresso_data', 'data');
const out = (rel, buf) => {
	const f = path.join(DATA, ...rel.split('/'));
	fs.mkdirSync(path.dirname(f), { recursive: true });
	fs.writeFileSync(f, buf);
};

// Every overworld wood with a fence and stairs. SpawnGazebo.WOODS lists the same names.
const WOODS = ['oak', 'spruce', 'birch', 'jungle', 'acacia', 'dark_oak', 'mangrove', 'cherry', 'pale_oak', 'bamboo', 'poplar'];

const PALETTES = {
	// Grey stone with moss creeping in, grass and pink flowers round the edge.
	stone: {
		apron: 'minecraft:cobblestone', apronWorn: 'minecraft:mossy_cobblestone',
		plinth: 'minecraft:stone_bricks', plinthWorn: 'minecraft:mossy_stone_bricks', plinthAccent: 'minecraft:tuff_bricks',
		stair: 'minecraft:stone_brick', stairWorn: 'minecraft:mossy_stone_brick',
		ceiling: 'minecraft:tuff_bricks', ceilingCentre: 'minecraft:chiseled_tuff_bricks', fill: 'minecraft:stone_bricks',
		cap: 'minecraft:stone_brick_slab', post: 'minecraft:chiseled_stone_bricks', rail: 'minecraft:tuff_brick_wall', footing: 'minecraft:stone_brick_wall',
		ground: 'minecraft:grass_block[snowy=false]', path: 'minecraft:dirt_path', carpet: 'minecraft:moss_carpet',
		corners: 'minecraft:flowering_azalea', sides: 'minecraft:pink_petals[facing=north,segment_amount=3]',
		front: 'minecraft:pink_tulip', back: 'minecraft:allium',
	},
	// Sandstone for deserts and badlands: no moss, dead bushes and cactus flowers instead of flowers.
	sand: {
		apron: 'minecraft:sandstone', apronWorn: 'minecraft:cut_sandstone',
		plinth: 'minecraft:smooth_sandstone', plinthWorn: 'minecraft:sandstone', plinthAccent: 'minecraft:chiseled_sandstone',
		stair: 'minecraft:smooth_sandstone', stairWorn: 'minecraft:sandstone',
		ceiling: 'minecraft:cut_sandstone', ceilingCentre: 'minecraft:chiseled_sandstone', fill: 'minecraft:sandstone',
		cap: 'minecraft:smooth_sandstone_slab', post: 'minecraft:chiseled_sandstone', rail: 'minecraft:sandstone_wall', footing: 'minecraft:sandstone_wall',
		ground: 'minecraft:sand', path: 'minecraft:sand', carpet: null,
		corners: 'minecraft:dead_bush', sides: 'minecraft:cactus_flower', front: 'minecraft:dead_bush', back: 'minecraft:cactus_flower',
	},
};

// A fixed little random so every wood gets the same worn blocks in the same places.
function rng(seed) {
	let s = seed >>> 0;
	return () => { s = (s * 1664525 + 1013904223) >>> 0; return s / 4294967296; };
}

const wall = (base, up = false) => `${base}[east=none,north=none,south=none,up=${up},waterlogged=false,west=none]`;

// Template: 13 x 10 x 13, the gazebo centred on (6, y, 6). y=0 is the ground surface; the apron
// replaces the top layer of ground and the plinth stands one block proud of it. The entrance and
// the archway face south (+z); the mod rotates the whole thing at random.
function gazebo(wood, p) {
	const g = new Structure([13, 10, 13]);
	const set = (x, y, z, s, nbt) => g.set(x, y, z, s, nbt);
	const rand = rng(20261011);
	const worn = (chance, a, b) => (p.apronWorn !== p.apron || p.plinthWorn !== p.plinth) && rand() < chance ? b : a;
	const w = `minecraft:${wood}`;
	const C = 6;

	// Apron at ground level: a 9 x 9 with the corners clipped, mossier towards the edge.
	for (let x = 2; x <= 10; x++) for (let z = 2; z <= 10; z++) {
		const edge = x === 2 || x === 10 || z === 2 || z === 10;
		if (edge && (x === 2 || x === 10) && (z === 2 || z === 10)) continue;
		set(x, 0, z, worn(edge ? 0.45 : 0.2, p.apron, p.apronWorn));
	}
	// Moss carpet on a few apron stones.
	if (p.carpet) for (const [x, z] of [[2, 5], [10, 7], [4, 10], [9, 2]]) set(x, 1, z, p.carpet);

	// Plinth, one block up: 7 x 7 of bricks with the odd mossy or tuff one.
	for (let x = 3; x <= 9; x++) for (let z = 3; z <= 9; z++) {
		const r = rand();
		set(x, 1, z, r < 0.12 ? p.plinthWorn : r < 0.2 ? p.plinthAccent : p.plinth);
	}
	// Steps up at the entrance.
	for (let x = 5; x <= 7; x++) set(x, 1, 10, stairs(p.stair, 'north'));

	// Railing round the plinth edge, open at the entrance, with a footing and fence post at each corner.
	for (let x = 3; x <= 9; x++) for (let z = 3; z <= 9; z++) {
		const edge = x === 3 || x === 9 || z === 3 || z === 9;
		if (!edge) continue;
		const corner = (x === 3 || x === 9) && (z === 3 || z === 9);
		if (corner) {
			set(x, 2, z, wall(p.footing, true));
			for (let y = 3; y <= 5; y++) set(x, y, z, fence(w));
		} else if (x === C && z === 3) {
			// The starter kit, in the railing opposite the entrance. SpawnGazebo.java knows this spot.
			set(x, 2, z, 'minecraft:chest[facing=south,type=single,waterlogged=false]', { LootTable: 'espresso:chests/spawn_kit' });
		} else if (!(z === 9 && x >= 5 && x <= 7)) {
			set(x, 2, z, wall(p.rail));
		}
	}

	// Two benches facing each other across the middle, backs to the railing.
	for (const z of [5, 6, 7]) {
		set(4, 2, z, stairs(w, 'west'));
		set(8, 2, z, stairs(w, 'east'));
	}

	// A low roof in two flat tiers. Each tier is a ring of upside-down stairs (the brackets you see
	// from below), a layer of slabs on top of them, and a square post at each corner. The ceiling
	// under it is full blocks so things can hang from it, and a fringe of closed fence gates runs
	// under its edge between the piers.
	const facingIn = (x, z, lo, hi) => x === lo ? 'east' : x === hi ? 'west' : z === lo ? 'south' : 'north';
	const bracket = (x, z, lo, hi) => `${worn(0.2, p.stair, p.stairWorn)}_stairs[facing=${facingIn(x, z, lo, hi)},half=top,shape=straight,waterlogged=false]`;
	const slab = () => `${p.cap}[type=bottom,waterlogged=false]`;
	const corner = (x, z, lo, hi) => (x === lo || x === hi) && (z === lo || z === hi);
	// A closed gate along the edge: on the north and south sides its bar runs east-west.
	const gate = (x, z) => `${w}_fence_gate[facing=${z === 3 || z === 9 ? 'south' : 'east'},in_wall=false,open=false,powered=false]`;
	// The ceiling is a ring of beam blocks at y=6 round an open middle, so from inside you look up
	// into the roof: the recessed ceiling is the underside of the upper tier at y=7.
	for (let x = 3; x <= 9; x++) for (let z = 3; z <= 9; z++) {
		const ring = x === 3 || x === 9 || z === 3 || z === 9;
		if (ring) set(x, 6, z, p.ceiling);
		if (ring && !corner(x, z, 3, 9)) set(x, 5, z, gate(x, z));
	}
	// Lower tier: brackets round the ceiling at y=6, slabs and corner posts at y=7.
	for (let x = 2; x <= 10; x++) for (let z = 2; z <= 10; z++) {
		if (x !== 2 && x !== 10 && z !== 2 && z !== 10) continue;
		set(x, 6, z, bracket(x, z, 2, 10));
		set(x, 7, z, corner(x, z, 2, 10) ? p.post : slab());
	}
	// Upper tier, set in by one: brackets at y=7 round the recessed ceiling, slabs and posts at y=8.
	for (let x = 3; x <= 9; x++) for (let z = 3; z <= 9; z++) {
		const ring = x === 3 || x === 9 || z === 3 || z === 9;
		set(x, 7, z, ring ? bracket(x, z, 3, 9) : x === C && z === C ? p.ceilingCentre : p.ceiling);
		set(x, 8, z, ring && corner(x, z, 3, 9) ? p.post : slab());
	}

	// From the recessed ceiling: the spore blossom in the middle and glow berries round it, which
	// are all the light it needs (two longer strands hang in opposite corners).
	set(C, 6, C, 'minecraft:spore_blossom');
	const berries = 'minecraft:cave_vines[age=25,berries=true]';
	for (const [x, z] of [[4, C], [8, C], [C, 4], [C, 8]]) set(x, 6, z, berries);
	for (const [x, z] of [[4, 4], [8, 8]]) {
		set(x, 6, z, 'minecraft:cave_vines_plant[berries=true]');
		set(x, 5, z, berries);
	}

	// A short path out from the steps.
	for (const z of [11, 12]) for (let x = 5; x <= 7; x++) set(x, 0, z, p.path);

	// Planting round the outside, on ground the template lays itself so it always takes.
	const plant = (x, z, s) => { set(x, 0, z, p.ground); set(x, 1, z, s); };
	for (const [x, z] of [[1, 3], [1, 9], [11, 3], [11, 9]]) plant(x, z, p.corners);
	for (const [x, z] of [[1, C], [11, C], [C, 1]]) plant(x, z, p.sides);
	for (const x of [3, 9]) plant(x, 1, p.front);
	for (const x of [3, 9]) plant(x, 11, p.back);
	return g.toNbt();
}

for (const [palette, p] of Object.entries(PALETTES)) {
	for (const wood of WOODS) out(`espresso/structure/spawn/gazebo_${palette}_${wood}.nbt`, gazebo(wood, p));
}

// Writes the spawn gazebo: a little stone pavilion that SpawnGazebo.java builds at the world
// spawn of every new world. Mostly stone (bricks, tuff, moss) with fence posts holding the roof
// up; a spore blossom hangs in the middle and glowstone in the floor lights it. One file per
// wood (the mod picks the wood growing around spawn) and per palette (grey stone, or sandstone in
// dry biomes).
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
		floorBorder: 'minecraft:tuff_bricks', floorSide: 'minecraft:chiseled_tuff', floorCorner: 'minecraft:polished_tuff',
		cap: 'minecraft:stone_brick_slab', rail: 'minecraft:tuff_brick_wall', footing: 'minecraft:stone_brick_wall',
		ground: 'minecraft:grass_block[snowy=false]', path: 'minecraft:dirt_path',
		moss: 'minecraft:moss_block', carpet: 'minecraft:moss_carpet',
		corners: 'minecraft:flowering_azalea', sides: 'minecraft:pink_petals[facing=north,segment_amount=3]',
		front: 'minecraft:pink_tulip', back: 'minecraft:allium',
	},
	// Sandstone for deserts and badlands: no moss, dead bushes and cactus flowers instead of flowers.
	sand: {
		apron: 'minecraft:sandstone', apronWorn: 'minecraft:cut_sandstone',
		plinth: 'minecraft:smooth_sandstone', plinthWorn: 'minecraft:sandstone', plinthAccent: 'minecraft:chiseled_sandstone',
		stair: 'minecraft:smooth_sandstone', stairWorn: 'minecraft:sandstone',
		ceiling: 'minecraft:cut_sandstone', ceilingCentre: 'minecraft:chiseled_sandstone', fill: 'minecraft:sandstone',
		floorBorder: 'minecraft:cut_sandstone', floorSide: 'minecraft:chiseled_sandstone', floorCorner: 'minecraft:smooth_sandstone',
		cap: 'minecraft:smooth_sandstone_slab', rail: 'minecraft:sandstone_wall', footing: 'minecraft:sandstone_wall',
		ground: 'minecraft:sand', path: 'minecraft:sand',
		moss: null,
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
// replaces the top layer of ground and the plinth stands two blocks proud of it. The entrance and
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
	// A low wall round the foot of the plinth, a block out from it, open at the steps.
	for (let x = 2; x <= 10; x++) for (let z = 2; z <= 10; z++) {
		if (x !== 2 && x !== 10 && z !== 2 && z !== 10) continue;
		if (z === 10 && x >= 5 && x <= 7) continue;
		const post = (x === 2 || x === 10) && (z === 2 || z === 10) || z === 10 && (x === 4 || x === 8);
		set(x, 1, z, wall(p.footing, post));
	}

	// Plinth, two blocks high: 7 x 7 of bricks with the odd mossy or tuff one. Inside the railing
	// its top is a floor: a border round a glowstone under the spore blossom, set in chiseled stone.
	for (let x = 3; x <= 9; x++) for (let z = 3; z <= 9; z++) {
		for (const y of [1, 2]) {
			const r = rand();
			set(x, y, z, r < 0.12 ? p.plinthWorn : r < 0.2 ? p.plinthAccent : p.plinth);
		}
		const inside = x >= 4 && x <= 8 && z >= 4 && z <= 8;
		const panel = x >= 5 && x <= 7 && z >= 5 && z <= 7;
		const side = x === C || z === C;
		if (inside) set(x, 2, z, x === C && z === C ? 'minecraft:glowstone' : !panel ? p.floorBorder : side ? p.floorSide : p.floorCorner);
	}
	// Two steps up at the entrance, the upper one on a block of plinth in the gap in the wall.
	for (let x = 5; x <= 7; x++) {
		set(x, 1, 11, stairs(p.stair, 'north'));
		set(x, 1, 10, p.plinth);
		set(x, 2, 10, stairs(p.stair, 'north'));
	}

	// Pots, for a builder's first garden. Flower pots stand on the railing either side of the
	// entrance and on the ground by the steps, with saplings of woods that don't grow everywhere
	// (never the gazebo's own) and a cactus; take a plant out with an empty hand. Inside, a
	// decorated pot in each corner holds mangrove propagules, bamboo or seeds (break it to get them).
	const potted = ['jungle', 'spruce'].filter(x => x !== wood).map(x => `minecraft:potted_${x}_sapling`)
		.concat('minecraft:potted_cactus', 'minecraft:potted_azalea_bush', 'minecraft:potted_flowering_azalea_bush');
	const [railL, railR, stepL, stepR] = potted;
	set(4, 4, 9, railL);
	set(8, 4, 9, railR);
	for (const [x, s] of [[4, stepL], [8, stepR]]) { set(x, 0, 11, p.ground); set(x, 1, 11, s); }
	// 26.3 keeps a pot's sherds by side; a side left out is plain brick.
	const pot = (front, id, count) => ({ sherds: { front: { id: `minecraft:${front}_pottery_sherd` } }, item: { id: `minecraft:${id}`, count } });
	const decorated = (x, z, facing, contents) => set(x, 3, z, `minecraft:decorated_pot[cracked=false,facing=${facing},waterlogged=false]`, contents);
	decorated(4, 4, 'south', pot('flower', 'mangrove_propagule', 2));
	decorated(8, 4, 'south', pot('heart', 'bamboo', 4));
	// Two different seeds by the entrance, picked by wood so worlds differ.
	const seeds = [['torchflower_seeds', 2], ['pitcher_pod', 1], ['pumpkin_seeds', 4], ['melon_seeds', 4], ['beetroot_seeds', 4], ['cocoa_beans', 3]];
	const first = WOODS.indexOf(wood) % seeds.length;
	decorated(4, 8, 'east', pot('sheaf', ...seeds[first]));
	decorated(8, 8, 'west', pot('snort', ...seeds[(first + 3) % seeds.length]));

	// Railing round the plinth edge, open at the entrance. Each corner has a fence post with a wall
	// at its foot and its head.
	for (let x = 3; x <= 9; x++) for (let z = 3; z <= 9; z++) {
		const edge = x === 3 || x === 9 || z === 3 || z === 9;
		if (!edge) continue;
		const corner = (x === 3 || x === 9) && (z === 3 || z === 9);
		if (corner) {
			set(x, 3, z, wall(p.footing, true));
			for (let y = 4; y <= 5; y++) set(x, y, z, fence(w));
			set(x, 6, z, wall(p.footing, true));
		} else if (x === C && z === 3) {
			// The starter kit, in the railing opposite the entrance. SpawnGazebo.java knows this spot.
			set(x, 3, z, 'minecraft:chest[facing=south,type=single,waterlogged=false]', { LootTable: 'espresso:chests/spawn_kit' });
		} else if (!(z === 9 && x >= 5 && x <= 7)) {
			set(x, 3, z, wall(p.rail));
		}
	}

	// Two benches facing each other across the middle, backs to the railing.
	for (const z of [5, 6, 7]) {
		set(4, 3, z, stairs(w, 'west'));
		set(8, 3, z, stairs(w, 'east'));
	}

	// A low roof in two flat tiers, the lower one no wider than the plinth. Each tier is a ring of
	// upside-down stairs (the brackets you see from below), a layer of slabs on top of them, and a
	// wall post at each corner. The ceiling under it is full blocks so things can hang from it, and
	// a fringe of closed fence gates runs under its edge between the piers.
	const facingIn = (x, z, lo, hi) => x === lo ? 'east' : x === hi ? 'west' : z === lo ? 'south' : 'north';
	const bracket = (x, z, lo, hi) => `${worn(0.2, p.stair, p.stairWorn)}_stairs[facing=${facingIn(x, z, lo, hi)},half=top,shape=straight,waterlogged=false]`;
	const slab = () => `${p.cap}[type=bottom,waterlogged=false]`;
	const corner = (x, z, lo, hi) => (x === lo || x === hi) && (z === lo || z === hi);
	// A closed gate along the edge: on the north and south sides its bar runs east-west.
	const gate = (x, z) => `${w}_fence_gate[facing=${z === 3 || z === 9 ? 'south' : 'east'},in_wall=false,open=false,powered=false]`;
	// Lower tier: brackets over the railing at y=7 round an open middle (so from inside you look up
	// into the roof), slabs and corner posts at y=8, and the gates hung under the brackets.
	for (let x = 3; x <= 9; x++) for (let z = 3; z <= 9; z++) {
		if (x !== 3 && x !== 9 && z !== 3 && z !== 9) continue;
		set(x, 7, z, bracket(x, z, 3, 9));
		set(x, 8, z, corner(x, z, 3, 9) ? wall(p.footing, true) : slab());
		if (!corner(x, z, 3, 9)) set(x, 6, z, gate(x, z));
	}
	// Upper tier, set in by one: brackets at y=8 round the recessed ceiling, slabs and posts at y=9.
	for (let x = 4; x <= 8; x++) for (let z = 4; z <= 8; z++) {
		const ring = x === 4 || x === 8 || z === 4 || z === 8;
		set(x, 8, z, ring ? bracket(x, z, 4, 8) : x === C && z === C ? p.ceilingCentre : p.ceiling);
		set(x, 9, z, ring && corner(x, z, 4, 8) ? wall(p.footing, true) : slab());
	}

	// From the recessed ceiling: the spore blossom in the middle and a sprig of glow berries in two
	// opposite corners. With the glowstone in the floor that's all the light it needs.
	set(C, 7, C, 'minecraft:spore_blossom');
	for (const [x, z] of [[5, 5], [7, 7]]) set(x, 7, z, 'minecraft:cave_vines[age=25,berries=true]');

	// A short path out from the steps.
	for (let x = 5; x <= 7; x++) set(x, 0, 12, p.path);

	// Moss and mossy stone worked into the ground round the base, thick by the wall and thinning
	// out, so the gazebo looks like it has sat there a while. The path, the pots by the steps and
	// the planting are left alone.
	if (p.moss) for (let x = 0; x <= 12; x++) for (let z = 0; z <= 12; z++) {
		const ring = Math.max(Math.abs(x - C), Math.abs(z - C));
		if (ring < 5 || (x >= 4 && x <= 8 && z >= 10)) continue;
		if (rand() > (ring === 5 ? 0.7 : 0.3)) continue;
		const r = rand();
		if (r < 0.55) {
			set(x, 0, z, p.moss);
			if (rand() < 0.4) set(x, 1, z, p.carpet);
		} else set(x, 0, z, r < 0.8 ? p.plinthWorn : p.apronWorn);
	}

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

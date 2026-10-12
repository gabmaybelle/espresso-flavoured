// Writes the spawn gazebo: a little stone pavilion that SpawnGazebo.java builds at the world
// spawn of every new world. A stone plinth and floor, stripped-log pillars carrying a ring of
// beams, a fence railing between them, and a pitched roof of stone stairs stepping up to a
// lightning rod; a spore blossom hangs in the middle and glowstone in the floor lights it, with
// lanterns under the eaves and by the steps. One file per wood (the mod picks the wood growing
// around spawn) and per palette (grey stone, or sandstone in dry biomes).
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
		footing: 'minecraft:stone_brick_wall',
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
		footing: 'minecraft:sandstone_wall',
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
const lantern = (hanging) => `minecraft:lantern[hanging=${hanging},waterlogged=false]`;

// Template: 13 x 12 x 13, the gazebo centred on (6, y, 6). y=0 is the ground surface; the apron
// replaces the top layer of ground and the plinth stands two blocks proud of it. The entrance and
// the archway face south (+z); the mod rotates the whole thing at random.
function gazebo(wood, p) {
	const g = new Structure([13, 12, 13]);
	const set = (x, y, z, s, nbt) => g.set(x, y, z, s, nbt);
	const rand = rng(20261011);
	const worn = (chance, a, b) => (p.apronWorn !== p.apron || p.plinthWorn !== p.plinth) && rand() < chance ? b : a;
	const w = `minecraft:${wood}`;
	// Bamboo has no log; its stripped block stands in.
	const log = (axis) => wood === 'bamboo' ? `minecraft:stripped_bamboo_block[axis=${axis}]` : `minecraft:stripped_${wood}_log[axis=${axis}]`;
	const C = 6;

	// Apron at ground level: a 9 x 9 with the corners clipped, mossier towards the edge.
	for (let x = 2; x <= 10; x++) for (let z = 2; z <= 10; z++) {
		const edge = x === 2 || x === 10 || z === 2 || z === 10;
		if (edge && (x === 2 || x === 10) && (z === 2 || z === 10)) continue;
		set(x, 0, z, worn(edge ? 0.45 : 0.2, p.apron, p.apronWorn));
	}
	// A low wall round the foot of the plinth, a block out from it, open at the steps, with a
	// lantern on each of the two posts that flank them.
	for (let x = 2; x <= 10; x++) for (let z = 2; z <= 10; z++) {
		if (x !== 2 && x !== 10 && z !== 2 && z !== 10) continue;
		if (z === 10 && x >= 5 && x <= 7) continue;
		const post = (x === 2 || x === 10) && (z === 2 || z === 10) || z === 10 && (x === 4 || x === 8);
		set(x, 1, z, wall(p.footing, post));
	}
	for (const x of [4, 8]) set(x, 2, 10, lantern(false));

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
	decorated(4, 4, 'south', pot('plenty', 'mangrove_propagule', 2));
	decorated(8, 4, 'south', pot('heart', 'bamboo', 4));
	// Two different seeds by the entrance, picked by wood so worlds differ.
	const seeds = [['torchflower_seeds', 2], ['pitcher_pod', 1], ['pumpkin_seeds', 4], ['melon_seeds', 4], ['beetroot_seeds', 4], ['cocoa_beans', 3]];
	const first = WOODS.indexOf(wood) % seeds.length;
	decorated(4, 8, 'east', pot('sheaf', ...seeds[first]));
	decorated(8, 8, 'west', pot('snort', ...seeds[(first + 3) % seeds.length]));

	// The frame, in stripped logs of the gazebo's wood: a pillar at each corner of the plinth, three
	// blocks tall, and a ring of beams across their tops that the roof sits on. The pillars run on
	// up through the beams at the corners.
	const corner = (x, z, lo, hi) => (x === lo || x === hi) && (z === lo || z === hi);
	const onRing = (lo, hi, fn) => { for (let x = lo; x <= hi; x++) for (let z = lo; z <= hi; z++) if (x === lo || x === hi || z === lo || z === hi) fn(x, z); };
	for (const [x, z] of [[3, 3], [9, 3], [3, 9], [9, 9]]) for (let y = 3; y <= 6; y++) set(x, y, z, log('y'));
	onRing(3, 9, (x, z) => { if (!corner(x, z, 3, 9)) set(x, 6, z, log(z === 3 || z === 9 ? 'x' : 'z')); });

	// A fence railing between the pillars, open at the entrance. Its rails run into the pillars,
	// but not into the chest.
	const chest = (x, z) => x === C && z === 3;
	const solid = (x, z) => corner(x, z, 3, 9) || x >= 3 && x <= 9 && z >= 3 && z <= 9 && (x === 3 || x === 9 || z === 3 || z === 9) && !chest(x, z) && !(z === 9 && x >= 5 && x <= 7);
	onRing(3, 9, (x, z) => {
		if (corner(x, z, 3, 9) || z === 9 && x >= 5 && x <= 7) return;
		if (chest(x, z)) {
			// The starter kit, in the railing opposite the entrance. SpawnGazebo.java knows this spot.
			set(x, 3, z, 'minecraft:chest[facing=south,type=single,waterlogged=false]', { LootTable: 'espresso_flavoured:chests/spawn_kit' });
			return;
		}
		const sides = [['east', x + 1, z], ['west', x - 1, z], ['south', x, z + 1], ['north', x, z - 1]].filter(([, nx, nz]) => solid(nx, nz)).map(([d]) => d);
		set(x, 3, z, fence(w, sides));
	});

	// Two benches facing each other across the middle, backs to the railing.
	for (const z of [5, 6, 7]) {
		set(4, 3, z, stairs(w, 'west'));
		set(8, 3, z, stairs(w, 'east'));
	}

	// A pitched roof of stone stairs, four steps up to a point. Every course is a ring of stairs
	// facing in, a block in and a block up from the one below; at a corner the stair is an outer
	// corner, keeping the quarter that points to the middle. The lowest course is upside down and
	// a block out past the beams, so from below the eaves are brackets; inside it a second ring of
	// brackets sits on the beams, and the ceiling steps up again to the middle, so from inside
	// you look up into a coffered roof.
	const facingIn = (x, z, lo, hi) => x === lo ? 'east' : x === hi ? 'west' : z === lo ? 'south' : 'north';
	const shape = (x, z, lo, hi) => !corner(x, z, lo, hi) ? 'straight' : (x === lo) === (z === lo) ? 'outer_right' : 'outer_left';
	const roofStair = (x, z, lo, hi, half) =>
		`${worn(0.2, p.stair, p.stairWorn)}_stairs[facing=${facingIn(x, z, lo, hi)},half=${half},shape=${shape(x, z, lo, hi)},waterlogged=false]`;
	onRing(2, 10, (x, z) => set(x, 7, z, roofStair(x, z, 2, 10, 'top')));
	onRing(3, 9, (x, z) => set(x, 7, z, roofStair(x, z, 3, 9, 'top')));
	for (const [y, lo, hi] of [[8, 3, 9], [9, 4, 8], [10, 5, 7]]) onRing(lo, hi, (x, z) => set(x, y, z, roofStair(x, z, lo, hi, 'bottom')));
	// Under the pitch: brackets round the recessed ceiling, which is full blocks so things can hang
	// from it, and solid stone filling the roof up to the lightning rod on its point.
	onRing(4, 8, (x, z) => set(x, 8, z, roofStair(x, z, 4, 8, 'top')));
	for (let x = 5; x <= 7; x++) for (let z = 5; z <= 7; z++) {
		set(x, 8, z, x === C && z === C ? p.ceilingCentre : p.ceiling);
		set(x, 9, z, p.fill);
	}
	set(C, 10, C, p.fill);
	set(C, 11, C, 'minecraft:lightning_rod[facing=up,powered=false,waterlogged=false]');

	// From the ceiling: the spore blossom in the middle and a sprig of glow berries in two opposite
	// corners. With the glowstone in the floor that's all the light the inside needs.
	set(C, 7, C, 'minecraft:spore_blossom');
	for (const [x, z] of [[5, 5], [7, 7]]) set(x, 7, z, 'minecraft:cave_vines[age=25,berries=true]');
	// A lantern on a chain under each corner of the eaves, lighting the ground round about.
	for (const [x, z] of [[2, 2], [10, 2], [2, 10], [10, 10]]) {
		set(x, 6, z, 'minecraft:chain[axis=y,waterlogged=false]');
		set(x, 5, z, lantern(true));
	}

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
	for (const wood of WOODS) out(`espresso_flavoured/structure/spawn/gazebo_${palette}_${wood}.nbt`, gazebo(wood, p));
}

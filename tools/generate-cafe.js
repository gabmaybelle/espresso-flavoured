// Writes the village café: one small building in each village style, as structure files in
// the espresso_data pack. The mod adds them to each village's houses pool (see VillageCafe.java).
// Run by generate-data.js (which clears the pack first); can also be run on its own.
const fs = require('fs'), path = require('path');
const { write, list, T } = require('./nbt.js');
const ROOT = path.join(__dirname, '..', 'src', 'main', 'resources', 'resourcepacks', 'espresso_data', 'data', 'espresso', 'structure', 'village');
const DATA_VERSION = 5023; // Minecraft 26.3

// Each style: corner posts, walls, floor, roof stairs and planks, the coffee bar's wood, door, and a table.
const STYLES = {
	plains: { post: 'minecraft:stripped_oak_log[axis=y]', wall: 'minecraft:oak_planks', base: 'minecraft:cobblestone', floor: 'minecraft:spruce_planks',
		roof: 'minecraft:spruce', roofBlock: 'minecraft:spruce_planks', wood: 'oak', door: 'minecraft:oak_door', fence: 'minecraft:oak_fence', plate: 'minecraft:oak_pressure_plate', chair: 'minecraft:oak_stairs', plant: 'minecraft:potted_azalea_bush' },
	desert: { post: 'minecraft:cut_sandstone', wall: 'minecraft:smooth_sandstone', base: 'minecraft:sandstone', floor: 'minecraft:smooth_sandstone',
		roof: 'minecraft:smooth_sandstone', roofBlock: 'minecraft:cut_sandstone', flatRoof: true, wood: 'jungle', door: 'minecraft:jungle_door', fence: 'minecraft:jungle_fence', plate: 'minecraft:jungle_pressure_plate', chair: 'minecraft:jungle_stairs', plant: 'minecraft:potted_cactus' },
	savanna: { post: 'minecraft:acacia_log[axis=y]', wall: 'minecraft:acacia_planks', base: 'minecraft:terracotta', floor: 'minecraft:acacia_planks',
		roof: 'minecraft:acacia', roofBlock: 'minecraft:acacia_planks', wood: 'acacia', door: 'minecraft:acacia_door', fence: 'minecraft:acacia_fence', plate: 'minecraft:acacia_pressure_plate', chair: 'minecraft:acacia_stairs', plant: 'minecraft:potted_acacia_sapling' },
	taiga: { post: 'minecraft:spruce_log[axis=y]', wall: 'minecraft:spruce_planks', base: 'minecraft:cobblestone', floor: 'minecraft:spruce_planks',
		roof: 'minecraft:spruce', roofBlock: 'minecraft:spruce_planks', wood: 'spruce', door: 'minecraft:spruce_door', fence: 'minecraft:spruce_fence', plate: 'minecraft:spruce_pressure_plate', chair: 'minecraft:spruce_stairs', plant: 'minecraft:potted_fern' },
	snowy: { post: 'minecraft:stripped_spruce_log[axis=y]', wall: 'minecraft:spruce_planks', base: 'minecraft:stone_bricks', floor: 'minecraft:spruce_planks',
		roof: 'minecraft:spruce', roofBlock: 'minecraft:spruce_planks', wood: 'spruce', door: 'minecraft:spruce_door', fence: 'minecraft:spruce_fence', plate: 'minecraft:spruce_pressure_plate', chair: 'minecraft:spruce_stairs', plant: 'minecraft:potted_spruce_sapling' },
};

// 'minecraft:oak_stairs[facing=east,half=bottom]' -> { id, properties }
function parse(state) {
	const m = /^([^\[]+)(?:\[(.*)\])?$/.exec(state);
	const properties = m[2] ? Object.fromEntries(m[2].split(',').map(kv => kv.split('='))) : undefined;
	return properties ? { id: m[1], properties } : { id: m[1] };
}
const stairs = (base, facing, shape = 'straight') => `${base}_stairs[facing=${facing},half=bottom,shape=${shape},waterlogged=false]`;

function build(biome, s) {
	const SIZE = [9, 8, 9];
	const blocks = new Map(); // "x,y,z" -> { state, nbt }
	const set = (x, y, z, state, nbt) => blocks.set(`${x},${y},${z}`, { state, nbt });

	// Everything inside the footprint starts as air, so terrain is cleared; the edges stay void.
	for (let x = 1; x <= 7; x++) for (let y = 1; y <= 3; y++) for (let z = 1; z <= 7; z++) set(x, y, z, 'minecraft:air');

	// Floor: a base ring under the walls, floorboards inside.
	for (let x = 1; x <= 7; x++) for (let z = 1; z <= 7; z++) {
		const edge = x === 1 || x === 7 || z === 1 || z === 7;
		set(x, 0, z, edge ? s.base : s.floor);
	}
	// Walls, posts at the corners, windows on three sides and the door facing the street (west).
	for (let y = 1; y <= 3; y++) {
		for (let i = 1; i <= 7; i++) {
			for (const [x, z] of [[1, i], [7, i], [i, 1], [i, 7]]) {
				const corner = (x === 1 || x === 7) && (z === 1 || z === 7);
				set(x, y, z, corner ? s.post : s.wall);
			}
		}
	}
	const pane = (axis) => `minecraft:glass_pane[east=${axis === 'x'},west=${axis === 'x'},north=${axis === 'z'},south=${axis === 'z'},waterlogged=false]`;
	for (const x of [3, 5]) { set(x, 2, 1, pane('x')); set(x, 2, 7, pane('x')); }
	for (const z of [2, 6]) set(1, 2, z, pane('z'));
	set(7, 2, 4, pane('z'));
	set(1, 1, 4, `${s.door}[facing=east,half=lower,hinge=right,open=false,powered=false]`);
	set(1, 2, 4, `${s.door}[facing=east,half=upper,hinge=right,open=false,powered=false]`);
	// Torches either side of the door, on the outside of the wall.
	set(0, 3, 3, 'minecraft:wall_torch[facing=west]');
	set(0, 3, 5, 'minecraft:wall_torch[facing=west]');

	// The street joins at the doorstep; a villager joins in the middle of the floor.
	set(0, 0, 4, 'minecraft:jigsaw[orientation=west_up]', { id: 'minecraft:jigsaw', joint: 'aligned', name: 'minecraft:building_entrance',
		target: 'minecraft:building_entrance', pool: `minecraft:village/${biome}/streets`, final_state: stairs(s.roof, 'east') });
	set(4, 0, 4, 'minecraft:jigsaw[orientation=up_north]', { id: 'minecraft:jigsaw', joint: 'rollable', name: 'minecraft:bottom',
		target: 'minecraft:bottom', pool: `minecraft:village/${biome}/villagers`, final_state: s.floor });

	// The counter along the back wall, facing the room: a plant, the machine, a mug and a canister of beans.
	const bar = `espresso:${s.wood}_coffee_bar[facing=west]`;
	for (const z of [2, 3, 4, 5]) set(6, 1, z, bar);
	set(6, 2, 2, s.plant);
	set(6, 2, 3, 'espresso:espresso_machine[facing=west,beans=false]');
	set(6, 2, 4, 'espresso:placed_mug[facing=west,contents=empty,color=0]', { id: 'espresso:placed_mug', Mug: { id: 'espresso:mug', count: 1 } });
	set(6, 2, 5, 'espresso:canister[facing=west]', { id: 'espresso:canister', variant: { item: 'espresso:roasted_coffee_beans' }, amount: { __nbt: T.LONG, value: 12n } });
	set(6, 1, 6, 'minecraft:chest[facing=west,type=single,waterlogged=false]', { id: 'minecraft:chest', LootTable: 'espresso:chests/village_cafe' });
	// Two little tables by the windows, with a chair at each.
	for (const z of [2, 6]) {
		set(3, 1, z, `${s.fence}[east=false,west=false,north=false,south=false,waterlogged=false]`);
		set(3, 2, z, `${s.plate}[powered=false]`);
		set(4, 1, z, `${s.chair}[facing=east,half=bottom,shape=straight,waterlogged=false]`);
	}
	set(3, 3, 4, 'minecraft:lantern[hanging=true,waterlogged=false]');

	// Desert houses have flat roofs with a low rim; the others a hipped roof of stairs stepping inwards.
	if (s.flatRoof) {
		for (let x = 1; x <= 7; x++) for (let z = 1; z <= 7; z++) {
			set(x, 4, z, s.roofBlock);
			if (x === 1 || x === 7 || z === 1 || z === 7) set(x, 5, z, 'minecraft:smooth_sandstone_slab[type=bottom,waterlogged=false]');
		}
	}
	for (let k = 0; k <= 3 && !s.flatRoof; k++) {
		const y = 4 + k, lo = k, hi = 8 - k;
		for (let x = lo; x <= hi; x++) for (let z = lo; z <= hi; z++) {
			const onX = x === lo || x === hi, onZ = z === lo || z === hi;
			if (!onX && !onZ) { if (k === 0) set(x, y, z, s.roofBlock); else if (k === 3) set(x, y, z, s.roofBlock); continue; }
			let state;
			if (onX && onZ) {
				const facing = x === lo ? (z === lo ? 'south' : 'east') : (z === lo ? 'west' : 'north');
				state = stairs(s.roof, facing, 'outer_left');
			} else if (z === lo) state = stairs(s.roof, 'south');
			else if (z === hi) state = stairs(s.roof, 'north');
			else if (x === lo) state = stairs(s.roof, 'east');
			else state = stairs(s.roof, 'west');
			set(x, y, z, state);
		}
	}

	// Pack into a structure file.
	const palette = [], index = new Map();
	const blockList = [];
	for (const [key, { state, nbt }] of blocks) {
		if (!index.has(state)) { index.set(state, palette.length); palette.push(state); }
		const pos = key.split(',').map(Number);
		const entry = { pos: list(T.INT, pos), state: index.get(state) };
		if (nbt) entry.nbt = nbt;
		blockList.push(entry);
	}
	const paletteTag = palette.map(p => {
		const { id, properties } = parse(p);
		return properties ? { id, properties } : { id };
	});
	return write({
		size: list(T.INT, SIZE), entities: list(T.COMPOUND, []), blocks: list(T.COMPOUND, blockList),
		palette: list(T.COMPOUND, paletteTag), DataVersion: DATA_VERSION,
	});
}

for (const [biome, style] of Object.entries(STYLES)) {
	const f = path.join(ROOT, biome, 'cafe.nbt');
	fs.mkdirSync(path.dirname(f), { recursive: true });
	fs.writeFileSync(f, build(biome, style));
}
console.log('village cafés written to', ROOT);
module.exports = { STYLES };

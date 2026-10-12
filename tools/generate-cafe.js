// Writes the village café: a cottage café, built in each village's own materials, as structure
// files in the espresso_data pack. The mod adds them to village houses pools (see VillageCafe.java).
// Run by generate-data.js (which clears the pack first); can also be run on its own.
//
// The cottage: a front gable facing the street, a covered porch with stone-footed posts, gates, two
// little tables and hanging lanterns; a stone plinth, log posts and a beam under the eaves, tall
// windows with flower boxes, and a stone chimney at the back with a campfire on top for smoke.
// Inside, a counter along the back wall with the espresso machine and a cake, a fireplace in the
// chimney, two tables by the windows, a rug and a lantern.
//
// Layout (x is depth from the street, z across, y up): the street joins at x = 0, z = 6; the porch
// is x 1-3; the house x 4-10, z 2-10; the roof overhangs to x 3-10, z 1-11; the chimney is built into
// the back wall. (The file is stored shifted by one in z, as nothing sits at z = 0: 11 x 12 x 11.)
const fs = require('fs'), path = require('path');
const { T } = require('./nbt.js');
const { Structure, stairs, fence } = require('./structure.js');
const ROOT = path.join(__dirname, '..', 'src', 'main', 'resources', 'resourcepacks', 'espresso_data', 'data', 'espresso', 'structure', 'village');

// Each village's materials: plinth, walls, posts and beams (logs), floor and porch deck, roof
// (stairs, full block and slab), the stone wall for the porch footings and railing, the
// wood for the counter, door, furniture and fence posts, the chimney, the rug, two potted plants, and two flowers for the window boxes.
const STYLES = {
	plains: { porchWall: 'minecraft:cobblestone_wall', base: 'minecraft:cobblestone', wall: 'minecraft:oak_planks', post: 'minecraft:oak_log', beam: 'minecraft:stripped_oak_log',
		floor: 'minecraft:spruce_planks', deck: 'minecraft:spruce_planks', roof: 'minecraft:spruce', roofBlock: 'minecraft:spruce_planks', slab: 'minecraft:spruce_slab',
		wood: 'oak', door: 'minecraft:oak_door', chimney: 'minecraft:cobblestone', carpet: 'minecraft:brown_carpet',
		plants: ['minecraft:potted_azalea_bush', 'minecraft:potted_red_tulip'], flowers: ['minecraft:potted_oxeye_daisy', 'minecraft:potted_red_tulip'] },
	desert: { porchWall: 'minecraft:sandstone_wall', base: 'minecraft:sandstone', wall: 'minecraft:smooth_sandstone', post: 'minecraft:stripped_jungle_log', beam: 'minecraft:stripped_jungle_log',
		floor: 'minecraft:smooth_sandstone', deck: 'minecraft:jungle_planks', roof: 'minecraft:smooth_sandstone', roofBlock: 'minecraft:cut_sandstone', slab: 'minecraft:smooth_sandstone_slab',
		wood: 'jungle', door: 'minecraft:jungle_door', chimney: 'minecraft:sandstone', carpet: 'minecraft:orange_carpet',
		plants: ['minecraft:potted_cactus', 'minecraft:potted_dead_bush'], flowers: ['minecraft:potted_dandelion', 'minecraft:potted_cactus'] },
	savanna: { porchWall: 'minecraft:mud_brick_wall', base: 'minecraft:terracotta', wall: 'minecraft:acacia_planks', post: 'minecraft:acacia_log', beam: 'minecraft:stripped_acacia_log',
		floor: 'minecraft:acacia_planks', deck: 'minecraft:acacia_planks', roof: 'minecraft:spruce', roofBlock: 'minecraft:spruce_planks', slab: 'minecraft:spruce_slab',
		wood: 'acacia', door: 'minecraft:acacia_door', chimney: 'minecraft:cobblestone', carpet: 'minecraft:yellow_carpet',
		plants: ['minecraft:potted_acacia_sapling', 'minecraft:potted_orange_tulip'], flowers: ['minecraft:potted_orange_tulip', 'minecraft:potted_dandelion'] },
	taiga: { porchWall: 'minecraft:mossy_cobblestone_wall', base: 'minecraft:mossy_cobblestone', wall: 'minecraft:spruce_planks', post: 'minecraft:spruce_log', beam: 'minecraft:stripped_spruce_log',
		floor: 'minecraft:spruce_planks', deck: 'minecraft:spruce_planks', roof: 'minecraft:dark_oak', roofBlock: 'minecraft:dark_oak_planks', slab: 'minecraft:dark_oak_slab',
		wood: 'spruce', door: 'minecraft:spruce_door', chimney: 'minecraft:cobblestone', carpet: 'minecraft:green_carpet',
		plants: ['minecraft:potted_fern', 'minecraft:potted_spruce_sapling'], flowers: ['minecraft:potted_cornflower', 'minecraft:potted_lily_of_the_valley'] },
	snowy: { porchWall: 'minecraft:stone_brick_wall', base: 'minecraft:stone_bricks', wall: 'minecraft:stripped_spruce_wood', post: 'minecraft:spruce_log', beam: 'minecraft:spruce_log',
		floor: 'minecraft:spruce_planks', deck: 'minecraft:spruce_planks', roof: 'minecraft:spruce', roofBlock: 'minecraft:spruce_planks', slab: 'minecraft:spruce_slab',
		wood: 'spruce', door: 'minecraft:spruce_door', chimney: 'minecraft:stone_bricks', carpet: 'minecraft:red_carpet',
		plants: ['minecraft:potted_spruce_sapling', 'minecraft:potted_blue_orchid'], flowers: ['minecraft:potted_blue_orchid', 'minecraft:potted_white_tulip'] },
};

const pane = (along) => `minecraft:glass_pane[east=${along === 'x'},west=${along === 'x'},north=${along === 'z'},south=${along === 'z'},waterlogged=false]`;
const lantern = 'minecraft:lantern[hanging=true,waterlogged=false]';

function build(biome, s) {
	// Kept small so it fits on more village plots.
	const cafe = new Structure([11, 12, 11]);
	const set = (x, y, z, state, nbt) => cafe.set(x, y, z - 1, state, nbt);
	const wood = `minecraft:${s.wood}`;
	const log = (axis) => `${s.post}[axis=${axis}]`;
	const beam = (axis) => `${s.beam}[axis=${axis}]`;
	const chair = (facing) => stairs(wood, facing); // the back is on the `facing` side
	const table = (x, z) => { set(x, 1, z, fence(wood)); set(x, 2, z, `${wood}_pressure_plate[powered=false]`); };

	// Clear the porch and the room, so terrain is cleared; outside the footprint stays as it is.
	for (let x = 1; x <= 10; x++) for (let y = 1; y <= 4; y++) for (let z = 1; z <= 11; z++) set(x, y, z, 'minecraft:air');

	// Ground: the porch deck, and the house floor on a stone footing.
	for (let x = 1; x <= 10; x++) for (let z = 1; z <= 11; z++) {
		if (x <= 3) { if (z >= 3 && z <= 9) set(x, 0, z, s.deck); }
		else if (z >= 2 && z <= 10) set(x, 0, z, x === 4 || x === 10 || z === 2 || z === 10 ? s.base : s.floor);
	}

	// Walls: a stone plinth, the wall in between, a beam under the eaves, and log posts at the
	// corners, beside the door and halfway along the other walls.
	const isPost = (x, z) => ((x === 4 || x === 10) && (z === 2 || z === 10)) || (x === 4 && (z === 5 || z === 7))
		|| (x === 10 && z === 6) || ((z === 2 || z === 10) && x === 7);
	for (let y = 1; y <= 4; y++) {
		for (let x = 4; x <= 10; x++) for (let z = 2; z <= 10; z++) {
			if (x !== 4 && x !== 10 && z !== 2 && z !== 10) continue;
			const along = z === 2 || z === 10 ? 'x' : 'z';
			set(x, y, z, isPost(x, z) ? log('y') : y === 1 ? s.base : y === 4 ? beam(along) : s.wall);
		}
	}
	// Two-block-tall windows: either side of the door, two on each side wall and two at the back.
	for (const y of [2, 3]) {
		for (const z of [3, 4, 8, 9]) set(4, y, z, pane('z'));
		for (const x of [5, 6, 8, 9]) { set(x, y, 2, pane('x')); set(x, y, 10, pane('x')); }
		for (const z of [4, 5, 7]) set(10, y, z, pane('z'));
	}
	set(4, 1, 6, `${s.door}[facing=east,half=lower,hinge=right,open=false,powered=false]`);
	set(4, 2, 6, `${s.door}[facing=east,half=upper,hinge=right,open=false,powered=false]`);

	// A gable roof, ridge running back from the street, overhanging at the front and sides.
	for (let x = 3; x <= 10; x++) {
		for (let k = 0; k <= 4; k++) {
			set(x, 5 + k, 1 + k, stairs(s.roof, 'south'));
			set(x, 5 + k, 11 - k, stairs(s.roof, 'north'));
			if (k <= 3) { set(x, 5 + k, 2 + k, s.roofBlock); set(x, 5 + k, 10 - k, s.roofBlock); }
		}
		set(x, 9, 6, s.roofBlock);
		set(x, 10, 6, `${s.slab}[type=bottom,waterlogged=false]`);
	}
	// The gable ends, each with a little window, and a ceiling over the room.
	for (const x of [4, 10]) {
		for (let k = 0; k <= 3; k++) for (let z = 3 + k; z <= 9 - k; z++) set(x, 5 + k, z, s.wall);
		set(x, 6, 6, pane('z'));
		set(x, 7, 6, pane('z'));
	}
	for (let x = 5; x <= 9; x++) for (let z = 3; z <= 9; z++) set(x, 5, z, s.floor);

	// The porch, narrower than the house and centred on the door: posts, a railing of gates, a
	// lean-to roof with hanging lanterns, and a little table each side with a chair against the wall.
	// Each post is a stone wall footing with fence posts above it; a low stone wall is the railing.
	const wall = (sides, up) => `${s.porchWall}[${['east', 'north', 'south', 'west'].map(d => `${d}=${sides.includes(d) ? 'low' : 'none'}`).join(',')},up=${up},waterlogged=false]`;
	set(1, 1, 3, wall(['south', 'east'], true));
	set(1, 1, 5, wall(['north'], true));
	set(1, 1, 7, wall(['south'], true));
	set(1, 1, 9, wall(['north', 'east'], true));
	for (const z of [3, 5, 7, 9]) for (let y = 2; y <= 3; y++) set(1, y, z, fence(wood));
	// Between the posts, closed gates (sitting low between the stone footings); at each side a gate
	// and a short stone wall back to the house, with a flower pot on it.
	const gate = (facing) => `${wood}_fence_gate[facing=${facing},in_wall=true,open=false,powered=false]`;
	for (const z of [4, 8]) set(1, 1, z, gate('east'));
	for (const z of [3, 9]) {
		set(2, 1, z, gate('north'));
		set(3, 1, z, wall(['east', 'west'], true));
		set(3, 2, z, s.plants[1]);
	}
	for (let z = 3; z <= 9; z++) {
		set(1, 4, z, stairs(s.roof, 'east'));
		set(2, 4, z, `${s.slab}[type=top,waterlogged=false]`);
		set(3, 4, z, `${s.slab}[type=top,waterlogged=false]`);
	}
	for (const z of [4, 8]) {
		set(2, 4, z, s.roofBlock); // a lantern can't hang from a top slab
		set(2, 3, z, lantern);
		table(2, z);
		set(3, 1, z, chair('east'));
	}

	// The chimney, built into the back wall, with a campfire on top for a wisp of smoke, and a
	// fireplace in front of it: a fire under a stone hood up to the ceiling.
	for (let y = 0; y <= 10; y++) set(10, y, 8, s.chimney);
	set(10, 11, 8, 'minecraft:campfire[facing=north,lit=true,signal_fire=false,waterlogged=false]');
	set(9, 1, 8, 'minecraft:campfire[facing=west,lit=true,signal_fire=false,waterlogged=false]');
	for (let y = 2; y <= 4; y++) set(9, y, 8, s.chimney);

	// Flower boxes under the side windows: a shelf with potted flowers.
	for (const z of [1, 11]) for (const x of [5, 6, 8, 9]) {
		set(x, 1, z, `${wood}_slab[type=top,waterlogged=false]`);
		set(x, 2, z, s.flowers[(x + z) % 2]);
	}

	// Inside: the counter along the back wall with a cake, the machine, a mug and canisters of beans
	// and cherries (that one on a barrel by the fire); the café's chest; two tables by the side
	// windows; a rug; lanterns.
	for (const z of [4, 5, 6, 7]) set(9, 1, z, `espresso:${s.wood}_coffee_bar[facing=west]`);
	set(9, 2, 4, 'minecraft:cake[bites=0]');
	set(9, 2, 5, 'espresso:espresso_machine[facing=west,beans=false]');
	set(9, 2, 6, 'espresso:placed_mug[facing=west,contents=empty,color=0]', { id: 'espresso:placed_mug', Mug: { id: 'espresso:mug', count: 1 } });
	set(9, 2, 7, 'espresso:canister[facing=west]', { id: 'espresso:canister', variant: { item: 'espresso:roasted_coffee_beans' }, amount: { __nbt: T.LONG, value: 12n } });
	set(9, 1, 3, 'minecraft:chest[facing=west,type=single,waterlogged=false]', { id: 'minecraft:chest', LootTable: 'espresso:chests/village_cafe' });
	set(9, 2, 3, 'minecraft:potted_cactus'); // a little cactus on the chest, by the cake
	set(9, 1, 9, 'minecraft:barrel[facing=up,open=false]');
	set(9, 2, 9, 'espresso:canister[facing=west]', { id: 'espresso:canister', variant: { item: 'espresso:coffee_cherries' }, amount: { __nbt: T.LONG, value: 8n } });
	for (const z of [3, 9]) {
		table(6, z);
		set(5, 1, z, chair('west'));
		set(7, 1, z, chair('east'));
	}
	for (let x = 6; x <= 7; x++) for (let z = 5; z <= 7; z++) set(x, 1, z, s.carpet);
	set(7, 4, 6, lantern);
	set(5, 4, 6, lantern);

	// The street joins at the porch steps; a villager joins on a bare bit of floor by the counter
	// (nothing on top, since the marker is placed after the blocks around it).
	set(0, 0, 6, 'minecraft:jigsaw[orientation=west_up]', { id: 'minecraft:jigsaw', joint: 'aligned', name: 'minecraft:building_entrance',
		target: 'minecraft:building_entrance', pool: `minecraft:village/${biome}/streets`, final_state: stairs(wood, 'east') });
	set(8, 0, 6, 'minecraft:jigsaw[orientation=up_north]', { id: 'minecraft:jigsaw', joint: 'rollable', name: 'minecraft:bottom',
		target: 'minecraft:bottom', pool: `minecraft:village/${biome}/villagers`, final_state: s.floor });

	return cafe.toNbt();
}

// A teahouse café for Towns and Towers' Japanese villages, in their spruce, white plaster and
// cobblestone: a wooden veranda (engawa) with a lantern on a stone post either side, spruce posts
// and beams over white walls and paper screens, a pale floor, and a
// wide, low hipped roof of dark tiles with flared corners. Inside, low tables with cushions, a
// paper lantern, and the same counter and machine, in calm spruce, birch and tea green.
// Layout: the street joins at x = 0, z = 5; the veranda is x 1-2; the house x 3-9, z 1-9;
// the roof covers x 1-10, z 0-10.
function buildTeahouse() {
	const tea = new Structure([11, 10, 11]);
	const set = tea.set.bind(tea);
	const post = 'minecraft:stripped_spruce_log[axis=y]';
	const beam = (axis) => `minecraft:stripped_spruce_log[axis=${axis}]`;
	const roof = 'minecraft:deepslate_tile';
	const stair = (facing, half = 'bottom', shape = 'straight') => `${roof}_stairs[facing=${facing},half=${half},shape=${shape},waterlogged=false]`;
	const lantern = (hanging) => `minecraft:lantern[hanging=${hanging},waterlogged=false]`;
	// Paper screens: white glass, soft like shōji paper.
	const paper = (along) => `minecraft:white_stained_glass_pane[east=${along === 'x'},west=${along === 'x'},north=${along === 'z'},south=${along === 'z'},waterlogged=false]`;

	for (let x = 1; x <= 9; x++) for (let y = 1; y <= 3; y++) for (let z = 1; z <= 9; z++) set(x, y, z, 'minecraft:air');
	// Ground: the veranda's boards, and the house on a stone footing with a pale floor, like tatami.
	for (let z = 1; z <= 9; z++) for (const x of [1, 2]) set(x, 0, z, 'minecraft:spruce_planks');
	for (let x = 3; x <= 9; x++) for (let z = 1; z <= 9; z++) {
		set(x, 0, z, x === 3 || x === 9 || z === 1 || z === 9 ? 'minecraft:cobblestone' : 'minecraft:birch_planks');
	}

	// Walls: dark panelling, pale walls above, a beam under the eaves, posts every few blocks.
	const isPost = (x, z) => ((x === 3 || x === 9) && (z === 1 || z === 9)) || (x === 3 && (z === 4 || z === 6))
		|| ((z === 1 || z === 9) && x === 6) || (x === 9 && z === 5);
	for (let y = 1; y <= 3; y++) {
		for (let x = 3; x <= 9; x++) for (let z = 1; z <= 9; z++) {
			if (x !== 3 && x !== 9 && z !== 1 && z !== 9) continue;
			const along = z === 1 || z === 9 ? 'x' : 'z';
			set(x, y, z, isPost(x, z) ? post : y === 1 ? 'minecraft:spruce_planks' : y === 3 ? beam(along) : 'minecraft:calcite');
		}
	}
	for (const z of [2, 3, 7, 8]) set(3, 2, z, paper('z'));
	for (const x of [4, 5, 7, 8]) { set(x, 2, 1, paper('x')); set(x, 2, 9, paper('x')); }
	for (const z of [3, 7]) set(9, 2, z, paper('z'));
	// A birch door: its little panes look like a paper door.
	set(3, 1, 5, 'minecraft:birch_door[facing=east,half=lower,hinge=right,open=false,powered=false]');
	set(3, 2, 5, 'minecraft:birch_door[facing=east,half=upper,hinge=right,open=false,powered=false]');

	// The veranda: posts holding up the roof's edge, framing the steps; out on the ground in front,
	// a lantern on a stone post either side; and a potted cherry sapling.
	for (const z of [1, 3, 7, 9]) for (let y = 1; y <= 3; y++) set(1, y, z, post);
	for (const z of [2, 8]) {
		set(0, 0, z, 'minecraft:mossy_cobblestone');
		set(0, 1, z, 'minecraft:stone_brick_wall[east=none,north=none,south=none,west=none,up=true,waterlogged=false]');
		set(0, 2, z, lantern(false));
	}
	set(2, 1, 8, 'minecraft:potted_cherry_sapling');

	// A wide, low hipped roof, overhanging the veranda, with the corners flared up.
	for (let k = 0; k <= 4; k++) {
		const y = 4 + k, x0 = 1 + k, x1 = 10 - k, z0 = k, z1 = 10 - k;
		if (x0 > x1) break;
		for (let x = x0; x <= x1; x++) for (let z = z0; z <= z1; z++) {
			const onX = x === x0 || x === x1, onZ = z === z0 || z === z1;
			if (!onX && !onZ) {
				if (k === 0) set(x, y, z, 'minecraft:spruce_planks'); // the ceiling
				continue;
			}
			if (onX && onZ) {
				const facing = x === x0 ? (z === z0 ? 'south' : 'east') : (z === z0 ? 'west' : 'north');
				set(x, y, z, k === 0 ? stair(facing, 'top', 'outer_left') : stair(facing, 'bottom', 'outer_left'));
			} else {
				set(x, y, z, stair(z === z0 ? 'south' : z === z1 ? 'north' : x === x0 ? 'east' : 'west'));
			}
		}
	}
	for (let z = 4; z <= 6; z++) for (let x = 5; x <= 6; x++) set(x, 9, z, `${roof}_slab[type=bottom,waterlogged=false]`);

	// Inside: the counter along the back, two low tables with cushions, and a paper lantern.
	for (const z of [3, 4, 5, 6, 7]) set(8, 1, z, 'espresso:spruce_coffee_bar[facing=west]');
	set(8, 2, 3, 'minecraft:potted_bamboo');
	set(8, 2, 4, 'espresso:espresso_machine[facing=west,beans=false]');
	set(8, 2, 5, 'espresso:placed_mug[facing=west,contents=empty,color=0]', { id: 'espresso:placed_mug', Mug: { id: 'espresso:mug', count: 1 } });
	set(8, 2, 6, 'espresso:canister[facing=west]', { id: 'espresso:canister', variant: { item: 'espresso:roasted_coffee_beans' }, amount: { __nbt: T.LONG, value: 12n } });
	set(8, 2, 7, 'espresso:canister[facing=west]', { id: 'espresso:canister', variant: { item: 'espresso:coffee_cherries' }, amount: { __nbt: T.LONG, value: 8n } });
	set(8, 1, 2, 'minecraft:chest[facing=west,type=single,waterlogged=false]', { id: 'minecraft:chest', LootTable: 'espresso:chests/village_cafe' });
	set(8, 2, 2, 'minecraft:potted_cactus'); // a little cactus on the chest
	set(8, 1, 8, 'minecraft:barrel[facing=up,open=false]');
	for (const z of [3, 7]) {
		set(5, 1, z, 'minecraft:spruce_slab[type=bottom,waterlogged=false]');
		set(4, 1, z, 'minecraft:green_carpet');
		set(6, 1, z, 'minecraft:green_carpet');
	}
	set(5, 3, 5, lantern(true));

	set(0, 0, 5, 'minecraft:jigsaw[orientation=west_up]', { id: 'minecraft:jigsaw', joint: 'aligned', name: 'minecraft:building_entrance',
		target: 'minecraft:building_entrance', pool: 'minecraft:empty', final_state: 'minecraft:spruce_stairs[facing=east,half=bottom,shape=straight,waterlogged=false]' });
	set(7, 0, 5, 'minecraft:jigsaw[orientation=up_north]', { id: 'minecraft:jigsaw', joint: 'rollable', name: 'minecraft:bottom',
		target: 'minecraft:bottom', pool: 'minecraft:village/plains/villagers', final_state: 'minecraft:birch_planks' });
	return tea.toNbt();
}

for (const [biome, style] of Object.entries(STYLES)) {
	const f = path.join(ROOT, biome, 'cafe.nbt');
	fs.mkdirSync(path.dirname(f), { recursive: true });
	fs.writeFileSync(f, build(biome, style));
}
fs.mkdirSync(path.join(ROOT, 'japanese'), { recursive: true });
fs.writeFileSync(path.join(ROOT, 'japanese', 'cafe.nbt'), buildTeahouse());
console.log('village cafés written to', ROOT);
module.exports = { STYLES };

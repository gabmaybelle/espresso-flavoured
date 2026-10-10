// Draws the coffee textures (16x16 items and bush stages, 18x18 effect icons) and writes
// their models, block states, item definitions and English names.
//   node tools/generate-coffee-assets.js
const fs = require('fs'), path = require('path'), zlib = require('zlib');
const A = path.join(__dirname, '..', 'src', 'main', 'resources', 'assets', 'espresso');
const out = (rel, obj) => {
	const f = path.join(A, ...rel.split('/'));
	fs.mkdirSync(path.dirname(f), { recursive: true });
	fs.writeFileSync(f, JSON.stringify(obj, null, 2) + '\n');
};

// ---------------------------------------------------------------- PNG
const T = []; for (let n = 0; n < 256; n++) { let c = n; for (let k = 0; k < 8; k++) c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1; T[n] = c >>> 0; }
const crc = b => { let x = 0xffffffff; for (const v of b) x = T[(x ^ v) & 255] ^ (x >>> 8); return (x ^ 0xffffffff) >>> 0; };
const chunk = (t, d) => { const l = Buffer.alloc(4); l.writeUInt32BE(d.length); const td = Buffer.concat([Buffer.from(t), d]); const c = Buffer.alloc(4); c.writeUInt32BE(crc(td)); return Buffer.concat([l, td, c]); };
/** Draws a pixel map: one character per pixel, '.' is transparent. */
function png(rel, rows, palette) {
	const h = rows.length, w = rows[0].length;
	const raw = [];
	for (const row of rows) {
		if (row.length !== w) throw new Error(`${rel}: row "${row}" is ${row.length} wide, expected ${w}`);
		const r = Buffer.alloc(1 + w * 4);
		[...row].forEach((ch, x) => { const c = palette[ch]; if (c) r.set([c[0], c[1], c[2], c[3] ?? 255], 1 + x * 4); });
		raw.push(r);
	}
	const ih = Buffer.alloc(13); ih.writeUInt32BE(w, 0); ih.writeUInt32BE(h, 4); ih[8] = 8; ih[9] = 6;
	const f = path.join(A, 'textures', ...rel.split('/')) + '.png';
	fs.mkdirSync(path.dirname(f), { recursive: true });
	fs.writeFileSync(f, Buffer.concat([Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]), chunk('IHDR', ih), chunk('IDAT', zlib.deflateSync(Buffer.concat(raw))), chunk('IEND', Buffer.alloc(0))]));
}

// ---------------------------------------------------------------- palettes
const LEAF = { g: [47, 96, 38], G: [74, 132, 54], h: [104, 162, 72], s: [88, 64, 40] };
const CHERRY = { r: [150, 28, 30], R: [214, 60, 52], w: [250, 170, 150] };
const UNRIPE = { u: [128, 168, 64], U: [164, 196, 92] };
const BEAN = { k: [44, 24, 12], b: [112, 64, 34], B: [164, 104, 58], c: [64, 34, 16] };
const GLASS = { o: [86, 58, 34], O: [130, 92, 56], q: [196, 214, 226, 220], Q: [236, 244, 250, 235] };

// ---------------------------------------------------------------- items
png('item/coffee_cherries', [
	'................',
	'..........sG....',
	'.........sGhG...',
	'........s..G....',
	'.......s...s....',
	'......s.....s...',
	'....rrr....rrr..',
	'...rRwrr..rRwrr.',
	'...rRRrr..rRRrr.',
	'...rrrrr..rrrrr.',
	'....rrr.s..rrr..',
	'.......rrr......',
	'......rRwrr.....',
	'......rRRrr.....',
	'......rrrrr.....',
	'.......rrr......',
], { ...LEAF, ...CHERRY });

png('item/roasted_coffee_beans', [
	'................',
	'................',
	'...kkkk.........',
	'..kbBbbk........',
	'.kbBbcbbk.......',
	'.kbbbcbbk.......',
	'.kbbcbbbk.......',
	'..kbcbbk..kkkk..',
	'...kkkk..kbBbbk.',
	'........kbBbcbbk',
	'........kbbbcbbk',
	'........kbbcbbbk',
	'.........kbcbbk.',
	'..........kkkk..',
	'................',
	'................',
], BEAN);

// ---------------------------------------------------------------- mugs
// Mugs are drawn in greys and tinted by their dye colour (cream when undyed); a drink adds
// an untinted second layer with the coffee and a curl of steam.
const grid = (fn) => Array.from({ length: 16 }, (_, y) => Array.from({ length: 16 }, (_, x) => fn(x, y)).join(''));
const MUG = { W: [255, 255, 255], B: [226, 226, 226], S: [178, 178, 178], i: [120, 120, 120] };
const mugShape = (x, y) => {
	if (y === 3 && x >= 4 && x <= 10) return 'W';
	if (y === 4) return x === 3 || x === 11 ? 'W' : (x >= 4 && x <= 10 ? 'i' : '.');
	if (y === 5 && x >= 3 && x <= 11) return 'W';
	if (y >= 6 && y <= 13 && x >= 3 && x <= 11) return x === 3 ? 'W' : (x === 11 || y === 13 ? 'S' : 'B');
	if (y === 14 && x >= 4 && x <= 10) return 'S';
	// The handle.
	if ((y === 6 || y === 11) && (x === 12 || x === 13)) return 'B';
	if ((y === 7 || y === 10) && x === 13) return 'B';
	if (y >= 7 && y <= 10 && x === 14) return 'S';
	return '.';
};
png('item/mug', grid(mugShape), MUG);
png('item/unfired_mug', grid(mugShape), { W: [176, 182, 196], B: [160, 166, 179], S: [128, 134, 148], i: [104, 108, 120] });
/** A drink's layer: the coffee in the mouth of the mug, and steam above. */
function drinkLayer(name, liquid, top, detail) {
	png(`item/${name}_fill`, grid((x, y) => {
		if (y === 4 && x >= 4 && x <= 10) return x === 6 || x === 9 ? 'T' : (x === 8 ? 'D' : 'L');
		if (y === 2 && (x === 6 || x === 9)) return 's';
		if (y === 1 && (x === 7 || x === 8)) return 's';
		if (y === 0 && x === 7) return 's';
		return '.';
	}), { L: liquid, T: top, D: detail, s: [240, 240, 240, 150] });
}
drinkLayer('espresso', [58, 30, 14], [196, 136, 78], [44, 22, 10]);
drinkLayer('latte', [178, 128, 84], [246, 238, 222], [160, 112, 72]);
drinkLayer('mocha_latte', [104, 58, 32], [250, 246, 238], [70, 36, 18]);
for (const old of ['espresso', 'latte', 'mocha_latte']) fs.rmSync(path.join(A, 'textures', 'item', old + '.png'), { force: true });

// Placed mugs: glazed sides, a rim, and the drink's surface.
png('block/mug', grid((x, y) => (y === 0 ? 'W' : (y >= 14 ? 'S' : ((x + y * 3) % 11 === 0 ? 'S' : 'B')))), MUG);
png('block/mug_inside', grid((x, y) => (x <= 1 || x >= 14 || y <= 1 || y >= 14 ? 'W' : 'i')), MUG);
for (const [name, liquid, top] of [['espresso', [58, 30, 14], [196, 136, 78]], ['latte', [178, 128, 84], [246, 238, 222]], ['mocha', [104, 58, 32], [250, 246, 238]]]) {
	// A round of crema or foam in the middle.
	png(`block/mug_${name}`, grid((x, y) => ((x - 7.5) ** 2 + (y - 7.5) ** 2 < 14 ? 'T' : 'L')), { L: liquid, T: top });
}

// ---------------------------------------------------------------- bush stages
png('block/coffee_bush_stage0', [
	'................', '................', '................', '................',
	'................', '................', '................', '................',
	'................', '................', '.......G........', '......GhG.......',
	'....G..sG.G.....', '...GhG.s.GhG....', '....s..s..s.....', '.......s........',
], LEAF);
png('block/coffee_bush_stage1', [
	'................', '................', '................', '................',
	'................', '.......G........', '......GhG.......', '...G..gGg..G....',
	'..GhG.gsg.GhG...', '..gGgg.s.ggGg...', '...g.gGsGg.g....', '..GhG.gsg.GhG...',
	'..gGg..s..gGg...', '....s..s..s.....', '.....s.s.s......', '.......s........',
], LEAF);
png('block/coffee_bush_stage2', [
	'................', '................', '......G.G.......', '.....GhGhG......',
	'...G.gGgGg.G....', '..GhG.gsg.GhG...', '..gGguUsgugGg...', '...g.gGsGg.u....',
	'..GhGuUsgGhG....', '.ugGg.gsg.gGgu..', '.Uu.gGgsgGg.Uu..', '..GhG.gsguGhG...',
	'..gGg..sUugGg...', '....s..s..s.....', '.....s.s.s......', '.......s........',
], { ...LEAF, ...UNRIPE });
png('block/coffee_bush_stage3', [
	'................', '................', '......G.G.......', '.....GhGhG......',
	'...G.gGgGg.G....', '..GhG.gsg.GhG...', '..gGgrRsgrgGg...', '...g.gGsGg.r....',
	'..GhGrRsgGhG....', '.rgGg.gsg.gGgr..', '.Rr.gGgsgGg.Rr..', '..GhG.gsgrGhG...',
	'..gGg..sRrgGg...', '....s..s..s.....', '.....s.s.s......', '.......s........',
], { ...LEAF, ...CHERRY });

// ---------------------------------------------------------------- effect icons (18x18)
png('mob_effect/drowsy', [
	'..................', '..................', '..........zzzz....', '.............z....',
	'............z.....', '...........z......', '..........zzzz....', '..................',
	'..................', '..eeeeeeeeeeeee...', '.e.............e..', '..eeeeeeeeeeeee...',
	'....l.l.l.l.l.....', '..................', '..................', '..................',
	'..................', '..................',
], { z: [196, 186, 230], e: [120, 100, 150], l: [90, 74, 118] });
png('mob_effect/caffeinated', [
	'..................', '..................', '..................', '......kkkkk.......',
	'.....kbBbbbk......', '....kbBbbcbbk.....', '....kbBbcbbbk.....', '....kbbbcbbbk.....',
	'....kbbcbbbbk.....', '....kbbcbbbbk.....', '....kbcbbbbbk.....', '....kbcbbbbbk.....',
	'.....kbcbbbk......', '......kkkkk.......', '..................', '..................',
	'..................', '..................',
], BEAN);

// ---------------------------------------------------------------- models, states, names
out('blockstates/coffee_bush.json', { variants: Object.fromEntries([0, 1, 2, 3].map(a => [`age=${a}`, { model: `espresso:block/coffee_bush_stage${a}` }])) });
for (const a of [0, 1, 2, 3]) out(`models/block/coffee_bush_stage${a}.json`, { parent: 'minecraft:block/cross', textures: { cross: `espresso:block/coffee_bush_stage${a}` } });
for (const item of ['coffee_cherries', 'roasted_coffee_beans', 'unfired_mug']) {
	out(`items/${item}.json`, { model: { type: 'minecraft:model', model: `espresso:item/${item}` } });
	out(`models/item/${item}.json`, { parent: 'minecraft:item/generated', textures: { layer0: `espresso:item/${item}` } });
}
const PLAIN_MUG = 0xF0E4CF - 0x1000000; // Mugs.PLAIN as a signed ARGB int
const dyed = (model) => ({ model: { type: 'minecraft:model', model, tints: [{ type: 'minecraft:dye', default: PLAIN_MUG }] } });
out('items/mug.json', dyed('espresso:item/mug'));
out('models/item/mug.json', { parent: 'minecraft:item/generated', textures: { layer0: 'espresso:item/mug' } });
for (const item of ['espresso', 'latte', 'mocha_latte']) {
	out(`items/${item}.json`, dyed(`espresso:item/${item}`));
	out(`models/item/${item}.json`, { parent: 'minecraft:item/generated', textures: { layer0: 'espresso:item/mug', layer1: `espresso:item/${item}_fill` } });
}

// Placed mug: tinted body and handle (tintindex 0), rim on top, and the drink's surface.
const tinted = (faces) => Object.fromEntries(Object.entries(faces).map(([f, t]) => [f, { texture: t, tintindex: 0 }]));
const sides = (t) => ({ north: t, south: t, east: t, west: t });
const mugElements = [
	{ from: [5, 0, 5], to: [11, 6, 11], faces: tinted({ ...sides('#mug'), down: '#mug', up: '#inside' }) },
	{ from: [11, 4, 7.5], to: [12.5, 5, 8.5], faces: tinted({ ...sides('#mug'), up: '#mug', down: '#mug' }) },
	{ from: [11, 1, 7.5], to: [12.5, 2, 8.5], faces: tinted({ ...sides('#mug'), up: '#mug', down: '#mug' }) },
	{ from: [12.5, 1, 7.5], to: [13.5, 5, 8.5], faces: tinted({ ...sides('#mug'), up: '#mug', down: '#mug' }) },
];
const mugTextures = { particle: 'espresso:block/mug', mug: 'espresso:block/mug', inside: 'espresso:block/mug_inside' };
out('models/block/mug_empty.json', { parent: 'minecraft:block/block', textures: mugTextures, elements: mugElements });
out('models/block/mug_full.json', { parent: 'minecraft:block/block', textures: mugTextures,
	elements: [...mugElements, { from: [6, 6.01, 6], to: [10, 6.01, 10], faces: { up: { texture: '#liquid' } } }] });
for (const c of ['espresso', 'latte', 'mocha']) out(`models/block/mug_${c}.json`, { parent: 'espresso:block/mug_full', textures: { liquid: `espresso:block/mug_${c}` } });
out('blockstates/placed_mug.json', { variants: Object.fromEntries(
	[['north', 0], ['east', 90], ['south', 180], ['west', 270]].flatMap(([facing, y]) =>
		['empty', 'espresso', 'latte', 'mocha'].map(c => [`contents=${c},facing=${facing}`, { model: `espresso:block/mug_${c}`, ...(y ? { y } : {}) }])),
) });
// ---------------------------------------------------------------- espresso machine
// Drawn procedurally: brushed steel with a pressure gauge, dark fittings, a glass bean hopper.
const STEEL = { a: [150, 155, 162], b: [176, 181, 188], c: [202, 206, 212], e: [112, 116, 124],
	d: [52, 52, 58], D: [78, 78, 86], w: [236, 232, 220], n: [150, 40, 30], r: [184, 115, 51], R: [214, 150, 80] };
const brushed = (x, y) => (x === 0 || x === 15 || y === 0 || y === 15) ? 'e' : ((y + (x >> 2)) % 3 === 0 ? 'c' : ((x * 7 + y * 3) % 5 === 0 ? 'a' : 'b'));
png('block/espresso_machine_side', grid((x, y) => {
	// A small gauge at (7..9, 8..10): copper rim, cream face, red needle.
	if (x >= 6 && x <= 10 && y >= 7 && y <= 11) {
		const rim = x === 6 || x === 10 || y === 7 || y === 11;
		const corner = (x === 6 || x === 10) && (y === 7 || y === 11);
		if (corner) return brushed(x, y);
		if (rim) return 'r';
		return (x === 8 && y === 9) || (x === 9 && y === 8) ? 'n' : 'w';
	}
	if (y === 4 || y === 15) return 'e';
	return brushed(x, y);
}), STEEL);
png('block/espresso_machine_front', grid((x, y) => {
	if (y >= 9 && y <= 14 && x >= 5 && x <= 10) return (x === 5 || x === 10 || y === 9) ? 'D' : 'd'; // cup alcove
	if (y === 5 || y === 6) return x % 2 ? 'r' : 'R'; // copper band
	if (y === 4 || y === 15) return 'e';
	return brushed(x, y);
}), STEEL);
png('block/espresso_machine_top', grid((x, y) => (x === 0 || x === 15 || y === 0 || y === 15) ? 'e' : (y % 3 === 1 && x > 3 && x < 12 ? 'D' : brushed(x, y))), STEEL);
png('block/espresso_machine_dark', grid((x, y) => (x + y) % 4 === 0 ? 'D' : ((x * y) % 7 === 1 ? 'e' : 'd')), STEEL);
const HOPPER = { ...STEEL, g: [196, 214, 226, 150], G: [236, 244, 250, 190], ...BEAN };
const hopper = (filled) => grid((x, y) => {
	if (y <= 1 || x === 0 || x === 15) return 'd';
	if (filled && y >= 6) return (x * 3 + y * 5) % 7 === 0 ? 'k' : ((x + y) % 3 ? 'b' : 'B');
	return (x + y) % 6 === 0 ? 'G' : 'g';
});
png('block/espresso_machine_hopper', hopper(false), HOPPER);
png('block/espresso_machine_hopper_filled', hopper(true), HOPPER);

const machineFaces = (front) => ({
	north: { texture: front, uv: [3, 4, 13, 16] }, south: { texture: '#side', uv: [3, 4, 13, 16] },
	east: { texture: '#side', uv: [4, 4, 12, 16] }, west: { texture: '#side', uv: [4, 4, 12, 16] },
	up: { texture: '#top', uv: [3, 6, 13, 14] }, down: { texture: '#dark', uv: [3, 2, 13, 10] },
});
const all = (texture) => Object.fromEntries(['north', 'south', 'east', 'west', 'up', 'down'].map(f => [f, { texture }]));
out('models/block/espresso_machine.json', {
	parent: 'minecraft:block/block',
	textures: { particle: 'espresso:block/espresso_machine_side', side: 'espresso:block/espresso_machine_side',
		front: 'espresso:block/espresso_machine_front', top: 'espresso:block/espresso_machine_top',
		dark: 'espresso:block/espresso_machine_dark', hopper: 'espresso:block/espresso_machine_hopper' },
	elements: [
		{ from: [3, 0, 6], to: [13, 12, 14], faces: machineFaces('#front') },
		{ from: [4, 0, 1], to: [12, 1, 6], faces: all('#dark') },
		{ from: [4, 8, 2], to: [12, 11, 6], faces: all('#dark') },
		{ from: [7, 6, 3], to: [9, 8, 5], faces: all('#dark') },
		{ from: [5, 12, 8], to: [11, 15, 13], faces: { ...all('#hopper'), up: { texture: '#dark' }, down: { texture: '#dark' } } },
	],
});
out('models/block/espresso_machine_filled.json', {
	parent: 'espresso:block/espresso_machine', textures: { hopper: 'espresso:block/espresso_machine_hopper_filled' },
});
out('blockstates/espresso_machine.json', { variants: Object.fromEntries(
	[['north', 0], ['east', 90], ['south', 180], ['west', 270]].flatMap(([facing, y]) =>
		[false, true].map(beans => [`beans=${beans},facing=${facing}`,
			{ model: `espresso:block/espresso_machine${beans ? '_filled' : ''}`, ...(y ? { y } : {}) }])),
) });

// The machine's menu (176x166 in a 256x256 sheet), in the vanilla container style, with
// the coffee drip progress bar beside it at (176, 0).
{
	const W = 256, H = 256, px = Array.from({ length: H }, () => Array(W).fill('.'));
	const rect = (x0, y0, w, h, c) => { for (let y = y0; y < y0 + h; y++) for (let x = x0; x < x0 + w; x++) px[y][x] = c; };
	// Panel: black outline with clipped corners, white top-left bevel, grey bottom-right bevel.
	rect(0, 0, 176, 166, 'K');
	rect(1, 1, 174, 164, 'W');
	rect(3, 3, 172, 162, 'S');
	rect(3, 3, 170, 160, 'P');
	for (const [x, y] of [[0, 0], [1, 0], [0, 1], [175, 0], [174, 0], [175, 1], [0, 165], [1, 165], [0, 164], [175, 165], [174, 165], [175, 164]]) px[y][x] = '.';
	const slot = (x, y) => { rect(x - 1, y - 1, 18, 18, 'D'); rect(x, y, 17, 17, 'W'); rect(x, y, 16, 16, 'M'); };
	for (const [x, y] of [[56, 51], [79, 58], [102, 51], [79, 17]]) slot(x, y);
	for (let r = 0; r < 3; r++) for (let c = 0; c < 9; c++) slot(8 + c * 18, 84 + r * 18);
	for (let c = 0; c < 9; c++) slot(8 + c * 18, 142);
	// The drip channel from the beans down to the middle bottle, and little pipes to the side bottles.
	rect(83, 36, 9, 20, 'D'); rect(84, 37, 7, 18, 'M');
	rect(73, 48, 10, 2, 'D'); rect(92, 48, 10, 2, 'D');
	rect(176, 0, 9, 20, 'c'); for (let y = 0; y < 20; y++) { px[y][176] = 'k'; px[y][184] = 'k'; if (y % 4 === 1) px[y][179] = 'C'; }
	png('gui/container/espresso_machine', px.map(r => r.join('')), {
		K: [0, 0, 0], W: [255, 255, 255], S: [85, 85, 85], P: [198, 198, 198], D: [55, 55, 55], M: [139, 139, 139],
		c: [112, 64, 34], C: [164, 104, 58], k: [64, 34, 16],
	});
}
out('items/espresso_machine.json', { model: { type: 'minecraft:model', model: 'espresso:block/espresso_machine_filled' } });

out('lang/en_us.json', {
	'block.espresso.coffee_bush': 'Coffee Bush',
	'item.espresso.coffee_cherries': 'Coffee Cherries',
	'item.espresso.roasted_coffee_beans': 'Roasted Coffee Beans',
	'item.espresso.espresso': 'Espresso',
	'item.espresso.latte': 'Latte',
	'item.espresso.mocha_latte': 'Mocha Latte',
	'effect.espresso.drowsy': 'Drowsy',
	'effect.espresso.caffeinated': 'Caffeinated',
	'block.espresso.espresso_machine': 'Espresso Machine',
	'item.espresso.mug': 'Mug',
	'item.espresso.unfired_mug': 'Unfired Mug',
	'block.espresso.placed_mug': 'Mug',
	'container.espresso.espresso_machine': 'Espresso Machine',
	"espresso.options.title": "Espresso Flavoured Settings",
	"espresso.options.section.home": "Home and Builds",
	"espresso.options.section.days": "Days and Nights",
	"espresso.options.section.wellbeing": "Looking After Yourself",
	"espresso.options.section.storage": "Storage and Trading",
	"espresso.options.safeHomeRadius": "Safe Home Radius",
	"espresso.options.safeHomeRadius.tooltip": "No monsters spawn naturally within this many blocks of your home bed (crouch and use a bed with an empty hand to set it).",
	"espresso.options.keepHotbarOnDeath": "Keep Hotbar on Death",
	"espresso.options.keepHotbarOnDeath.tooltip": "Keep your hotbar, armour and off-hand when you die. Everything else stays where you fell.",
	"espresso.options.creeperGriefing": "Creepers Break Blocks",
	"espresso.options.creeperGriefing.tooltip": "Off: creeper explosions still hurt, but leave your builds alone.",
	"espresso.options.endermanGriefing": "Endermen Take Blocks",
	"espresso.options.endermanGriefing.tooltip": "Off: endermen leave blocks where they are.",
	"espresso.options.longerDays": "Longer Days",
	"espresso.options.longerDays.tooltip": "20-minute days and 10-minute nights. Takes effect after restarting the game.",
	"espresso.options.toughNights": "Tougher Nights",
	"espresso.options.toughNights.tooltip": "A few more monsters at night, with a little more health and damage.",
	"espresso.options.phantoms": "Phantoms",
	"espresso.options.phantoms.tooltip": "Off: phantoms never visit, so sleeping is up to you.",
	"espresso.options.regenNeedsSaturation": "Heal Only When Fed",
	"espresso.options.regenNeedsSaturation.tooltip": "Natural healing only happens when you have eaten recently.",
	"espresso.options.drowsiness": "Drowsiness",
	"espresso.options.drowsiness.tooltip": "Become Drowsy after two days without sleeping at home, and Hungry if you haven't eaten in that time either.",
	"espresso.options.bundleUnstackables": "Tools per Bundle",
	"espresso.options.bundleUnstackables.tooltip": "How many tools or pieces of armour fit in one bundle (1 is vanilla).",
	"espresso.options.blockMerchant": "Block Merchant",
	"espresso.options.blockMerchant.tooltip": "The wandering trader also sells building blocks from far-off biomes.",
});
console.log('coffee assets written');

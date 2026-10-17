// Draws the coffee textures (16x16 items and bush stages, 18x18 effect icons) and writes
// their models, block states, item definitions and English names.
//   node tools/generate-coffee-assets.js
const fs = require('fs'), path = require('path'), zlib = require('zlib');
const A = path.join(__dirname, '..', 'common', 'src', 'main', 'resources', 'assets', 'espresso_flavoured');
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
// Decaf beans: the same beans, washed paler, with a little water drop.
png('item/decaf_coffee_beans', [
	'................',
	'............w...',
	'...kkkk....wWw..',
	'..kbBbbk...wWw..',
	'.kbBbcbbk...w...',
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
], { k: [96, 64, 40], b: [176, 132, 92], B: [212, 172, 126], c: [128, 90, 58], w: [120, 170, 220], W: [200, 230, 250] });

// ---------------------------------------------------------------- mugs
// Mugs are drawn in greys and tinted by their dye colour (cream when undyed); a drink adds
// an untinted second layer with the coffee and a curl of steam.
const grid18 = (fn) => Array.from({ length: 18 }, (_, y) => Array.from({ length: 18 }, (_, x) => fn(x, y)).join(''));
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
/**
 * A drink's layer, drawn over the tinted mug: the drink in the mouth of the mug, steam, and
 * the drink's own detail (from {@code extra}) so each one is easy to tell apart at a glance.
 */
function drinkLayer(name, liquid, top, detail, extra = () => null, extraPalette = {}, steam = true) {
	png(`item/${name}_fill`, grid((x, y) => {
		const e = extra(x, y);
		if (e) return e;
		if (y === 4 && x >= 4 && x <= 10) return x === 6 || x === 9 ? 'T' : (x === 8 ? 'D' : 'L');
		if (steam && ((y === 2 && (x === 6 || x === 9)) || (y === 1 && (x === 7 || x === 8)) || (y === 0 && x === 7))) return 's';
		return '.';
	}), { L: liquid, T: top, D: detail, s: [240, 240, 240, 150], ...extraPalette });
}
const WHITE = [252, 250, 246], CREAM = [238, 228, 210];
/** A dome of foam or cream rising above the rim. */
const foam = (x, y) => (y === 3 && x >= 4 && x <= 10) || (y === 2 && x >= 5 && x <= 9) || (y === 1 && x >= 6 && x <= 8);
drinkLayer('espresso', [58, 30, 14], [196, 136, 78], [44, 22, 10]);
// Latte: a tall foam cap with a little leaf.
drinkLayer('latte', [178, 128, 84], [246, 238, 222], [160, 112, 72],
	(x, y) => (y === 2 && x === 7 ? 'K' : foam(x, y) ? (x === 4 || x === 10 ? 'c' : 'F') : null), { F: WHITE, c: CREAM, K: [150, 104, 64] }, false);
// Mocha: cream on top and chocolate drizzled down the side.
drinkLayer('mocha_latte', [104, 58, 32], [250, 246, 238], [70, 36, 18],
	(x, y) => ((x === 5 && y >= 5 && y <= 8) || (x === 8 && y >= 5 && y <= 10) || (x === 10 && y >= 5 && y <= 7) ? 'C'
		: (y === 3 && x >= 5 && x <= 9) || (y === 2 && x >= 6 && x <= 8) ? 'F' : null), { F: WHITE, C: [92, 48, 26] }, false);
for (const old of ['espresso', 'latte', 'mocha_latte']) fs.rmSync(path.join(A, 'textures', 'item', old + '.png'), { force: true });

// Placed mugs: a rounded glazed mug on a matching saucer (both tinted to the mug's colour),
// with each drink's own surface and toppings.
png('block/mug_glaze', grid((x, y) => (y <= 9 ? 'W' : y >= 15 ? 'S' : 'B')), MUG);
png('block/mug_rim', grid((x, y) => (x <= 1 || x >= 14 || y <= 1 || y >= 14 ? 'W' : 'i')), MUG);
png('block/saucer', grid((x, y) => {
	const d = Math.max(Math.abs(x - 7.5), Math.abs(y - 7.5));
	return d > 6.5 ? 'S' : d > 5.5 ? 'W' : d < 3.5 ? 'S' : 'B';
}), MUG);
// Each drink's surface: liquid, with a round of crema or foam (T) and the odd detail (D).
const SURFACES = {
	espresso: [[58, 30, 14], [196, 136, 78], [44, 22, 10]],
	ristretto: [[36, 18, 8], [214, 150, 80], [24, 12, 6]],
	latte: [[178, 128, 84], [246, 238, 222], [160, 112, 72]],
	mocha: [[104, 58, 32], [250, 246, 238], [70, 36, 18]],
	vanilla: [[214, 178, 120], [250, 244, 230], [120, 80, 40]],
	pumpkin_spice: [[196, 120, 60], [246, 214, 170], [150, 82, 40]],
	iced: [[58, 30, 14], [140, 90, 60], [44, 22, 10]],
	night_shift: [[36, 24, 18], [70, 46, 24], [252, 222, 96]],
	hot_chocolate: [[92, 50, 28], [120, 68, 40], [64, 34, 18]],
	honey: [[196, 142, 82], [246, 234, 204], [222, 164, 40]],
	caramel: [[176, 116, 66], [246, 236, 214], [196, 120, 44]],
	cold_brew: [[30, 18, 10], [62, 38, 20], [22, 12, 6]],
};
for (const [name, [liquid, top, detail]] of Object.entries(SURFACES)) {
	png(`block/mug_${name}`, grid((x, y) => {
		const r2 = (x - 7.5) ** 2 + (y - 7.5) ** 2;
		if (name === 'night_shift' && (x <= 1 || x >= 14 || y <= 1 || y >= 14)) return 'D'; // the glowing edge
		if ((x * 5 + y * 3) % 17 === 0) return 'D';
		return r2 < 14 ? 'T' : 'L';
	}), { L: liquid, T: top, D: detail });
}
// Toppings and trims (not tinted).
png('block/mug_foam', grid((x, y) => ((x + y * 2) % 7 === 0 ? 'c' : 'w')), { w: [252, 250, 246], c: [236, 226, 210] });
png('block/mug_sleeve', grid((x, y) => (y % 5 === 0 ? 'd' : (x + y) % 6 === 0 ? 'l' : 'o')), { o: [226, 120, 44], d: [180, 88, 30], l: [244, 160, 90] });
png('block/mug_ice', grid((x, y) => (x === 0 || y === 0 ? 'h' : (x + y) % 5 === 0 ? 'h' : 'i')), { i: [176, 214, 238], h: [228, 244, 252] });
png('block/mug_straw', grid((x, y) => ((y >> 1) % 2 ? 'r' : 'w')), { r: [210, 56, 56], w: [250, 250, 250] });
png('block/mug_marshmallow', grid((x, y) => (y >= 12 ? 'p' : (x + y) % 9 === 0 ? 'p' : 'w')), { w: [252, 250, 246], p: [244, 208, 214] });
png('block/mug_gold', grid((x, y) => ((x + y) % 4 === 0 ? 'h' : 'g')), { g: [232, 184, 58], h: [252, 222, 120] });
png('block/mug_pod', grid((x, y) => ((x + y) % 3 ? 'p' : 'd')), { p: [96, 58, 32], d: [62, 36, 20] });
png('block/mug_drizzle', grid((x, y) => ((x + y) % 4 === 0 ? 'l' : 'c')), { c: [92, 48, 26], l: [124, 70, 40] });
png('block/mug_honey', grid((x, y) => ((x + y) % 4 === 0 ? 'l' : 'c')), { c: [226, 162, 36], l: [250, 204, 84] });
png('block/mug_caramel', grid((x, y) => ((x + y) % 4 === 0 ? 'l' : 'c')), { c: [186, 112, 40], l: [222, 150, 70] });

// ---------------------------------------------------------------- café extras
// Vanilla latte: foam with a vanilla pod standing in it.
drinkLayer('vanilla_latte', [214, 178, 120], [250, 244, 230], [120, 80, 40],
	(x, y) => (x - 7 === 4 - y && y >= 0 && y <= 4 ? 'P' : x - 8 === 4 - y && y >= 0 && y <= 3 ? 'p' : foam(x, y) ? 'F' : null),
	{ F: [250, 242, 222], P: [62, 36, 20], p: [96, 58, 32] }, false);
// The newer drinks: liquid, the top (foam, ice, glow or marshmallow) and a detail.
// Pumpkin spice latte: whipped cream with a dusting of cinnamon, and an orange sleeve.
drinkLayer('pumpkin_spice_latte', [196, 120, 60], [246, 214, 170], [120, 64, 30],
	(x, y) => (y >= 8 && y <= 10 && x >= 3 && x <= 11 ? (y === 9 ? 'O' : 'o') : (y === 1 && x === 7) || (y === 2 && x === 6) ? 'n' : foam(x, y) ? 'F' : null),
	{ F: WHITE, O: [232, 126, 48], o: [196, 96, 34], n: [150, 82, 40] }, false);
// Iced coffee: ice cubes poking out and a striped straw, no steam.
drinkLayer('iced_coffee', [58, 30, 14], [200, 228, 246], [140, 90, 60],
	(x, y) => (x === 10 - Math.floor(y / 2) && y <= 4 ? (y % 2 ? 'r' : 'w')
		: (x >= 4 && x <= 5 && y >= 2 && y <= 3) || (x >= 7 && x <= 8 && y >= 3 && y <= 4) ? (x === 4 || x === 7 ? 'J' : 'I') : null),
	{ I: [178, 216, 240], J: [226, 244, 252], r: [210, 56, 56], w: [250, 250, 250] }, false);
// Night shift: a glowing rim and a few stars.
drinkLayer('night_shift', [36, 24, 18], [250, 214, 90], [70, 46, 24],
	(x, y) => ((y === 3 && x >= 4 && x <= 10) || ((x === 3 || x === 11) && y === 4) ? 'G'
		: (x === 2 && y === 1) || (x === 13 && y === 2) || (x === 12 && y === 0) || (x === 4 && y === 0) ? 'g' : null),
	{ G: [252, 222, 96], g: [255, 244, 170] }, false);
// Hot chocolate: marshmallows on top, and steam.
drinkLayer('hot_chocolate', [92, 50, 28], [250, 248, 244], [64, 34, 18],
	(x, y) => ((x >= 5 && x <= 6 && y === 3) || (x >= 8 && x <= 9 && y >= 3 && y <= 4) || (x === 7 && y === 4) ? (x === 6 || x === 9 ? 'm' : 'M') : null),
	{ M: WHITE, m: [244, 210, 214] });
// Honey latte: foam with honey dripping over the rim.
drinkLayer('honey_latte', [196, 142, 82], [246, 234, 204], [160, 112, 72],
	(x, y) => ((x === 5 && y >= 3 && y <= 6) || (x === 9 && y >= 3 && y <= 5) || (y === 2 && x >= 6 && x <= 8) ? 'H'
		: foam(x, y) ? 'F' : null), { F: WHITE, H: [236, 172, 40] }, false);
// Caramel macchiato: foam with caramel criss-crossed over the top.
drinkLayer('caramel_macchiato', [176, 116, 66], [246, 236, 214], [140, 92, 52],
	(x, y) => (foam(x, y) ? ((x + y) % 3 === 0 ? 'K' : 'F') : null), { F: WHITE, K: [196, 120, 44] }, false);
// Cold brew: very dark, no steam, and beads of cold on the mug.
drinkLayer('cold_brew', [30, 18, 10], [62, 38, 20], [22, 12, 6],
	(x, y) => ((x === 3 && y === 7) || (x === 11 && y === 9) || (x === 5 && y === 11) || (x === 9 && y === 6) ? 'd' : null),
	{ d: [200, 228, 246] }, false);
// Coffee grounds: a crumbly dark heap.
png('item/coffee_grounds', grid((x, y) => {
	const inside = (x - 7.5) ** 2 / 36 + (y - 12) ** 2 / 25 < 1 && y <= 13;
	if (!inside) return '.';
	return (x * 7 + y * 3) % 5 === 0 ? 'l' : (x + y * 2) % 3 === 0 ? 'd' : 'g';
}), { g: [70, 44, 26], d: [44, 26, 14], l: [104, 70, 44] });
// Caramel: a glossy amber square.
png('item/caramel', grid((x, y) => {
	if (x < 4 || x > 11 || y < 5 || y > 12) return '.';
	if (x === 4 || y === 12 || x === 11) return 'd';
	return (x === 5 && y <= 7) || (y === 5 && x <= 7) ? 'h' : 'c';
}), { c: [206, 128, 44], d: [150, 84, 26], h: [246, 196, 120] });
// Pastries. A croissant: a golden crescent with its folds showing.
png('item/croissant', grid((x, y) => {
	const body = (x - 7.5) ** 2 / 42 + (y - 8.5) ** 2 / 16 < 1;
	const bite = (x - 7.5) ** 2 / 10 + (y - 13) ** 2 / 9 < 1;
	if (!body || bite) return '.';
	if ((x - 7.5) ** 2 / 42 + (y - 7.5) ** 2 / 16 >= 1) return 'h'; // the glossy top edge
	return x % 3 === 0 ? 'd' : 'c';
}), { c: [214, 150, 70], d: [166, 102, 40], h: [242, 198, 120] });
// Biscotti: two slanted slices, speckled with nuts.
png('item/biscotti', grid((x, y) => {
	for (const shift of [0, 5]) {
		const lo = Math.floor(x / 2) + 1 + shift, hi = lo + 3;
		if (x >= 2 && x <= 13 && y >= lo && y <= hi) {
			if (y === lo || x === 2 || x === 13) return 'k'; // crust
			return (x * 3 + y * 5) % 7 === 0 ? 'n' : 'b';
		}
	}
	return '.';
}), { b: [226, 192, 134], k: [176, 128, 72], n: [140, 96, 56] });
// A berry muffin: a domed top dotted with berries, in a striped paper cup.
png('item/muffin', grid((x, y) => {
	if (y >= 9 && y <= 14) {
		const inset = Math.floor((y - 9) / 3);
		if (x < 4 + inset || x > 11 - inset) return '.';
		return x % 2 ? 'w' : 'p'; // the paper cup's folds
	}
	if ((x - 7.5) ** 2 / 30 + (y - 8.5) ** 2 / 25 < 1 && y <= 8) {
		if ((x === 5 && y === 6) || (x === 9 && y === 5) || (x === 7 && y === 8) || (x === 10 && y === 7)) return 'r';
		return y <= 5 && x <= 7 ? 'h' : 'm';
	}
	return '.';
}), { m: [176, 112, 60], h: [206, 146, 86], r: [150, 34, 70], p: [236, 226, 210], w: [204, 186, 160] });
// Pumpkin spice and cocoa mix: little paper sachets of powder.
const sachet = (name, powder, dark) => png(`item/${name}`, grid((x, y) => {
	if (y === 3 && x >= 5 && x <= 10) return 'f'; // folded top
	if (y >= 4 && y <= 13 && x >= 4 && x <= 11) {
		if (y >= 7 && y <= 10 && x >= 6 && x <= 9) return (x + y) % 3 ? 'p' : 'd'; // the powder showing through the window
		return x === 4 || x === 11 || y === 13 ? 'e' : 'w';
	}
	return '.';
}), { f: [214, 196, 160], w: [236, 224, 196], e: [190, 170, 136], p: powder, d: dark });
sachet('pumpkin_spice', [214, 120, 52], [150, 74, 30]);
// Cinnamon: three curled quills of bark, tied together.
png('item/cinnamon', grid((x, y) => {
	if (y === 8 && x >= 3 && x <= 12) return 't'; // the tie, over the quills
	for (const [cx, top] of [[5, 2], [8, 1], [11, 3]]) {
		const dx = x - cx;
		if (y >= top && y <= top + 11 && dx >= -1 && dx <= 1) return dx === -1 ? 'h' : dx === 1 ? 'd' : 'b';
	}
	return '.';
}), { h: [196, 128, 82], b: [160, 96, 56], d: [112, 62, 34], t: [214, 196, 160] });
sachet('cocoa_mix', [110, 62, 34], [70, 38, 20]);
// Vanilla syrup: a little jar of golden syrup with a vanilla pod.
png('item/vanilla_syrup', [
	'................',
	'................',
	'.........pP.....',
	'.....kkkkpk.....',
	'.....kWWWpk.....',
	'....gggggpggg...',
	'....gYYYYpYYg...',
	'....gYyYYpYYg...',
	'....gYYYYpyYg...',
	'....gYYyYPYYg...',
	'....gYYYYYYYg...',
	'....gyYYYYyYg...',
	'....gYYYYYYYg...',
	'.....ggggggg....',
	'................',
	'................',
], { k: [120, 90, 60], W: [240, 236, 226], g: [200, 220, 230, 220], Y: [232, 196, 120], y: [214, 170, 96], p: [70, 44, 24], P: [96, 62, 34] });

// Canisters: glazed like the mugs (greys, tinted), with a lid and knob.
png('block/canister', grid((x, y) => (y <= 1 ? 'W' : (y >= 14 ? 'S' : ((x * 7 + y) % 13 === 0 ? 'S' : 'B')))), MUG);
png('block/canister_top', grid((x, y) => (x <= 1 || x >= 14 || y <= 1 || y >= 14 ? 'S' : 'W')), MUG);
const canisterShape = (x, y) => {
	if (y === 2 && x >= 7 && x <= 8) return 'S'; // knob
	if (y === 3 && x >= 4 && x <= 11) return 'W'; // lid
	if (y === 4 && x >= 3 && x <= 12) return 'S';
	if (y >= 5 && y <= 13 && x >= 3 && x <= 12) return x === 3 ? 'W' : (x === 12 || y === 13 ? 'S' : 'B');
	return '.';
};
png('item/unfired_canister', grid(canisterShape), { W: [176, 182, 196], B: [160, 166, 179], S: [128, 134, 148] });

// ---------------------------------------------------------------- glass jar, reserve, ristretto, barista
// Glass jar: clear glass (see-through, so it draws as translucent) with brighter edges.
png('block/glass_jar', grid((x, y) => (x === 0 || x === 15 || y === 0 || y === 15 ? 'E' : ((x + y) % 7 === 0 && x > 2 && x < 9 ? 'H' : 'g'))),
	{ g: [214, 232, 240, 70], E: [236, 246, 250, 170], H: [255, 255, 255, 150] });
png('item/glass_jar', grid((x, y) => {
	if (y >= 1 && y <= 2 && x >= 5 && x <= 10) return 'w'; // wooden lid
	if (y === 3 && x >= 4 && x <= 11) return 'W';
	if (y >= 4 && y <= 14 && x >= 3 && x <= 12) return x === 3 || x === 12 || y === 14 ? 'E' : (x === 5 && y < 12 ? 'H' : 'g');
	return '.';
}), { w: [150, 112, 66], W: [112, 82, 46], g: [214, 232, 240, 90], E: [236, 246, 250, 200], H: [255, 255, 255, 170] });
// The Barista's Reserve: a small burlap sack of beans tied with a gold ribbon.
png('item/baristas_reserve', grid((x, y) => {
	if (y >= 2 && y <= 4 && x >= 6 && x <= 9) return y === 2 ? 'k' : (x === 7 || x === 8 ? 'b' : 'k'); // beans peeking out
	if (y === 5 && x >= 5 && x <= 10) return 'G'; // gold ribbon
	if (y >= 6 && y <= 14 && x >= 3 + (y < 8 ? 1 : 0) && x <= 12 - (y < 8 ? 1 : 0)) return (x + y * 2) % 5 === 0 ? 'S' : (x === 3 || x === 12 || y === 14 ? 'D' : 'B');
	return '.';
}), { k: [44, 24, 12], b: [112, 64, 34], G: [232, 184, 58], B: [176, 146, 102], S: [150, 122, 82], D: [120, 96, 62] });
// Ristretto: a thick layer of crema right to the rim, and a gold band (it's special).
drinkLayer('ristretto', [36, 18, 8], [214, 150, 80], [24, 12, 6],
	(x, y) => (y === 4 && x >= 4 && x <= 10 ? 'T' : y === 3 && x >= 5 && x <= 9 ? 'L' : y === 7 && x >= 3 && x <= 11 ? 'Y' : null), { Y: [232, 184, 58] });

// The barista's apron, over the villager's robe (front 6..13 x 44..63 on the 64x64 sheet),
// with ties round the sides and a coffee bean on the pocket.
{
	const px = Array.from({ length: 64 }, () => Array(64).fill('.'));
	for (let y = 46; y <= 63; y++) for (let x = 6; x <= 13; x++) px[y][x] = (x === 6 || x === 13) ? 'd' : 'a';
	for (const x of [7, 12]) { px[44][x] = 's'; px[45][x] = 's'; } // neck strap
	for (let x = 0; x <= 27; x++) if (x < 6 || x > 13) px[52][x] = 'd'; // waist ties round the sides and back
	for (let y = 55; y <= 58; y++) for (let x = 8; x <= 11; x++) px[y][x] = 'p'; // pocket
	px[56][9] = 'k'; px[56][10] = 'k'; px[57][9] = 'k'; px[57][10] = 'b'; // a coffee bean
	const APRON = { a: [236, 226, 206], d: [120, 78, 44], s: [120, 78, 44], p: [212, 196, 170], k: [44, 24, 12], b: [164, 104, 58] };
	const rows = px.map(r => r.join(''));
	png('entity/villager/profession/barista', rows, APRON);
	png('entity/zombie_villager/profession/barista', rows, APRON);
}

// Coffee bar cabinet doors, drawn over each wood's planks: two doors outlined in a soft shadow (it darkens
// whichever wood is underneath, so the trim always matches) and little handles.
png('block/coffee_bar_doors', grid((x, y) => {
	if (y > 13) return '.';
	if (x === 0 || x === 15 || y === 0 || y === 13 || x === 7 || x === 8) return 'g';
	if (y === 6 && (x === 5 || x === 10)) return 'h';
	return '.';
}), { g: [0, 0, 0, 80], h: [200, 200, 206] }); // a soft shadow, so the lines match each wood

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
// Grown bushes are two blocks tall, like lilac: woody stems below, a leafy crown above, and
// cherries along the branches (none at stage 2, green at 3, ripe at 4). Drawn as one 16x32
// picture and split into a bottom and a top half.
function tallBush(fruit) {
	const hash = (x, y) => (((x * 73856093) ^ (y * 19349663)) >>> 0);
	const rows = [];
	for (let y = 0; y < 32; y++) {
		let row = '';
		for (let x = 0; x < 16; x++) {
			const h = hash(x + 3, y + 7);
			const d = ((x - 7.5) / 7.2) ** 2 + ((y - 12) / 11) ** 2; // the crown
			let c = '.';
			if (d < 1 && y < 27) {
				if (h % 7 === 0 || (d > 0.75 && h % 2 === 0)) c = '.';
				else c = h % 5 === 0 ? 'h' : h % 3 === 0 ? 'g' : 'G';
			}
			// Three stems from the root, spreading into the crown.
			const stem = (x0, lean) => y >= 14 && Math.round(x0 + (31 - y) * lean) === x;
			if (stem(7, 0) || (y >= 18 && (stem(7, -0.18) || stem(8, 0.18)))) c = y > 24 || c === '.' || h % 2 ? 's' : c;
			row += c;
		}
		rows.push(row.split(''));
	}
	// Cherry clusters along the branches.
	if (fruit) {
		for (const [x, y] of [[3, 9], [10, 6], [6, 14], [12, 12], [4, 18], [9, 20], [7, 4], [13, 17], [2, 14]]) {
			rows[y][x] = 'R'; rows[y][x + 1] = 'r'; rows[y + 1][x] = 'r';
		}
	}
	const lines = rows.map(r => r.join(''));
	return [lines.slice(0, 16), lines.slice(16)];
}
for (const [age, fruit, colours] of [[2, false, LEAF], [3, true, { ...LEAF, R: [226, 190, 70], r: [184, 148, 48] }], [4, true, { ...LEAF, ...CHERRY }]]) {
	const [top, bottom] = tallBush(fruit);
	png(`block/coffee_bush_tall${age}_top`, top, colours);
	png(`block/coffee_bush_tall${age}_bottom`, bottom, colours);
}
for (const stale of ['coffee_bush_stage2', 'coffee_bush_stage3']) {
	fs.rmSync(path.join(A, 'textures', 'block', stale + '.png'), { force: true });
	fs.rmSync(path.join(A, 'models', 'block', stale + '.json'), { force: true });
}

// ---------------------------------------------------------------- effect icons (18x18)
png('mob_effect/drowsy', [
	'..................', '..................', '..........zzzz....', '.............z....',
	'............z.....', '...........z......', '..........zzzz....', '..................',
	'..................', '..eeeeeeeeeeeee...', '.e.............e..', '..eeeeeeeeeeeee...',
	'....l.l.l.l.l.....', '..................', '..................', '..................',
	'..................', '..................',
], { z: [196, 186, 230], e: [120, 100, 150], l: [90, 74, 118] });
// Cozy: a warm mug with a heart of steam. Well Rested: a little golden sun.
png('mob_effect/cozy', grid18((x, y) => {
	if (y >= 2 && y <= 4 && ((x === 7 || x === 9) && y === 2 || (x >= 6 && x <= 10 && y === 3) || (x === 8 && y === 4))) return 'h';
	if (y >= 7 && y <= 14 && x >= 4 && x <= 11) return x === 4 || x === 11 || y === 14 ? 'S' : 'B';
	if ((y === 9 || y === 12) && (x === 12 || x === 13)) return 'B';
	if (y >= 10 && y <= 11 && x === 13) return 'S';
	return '.';
}), { h: [232, 120, 120], B: [240, 196, 150], S: [200, 150, 104] });
// Warm: a little scarf-orange flame.
png('mob_effect/warm', grid18((x, y) => {
	const dx = x - 8.5, w = y < 6 ? 0 : (y - 5) * 0.55;
	if (y >= 3 && y <= 15 && Math.abs(dx) <= Math.min(w, 4.5)) return Math.abs(dx) <= Math.min(w, 4.5) - 2 && y > 8 ? 'Y' : 'O';
	return '.';
}), { O: [224, 110, 50], Y: [250, 200, 90] });
png('mob_effect/well_rested', grid18((x, y) => {
	const dx = x - 8.5, dy = y - 8.5, r2 = dx * dx + dy * dy;
	if (r2 < 9) return 'Y';
	if (r2 < 16) return 'y';
	if ((x === 8 || x === 9) && (y <= 2 || y >= 15)) return 'y';
	if ((y === 8 || y === 9) && (x <= 2 || x >= 15)) return 'y';
	return '.';
}), { Y: [252, 220, 110], y: [232, 176, 60] });
png('mob_effect/caffeinated', [
	'..................', '..................', '..................', '......kkkkk.......',
	'.....kbBbbbk......', '....kbBbbcbbk.....', '....kbBbcbbbk.....', '....kbbbcbbbk.....',
	'....kbbcbbbbk.....', '....kbbcbbbbk.....', '....kbcbbbbbk.....', '....kbcbbbbbk.....',
	'.....kbcbbbk......', '......kkkkk.......', '..................', '..................',
	'..................', '..................',
], BEAN);

// ---------------------------------------------------------------- models, states, names
// Stages 0 and 1 are short; from stage 2 the bush has a top half. (A top half never exists below
// stage 2; those states just borrow the stage 2 top.)
const bushModel = (age, half) => age < 2 ? `espresso_flavoured:block/coffee_bush_stage${age}` : `espresso_flavoured:block/coffee_bush_tall${age}_${half === 'upper' ? 'top' : 'bottom'}`;
out('blockstates/coffee_bush.json', { variants: Object.fromEntries([0, 1, 2, 3, 4].flatMap(age => ['lower', 'upper'].map(half =>
	[`age=${age},half=${half}`, { model: bushModel(age < 2 && half === 'upper' ? 2 : age, half) }]))) });
for (const a of [0, 1]) out(`models/block/coffee_bush_stage${a}.json`, { parent: 'minecraft:block/cross', textures: { cross: `espresso_flavoured:block/coffee_bush_stage${a}` } });
for (const a of [2, 3, 4]) for (const half of ['top', 'bottom']) {
	out(`models/block/coffee_bush_tall${a}_${half}.json`, { parent: 'minecraft:block/cross', textures: { cross: `espresso_flavoured:block/coffee_bush_tall${a}_${half}` } });
}
for (const item of ['coffee_cherries', 'roasted_coffee_beans', 'unfired_mug']) {
	out(`items/${item}.json`, { model: { type: 'minecraft:model', model: `espresso_flavoured:item/${item}` } });
	out(`models/item/${item}.json`, { parent: 'minecraft:item/generated', textures: { layer0: `espresso_flavoured:item/${item}` } });
}
const PLAIN_MUG = 0xF0E4CF - 0x1000000; // Mugs.PLAIN as a signed ARGB int
const dyed = (model) => ({ model: { type: 'minecraft:model', model, tints: [{ type: 'minecraft:dye', default: PLAIN_MUG }] } });
out('items/mug.json', dyed('espresso_flavoured:item/mug'));
out('models/item/mug.json', { parent: 'minecraft:item/generated', textures: { layer0: 'espresso_flavoured:item/mug' } });
for (const item of ['espresso', 'latte', 'mocha_latte']) {
	out(`items/${item}.json`, dyed(`espresso_flavoured:item/${item}`));
	out(`models/item/${item}.json`, { parent: 'minecraft:item/generated', textures: { layer0: 'espresso_flavoured:item/mug', layer1: `espresso_flavoured:item/${item}_fill` } });
}

// Placed mug: a saucer, a rounded mug (a body with gentle bulges on each side), a curved
// handle, the drink's surface, and the drink's own toppings. Mug and saucer are tinted
// (tintindex 0); toppings keep their own colours. Handle to the east when facing north.
const tinted = (faces) => Object.fromEntries(Object.entries(faces).map(([f, t]) => [f, { texture: t, tintindex: 0 }]));
const sides = (t) => ({ north: t, south: t, east: t, west: t });
const plain = (faces) => Object.fromEntries(Object.entries(faces).map(([f, t]) => [f, { texture: t }]));
const box = (from, to, texture, faces = ['north', 'south', 'east', 'west', 'up', 'down']) =>
	({ from, to, faces: plain(Object.fromEntries(faces.map(f => [f, texture]))) });
const full = (e) => { for (const f of e.uvFull || []) e.faces[f].uv = [0, 0, 16, 16]; delete e.uvFull; return e; };
const mugElements = [
	// The saucer.
	{ from: [3.5, 0, 3.5], to: [12.5, 0.5, 12.5], faces: tinted({ ...sides('#glaze'), up: '#saucer', down: '#saucer' }), uvFull: ['up', 'down'] },
	// The body, and a bulge on each side so it reads as round.
	{ from: [5, 0.5, 5], to: [11, 6.5, 11], faces: tinted({ ...sides('#glaze'), up: '#rim' }), uvFull: ['up'] },
	{ from: [5.5, 0.5, 4.6], to: [10.5, 6.25, 5], faces: tinted({ north: '#glaze', east: '#glaze', west: '#glaze', up: '#glaze' }) },
	{ from: [5.5, 0.5, 11], to: [10.5, 6.25, 11.4], faces: tinted({ south: '#glaze', east: '#glaze', west: '#glaze', up: '#glaze' }) },
	{ from: [4.6, 0.5, 5.5], to: [5, 6.25, 10.5], faces: tinted({ west: '#glaze', north: '#glaze', south: '#glaze', up: '#glaze' }) },
	{ from: [11, 0.5, 5.5], to: [11.4, 6.25, 10.5], faces: tinted({ east: '#glaze', north: '#glaze', south: '#glaze', up: '#glaze' }) },
	// A curved handle: top, outer and bottom pieces.
	{ from: [11.4, 4.5, 7.5], to: [12.6, 5.25, 8.5], faces: tinted({ ...sides('#glaze'), up: '#glaze', down: '#glaze' }) },
	{ from: [12.6, 2, 7.5], to: [13.35, 5, 8.5], faces: tinted({ ...sides('#glaze'), up: '#glaze', down: '#glaze' }) },
	{ from: [11.4, 1.75, 7.5], to: [12.6, 2.5, 8.5], faces: tinted({ ...sides('#glaze'), up: '#glaze', down: '#glaze' }) },
].map(full);
const surface = (light = 0) => ({ from: [5.75, 6.51, 5.75], to: [10.25, 6.51, 10.25], faces: { up: { texture: '#liquid', uv: [0, 0, 16, 16] } }, ...(light ? { light_emission: light } : {}) });
const foamDome = (h = 7) => [box([6.5, 6.5, 6.5], [9.5, h, 9.5], '#foam', ['north', 'south', 'east', 'west', 'up'])];
const band = (y0, y1, texture) => box([4.55, y0, 4.55], [11.45, y1, 11.45], texture, ['north', 'south', 'east', 'west']);
// Each drink: its surface texture, a glow (0 = none), and its toppings.
const DRINKS = {
	espresso: [0, []],
	ristretto: [0, [band(2, 2.75, '#gold')]],
	latte: [0, foamDome(7)],
	mocha: [0, [...foamDome(6.9), box([6, 3.5, 4.55], [7, 6.25, 4.55], '#drizzle', ['north']), box([8.5, 4.5, 4.55], [9.25, 6.25, 4.55], '#drizzle', ['north'])]],
	vanilla: [0, [...foamDome(6.9), box([8.75, 6.5, 7.75], [9.25, 9.5, 8.25], '#pod')]],
	pumpkin_spice: [0, [band(2.5, 4, '#sleeve'), box([6.25, 6.5, 6.25], [9.75, 7.25, 9.75], '#foam', ['north', 'south', 'east', 'west', 'up']), box([7, 7.25, 7], [9, 7.75, 9], '#foam', ['north', 'south', 'east', 'west', 'up'])]],
	iced: [0, [box([6, 6.5, 6], [7.5, 7.5, 7.5], '#ice'), box([8.25, 6.5, 8], [9.5, 7.25, 9.25], '#ice'), box([9.25, 6.5, 6.25], [9.75, 10, 6.75], '#straw')]],
	night_shift: [12, []],
	hot_chocolate: [0, [box([6.25, 6.5, 6.25], [7.25, 7.25, 7.25], '#marshmallow'), box([8.5, 6.5, 7.75], [9.5, 7.25, 8.75], '#marshmallow'), box([7, 6.5, 8.6], [8, 7, 9.6], '#marshmallow')]],
	// Honey running down the side from the foam; caramel criss-crossed over it; a lone ice cube in cold brew.
	honey: [0, [...foamDome(6.9), box([6.5, 4, 4.55], [7.25, 6.25, 4.55], '#honey', ['north']), box([9, 4.75, 4.55], [9.75, 6.25, 4.55], '#honey', ['north'])]],
	caramel: [0, [...foamDome(6.9), box([6.5, 6.9, 7.6], [9.5, 6.92, 8.1], '#caramel', ['up']), box([7.6, 6.9, 6.5], [8.1, 6.92, 9.5], '#caramel', ['up'])]],
	cold_brew: [0, [box([6.75, 6.5, 7], [8, 7.25, 8.25], '#ice')]],
};
const TOPPINGS = ['foam', 'sleeve', 'ice', 'straw', 'marshmallow', 'gold', 'pod', 'drizzle', 'honey', 'caramel'];
const mugTextures = {
	particle: 'espresso_flavoured:block/mug_glaze', glaze: 'espresso_flavoured:block/mug_glaze', rim: 'espresso_flavoured:block/mug_rim', saucer: 'espresso_flavoured:block/saucer',
	...Object.fromEntries(TOPPINGS.map(t => [t, `espresso_flavoured:block/mug_${t}`])),
};
out('models/block/mug_empty.json', { parent: 'minecraft:block/block', textures: mugTextures, elements: mugElements });
for (const [name, [glow, toppings]] of Object.entries(DRINKS)) {
	out(`models/block/mug_${name}.json`, { parent: 'minecraft:block/block', textures: { ...mugTextures, liquid: `espresso_flavoured:block/mug_${name}` },
		elements: [...mugElements, surface(glow), ...toppings] });
}
for (const stale of ['mug_full']) fs.rmSync(path.join(A, 'models', 'block', stale + '.json'), { force: true });
for (const stale of ['mug', 'mug_inside']) fs.rmSync(path.join(A, 'textures', 'block', stale + '.png'), { force: true });
out('blockstates/placed_mug.json', { variants: Object.fromEntries(
	[['north', 0], ['east', 90], ['south', 180], ['west', 270]].flatMap(([facing, y]) =>
		['empty', ...Object.keys(DRINKS)].map(c => [`contents=${c},facing=${facing}`, { model: `espresso_flavoured:block/mug_${c}`, ...(y ? { y } : {}) }])),
) });
// Vanilla, the golden mug and canisters.
for (const item of ['vanilla_syrup', 'unfired_canister']) {
	out(`items/${item}.json`, { model: { type: 'minecraft:model', model: `espresso_flavoured:item/${item}` } });
	out(`models/item/${item}.json`, { parent: 'minecraft:item/generated', textures: { layer0: `espresso_flavoured:item/${item}` } });
}
out('items/vanilla_latte.json', dyed('espresso_flavoured:item/vanilla_latte'));
for (const drink of ['pumpkin_spice_latte', 'iced_coffee', 'night_shift', 'hot_chocolate', 'honey_latte', 'caramel_macchiato', 'cold_brew']) {
	out(`items/${drink}.json`, dyed(`espresso_flavoured:item/${drink}`));
	out(`models/item/${drink}.json`, { parent: 'minecraft:item/generated', textures: { layer0: 'espresso_flavoured:item/mug', layer1: `espresso_flavoured:item/${drink}_fill` } });
}
out('models/item/vanilla_latte.json', { parent: 'minecraft:item/generated', textures: { layer0: 'espresso_flavoured:item/mug', layer1: 'espresso_flavoured:item/vanilla_latte_fill' } });
// The golden mug carries its gold colour as a dye, so it shares the mug's tinted model.
out('items/golden_mug.json', dyed('espresso_flavoured:item/mug'));
const canisterTextures = { particle: 'espresso_flavoured:block/canister', side: 'espresso_flavoured:block/canister', top: 'espresso_flavoured:block/canister_top' };
out('models/block/canister.json', { parent: 'minecraft:block/block', textures: canisterTextures, display: {
	gui: { rotation: [30, 225, 0], translation: [0, 1, 0], scale: [0.9, 0.9, 0.9] },
	ground: { rotation: [0, 0, 0], translation: [0, 3, 0], scale: [0.5, 0.5, 0.5] },
	fixed: { rotation: [0, 0, 0], translation: [0, 0, 0], scale: [0.8, 0.8, 0.8] },
	thirdperson_righthand: { rotation: [75, 45, 0], translation: [0, 2.5, 0], scale: [0.5, 0.5, 0.5] },
	firstperson_righthand: { rotation: [0, 45, 0], translation: [0, 2, 0], scale: [0.6, 0.6, 0.6] },
}, elements: [
	{ from: [4, 0, 4], to: [12, 10, 12], faces: tinted({ ...sides('#side'), down: '#side', up: '#top' }) },
	{ from: [4.5, 10, 4.5], to: [11.5, 11, 11.5], faces: tinted({ ...sides('#side'), up: '#top', down: '#side' }) },
	{ from: [7, 11, 7], to: [9, 12, 9], faces: tinted({ ...sides('#side'), up: '#top', down: '#side' }) },
] });
out('items/canister.json', dyed('espresso_flavoured:block/canister'));
out('blockstates/canister.json', { variants: Object.fromEntries(
	[['north', 0], ['east', 90], ['south', 180], ['west', 270]].map(([facing, y]) => [`facing=${facing}`, { model: 'espresso_flavoured:block/canister', ...(y ? { y } : {}) }]),
) });
// Glass jar, the Reserve and Ristretto.
for (const item of ['glass_jar', 'baristas_reserve', 'decaf_coffee_beans', 'pumpkin_spice', 'cocoa_mix', 'cinnamon', 'caramel', 'croissant', 'biscotti', 'muffin', 'coffee_grounds']) {
	out(`items/${item}.json`, { model: { type: 'minecraft:model', model: `espresso_flavoured:item/${item}` } });
	out(`models/item/${item}.json`, { parent: 'minecraft:item/generated', textures: { layer0: `espresso_flavoured:item/${item}` } });
}
out('items/ristretto.json', dyed('espresso_flavoured:item/ristretto'));
out('models/item/ristretto.json', { parent: 'minecraft:item/generated', textures: { layer0: 'espresso_flavoured:item/mug', layer1: 'espresso_flavoured:item/ristretto_fill' } });
const all6 = (texture) => Object.fromEntries(['north', 'south', 'east', 'west', 'up', 'down'].map(f => [f, { texture }]));
out('models/block/glass_jar.json', { parent: 'minecraft:block/block', textures: { particle: 'espresso_flavoured:block/glass_jar', glass: 'espresso_flavoured:block/glass_jar', lid: 'minecraft:block/oak_planks' }, elements: [
	{ from: [4, 0, 4], to: [12, 11, 12], faces: all6('#glass') },
	{ from: [5, 11, 5], to: [11, 13, 11], faces: all6('#lid') },
] });
// Coffee bars: plank cabinets with doors on the front and a smooth stone counter top.
out('models/block/coffee_bar.json', { parent: 'minecraft:block/block', textures: { particle: '#planks', top: 'minecraft:block/smooth_stone',
	top_side: 'minecraft:block/smooth_stone_slab_side', doors: 'espresso_flavoured:block/coffee_bar_doors' }, elements: [
	{ from: [0, 0, 0], to: [16, 14, 16], faces: { ...Object.fromEntries(['north', 'south', 'east', 'west'].map(f => [f, { texture: '#planks', uv: [0, 2, 16, 16] }])), down: { texture: '#planks', cullface: 'down' } } },
	{ from: [0, 14, 0], to: [16, 16, 16], faces: { up: { texture: '#top', cullface: 'up' }, ...Object.fromEntries(['north', 'south', 'east', 'west'].map(f => [f, { texture: '#top_side', uv: [0, 0, 16, 2] }])) } },
	{ from: [0.5, 0.5, -0.01], to: [15.5, 13.5, -0.01], faces: { north: { texture: '#doors', uv: [0, 0, 16, 14] } } },
] });
for (const wood of ['oak', 'spruce', 'birch', 'jungle', 'acacia', 'dark_oak', 'mangrove', 'cherry', 'pale_oak', 'poplar', 'bamboo', 'crimson', 'warped']) {
	out(`models/block/${wood}_coffee_bar.json`, { parent: 'espresso_flavoured:block/coffee_bar', textures: { planks: `minecraft:block/${wood}_planks` } });
	out(`items/${wood}_coffee_bar.json`, { model: { type: 'minecraft:model', model: `espresso_flavoured:block/${wood}_coffee_bar` } });
	out(`blockstates/${wood}_coffee_bar.json`, { variants: Object.fromEntries(
		[['north', 0], ['east', 90], ['south', 180], ['west', 270]].map(([facing, y]) => [`facing=${facing}`, { model: `espresso_flavoured:block/${wood}_coffee_bar`, ...(y ? { y } : {}) }]),
	) });
}
out('blockstates/glass_jar.json', { variants: Object.fromEntries(
	[['north', 0], ['east', 90], ['south', 180], ['west', 270]].map(([facing, y]) => [`facing=${facing}`, { model: 'espresso_flavoured:block/glass_jar', ...(y ? { y } : {}) }]),
) });


// ---------------------------------------------------------------- espresso machine
// A retro café machine: cream enamel body (like the mugs), copper trim and brew head, a copper
// steam wand on the side, a portafilter with a wooden handle, a drip tray, and a glass bean hopper with a copper lid.
const MACHINE = {
	c: [240, 228, 207], C: [250, 243, 228], s: [222, 206, 180], e: [184, 166, 140],
	r: [184, 115, 51], R: [222, 156, 86], o: [138, 80, 34], y: [246, 196, 128],
	d: [52, 50, 54], D: [82, 80, 86], w: [74, 48, 30], W: [104, 70, 44],
};
// Enamel: soft cream with a gentle highlight down the left and shade down the right.
const enamel = (x, y) => (x <= 1 ? 'C' : x >= 14 ? 's' : 'c');
png('block/espresso_machine_side', grid((x, y) => {
	if (y === 15) return 'e';
	if (y === 7) return 'r'; // level with the underside of the head
	return enamel(x, y);
}), MACHINE);
png('block/espresso_machine_front', grid((x, y) => {
	if (y === 7) return 'r';
	return enamel(x, y);
}), MACHINE);
// The face under the head, behind the cup: shaded cream.
png('block/espresso_machine_inner', grid((x, y) => (y === 0 ? 'e' : (x <= 1 || x >= 14 ? 'e' : 's'))), MACHINE);
// The top: a copper cup-warmer tray with a raised rim.
png('block/espresso_machine_top', grid((x, y) => (x === 0 || x === 15 || y === 0 || y === 15) ? 'o' : (y % 3 === 0 ? 'r' : 'R')), MACHINE);
// Polished copper for the trim, the brew head and the steam wand.
png('block/espresso_machine_copper', grid((x, y) => (x === 0 || y === 15) ? 'r' : ((x + y) % 9 === 0 || (x + y) % 9 === 1 && y < 6 ? 'y' : 'R')), MACHINE);
// The drip tray's grate and the dark fittings.
png('block/espresso_machine_dark', grid((x, y) => (x % 2 === 0 ? 'd' : ((x + y) % 4 === 0 ? 'D' : 'd'))), MACHINE);
png('block/espresso_machine_wood', grid((x, y) => ((x + y * 3) % 5 === 0 ? 'W' : 'w')), MACHINE);
const HOPPER = { ...MACHINE, g: [196, 214, 226, 150], G: [236, 244, 250, 190], ...BEAN };
const hopper = (filled) => grid((x, y) => {
	if (y <= 1 || x === 0 || x === 15) return 'o';
	if (filled && y >= 6) return (x * 3 + y * 5) % 7 === 0 ? 'k' : ((x + y) % 3 ? 'b' : 'B');
	return (x + y) % 6 === 0 ? 'G' : 'g';
});
png('block/espresso_machine_hopper', hopper(false), HOPPER);
png('block/espresso_machine_hopper_filled', hopper(true), HOPPER);
for (const stale of ['espresso_machine_old']) fs.rmSync(path.join(A, 'textures', 'block', stale + '.png'), { force: true });

const all = (texture) => Object.fromEntries(['north', 'south', 'east', 'west', 'up', 'down'].map(f => [f, { texture }]));
// Shaped like a small home espresso machine: a narrow upright body with the head reaching forward
// over the cup (a C shape from the side), a portafilter with a wooden handle under it, a drip tray
// with a grate, a copper cup-warmer tray on top with a glass bean jar, and the steam wand on the side.
out('models/block/espresso_machine.json', {
	parent: 'minecraft:block/block',
	textures: { particle: 'espresso_flavoured:block/espresso_machine_side', side: 'espresso_flavoured:block/espresso_machine_side',
		front: 'espresso_flavoured:block/espresso_machine_front', inner: 'espresso_flavoured:block/espresso_machine_inner',
		top: 'espresso_flavoured:block/espresso_machine_top', copper: 'espresso_flavoured:block/espresso_machine_copper',
		dark: 'espresso_flavoured:block/espresso_machine_dark', wood: 'espresso_flavoured:block/espresso_machine_wood',
		hopper: 'espresso_flavoured:block/espresso_machine_hopper' },
	elements: [
		// The upright body at the back, and the head reaching forward over the cup.
		{ from: [4, 0, 7], to: [12, 13, 15], faces: { north: { texture: '#inner' }, south: { texture: '#side' }, east: { texture: '#side' },
			west: { texture: '#side' }, up: { texture: '#top' }, down: { texture: '#dark' } } },
		{ from: [4, 9, 3], to: [12, 13, 7], faces: { north: { texture: '#front', uv: [4, 4, 12, 8] }, east: { texture: '#side' },
			west: { texture: '#side' }, up: { texture: '#top' }, down: { texture: '#inner' } } },
		// A copper rim round the top, framing the cup-warmer tray.
		{ from: [3.75, 13, 2.75], to: [12.25, 13.5, 15.25], faces: { ...all('#copper'), up: { texture: '#top' } } },
		// The glass bean jar on the tray, with a copper lid.
		{ from: [6, 13.5, 9], to: [10, 16, 13], faces: { ...Object.fromEntries(['north', 'south', 'east', 'west'].map(f => [f, { texture: '#hopper', uv: [0, 0, 16, 16] }])), up: { texture: '#copper' }, down: { texture: '#dark' } } },
		// The copper group head under the head, the portafilter clipped into it, and its wooden handle.
		{ from: [6.5, 8.25, 4], to: [9.5, 9, 7], faces: all('#copper') },
		{ from: [6.75, 7.25, 4.25], to: [9.25, 8.25, 6.75], faces: all('#dark') },
		{ from: [7.5, 7.5, 1], to: [8.5, 8, 4.25], faces: all('#wood') },
		// The drip tray: a low copper-edged base with a dark grate where the cup stands.
		{ from: [4, 0, 2], to: [12, 1.5, 7], faces: { ...all('#copper'), up: { texture: '#dark' } } },
		// The steam wand on the east side: a small copper valve and a thin copper pipe down from it.
		{ from: [12, 10, 5.25], to: [12.75, 10.75, 6.25], faces: all('#copper') },
		{ from: [12.25, 3, 5.5], to: [12.75, 10, 6], faces: all('#copper') },
		{ from: [12.25, 3, 4.5], to: [12.75, 3.5, 5.5], faces: all('#copper') },
	],
});
out('models/block/espresso_machine_filled.json', {
	parent: 'espresso_flavoured:block/espresso_machine', textures: { hopper: 'espresso_flavoured:block/espresso_machine_hopper_filled' },
});
out('blockstates/espresso_machine.json', { variants: Object.fromEntries(
	[['north', 0], ['east', 90], ['south', 180], ['west', 270]].flatMap(([facing, y]) =>
		[false, true].map(beans => [`beans=${beans},facing=${facing}`,
			{ model: `espresso_flavoured:block/espresso_machine${beans ? '_filled' : ''}`, ...(y ? { y } : {}) }])),
) });

// The machine's menu (176x166 in a 256x256 sheet): a warm café panel. A walnut frame round a cream
// panel; copper pipes from the bean slot to the mugs, drawn round a little machine; faint icons in
// the empty slots (beans, mugs, grounds); and the coffee stream that pours as it brews at (176, 0).
{
	const W = 256, H = 256, px = Array.from({ length: H }, () => Array(W).fill('.'));
	const rect = (x0, y0, w, h, c) => { for (let y = y0; y < y0 + h; y++) for (let x = x0; x < x0 + w; x++) px[y][x] = c; };
	const dot = (x, y, c) => { px[y][x] = c; };
	// Panel: walnut outline with clipped corners, a light top-left bevel, a warm shadow bottom-right.
	rect(0, 0, 176, 166, 'K');
	rect(1, 1, 174, 164, 'L');
	rect(3, 3, 172, 162, 'S');
	rect(3, 3, 170, 160, 'P');
	for (const [x, y] of [[0, 0], [1, 0], [0, 1], [175, 0], [174, 0], [175, 1], [0, 165], [1, 165], [0, 164], [175, 165], [174, 165], [175, 164]]) px[y][x] = '.';
	// A slightly deeper cream band behind the machine and the "Inventory" label.
	rect(4, 4, 168, 76, 'Q');
	const slot = (x, y) => { rect(x - 1, y - 1, 18, 18, 'D'); rect(x, y, 17, 17, 'L'); rect(x, y, 16, 16, 'M'); };
	// The little machine drawn behind the brewing slots: a cream body with copper trim.
	rect(60, 15, 56, 27, 'e'); rect(61, 16, 54, 25, 'c'); rect(60, 15, 56, 2, 'r');
	rect(72, 41, 32, 3, 'o'); // the brew head
	// Copper pipes: the drip channel down the middle, and branches out to the side mugs.
	rect(83, 36, 9, 20, 'o'); rect(84, 37, 7, 18, 'g');
	rect(64, 46, 19, 2, 'r'); rect(64, 46, 2, 5, 'r');
	rect(92, 46, 19, 2, 'r'); rect(109, 46, 2, 5, 'r');
	for (const [x, y] of [[56, 51], [79, 58], [102, 51], [79, 17], [17, 17]]) slot(x, y); // the last is the knock box
	for (let r = 0; r < 3; r++) for (let c = 0; c < 9; c++) slot(8 + c * 18, 84 + r * 18);
	for (let c = 0; c < 9; c++) slot(8 + c * 18, 142);
	// Faint icons in the empty slots (covered as soon as something's in them).
	const ghost = (x0, y0, rows) => rows.forEach((row, dy) => [...row].forEach((ch, dx) => { if (ch !== '.') dot(x0 + dx, y0 + dy, 'G'); }));
	const BEAN_ICON = ['....######......', '..##########....', '.####..######...', '.#####..#####...', '..#####..####...', '...#####..##....', '.....######.....'];
	ghost(79, 21, BEAN_ICON.map(r => r.slice(0, 16)));
	const MUG_ICON = ['..#########.....', '..#########.##..', '..#########..#..', '..#########..#..', '..#########.##..', '..#########.....', '...#######......', '.###########....'];
	for (const [x, y] of [[56, 51], [79, 58], [102, 51]]) ghost(x, y + 4, MUG_ICON);
	const GROUNDS_ICON = ['................', '.....#..#.......', '...#..#...#.#...', '..###.#.##.###..', '.##############.', '.##############.'];
	ghost(17, 26, GROUNDS_ICON);
	// The coffee stream that fills as it brews (drawn by the screen over the channel).
	rect(176, 0, 9, 20, 'k');
	for (let y = 0; y < 20; y++) { for (let x = 178; x <= 182; x++) px[y][x] = (x === 179 && y % 3 === 0) ? 'h' : 'b'; }
	png('gui/container/espresso_machine', px.map(r => r.join('')), {
		K: [58, 38, 26], L: [252, 246, 234], S: [190, 170, 140], P: [236, 224, 202], Q: [228, 214, 188],
		D: [120, 94, 72], M: [206, 190, 164], G: [190, 172, 146],
		c: [244, 234, 214], e: [184, 166, 140], r: [196, 124, 58], o: [140, 82, 36], g: [214, 200, 178],
		k: [64, 34, 16], b: [112, 64, 34], h: [164, 104, 58],
	});
}
out('items/espresso_machine.json', { model: { type: 'minecraft:model', model: 'espresso_flavoured:block/espresso_machine_filled' } });

// ---------------------------------------------------------------- café charms
// Seven small charms (CharmItem.java), worn in Trinkets slots. Hand-drawn 16x16.
const CHARM_ART = {
	// A gilded coffee bean on a cord, with its crease.
	lucky_bean: [[
		'................',
		'.......cc.......',
		'......c..c......',
		'......c..c......',
		'.......cc.......',
		'........c.......',
		'......hYYd......',
		'.....hYYkYd.....',
		'....hYYYkYYd....',
		'....hYYYkYYd....',
		'....hYYYkYYd....',
		'....hYYYkYYd....',
		'....YYYYkYYd....',
		'.....YYYkYd.....',
		'......dddd......',
		'................',
	], { c: [110, 72, 40], Y: [236, 190, 72], h: [255, 232, 140], d: [176, 128, 36], k: [140, 92, 24] }],
	// A teal vacuum flask with a cream band and a steel cup for a lid.
	thermos: [[
		'................',
		'......kkkk......',
		'.....kKKKKk.....',
		'.....kkkkkk.....',
		'......ssss......',
		'.....hSSSSd.....',
		'.....hSSSSd.....',
		'.....bBBBBb.....',
		'.....bBBBBb.....',
		'.....hSSSSd.....',
		'.....hSSSSd.....',
		'.....hSSSSd.....',
		'.....hSSSSd.....',
		'.....hSSSSd.....',
		'.....dddddd.....',
		'................',
	], { k: [120, 124, 132], K: [204, 208, 214], s: [150, 154, 160], S: [72, 140, 130], h: [120, 190, 175], d: [40, 90, 84],
		B: [238, 228, 210], b: [200, 188, 166] }],
	// A copper locket with an ember glowing inside.
	pocket_hearth: [[
		'................',
		'................',
		'......oooo......',
		'....ooCCCCoo....',
		'...oCCrrrrCCo...',
		'...oCrRRRRrCo...',
		'..oCrRYYYYRrCo..',
		'..oCrRYWWYRrCo..',
		'..oCrRYWWYRrCo..',
		'..oCrRYYYYRrCo..',
		'...oCrRRRRrCo...',
		'...oCCrrrrCCo...',
		'....ooCCCCoo....',
		'......oooo......',
		'................',
		'................',
	], { o: [120, 62, 36], C: [196, 110, 60], r: [150, 40, 20], R: [230, 110, 30], Y: [255, 190, 60], W: [255, 240, 170] }],
	// A slouchy knit reading sock: rose cuff, a sage stripe, folds bunched down the leg, sage heel and toe.
	reading_socks: [[
		'................',
		'....rRRRRr......',
		'....rRRRRr......',
		'...cCCCCCCc.....',
		'...sSSSSSSs.....',
		'....cCCCCc......',
		'...cCCCCCCc.....',
		'...cCCCCCCc.....',
		'....cCCCCc......',
		'...cCCCCCCc.....',
		'...cCCCCCCCcc...',
		'...cCCCCCCCCCc..',
		'...cHHCCCCCCHHc.',
		'....hhcccccchh..',
		'................',
		'................',
	], { C: [236, 224, 200], c: [178, 158, 126], R: [206, 112, 126], r: [156, 74, 90], S: [118, 156, 108], s: [86, 122, 80], H: [118, 156, 108], h: [86, 122, 80] }],
	cafe_parasol: [[
		'................',
		'.......k........',
		'.....RRWWRR.....',
		'...RRRWWWWRRR...',
		'..RRRWWWWWWRRR..',
		'.RRRRWWWWWWRRRR.',
		'.rrrrwwwwwwrrrr.',
		'.r..r.w..w.r..r.',
		'.......k........',
		'.......k........',
		'.......k........',
		'.......k........',
		'.......k........',
		'.......k........',
		'....k..k........',
		'.....kk.........',
	], { R: [200, 70, 60], r: [150, 48, 40], W: [244, 234, 214], w: [206, 192, 168], k: [80, 56, 38] }],
	// A rust-red knit beanie with a cream turned-up cuff and a pom-pom.
	knit_beanie: [[
		'................',
		'......wWWw......',
		'.....wWWWWw.....',
		'......wWWw......',
		'......bBBb......',
		'....bBBkBBBb....',
		'...bBBBkBBBBb...',
		'..bBBBkBBBkBBb..',
		'..bBBBkBBBkBBb..',
		'..bBBkBBBBkBBb..',
		'..bBBkBBBBkBBb..',
		'..cCcCcCcCcCcc..',
		'..cCcCcCcCcCcc..',
		'..cccccccccccc..',
		'................',
		'................',
	], { B: [196, 98, 62], b: [150, 66, 40], k: [168, 80, 50], C: [236, 222, 196], c: [196, 176, 146], W: [250, 246, 236], w: [214, 204, 186] }],
	// A roaster's leather glove, its fingertips glowing.
	ember_glove: [[
		'................',
		'...kkkkkkk......',
		'...kOkOkOk......',
		'...kBkBkBk.kk...',
		'...kBkBkBkkBk...',
		'...kBBBBBBkBk...',
		'...kBBBBBBBBk...',
		'...kBBBOOBBBk...',
		'...kBBOYYOBBk...',
		'...kBBBOOBBBk...',
		'....kBBBBBBk....',
		'....kCCCCCCk....',
		'....kCcCcCCk....',
		'....kkkkkkkk....',
		'................',
		'................',
	], { k: [70, 40, 24], B: [150, 92, 52], C: [200, 150, 90], c: [160, 110, 60], O: [240, 120, 30], Y: [255, 210, 90] }],
	// An espresso tamper: a walnut handle on a copper base.
	copper_tamper: [[
		'................',
		'................',
		'......wWWw......',
		'.....wWWWWw.....',
		'.....wWWWWw.....',
		'......wWWw......',
		'.......Ww.......',
		'.......Ww.......',
		'......wWWw......',
		'.....wWWWWw.....',
		'...hCCCCCCCCd...',
		'...hCCCCCCCCd...',
		'...hCCCCCCCCd...',
		'...dddddddddd...',
		'................',
		'................',
	], { W: [170, 120, 74], w: [120, 82, 48], C: [214, 128, 74], h: [240, 170, 110], d: [150, 80, 44] }],
};
for (const [charm, [rows, palette]] of Object.entries(CHARM_ART)) {
	png(`item/${charm}`, rows, palette);
	out(`items/${charm}.json`, { model: { type: 'minecraft:model', model: `espresso_flavoured:item/${charm}` } });
	out(`models/item/${charm}.json`, { parent: 'minecraft:item/generated', textures: { layer0: `espresso_flavoured:item/${charm}` } });
}
// The beanie as worn: a knit dome, a ribbed cuff and a pom-pom, drawn from one small texture
// (knit on top, cuff in the middle, pom-pom at the bottom). On the head it uses this model; in hand,
// in the inventory and on the ground, the flat sprite.
png('item/knit_beanie_worn', Array.from({ length: 16 }, (_, y) => Array.from({ length: 16 }, (_, x) =>
	y < 8 ? (x % 3 === 0 ? 'k' : (y % 4 === 3 ? 'b' : 'B')) : y < 12 ? (x % 2 === 0 ? 'C' : 'c') : (x + y) % 3 === 0 ? 'w' : 'W').join('')),
	{ B: [196, 98, 62], b: [176, 84, 52], k: [150, 66, 40], C: [236, 222, 196], c: [206, 188, 160], W: [250, 246, 236], w: [222, 212, 194] });
const knitBox = (from, to, uv) => ({ from, to, faces: Object.fromEntries(['north', 'south', 'east', 'west', 'up', 'down'].map(f => [f, { uv, texture: '#0' }])) });
out('models/item/knit_beanie_worn.json', { textures: { 0: 'espresso_flavoured:item/knit_beanie_worn', particle: 'espresso_flavoured:item/knit_beanie_worn' }, elements: [
	knitBox([1.2, 9.5, 1.2], [14.8, 15.6, 14.8], [0, 0, 16, 8]),   // the knit dome
	knitBox([0.7, 7.5, 0.7], [15.3, 10.5, 15.3], [0, 8, 16, 12]),  // the turned-up cuff
	knitBox([6.4, 15.6, 6.4], [9.6, 18.4, 9.6], [0, 12, 4, 16]),   // the pom-pom
] });
out('items/knit_beanie.json', { model: { type: 'minecraft:select', property: 'minecraft:display_context',
	cases: [{ when: 'head', model: { type: 'minecraft:model', model: 'espresso_flavoured:item/knit_beanie_worn' } }],
	fallback: { type: 'minecraft:model', model: 'espresso_flavoured:item/knit_beanie' } } });
// The pocket slot's empty-slot icon, in Trinkets' grey outline style.
png('gui/sprites/container/slots/pocket', [
	'................',
	'................',
	'................',
	'................',
	'....bbbbbbbb....',
	'....b......b....',
	'....bbbbbbbb....',
	'....b......b....',
	'....b......b....',
	'....b......b....',
	'....b......b....',
	'.....b....b.....',
	'......bbbb......',
	'................',
	'................',
	'................',
], { b: [85, 85, 85] });
// How Trinkets draws them on the player: the Lucky Bean on a cord, the Thermos at the hip, the parasol on the back.
const trinket = (charm, render) => out(`trinkets/${charm}.json`, { target: [`espresso_flavoured:${charm}`], render });
const turn = (angle) => ({ axis: [0, 1, 0], angle });
trinket('lucky_bean', [
	{ type: 'model', model_part: 'body', model: 'trinkets:item/generic_necklace', offset: [0, 1, 1],
		transformation: { translation: [0, 0.01, 0.01], left_rotation: turn(0), scale: [1, 1, 1], right_rotation: turn(0) } },
	{ type: 'item', model_part: 'body', display_context: 'none', offset: [0, 1, 1],
		transformation: { translation: [0, -0.3, 0.02], left_rotation: turn(3.141), scale: [0.35, 0.35, 0.35], right_rotation: turn(0) } },
]);
trinket('thermos', [{ type: 'minecraft:if_slot', slot: 'legs/belt', then: {
	type: 'item', model_part: 'left_leg', display_context: 'none', offset: [1, 1, 0],
	transformation: { translation: [0.05, -0.2, 0], left_rotation: turn(1.571), scale: [0.45, 0.45, 0.45], right_rotation: turn(0) } } }]);
trinket('knit_beanie', [{ type: 'item', model_part: 'head', display_context: 'head', offset: [0, 0, 0],
	transformation: { translation: [0, 0, 0], left_rotation: turn(3.141), scale: [0.625, 0.625, 0.625], right_rotation: turn(0) } }]);
trinket('cafe_parasol', [{ type: 'item', model_part: 'body', display_context: 'none', offset: [0, 1, -1],
	transformation: { translation: [0, -0.35, -0.06], left_rotation: turn(0), scale: [0.8, 0.8, 0.8], right_rotation: turn(0) } }]);

out('lang/en_us.json', {
	'item.espresso_flavoured.lucky_bean': 'Lucky Bean',
	'item.espresso_flavoured.lucky_bean.desc': '+1 Luck: better fishing and chest finds',
	'item.espresso_flavoured.thermos': 'Thermos',
	'item.espresso_flavoured.thermos.desc': 'Coffee and cocoa effects last a quarter longer',
	'item.espresso_flavoured.pocket_hearth': 'Pocket Hearth',
	'item.espresso_flavoured.pocket_hearth.desc': "Powder snow can't freeze you, and you can walk on it",
	'item.espresso_flavoured.reading_socks': 'Reading Socks',
	'item.espresso_flavoured.reading_socks.desc': 'Step up full blocks without jumping',
	'item.espresso_flavoured.cafe_parasol': 'Café Parasol',
	'item.espresso_flavoured.cafe_parasol.desc': 'Fall 3 more blocks before it hurts',
	'item.espresso_flavoured.knit_beanie': 'Knit Beanie',
	'item.espresso_flavoured.knit_beanie.desc': 'Cozy heals you anywhere, not just near home',
	'item.espresso_flavoured.ember_glove': 'Ember Glove',
	'item.espresso_flavoured.ember_glove.desc': 'Smelts ores, sand, cobblestone and logs as you mine them. Crouch to mine normally',
	'item.espresso_flavoured.copper_tamper': 'Copper Tamper',
	'item.espresso_flavoured.copper_tamper.desc': '15% more damage to hostile mobs',
	'item.espresso_flavoured.charm.carried': 'Works in your off-hand',
	'trinkets.slot.legs.pocket': 'Pocket',
	'block.espresso_flavoured.coffee_bush': 'Coffee Bush',
	'item.espresso_flavoured.coffee_cherries': 'Coffee Cherries',
	'item.espresso_flavoured.roasted_coffee_beans': 'Roasted Coffee Beans',
	'item.espresso_flavoured.espresso': 'Espresso',
	'item.espresso_flavoured.latte': 'Latte',
	'item.espresso_flavoured.mocha_latte': 'Mocha Latte',
	'effect.espresso_flavoured.drowsy': 'Drowsy',
	'effect.espresso_flavoured.caffeinated': 'Caffeinated',
	'effect.espresso_flavoured.cozy': 'Cozy',
	'effect.espresso_flavoured.well_rested': 'Well Rested',
	'item.espresso_flavoured.decaf_coffee_beans': 'Decaf Coffee Beans',
	'item.espresso_flavoured.decaf': 'Decaf',
	'effect.espresso_flavoured.warm': 'Warm',
	'item.espresso_flavoured.cinnamon': 'Cinnamon',
	'item.espresso_flavoured.pumpkin_spice': 'Pumpkin Spice',
	'item.espresso_flavoured.cocoa_mix': 'Cocoa Mix',
	'item.espresso_flavoured.pumpkin_spice_latte': 'Pumpkin Spice Latte',
	'item.espresso_flavoured.iced_coffee': 'Iced Coffee',
	'item.espresso_flavoured.night_shift': 'Night Shift',
	'item.espresso_flavoured.honey_latte': 'Honey Latte',
	'item.espresso_flavoured.caramel_macchiato': 'Caramel Macchiato',
	'item.espresso_flavoured.cold_brew': 'Cold Brew',
	'item.espresso_flavoured.caramel': 'Caramel',
	'item.espresso_flavoured.coffee_grounds': 'Coffee Grounds',
	'jade.espresso_flavoured.coffee_bush.0': 'Seedling',
	'jade.espresso_flavoured.coffee_bush.1': 'Young',
	'jade.espresso_flavoured.coffee_bush.2': 'Grown',
	'jade.espresso_flavoured.coffee_bush.3': 'Turning',
	'jade.espresso_flavoured.coffee_bush.4': 'Ripe: ready to pick',
	'jade.espresso_flavoured.espresso_machine.brewing': 'Brewing: %s%%',
	'jade.espresso_flavoured.espresso_machine.idle': 'Waiting for beans and a mug',
	'jade.espresso_flavoured.espresso_machine.no_bar': 'Needs a coffee bar underneath',
	'config.jade.plugin_espresso_flavoured.placed_mug': 'Placed mug drink names',
	'config.jade.plugin_espresso_flavoured.coffee_bush': 'Coffee bush growth',
	'config.jade.plugin_espresso_flavoured.espresso_machine': 'Espresso machine brewing',
	'entity.espresso_flavoured.seat': 'Seat',
	'item.espresso_flavoured.croissant': 'Croissant',
	'item.espresso_flavoured.biscotti': 'Biscotti',
	'item.espresso_flavoured.muffin': 'Berry Muffin',
	'item.espresso_flavoured.hot_chocolate': 'Hot Chocolate',
	'espresso_flavoured.options.sugarCaneHeight': 'Sugar Cane Height',
	'espresso_flavoured.options.sugarCaneHeight.tooltip': 'How tall sugar cane grows. Vanilla is 3.',
	'block.espresso_flavoured.espresso_machine': 'Espresso Machine',
	'item.espresso_flavoured.mug': 'Mug',
	'item.espresso_flavoured.unfired_mug': 'Unfired Mug',
	'block.espresso_flavoured.placed_mug': 'Mug',
	'item.espresso_flavoured.vanilla_syrup': 'Vanilla Syrup',
	'item.espresso_flavoured.vanilla_latte': 'Vanilla Latte',
	'item.espresso_flavoured.golden_mug': 'Golden Mug',
	'item.espresso_flavoured.bean_coin': 'Bean Coin',
	'jei.espresso_flavoured.espresso_machine.needs_bar': 'Brews on top of a coffee bar',
	'item.espresso_flavoured.bean_coin.desc': 'One for each Espresso advancement. A master barista takes them',
	'item.espresso_flavoured.unfired_canister': 'Unfired Canister',
	'block.espresso_flavoured.canister': 'Canister',
	'block.espresso_flavoured.glass_jar': 'Glass Jar',
	'container.espresso_flavoured.espresso_machine.needs_bar': 'Needs a coffee bar',
	'container.espresso_flavoured.coffee_bar': 'Coffee Bar',
	...Object.fromEntries(['oak', 'spruce', 'birch', 'jungle', 'acacia', 'dark_oak', 'mangrove', 'cherry', 'pale_oak', 'poplar', 'bamboo', 'crimson', 'warped'].map(w => [`block.espresso_flavoured.${w}_coffee_bar`, w.split('_').map(p => p[0].toUpperCase() + p.slice(1)).join(' ') + ' Coffee Bar'])),
	'item.espresso_flavoured.baristas_reserve': 'Barista\x27s Reserve',
	'item.espresso_flavoured.ristretto': 'Ristretto',
	'entity.espresso_flavoured.villager.barista': 'Barista',
	'advancements.espresso_flavoured.guide.barista.title': 'Barista',
	'advancements.espresso_flavoured.guide.first_sip.title': 'First Sip',
	'advancements.espresso_flavoured.guide.mug_shot.title': 'Mug Shot',
	'advancements.espresso_flavoured.guide.fair_trade.title': 'Fair Trade',
	'advancements.espresso_flavoured.guide.house_special.title': 'House Special',
	'advancements.espresso_flavoured.guide.sweet_dreams.title': 'Sweet Dreams',
	'container.espresso_flavoured.espresso_machine': 'Espresso Machine',
	"espresso_flavoured.options.title": "Espresso Flavoured Settings",
	"espresso_flavoured.options.section.days": "Days and Nights",
	"espresso_flavoured.options.section.wellbeing": "Looking After Yourself",
	"espresso_flavoured.options.section.storage": "Storage and Trading",
	"espresso_flavoured.options.on_server": "On a server, the greyed-out settings are the server's.",
	"espresso_flavoured.options.longerDays": "Longer Days",
	"espresso_flavoured.options.longerDays.tooltip": "20-minute days and 10-minute nights. Takes effect after restarting the game.",
	"espresso_flavoured.options.toughNights": "Tougher Nights",
	"espresso_flavoured.options.toughNights.tooltip": "A few more monsters at night, with a little more health and damage.",
	"espresso_flavoured.options.regenNeedsSaturation": "Heal Only When Fed",
	"espresso_flavoured.options.regenNeedsSaturation.tooltip": "Natural healing only happens when you have eaten recently.",
	"espresso_flavoured.options.drowsiness": "Drowsiness",
	"espresso_flavoured.options.drowsiness.tooltip": "Become Drowsy after three days without sleeping at home (crouch and use a bed with an empty hand to make it home). Coffee holds it off while it lasts.",
	"espresso_flavoured.options.caffeineCrash": "Caffeine Crash",
	"espresso_flavoured.options.caffeineCrash.tooltip": "A harder Drowsy: coffee only holds it off for a day before you crash, and three days without eating as well makes you Hungry.",
	"espresso_flavoured.options.section.coffee": "Coffee",
	"espresso_flavoured.options.section.world": "Out in the World",
	"espresso_flavoured.options.gazeboSettings": "Gazebo Settings…",
	"espresso_flavoured.options.gazeboSettings.tooltip": "The gazebo at spawn is Spawn Gazebo's, which Espresso includes: whether it's built, its starter chest, and how far its protection reaches.",
	"espresso_flavoured.options.coffeeBarRequired": "Machine Needs a Coffee Bar",
	"espresso_flavoured.options.coffeeBarRequired.tooltip": "The espresso machine only brews on top of a coffee bar.",
	"espresso_flavoured.options.bundleUnstackables": "Tools per Bundle",
	"espresso_flavoured.options.bundleUnstackables.tooltip": "How many tools or pieces of armour fit in one bundle (1 is vanilla). On a server, the server's setting is used.",
	"espresso_flavoured.options.blockMerchant": "Block Merchant",
	"espresso_flavoured.options.blockMerchant.tooltip": "The wandering trader also sells building blocks from far-off biomes.",
	"message.espresso_flavoured.home_set": "This bed is now your home: a night here clears Drowsy, and Cozy heals you nearby.",
	"message.espresso_flavoured.home_not_here": "A bed can't be your home here.",
	"message.espresso_flavoured.home_taken": "This bed is already someone else's home.",
	// Names of the built-in packs in the Resource Packs and Data Packs screens.
	"pack.espresso_flavoured.data": "Espresso Flavoured",
	"pack.espresso_flavoured.look": "Espresso Flavoured Look",
	"pack.espresso_flavoured.long_days": "Espresso Flavoured: Longer Days",
});
console.log('coffee assets written');

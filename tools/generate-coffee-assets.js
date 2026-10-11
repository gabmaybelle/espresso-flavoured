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

// Loyalty card: a cream card with a coffee cup logo and six stamp circles, filled one by one.
for (let n = 0; n <= 6; n++) {
	png(`item/loyalty_card_${n}`, grid((x, y) => {
		if (y < 3 || y > 12 || x < 1 || x > 14) return '.';
		if (y === 3 || y === 12 || x === 1 || x === 14) return 'e';
		if (y >= 4 && y <= 6 && x >= 3 && x <= 5) return y === 4 ? 'b' : 'B'; // cup logo
		if (y === 5 && x === 6) return 'b';
		if (y === 5 && x >= 8 && x <= 12) return 'l'; // the café's name, as a line
		// Stamps: two rows of three, two pixels each, at x = 4, 7, 10 and y = 7..8, 10..11.
		const column = [4, 7, 10].findIndex(sx => x === sx || x === sx + 1);
		const row = y === 7 || y === 8 ? 0 : y === 10 || y === 11 ? 1 : -1;
		if (column >= 0 && row >= 0) return row * 3 + column < n ? 's' : 'o';
		return 'c';
	}), { e: [150, 120, 90], c: [246, 236, 214], b: [112, 64, 34], B: [164, 104, 58], l: [190, 170, 140], o: [214, 200, 176], s: [176, 52, 40] });
}

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
const bushModel = (age, half) => age < 2 ? `espresso:block/coffee_bush_stage${age}` : `espresso:block/coffee_bush_tall${age}_${half === 'upper' ? 'top' : 'bottom'}`;
out('blockstates/coffee_bush.json', { variants: Object.fromEntries([0, 1, 2, 3, 4].flatMap(age => ['lower', 'upper'].map(half =>
	[`age=${age},half=${half}`, { model: bushModel(age < 2 && half === 'upper' ? 2 : age, half) }]))) });
for (const a of [0, 1]) out(`models/block/coffee_bush_stage${a}.json`, { parent: 'minecraft:block/cross', textures: { cross: `espresso:block/coffee_bush_stage${a}` } });
for (const a of [2, 3, 4]) for (const half of ['top', 'bottom']) {
	out(`models/block/coffee_bush_tall${a}_${half}.json`, { parent: 'minecraft:block/cross', textures: { cross: `espresso:block/coffee_bush_tall${a}_${half}` } });
}
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
	particle: 'espresso:block/mug_glaze', glaze: 'espresso:block/mug_glaze', rim: 'espresso:block/mug_rim', saucer: 'espresso:block/saucer',
	...Object.fromEntries(TOPPINGS.map(t => [t, `espresso:block/mug_${t}`])),
};
out('models/block/mug_empty.json', { parent: 'minecraft:block/block', textures: mugTextures, elements: mugElements });
for (const [name, [glow, toppings]] of Object.entries(DRINKS)) {
	out(`models/block/mug_${name}.json`, { parent: 'minecraft:block/block', textures: { ...mugTextures, liquid: `espresso:block/mug_${name}` },
		elements: [...mugElements, surface(glow), ...toppings] });
}
for (const stale of ['mug_full']) fs.rmSync(path.join(A, 'models', 'block', stale + '.json'), { force: true });
for (const stale of ['mug', 'mug_inside']) fs.rmSync(path.join(A, 'textures', 'block', stale + '.png'), { force: true });
out('blockstates/placed_mug.json', { variants: Object.fromEntries(
	[['north', 0], ['east', 90], ['south', 180], ['west', 270]].flatMap(([facing, y]) =>
		['empty', ...Object.keys(DRINKS)].map(c => [`contents=${c},facing=${facing}`, { model: `espresso:block/mug_${c}`, ...(y ? { y } : {}) }])),
) });
// Vanilla, the golden mug, the loyalty card and canisters.
for (const item of ['vanilla_syrup', 'unfired_canister']) {
	out(`items/${item}.json`, { model: { type: 'minecraft:model', model: `espresso:item/${item}` } });
	out(`models/item/${item}.json`, { parent: 'minecraft:item/generated', textures: { layer0: `espresso:item/${item}` } });
}
out('items/vanilla_latte.json', dyed('espresso:item/vanilla_latte'));
for (const drink of ['pumpkin_spice_latte', 'iced_coffee', 'night_shift', 'hot_chocolate', 'honey_latte', 'caramel_macchiato', 'cold_brew']) {
	out(`items/${drink}.json`, dyed(`espresso:item/${drink}`));
	out(`models/item/${drink}.json`, { parent: 'minecraft:item/generated', textures: { layer0: 'espresso:item/mug', layer1: `espresso:item/${drink}_fill` } });
}
out('models/item/vanilla_latte.json', { parent: 'minecraft:item/generated', textures: { layer0: 'espresso:item/mug', layer1: 'espresso:item/vanilla_latte_fill' } });
// The golden mug carries its gold colour as a dye, so it shares the mug's tinted model.
out('items/golden_mug.json', dyed('espresso:item/mug'));
for (let n = 0; n <= 6; n++) {
	out(`models/item/loyalty_card_${n}.json`, { parent: 'minecraft:item/generated', textures: { layer0: `espresso:item/loyalty_card_${n}` } });
}
out('items/loyalty_card.json', { model: {
	type: 'minecraft:range_dispatch', property: 'minecraft:custom_model_data', index: 0,
	entries: [1, 2, 3, 4, 5, 6].map(n => ({ threshold: n, model: { type: 'minecraft:model', model: `espresso:item/loyalty_card_${n}` } })),
	fallback: { type: 'minecraft:model', model: 'espresso:item/loyalty_card_0' },
} });
const canisterTextures = { particle: 'espresso:block/canister', side: 'espresso:block/canister', top: 'espresso:block/canister_top' };
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
out('items/canister.json', dyed('espresso:block/canister'));
out('blockstates/canister.json', { variants: Object.fromEntries(
	[['north', 0], ['east', 90], ['south', 180], ['west', 270]].map(([facing, y]) => [`facing=${facing}`, { model: 'espresso:block/canister', ...(y ? { y } : {}) }]),
) });
// Glass jar, the Reserve and Ristretto.
for (const item of ['glass_jar', 'baristas_reserve', 'decaf_coffee_beans', 'pumpkin_spice', 'cocoa_mix', 'cinnamon', 'caramel', 'croissant', 'biscotti', 'muffin', 'coffee_grounds']) {
	out(`items/${item}.json`, { model: { type: 'minecraft:model', model: `espresso:item/${item}` } });
	out(`models/item/${item}.json`, { parent: 'minecraft:item/generated', textures: { layer0: `espresso:item/${item}` } });
}
out('items/ristretto.json', dyed('espresso:item/ristretto'));
out('models/item/ristretto.json', { parent: 'minecraft:item/generated', textures: { layer0: 'espresso:item/mug', layer1: 'espresso:item/ristretto_fill' } });
const all6 = (texture) => Object.fromEntries(['north', 'south', 'east', 'west', 'up', 'down'].map(f => [f, { texture }]));
out('models/block/glass_jar.json', { parent: 'minecraft:block/block', textures: { particle: 'espresso:block/glass_jar', glass: 'espresso:block/glass_jar', lid: 'minecraft:block/oak_planks' }, elements: [
	{ from: [4, 0, 4], to: [12, 11, 12], faces: all6('#glass') },
	{ from: [5, 11, 5], to: [11, 13, 11], faces: all6('#lid') },
] });
// Coffee bars: plank cabinets with doors on the front and a smooth stone counter top.
out('models/block/coffee_bar.json', { parent: 'minecraft:block/block', textures: { particle: '#planks', top: 'minecraft:block/smooth_stone',
	top_side: 'minecraft:block/smooth_stone_slab_side', doors: 'espresso:block/coffee_bar_doors' }, elements: [
	{ from: [0, 0, 0], to: [16, 14, 16], faces: { ...Object.fromEntries(['north', 'south', 'east', 'west'].map(f => [f, { texture: '#planks', uv: [0, 2, 16, 16] }])), down: { texture: '#planks', cullface: 'down' } } },
	{ from: [0, 14, 0], to: [16, 16, 16], faces: { up: { texture: '#top', cullface: 'up' }, ...Object.fromEntries(['north', 'south', 'east', 'west'].map(f => [f, { texture: '#top_side', uv: [0, 0, 16, 2] }])) } },
	{ from: [0.5, 0.5, -0.01], to: [15.5, 13.5, -0.01], faces: { north: { texture: '#doors', uv: [0, 0, 16, 14] } } },
] });
for (const wood of ['oak', 'spruce', 'birch', 'jungle', 'acacia', 'dark_oak', 'mangrove', 'cherry', 'pale_oak', 'poplar', 'bamboo', 'crimson', 'warped']) {
	out(`models/block/${wood}_coffee_bar.json`, { parent: 'espresso:block/coffee_bar', textures: { planks: `minecraft:block/${wood}_planks` } });
	out(`items/${wood}_coffee_bar.json`, { model: { type: 'minecraft:model', model: `espresso:block/${wood}_coffee_bar` } });
	out(`blockstates/${wood}_coffee_bar.json`, { variants: Object.fromEntries(
		[['north', 0], ['east', 90], ['south', 180], ['west', 270]].map(([facing, y]) => [`facing=${facing}`, { model: `espresso:block/${wood}_coffee_bar`, ...(y ? { y } : {}) }]),
	) });
}
out('blockstates/glass_jar.json', { variants: Object.fromEntries(
	[['north', 0], ['east', 90], ['south', 180], ['west', 270]].map(([facing, y]) => [`facing=${facing}`, { model: 'espresso:block/glass_jar', ...(y ? { y } : {}) }]),
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
	for (const [x, y] of [[56, 51], [79, 58], [102, 51], [79, 17], [17, 17]]) slot(x, y); // the last is the knock box
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
	'effect.espresso.cozy': 'Cozy',
	'effect.espresso.well_rested': 'Well Rested',
	'item.espresso.decaf_coffee_beans': 'Decaf Coffee Beans',
	'item.espresso.decaf': 'Decaf',
	'effect.espresso.warm': 'Warm',
	'item.espresso.cinnamon': 'Cinnamon',
	'item.espresso.pumpkin_spice': 'Pumpkin Spice',
	'item.espresso.cocoa_mix': 'Cocoa Mix',
	'item.espresso.pumpkin_spice_latte': 'Pumpkin Spice Latte',
	'item.espresso.iced_coffee': 'Iced Coffee',
	'item.espresso.night_shift': 'Night Shift',
	'item.espresso.honey_latte': 'Honey Latte',
	'item.espresso.caramel_macchiato': 'Caramel Macchiato',
	'item.espresso.cold_brew': 'Cold Brew',
	'item.espresso.caramel': 'Caramel',
	'item.espresso.coffee_grounds': 'Coffee Grounds',
	'jade.espresso.coffee_bush.0': 'Seedling',
	'jade.espresso.coffee_bush.1': 'Young',
	'jade.espresso.coffee_bush.2': 'Grown',
	'jade.espresso.coffee_bush.3': 'Turning',
	'jade.espresso.coffee_bush.4': 'Ripe: ready to pick',
	'jade.espresso.espresso_machine.brewing': 'Brewing: %s%%',
	'jade.espresso.espresso_machine.idle': 'Waiting for beans and a mug',
	'jade.espresso.espresso_machine.no_bar': 'Needs a coffee bar underneath',
	'config.jade.plugin_espresso.placed_mug': 'Placed mug drink names',
	'config.jade.plugin_espresso.coffee_bush': 'Coffee bush growth',
	'config.jade.plugin_espresso.espresso_machine': 'Espresso machine brewing',
	'entity.espresso.seat': 'Seat',
	'item.espresso.croissant': 'Croissant',
	'item.espresso.biscotti': 'Biscotti',
	'item.espresso.muffin': 'Berry Muffin',
	'item.espresso.hot_chocolate': 'Hot Chocolate',
	'espresso.options.sugarCaneHeight': 'Sugar Cane Height',
	'espresso.options.sugarCaneHeight.tooltip': 'How tall sugar cane grows. Vanilla is 3.',
	'block.espresso.espresso_machine': 'Espresso Machine',
	'item.espresso.mug': 'Mug',
	'item.espresso.unfired_mug': 'Unfired Mug',
	'block.espresso.placed_mug': 'Mug',
	'item.espresso.vanilla_syrup': 'Vanilla Syrup',
	'item.espresso.vanilla_latte': 'Vanilla Latte',
	'item.espresso.golden_mug': 'Golden Mug',
	'item.espresso.loyalty_card': 'Loyalty Card',
	'item.espresso.loyalty_card.full': 'Full! Swap it with the wandering trader for the Golden Mug',
	'item.espresso.unfired_canister': 'Unfired Canister',
	'block.espresso.canister': 'Canister',
	'block.espresso.glass_jar': 'Glass Jar',
	'container.espresso.espresso_machine.needs_bar': 'Needs a coffee bar',
	...Object.fromEntries(['oak', 'spruce', 'birch', 'jungle', 'acacia', 'dark_oak', 'mangrove', 'cherry', 'pale_oak', 'poplar', 'bamboo', 'crimson', 'warped'].map(w => [`block.espresso.${w}_coffee_bar`, w.split('_').map(p => p[0].toUpperCase() + p.slice(1)).join(' ') + ' Coffee Bar'])),
	'item.espresso.baristas_reserve': 'Barista\x27s Reserve',
	'item.espresso.ristretto': 'Ristretto',
	'entity.espresso.villager.barista': 'Barista',
	'advancements.espresso.guide.barista.title': 'Barista',
	'advancements.espresso.guide.first_sip.title': 'First Sip',
	'advancements.espresso.guide.mug_shot.title': 'Mug Shot',
	'advancements.espresso.guide.fair_trade.title': 'Fair Trade',
	'advancements.espresso.guide.house_special.title': 'House Special',
	'advancements.espresso.guide.sweet_dreams.title': 'Sweet Dreams',
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
	"espresso.options.clock": "Clock",
	"espresso.options.clock.tooltip": "A day counter and clock in the top-right corner, right for the longer days. It turns gold in the hour before night.",
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
	"espresso.options.caffeineCrash": "Caffeine Crash",
	"espresso.options.caffeineCrash.tooltip": "Coffee holds off Drowsy for up to a day, then you crash. Off: coffee holds it off as long as you keep drinking.",
	"espresso.options.section.coffee": "Coffee",
	"espresso.options.section.world": "Out in the World",
	"espresso.options.lusherRivers": "Lusher Rivers",
	"espresso.options.lusherRivers.tooltip": "Seagrass and kelp, plants and flowers on the banks, moss and mud at the waterline, and more firefly bushes and lily pads, in newly explored land. Off by default with Terralith, which dresses the world itself. Takes effect after restarting the game.",
	"espresso.options.coffeeBarRequired": "Machine Needs a Coffee Bar",
	"espresso.options.coffeeBarRequired.tooltip": "The espresso machine only brews on top of a coffee bar.",
	"espresso.options.bundleUnstackables": "Tools per Bundle",
	"espresso.options.bundleUnstackables.tooltip": "How many tools or pieces of armour fit in one bundle (1 is vanilla). On a server, the server's setting is used.",
	"espresso.options.blockMerchant": "Block Merchant",
	"espresso.options.blockMerchant.tooltip": "The wandering trader also sells building blocks from far-off biomes.",
});
console.log('coffee assets written');

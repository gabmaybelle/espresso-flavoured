// Draws Espresso Flavoured's title subtitle and mod icon, and writes the splash texts.
//   node tools/generate-art.js
const fs = require('fs'), path = require('path'), zlib = require('zlib');
const RES = path.join(__dirname, '..', 'src', 'main', 'resources');
const LOOK = path.join(RES, 'resourcepacks', 'espresso_look');

// ---------------------------------------------------------------- PNG helpers
const T = []; for (let n = 0; n < 256; n++) { let c = n; for (let k = 0; k < 8; k++) c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1; T[n] = c >>> 0; }
const crc = b => { let x = 0xffffffff; for (const v of b) x = T[(x ^ v) & 255] ^ (x >>> 8); return (x ^ 0xffffffff) >>> 0; };
const chunk = (t, d) => { const l = Buffer.alloc(4); l.writeUInt32BE(d.length); const td = Buffer.concat([Buffer.from(t), d]); const c = Buffer.alloc(4); c.writeUInt32BE(crc(td)); return Buffer.concat([l, td, c]); };
function canvas(w, h) {
	const px = new Uint8Array(w * h * 4);
	return {
		w, h, px,
		set(x, y, [r, g, b, a = 255]) { if (x < 0 || y < 0 || x >= w || y >= h) return; const i = (y * w + x) * 4; px[i] = r; px[i + 1] = g; px[i + 2] = b; px[i + 3] = a; },
		get(x, y) { const i = (y * w + x) * 4; return px[i + 3]; },
		rect(x, y, rw, rh, c) { for (let yy = 0; yy < rh; yy++) for (let xx = 0; xx < rw; xx++) this.set(x + xx, y + yy, c); },
		save(file) {
			const rows = []; for (let y = 0; y < h; y++) { const r = Buffer.alloc(1 + w * 4); r.set(px.subarray(y * w * 4, (y + 1) * w * 4), 1); rows.push(r); }
			const ih = Buffer.alloc(13); ih.writeUInt32BE(w, 0); ih.writeUInt32BE(h, 4); ih[8] = 8; ih[9] = 6;
			fs.mkdirSync(path.dirname(file), { recursive: true });
			fs.writeFileSync(file, Buffer.concat([Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]), chunk('IHDR', ih), chunk('IDAT', zlib.deflateSync(Buffer.concat(rows), { level: 9 })), chunk('IEND', Buffer.alloc(0))]));
		},
	};
}

// ---------------------------------------------------------------- blocky font (5x7)
const FONT = {
	E: ['#####', '#....', '#....', '####.', '#....', '#....', '#####'],
	S: ['.####', '#....', '#....', '.###.', '....#', '....#', '####.'],
	P: ['####.', '#...#', '#...#', '####.', '#....', '#....', '#....'],
	R: ['####.', '#...#', '#...#', '####.', '#.#..', '#..#.', '#...#'],
	O: ['.###.', '#...#', '#...#', '#...#', '#...#', '#...#', '.###.'],
	F: ['#####', '#....', '#....', '####.', '#....', '#....', '#....'],
	L: ['#....', '#....', '#....', '#....', '#....', '#....', '#####'],
	A: ['.###.', '#...#', '#...#', '#####', '#...#', '#...#', '#...#'],
	V: ['#...#', '#...#', '#...#', '#...#', '#...#', '.#.#.', '..#..'],
	U: ['#...#', '#...#', '#...#', '#...#', '#...#', '#...#', '.###.'],
	D: ['####.', '#...#', '#...#', '#...#', '#...#', '#...#', '####.'],
	' ': ['...', '...', '...', '...', '...', '...', '...'],
};
const CREMA_TOP = [244, 220, 172], CREMA_BOTTOM = [196, 136, 78], ESPRESSO = [74, 44, 26];

/** Plain pixel text, one texture pixel to a screen pixel, like the game's own "Java Edition": a crema-to-latte fade with a one-pixel espresso shadow. */
function pixelText(text, w, h, top) {
	const cells = [];
	// Each letter is as wide as its glyph, plus a column of space.
	let at = 0;
	for (const ch of text) {
		FONT[ch].forEach((row, y) => [...row].forEach((c, x) => { if (c === '#') cells.push([at + x, y]); }));
		at += FONT[ch][0].length + 1;
	}
	const left = Math.floor((w - (at - 1)) / 2);
	const out = canvas(w, h);
	for (const [x, y] of cells) out.set(left + x + 1, top + y + 1, [...ESPRESSO, 255]);
	for (const [x, y] of cells) {
		const t = y / 6;
		out.set(left + x, top + y, [...CREMA_TOP.map((v, k) => Math.round(v * (1 - t) + CREMA_BOTTOM[k] * t)), 255]);
	}
	return out;
}

// The Minecraft logo stays as it is; "Espresso Flavoured" takes the place of "Java Edition" under it,
// drawn at the size the game shows it (128 x 16, of which it uses the top 14 rows).
fs.rmSync(path.join(LOOK, 'assets/minecraft/textures/gui/title/minecraft.png'), { force: true });
pixelText('ESPRESSO FLAVOURED', 128, 16, 3).save(path.join(LOOK, 'assets/minecraft/textures/gui/title/edition.png'));

// ---------------------------------------------------------------- mod icon: an espresso cup
{
	const G = 24, S = 20, N = 512, OFF = (N - G * S) / 2;
	const cells = Array.from({ length: G }, () => Array(G).fill(null));
	const setc = (x, y, c) => { if (x >= 0 && y >= 0 && x < G && y < G) cells[y][x] = c; };
	const K = [42, 26, 16], CUP = [244, 238, 226], CUP_SH = [214, 204, 186], CUP_SH2 = [186, 172, 150];
	const CREMA = [196, 136, 78], CREMA_H = [232, 190, 128], STEAM = [236, 236, 236, 190];
	const X0 = 5, X1 = 17, TOP = 8, BOT = 16;
	for (let x = X0 + 1; x < X1; x++) setc(x, TOP, K);
	for (let y = TOP + 1; y <= BOT; y++) {
		const inset = y >= BOT - 1 ? (y === BOT ? 2 : 1) : 0, l = X0 + inset, r = X1 - inset;
		for (let x = l; x <= r; x++) {
			let c;
			if (x === l || x === r || y === BOT) c = K;
			else if (y === TOP + 1) c = (x === l + 2 || x === l + 3) ? CREMA_H : CREMA;
			else c = x >= r - 2 ? CUP_SH2 : x >= r - 4 ? CUP_SH : CUP;
			setc(x, y, c);
		}
	}
	// handle on the right
	for (let y = TOP + 2; y <= TOP + 6; y++) { setc(X1 + 3, y, K); }
	for (const x of [X1 + 1, X1 + 2]) { setc(x, TOP + 2, K); setc(x, TOP + 6, K); }
	for (let y = TOP + 3; y <= TOP + 5; y++) setc(X1 + 1, y, CUP_SH);
	// saucer
	for (let x = 2; x <= 20; x++) setc(x, BOT + 1, x === 2 || x === 20 ? K : (x > 15 ? CUP_SH : CUP));
	for (let x = 3; x <= 19; x++) setc(x, BOT + 2, K);
	// steam
	for (const [x0, ph] of [[8, 0], [11, 1], [14, 0]]) for (let y = 3; y < 7; y++) setc(x0 + ((y + ph) % 2), y, STEAM);
	const img = canvas(N, N);
	const fill = (cx, cy, c, a) => img.rect(OFF + cx * S, OFF + cy * S, S, S, [c[0], c[1], c[2], a]);
	for (let y = 0; y < G; y++) for (let x = 0; x < G; x++) if (cells[y][x] && cells[y][x].length === 3 && (y + 1 >= G || x + 1 >= G || !cells[y + 1][x + 1])) fill(x + 1, y + 1, [20, 12, 6], 100);
	for (let y = 0; y < G; y++) for (let x = 0; x < G; x++) if (cells[y][x]) fill(x, y, cells[y][x], cells[y][x][3] ?? 255);
	img.save(path.join(RES, 'assets/espresso_flavoured/icon.png'));
	img.save(path.join(LOOK, 'pack.png'));
}

// ---------------------------------------------------------------- splash texts
const SPLASHES = [
	'Freshly brewed!', 'Builders welcome!', 'Extra shot of survival!', 'Creepers, but polite!',
	'Now with zero phantoms!', 'Your bed has your back!', 'Sleep when you like!', 'Grab a mug and build!',
	'Crema of the crop!', 'Less grind, more building!', 'Endermen keep their hands to themselves!',
	'A shulker box by lunchtime!', 'Bundle up!', 'The trader brought terracotta!', 'House blend!',
	'Double shot of cosy!', 'Built to last!', 'Calm and productive!', 'Rise and build!',
	'Take your time!', 'Home is where the bed is!', 'Read the field notes!', 'Fully caffeinated creativity!',
	'Mud bricks on the menu!', 'Your builds stay built!', 'Small batch, big builds!',
	'Made in Canada, eh!', 'Pour over your plans!', 'Steamed and ready!', 'One more block!',
];
fs.mkdirSync(path.join(LOOK, 'assets/minecraft/texts'), { recursive: true });
fs.writeFileSync(path.join(LOOK, 'assets/minecraft/texts/splashes.txt'), SPLASHES.join('\n') + '\n');
fs.writeFileSync(path.join(LOOK, 'pack.mcmeta'), JSON.stringify({ pack: { description: 'Espresso Flavoured: title subtitle and splash texts', min_format: [97, 1], max_format: [97, 1] } }, null, 2) + '\n');
console.log('art and splashes written');

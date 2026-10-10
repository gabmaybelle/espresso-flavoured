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

/** A corked glass bottle filled with a drink: L liquid, F foam on top, D a detail colour. */
function drink(name, liquid, foam, detail) {
	png(`item/${name}`, [
		'................',
		'......oOOo......',
		'......oOOo......',
		'.......qq.......',
		'......qQQq......',
		'.....qQ..Qq.....',
		'....qFFFFFFq....',
		'...qFFFFFFFFq...',
		'...qLLLDLLLLq...',
		'...qLLLLLLDLq...',
		'...qLDLLLLLLq...',
		'...qLLLLLDLLq...',
		'....qLLLLLLq....',
		'.....qqqqqq.....',
		'................',
		'................',
	], { ...GLASS, L: liquid, F: foam, D: detail });
}
drink('espresso', [58, 30, 14], [196, 136, 78], [44, 22, 10]);
drink('latte', [178, 128, 84], [246, 238, 222], [160, 112, 72]);
drink('mocha_latte', [104, 58, 32], [250, 246, 238], [70, 36, 18]);

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
for (const item of ['coffee_cherries', 'roasted_coffee_beans', 'espresso', 'latte', 'mocha_latte']) {
	out(`items/${item}.json`, { model: { type: 'minecraft:model', model: `espresso:item/${item}` } });
	out(`models/item/${item}.json`, { parent: 'minecraft:item/generated', textures: { layer0: `espresso:item/${item}` } });
}
out('lang/en_us.json', {
	'block.espresso.coffee_bush': 'Coffee Bush',
	'item.espresso.coffee_cherries': 'Coffee Cherries',
	'item.espresso.roasted_coffee_beans': 'Roasted Coffee Beans',
	'item.espresso.espresso': 'Espresso',
	'item.espresso.latte': 'Latte',
	'item.espresso.mocha_latte': 'Mocha Latte',
	'effect.espresso.drowsy': 'Drowsy',
	'effect.espresso.caffeinated': 'Caffeinated',
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

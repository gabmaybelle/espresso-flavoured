// Cinnamon: stripping a jungle or acacia log (or wood) with an axe sometimes drops cinnamon bark.
//   node tools/generate-strip.js "<path to the Minecraft 26.3 client jar>"
//
// 26.3 axes strip logs through the data-driven block transformer in
// data/minecraft/block_transformer/axe.json, which can also drop loot. This reads vanilla's
// file from the jar and writes a copy with one extra transform at the front: the jungle and
// acacia rules, plus the espresso:strip/cinnamon loot. The first transform that matches a
// block wins, so everything else strips exactly as in vanilla.
const fs = require('fs'), path = require('path'), zlib = require('zlib');
const JAR = process.argv[2];
if (!JAR) { console.error('usage: node tools/generate-strip.js <minecraft 26.3 jar>'); process.exit(1); }
const OUT = path.join(__dirname, '..', 'src', 'main', 'resources', 'data', 'minecraft', 'block_transformer', 'axe.json');

function readZipEntry(zipFile, name) {
	const buf = fs.readFileSync(zipFile);
	let eocd = buf.length - 22;
	while (eocd >= 0 && buf.readUInt32LE(eocd) !== 0x06054b50) eocd--;
	const count = buf.readUInt16LE(eocd + 10);
	let p = buf.readUInt32LE(eocd + 16);
	for (let i = 0; i < count; i++) {
		const method = buf.readUInt16LE(p + 10), csize = buf.readUInt32LE(p + 20);
		const nlen = buf.readUInt16LE(p + 28), xlen = buf.readUInt16LE(p + 30), clen = buf.readUInt16LE(p + 32);
		const local = buf.readUInt32LE(p + 42), fname = buf.toString('utf8', p + 46, p + 46 + nlen);
		if (fname === name) {
			const start = local + 30 + buf.readUInt16LE(local + 26) + buf.readUInt16LE(local + 28);
			const data = buf.subarray(start, start + csize);
			return method === 8 ? zlib.inflateRawSync(data) : data;
		}
		p += 46 + nlen + xlen + clen;
	}
	throw new Error(`${name} not found in ${zipFile}`);
}

const axe = JSON.parse(readZipEntry(JAR, 'data/minecraft/block_transformer/axe.json').toString('utf8'));
const strip = axe[0];
const CINNAMON_WOOD = ['minecraft:jungle_log', 'minecraft:jungle_wood', 'minecraft:acacia_log', 'minecraft:acacia_wood'];
const rules = strip.block_state_provider.rules.filter(r => CINNAMON_WOOD.includes(r.if_true.blocks));
if (rules.length !== CINNAMON_WOOD.length) throw new Error(`expected ${CINNAMON_WOOD.length} jungle/acacia strip rules, found ${rules.length}`);
const cinnamon = {
	block_state_provider: { type: 'minecraft:rule_based', rules },
	drop_strategy: 'clicked_face',
	item_damage_per_use: strip.item_damage_per_use,
	loot: 'espresso:strip/cinnamon',
	sound: strip.sound,
};
fs.mkdirSync(path.dirname(OUT), { recursive: true });
fs.writeFileSync(OUT, JSON.stringify([cinnamon, ...axe], null, 2) + '\n');
console.log(`axe.json written with cinnamon from ${rules.length} jungle/acacia rules`);

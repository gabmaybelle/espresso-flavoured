// Writes the espresso_long_days built-in data pack: longer daylight, normal nights.
// Vanilla's day is 24000 ticks (tick 0 = 6:00 AM, 12000 = 6:00 PM). Espresso Flavoured
// plays daylight (0..12000) at half speed and night (12000..24000) at normal speed, so
// a full day is 36000 ticks: 20 minutes of daylight and 10 minutes of night.
//
//   node tools/generate-long-days.js "<path to the Minecraft 26.3 client jar>"
//
// It reads vanilla's own timeline files from the jar and writes re-timed copies.
const fs = require('fs'), path = require('path'), zlib = require('zlib');
const JAR = process.argv[2];
if (!JAR) { console.error('usage: node tools/generate-long-days.js <minecraft 26.3 jar>'); process.exit(1); }
const ROOT = path.join(__dirname, '..', 'src', 'main', 'resources', 'resourcepacks', 'espresso_long_days');

const VANILLA_DAY = 24000, DAYLIGHT = 12000, STRETCH = 2;
const NEW_DAY = DAYLIGHT * STRETCH + (VANILLA_DAY - DAYLIGHT);   // 36000
/** Vanilla tick within a day -> Espresso tick within a day. */
const remap = t => (t <= DAYLIGHT ? t * STRETCH : DAYLIGHT * STRETCH + (t - DAYLIGHT));

// ---------------------------------------------------------------- read files from the jar
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
const vanilla = name => JSON.parse(readZipEntry(JAR, `data/minecraft/timeline/${name}.json`).toString('utf8'));

// ---------------------------------------------------------------- easing
function cubicBezier([x1, y1, x2, y2], p) {
	const bx = s => 3 * (1 - s) ** 2 * s * x1 + 3 * (1 - s) * s ** 2 * x2 + s ** 3;
	const by = s => 3 * (1 - s) ** 2 * s * y1 + 3 * (1 - s) * s ** 2 * y2 + s ** 3;
	let lo = 0, hi = 1;
	for (let i = 0; i < 60; i++) { const mid = (lo + hi) / 2; if (bx(mid) < p) lo = mid; else hi = mid; }
	return by((lo + hi) / 2);
}

/**
 * The sun, moon and stars turn once per day along an eased curve that starts and ends at
 * one keyframe tick. Sample the vanilla angle every 200 ticks and place each sample at its
 * re-timed tick, so the sky moves slowly by day and at the usual pace by night.
 */
function retimeRotation(track) {
	const [end, start] = track.keyframes;        // e.g. {6000: 360} then {6000: 0}
	const bez = track.ease.cubic_bezier;
	const origin = start.ticks, from = start.value, to = end.value;
	const keyframes = [];
	for (let dt = 0; dt < VANILLA_DAY; dt += 200) {
		const t = (origin + dt) % VANILLA_DAY;
		const value = from + (to - from) * cubicBezier(bez, dt / VANILLA_DAY);
		keyframes.push({ ticks: remap(t), value: Math.round(value * 1000) / 1000 });
	}
	const at = remap(origin);
	keyframes.push({ ticks: at, value: to });     // the full turn ends where it began
	keyframes.sort((a, b) => a.ticks - b.ticks || (a.ticks === at ? (a.value === to ? -1 : 1) : 0));
	const { ease, ...rest } = track;
	return { ...rest, keyframes };
}

function retimeDayTimeline(timeline) {
	const out = { ...timeline, period_ticks: NEW_DAY, tracks: {} };
	for (const [id, track] of Object.entries(timeline.tracks)) {
		out.tracks[id] = track.ease && track.ease.cubic_bezier
			? retimeRotation(track)
			: { ...track, keyframes: track.keyframes.map(k => ({ ...k, ticks: remap(k.ticks) })) };
	}
	if (timeline.time_markers) {
		out.time_markers = {};
		for (const [id, marker] of Object.entries(timeline.time_markers)) {
			out.time_markers[id] = typeof marker === 'number' ? remap(marker) : { ...marker, ticks: remap(marker.ticks) };
		}
	}
	return out;
}

const write = (name, obj) => {
	const f = path.join(ROOT, 'data', 'minecraft', 'timeline', `${name}.json`);
	fs.mkdirSync(path.dirname(f), { recursive: true });
	fs.writeFileSync(f, JSON.stringify(obj, null, 2) + '\n');
};
fs.rmSync(ROOT, { recursive: true, force: true });
fs.mkdirSync(ROOT, { recursive: true });
fs.writeFileSync(path.join(ROOT, 'pack.mcmeta'), JSON.stringify({ pack: {
	description: 'Espresso Flavoured: 20-minute days, 10-minute nights', min_format: 121, max_format: 121 } }, null, 2) + '\n');

write('day', retimeDayTimeline(vanilla('day')));
write('villager_schedule', retimeDayTimeline(vanilla('villager_schedule')));

// The moon changes phase once per full day, over 8 days.
const moon = vanilla('moon');
write('moon', { ...moon, period_ticks: moon.period_ticks / VANILLA_DAY * NEW_DAY, tracks: Object.fromEntries(
	Object.entries(moon.tracks).map(([id, t]) => [id, { ...t, keyframes: t.keyframes.map(k => ({ ...k, ticks: k.ticks / VANILLA_DAY * NEW_DAY })) }])) });

// Pillager patrols still start after the same number of days.
const early = vanilla('early_game');
write('early_game', { ...early, tracks: Object.fromEntries(
	Object.entries(early.tracks).map(([id, t]) => [id, { ...t, keyframes: t.keyframes.map(k => ({ ...k, ticks: k.ticks / VANILLA_DAY * NEW_DAY })) }])) });

console.log(`espresso_long_days written: day ${NEW_DAY} ticks (daylight ${DAYLIGHT * STRETCH}, night ${VANILLA_DAY - DAYLIGHT})`);

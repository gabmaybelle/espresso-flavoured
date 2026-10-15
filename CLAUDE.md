# Espresso Flavoured Core

The mod at the heart of the Espresso Flavoured modpack (mod ID `espresso_flavoured`): a safe home
radius, builds creepers can't wreck, longer days, keep-your-hotbar deaths, roomier bundles, a block
merchant, the gazebo at spawn (Spawn Gazebo, bundled), and a café (coffee you grow and brew, mugs, a barista). Fabric,
Minecraft 26.3. See README.md for what players get.

`PROGRESS.md` says what's done, what's broken and what's next, with a row per session: read
it first, and add this session's row before ending.

## Layout

- `src/main/java/com/gabbymaybelle/espresso/`: `Espresso.java` (entrypoint, built-in packs),
  `Config.java` (`config/espresso_flavoured.properties`), the rules (`HomeBeds`, `KeepOnDeath`,
  `Wellbeing`, `Nights`), `content/` (blocks, items, the espresso machine),
  `mixin/`, `client/` and `compat/` (Jade, JEI, Trinkets: all optional, compile-only).
- `src/main/resources/resourcepacks/`: the built-in packs. `espresso_data` (recipes,
  advancements, the starter kit, trades) is always on.
- `src/gametest/`: the client game test (`EspressoClientTest`).
- `tools/`: Node scripts that generate data, structures and art.

## Build and test

- `./gradlew build`: jar in `build/libs/`.
- `./gradlew runClientGameTest`: plays through the main rules in a real game (about 1.5 minutes): settings file,
  gazebo, brewing, Ristretto, safe home, Drowsy, tough nights, block merchant, village café,
  café charms, sitting, coffee bushes, placed mugs, canisters, Well Rested, Bean Coins, barista
  trades, keep-on-death, creepers, bundles, settings screen.
  Add `-PtestTrinkets` to run it with Trinkets Updated (charms in slots, not the off-hand); CI
  runs both. Screenshots go to `build/run/clientGameTest/screenshots/`.

## Gotchas

- **`resourcepacks/espresso_data` is generated.** `node tools/generate-data.js` deletes the
  whole folder and writes it again, running the café generator (`generate-cafe.js`) as it goes. Edit the scripts, never the generated files.
- **The gazebo is Spawn Gazebo's.** `libs/spawn-gazebo-<version>.jar` (Gabby's own mod,
  github.com/gabmaybelle/spawn-gazebo) is bundled jar-in-jar (`include` in build.gradle) and
  required in fabric.mod.json; Espresso has no gazebo code of its own any more. fabric.mod.json's
  `custom.spawn_gazebo.starter_kit` makes its chest hold Espresso's kit, and the starter kit
  function reads storage `spawn_gazebo:spawn`. To take a new Spawn Gazebo: build it, copy the jar
  into libs/, update the version in build.gradle and fabric.mod.json. (Until 2026-10-13 the two
  carried duplicate copies of the builder, which had drifted.)
- **The modpack carries a copy of this jar** (espresso-flavoured-pack, `overrides/mods/`).
  After a change players should get, the pack needs a jar built from a clean, committed `main`.
- Keep the mod ID `espresso_flavoured`: worlds and configs depend on it.
- **Longer days:** a day is 36000 ticks (24000 of daylight, then night; midnight is 30000), so
  vanilla tick numbers for times of day are wrong here. Tests that need night use 30000.

## Working on Gabby's mods (the same in every repo)

Gabby makes these mods with help from Claude. Several Claude sessions work on them: one on her
computer and others in the cloud. These rules keep their work fitting together.

**One session per repo at a time.**
- A cloud session works on a `claude/...` branch and never pushes to `main`. Gabby's local
  session reviews the branch, runs its tests and merges it as one commit.
- Start from the latest `main` (`git pull`), and say in the branch's last commit or a short note
  what is finished and what isn't.

**Commits are made one at a time, by hand.** Never commit or push from a chained script. Run
the test, read its result (and the screenshot, for anything visual), then commit that one
branch, then push it. A cherry-pick that conflicts stops the work until it is resolved by hand.
A worldgen or balance change is tested on every terrain in the branch's matrix before it goes
to any other branch. (Learned on Beyond the Dragon, 2026-10-14, when scripted chains committed
a conflicted cherry-pick, put one branch's files on another, and sent an untested ore change to
all three branches.) For a Gradle game test use the toolkit's watcher (`~/.claude/CLAUDE.md`),
not a wait loop.

**Commits.**
- Author and committer are always `gabmaybelle <211247938+gabmaybelle@users.noreply.github.com>`:
  `git -c user.name=gabmaybelle -c user.email=211247938+gabmaybelle@users.noreply.github.com commit ...`.
  Never a personal email, and never `Claude <noreply@anthropic.com>` as the author. Claude goes in
  a `Co-Authored-By:` line at the end of the message.
- Ask Gabby before committing or pushing when she's around. Finish the build and the tests first.
- Commit messages say what changed for players, in plain sentences.

**Privacy.** These repos are public. Don't add personal email addresses, real names other than
"Gabby", or anything about her other private projects and modpacks: not in code, docs, commits
or test names. Check the diff for these before every push.

**Tests.**
- `./gradlew build` and `./gradlew runClientGameTest` (where the repo has one) must pass before
  a commit. The game test opens a real game window for a minute or two.
- A new or changed check should fail with the feature switched off: run it that way once, then
  put the code back. A check that compares against the setting it tests proves nothing.
- A data change (worldgen, loot, a template, a pack's mod list) gets the same proof: run the part
  of the test that sees it, and quote the numbers in the progress row.
- On Linux without a screen, run game tests under `xvfb-run -a`. 26.3 also needs
  `SDL_VIDEO_FORCE_EGL=1` with `libegl1 libegl-mesa0` installed, or the game hangs at startup.

**Writing.**
- README and CHANGELOG are for players: plain, friendly, no jargon. New player-facing changes
  go in the CHANGELOG's newest section.
- Keep the credit line: "made by Gabby with help from Claude, an AI assistant".
- Gabby publishes on Modrinth herself. Don't upload anything there.

**Minecraft.** Fabric on Minecraft 26.3 (Mojang names; Loom 1.18.4, Java 25). Vanilla names
changed a lot in 26.x: check the merged jar rather than older docs (for example `iron_chain`,
`pink_petals[flower_amount=N]`, timelines instead of `time query daytime`).

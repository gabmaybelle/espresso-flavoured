# Plan: Espresso Flavoured Core 0.4.0, the café mod

Gabby decided on 2026-10-15 that Core becomes the café mod. The builder rules that Cozy Core now
does as game rules leave Core, so a player with both mods isn't protected twice in two different
ways, and each mod has one job (the map in CLAUDE.md). This file is the cut list for Gabby to
approve. **Done on 2026-10-15** (the cut, the texts and the test changes; see PROGRESS.md), kept here
as the record of what left and why. Gabby answered the questions at the bottom that day: all four
removals, none of the five toggles, and the charm-only keep-on-death stays.

## What leaves

| Leaves | Why | What it touches |
| --- | --- | --- |
| **Builds stay built**: creepers (and the `spares_blocks` tag), endermen | Cozy Core: `mob_explosions_break_blocks`, `endermen_take_blocks` | `ServerExplosionMixin`, `EndermanTakeBlockGoalMixin`, `tags/entity_type/spares_blocks.json`, settings `creeperGriefing` and `endermanGriefing`, the "Home and Builds" settings section, test `creepers` |
| **No phantoms** | Cozy Core turns vanilla's `spawn_phantoms` off for new worlds | `PhantomSpawnerMixin`, setting `phantoms` |
| **The safe home radius** (no monsters near your home bed) | Cozy Core's Hearth Lantern does the quiet zone, as a block you place | `NaturalSpawnerMixin`, `HomeBeds.isProtected`, setting `safeHomeRadius`, the "won't spawn within %s blocks" message, test `safeHome`'s protection checks, README "A safe home" |
| **Keep your hotbar on death** (hotbar, armour, off-hand) | Cozy Core: `keep_hotbar_on_death` (moving there on the `claude/keep-on-death` branch) | `DeathLootMixin`, most of `KeepOnDeath`, setting `keepHotbarOnDeath`, test `keepOnDeath`, README "Death" |

The field guide and the README's "What it changes" say what the game does; both get rewritten to
the café story, and the settings screen loses a section.

## What stays, and changes shape

- **The home bed stays.** Crouch-using a bed still makes it your home, because Drowsy only clears
  with a night at home and Cozy heals you near it. Only the monster-free radius goes. The
  "home set" message changes to say what home is for now (rest), and `HomeBeds` keeps
  `isAtHome` and `isNearHome` (used by `Wellbeing`) and loses `isProtected`.
- **Worn charms still stay with you on death.** That's a café feature, not a builder rule, and
  Cozy Core's rule won't know about Trinkets. `KeepOnDeath` shrinks to the charm part
  (`TrinketsCompat.takeCharms` / `putBack`) and no longer touches the hotbar, armour or off-hand.
  Without Trinkets a charm is an ordinary off-hand item, so it then follows Cozy Core's rule (or
  vanilla's), which is fine.
- **Coffee, the café, Bean Coins, charms, baristas, village cafés, Drowsy, decaf and Cozy**: unchanged.
- **The gazebo** stays bundled (Spawn Gazebo jar-in-jar); it's the welcome, not a builder rule.

## Not decided: stays unless Gabby says otherwise

These aren't builder rules, so the map doesn't settle them. They stay in 0.4.0 as they are; each
is one toggle, so any can go later without a world breaking.

- [ ] **Longer days** (`longerDays`): the 36000-tick day. Drowsy counts in real days either way.
- [ ] **Tougher nights** (`toughNights`, `Nights`, `MobNightMixin`, `NightNaturalSpawnerMixin`, `SpawnStateMixin`).
- [ ] **Heal only when fed** (`regenNeedsSaturation`, `FoodDataMixin`).
- [ ] **Roomier bundles and Overworld shulker boxes** (`bundleUnstackables`, `BundleContentsMixin`, recipes).
- [ ] **The block merchant** (`blockMerchant`, `WanderingTraderMixin`, the trader tags). Cozy Core has
      its own builder trades at the wandering trader and mason, so these overlap a little; the
      coffee-cherry and fair-trade parts are café and stay whatever happens.

## Settings file

Old keys in `config/espresso_flavoured.properties` (`creeperGriefing`, `endermanGriefing`,
`phantoms`, `safeHomeRadius`, `keepHotbarOnDeath`) are simply no longer read. `Config` already
keeps keys it doesn't know and never rewrites a complete file, so an old file loads without
complaint; the settings screen just stops showing them. Nothing to migrate.

## The game test

`EspressoClientTest` loses `creepers`, `keepOnDeath` and the protection half of `safeHome`
(`isProtected` at 20 and 40 blocks); `safeHome` keeps "crouch-using the bed makes it home" and
`isAtHome`. The settings-file check that uses `keepHotbarOnDeath` and `creeperGriefing` as its
sample keys switches to `drowsiness` and `coffeeBarRequired`, and the slider-limits check drops
`safeHomeRadius` (the other two sliders stay). A new check: a worn charm (with Trinkets) comes back after death while the hotbar's pickaxe drops,
which proves the charm-only `KeepOnDeath` and proves the hotbar rule really left. Proof run: with
the charm stash emptied out, that check fails; code put back.

CI keeps both runs (with and without Trinkets).

## CHANGELOG, "0.4.0: the café mod"

Draft of the "Gone" section, for players:

> **Gone, to Cozy Core.** Creeper-proof and enderman-proof builds, no phantoms, the monster-free
> radius around your home bed, and keeping your hotbar on death now live in
> [Cozy Core](https://github.com/gabmaybelle/cozy-core), as game rules you can set per world.
> Install it alongside Espresso Flavoured Core (the Espresso Flavoured pack already does). Your
> home bed is still your home: it's where Drowsy clears and Cozy heals. Old settings for the
> removed rules are ignored and can be deleted from `config/espresso_flavoured.properties`.
> Worn café charms still stay with you when you die.

Worlds from 0.3.0 carry over: no blocks, items or saved data change. The `kept_items` attachment
is still read, so a player who died in 0.3.0 and updates before respawning still gets their things.

## Order of work (one commit each, test between)

1. Keep-on-death lands in Cozy Core first (`claude/keep-on-death`), so nothing is lost in between.
2. Remove the four rules and their settings; shrink `KeepOnDeath` to charms; drop `isProtected`.
3. Rewrite the README, field guide, settings sections and lang; CHANGELOG section.
4. Game test changes and the proof run; CI green with and without Trinkets.
5. Version 0.4.0 in `gradle.properties` and `fabric.mod.json`; the Modrinth listing draft follows.

## Questions for Gabby (answered 2026-10-15)

- [x] Agree the four removals above? **Yes, all four.**
- [x] Any of the "not decided" five to remove as well in 0.4.0? **No, all five stay.**
- [x] Keep the charm-only keep-on-death in Core? **Yes, worn charms stay with you.**

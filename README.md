# Espresso Flavoured Core

*A little café for your survival world: coffee you grow and brew, mugs, baristas and charms.*

Espresso Flavoured Core is a Fabric mod for Minecraft 26.3. It needs only Fabric API. For builds that stay built (no creeper holes, no phantoms, a lantern that keeps monsters away), add [Cozy Core](https://github.com/gabmaybelle/cozy-core) alongside it; the two are made to go together.

## What's in it

**Coffee, from bush to mug.** Coffee bushes grow wild in jungles and savannas. Roast the cherries into beans. Put an espresso machine on a coffee bar (a counter, in every wood), beans on top and mugs below, like a brewing stand. Coffee gives you Speed and Haste.

**Mugs.** Shape them from clay, fire them and dye them any colour. Your coffee stays in the mug you chose, and you get it back when you drink. Crouch to set one down on a table or shelf.

**Drinks.** Espresso, Latte (add milk), Mocha (add cocoa), Vanilla Latte (add vanilla syrup) and more, plus decaf versions of all of them. Pastries go with them.

**Baristas.** A villager who takes an espresso machine as their workstation becomes a barista, trading beans, mugs, drinks, bakes and café decor. Cafés also turn up in villages.

**Bean Coins.** Every Espresso advancement pays you a Bean Coin. Spend them with a master barista: eight buy the Golden Mug, which makes coffee last longer, and a coin or two buys a treat, a charm or rare beans for a Ristretto, the strongest coffee there is.

**Café charms.** Eight small charms, each with one gentle perk, like a Thermos that makes coffee last longer or Reading Socks that step up full blocks. Wear them in Trinkets Updated slots, or hold one in your off-hand without it. Charms you're wearing stay with you when you die.

**Canisters and jars.** Clay canisters and glass jars hold four stacks each, show what's inside, and work with hoppers.

**Rest.** Crouch and use a bed with an empty hand to make it your home. Go three days without sleeping there and you get Drowsy: no sprinting, slower mining. Coffee holds Drowsy off; a night at home clears it. Decaf makes you Cozy instead, which heals you slowly near home.

**Longer days.** Daylight lasts 20 minutes and night 10, and nights are a little tougher. You only heal naturally when you've eaten recently.

**Little extras.** Bundles hold four tools, shulker boxes can be crafted in the Overworld, the wandering trader sells building blocks and buys your roasted beans, and new players start in a gazebo at spawn with a bed, a few essentials and a short field guide.

Everything can be switched off or adjusted in game through Mod Menu, or in `config/espresso_flavoured.properties`. On a server, the server's settings decide. For more of a challenge, switch on Caffeine Crash: coffee then only buys you a day before you crash.

## Playing with other mods

Espresso Flavoured Core needs only Fabric API. It notices a few other mods when they're there:

- **Cozy Core:** Gabby's builder rules (builds that stay built, no phantoms, keep your hotbar on death, the Hearth Lantern). Nothing links the two in code; they're simply made to go together.
- **Trinkets Updated:** the café charms go in trinket slots (right-click to wear one) and stay with you on death. Without it, a charm works in your off-hand, so you choose one at a time.
- **Jade:** placed mugs show their drink, coffee bushes how ripe they are, and the espresso machine how its brew is going.
- **JEI:** a page for what the espresso machine brews.
- **Iris:** shaders keep time with the longer days (and with any other mod's or datapack's day length).
- **Graves mods** (Universal Graves and others that take over death drops): your worn charms are set aside first (and, with Cozy Core, your hotbar and armour); the grave gets the rest.
- **Towns and Towers:** its villages get cafés too, in the closest of Espresso's styles, and coffee turns up in its chests.
- **Lootr:** the starter kit chest in the spawn gazebo is shared out per player.
- **Spawn Gazebo** (Gabby's own mod) is included inside Espresso, so there's nothing extra to install. Its settings are behind the Gazebo Settings button in Espresso's settings.
- **Nightfall Clock:** a clock that reads the sun, so it stays right on the longer days.

A few things are data, so a pack can change them without code:

| Tag | What it does |
|---|---|
| `espresso_flavoured:cinnamon_bark` (blocks) | Logs that sometimes give cinnamon when an axe strips them (jungle and acacia). |
| `espresso_flavoured:ember_glove_smelts` (items) | What the Ember Glove smelts as you mine it. |
| `espresso_flavoured:block_merchant` (items) | What counts as a building block for the House Blend advancement. |
| `espresso_flavoured:coffee` (items) | Every coffee drink, for recipes and advancements. |

The day length and the village cafés are ordinary worldgen data too. Changes that touch the same vanilla code as other mods (mob spawning, bundles, food, sugar cane and so on) are written to step aside rather than fail: if another mod has changed that code first, the Espresso rule for it quietly does nothing and the game carries on.

## Building

You need JDK 25.

```
./gradlew build
```

The jar is written to `build/libs/`. The scripts in `tools/` regenerate the data pack, the art and the longer-day timelines.

`./gradlew runClientGameTest` starts the game and plays through the main rules: the settings file (typos, unknown settings, safe saving), the spawn gazebo and its starter chest, brewing in the espresso machine (and not off a coffee bar), the Ristretto's Speed II and Haste II, the home bed, Drowsy and how coffee and sleeping at home deal with it, tougher nights, the block merchant, the village café, every café charm's perk, sitting on stairs, growing and picking coffee bushes, setting mugs down, canisters, waking Well Rested, Bean Coins and the master barista's coin trades, worn charms staying with you on death while the rest drops, four tools to a bundle, and the settings screen. Add `-PtestTrinkets` to run it with Trinkets Updated installed, so the charms are worn in their slots rather than the off-hand. Screenshots land in `build/run/clientGameTest/screenshots`.

## Licences

Espresso Flavoured uses two licences:

- **Code** (everything under `src/main/java` and `tools/`): [MIT](LICENSE). Learn from it, fix it, fork it or build on it, as long as the copyright notice stays.
- **Artwork and text** (the logo, icons, textures, splash texts and field guide): [CC BY-NC-SA 4.0](LICENSE-ASSETS). Share and adapt them for non-commercial use, with credit, under the same licence.

Espresso Flavoured is made by Gabby with help from Claude, an AI assistant.

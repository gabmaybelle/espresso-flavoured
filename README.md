# Espresso Flavoured Core

*Survival that's kind to builders: a safe home, builds that stay built, and coffee you grow and brew.*

This is the mod at the heart of the [Espresso Flavoured modpack](https://github.com/gabmaybelle/espresso-flavoured-pack). It works on its own too.

Espresso Flavoured Core is a Fabric mod for Minecraft 26.3 that makes survival kinder to builders without making it toothless. Your home is a refuge and your builds are safe, while nights, hunger and the wider world keep their bite. It also adds coffee, grown and brewed by you, to help you through a long day of building.

## What it changes

**A safe home.** Crouch and use a bed with an empty hand to make it your home. Monsters won't spawn naturally within 32 blocks of it. Sleeping somewhere else doesn't move it.

**Builds stay built.** Creeper explosions no longer break blocks, endermen leave blocks alone, and phantoms never visit.

**Longer days, sharper nights.** Daylight lasts 20 minutes and night 10. Nights bring a few more monsters, and the ones that spawn in the dark are a little tougher. A clock in the top-right corner keeps the time, and at 6 PM, an hour before dark, "The sun's getting real low" appears above your hotbar.

**Looking after yourself.** Natural healing needs you to have eaten recently. Spend three days without sleeping in your own bed and you become Drowsy: too tired to sprint, and slower to mine and walk. A bed in the mine won't fix it; only a night at home will, so long trips end with the walk back. Coffee can buy you up to a day more, but when it wears off you crash, and only home will fix that. Go those three days without eating as well, and you'll get Hungry too.

**Coffee.** Coffee bushes grow wild in jungles and savannas. Roast the cherries, then set an espresso machine on a coffee bar (a stone-topped cabinet counter, in every wood) and brew Espresso in it: beans on top, mugs below, just like a brewing stand (hoppers work too). Mugs are shaped from clay, fired in a furnace, and dyed any colour; your coffee stays in the mug you chose, from Espresso to Latte (add milk) to Mocha (add cocoa), and you get the mug back when you drink. Crouch to set a mug down on a table or shelf, empty or full. Sweeten things up with Vanilla Syrup (honey and sugar) for the house special, a Vanilla Latte.

**The café.** Every Espresso advancement pays you a Bean Coin, and a master barista takes them: eight buy the Golden Mug (coffee lasts longer from it), and a coin or two buys a treat. Clay canisters, dyeable like mugs, hold four stacks of anything that stacks, show what is inside on a little label, and work with hoppers. Glass jars do the same, and you can see what is inside; fill one with honey bottles for a honey jar.

**Decaf.** Wash roasted beans in water for decaf beans, and any coffee you brew from them is decaf all the way to a vanilla latte. Decaf makes you Cozy: you heal slowly near your home bed, sleeping while Cozy wakes you Well Rested with two golden hearts, and a decaf after a caffeine crash softens it.

**Café charms.** Eight small charms, each with one gentle perk: a Lucky Bean, a Thermos that makes coffee last longer, a Pocket Hearth against the cold, Reading Socks that step up full blocks, a Café Parasol that softens falls, a Knit Beanie that keeps Cozy healing you away from home, an Ember Glove that smelts what you mine, and a Copper Tamper that hits monsters harder. A master barista sells a couple for Bean Coins, and they turn up in café and village chests. With Trinkets Updated they go in trinket slots; without it they work from your inventory.

**The barista.** Villagers can take an espresso machine as their workstation and become baristas. They trade in beans, mugs, drinks, bakes and café decor, and a master barista sells the Barista's Reserve for Bean Coins: rare beans that brew a Ristretto, the only coffee strong enough for Speed II and Haste II. Coffee gives Speed and Haste, and holds off the drowsiness of staying up too late.

**Death.** You keep your hotbar, armour, off-hand and the charms you're wearing. Everything else stays where you fell, so there's still a trip back for your things (into a grave, if you also use a graves mod).

**Room for more.** Shulker boxes can be crafted in the Overworld (a chest, iron, ender pearls and amethyst), bundles also take wool or rabbit hide, and a bundle holds four tools or pieces of armour.

**The block merchant.** The wandering trader also sells building blocks and decorations from far-off biomes, like terracotta, calcite, cherry saplings and spore blossoms. It always carries coffee cherries to plant, and pays a fair price for your roasted beans: fair trade, of course.

**A warm welcome.** New players get a bed, a bundle of essentials and a short field guide, and an advancement tab walks through the rest. The title screen says Espresso Flavoured under the Minecraft logo, with its own splash texts.

Every rule can be switched off or adjusted in `config/espresso_flavoured.properties`.

## Building

You need JDK 25.

```
./gradlew build
```

The jar is written to `build/libs/`. The scripts in `tools/` regenerate the data pack, the art and the longer-day timelines.

## Licences

Espresso Flavoured uses two licences:

- **Code** (everything under `src/main/java` and `tools/`): [MIT](LICENSE). Learn from it, fix it, fork it or build on it, as long as the copyright notice stays.
- **Artwork and text** (the logo, icons, textures, splash texts and field guide): [CC BY-NC-SA 4.0](LICENSE-ASSETS). Share and adapt them for non-commercial use, with credit, under the same licence.

Espresso Flavoured is made by Gabby with help from Claude, an AI assistant.

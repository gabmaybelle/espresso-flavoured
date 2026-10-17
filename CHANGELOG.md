# Changelog

## 0.5.0: Now on NeoForge too

- **Espresso Flavoured Core comes in two files now, one for Fabric and one for NeoForge**, with the same café in both: coffee, mugs, baristas, Bean Coins, charms, Drowsy and the home bed. Spawn Gazebo 1.2.0 is built into both.
- **Fabric worlds carry over untouched.** Nothing about blocks, items or saved data changes, and the settings file is the same.
- **On NeoForge**, charms are held in the off-hand (Trinkets Updated only exists for Fabric), and the title-screen look and splash texts are always on (they can't be switched off in the Resource Packs screen).
- Ember Glove smelting and the Drowsy "sleep at home" rule no longer depend on Fabric API, so they work the same on both loaders.

## 0.4.0: The Café Mod

Espresso Flavoured Core is the café mod now. The builder rules it shared with [Cozy Core](https://github.com/gabmaybelle/cozy-core) live there alone, as game rules you can set per world, so a player with both mods isn't protected twice in two different ways.

### Gone, to Cozy Core
- **Creeper-proof and enderman-proof builds, no phantoms**, and the **monster-free radius around your home bed**. Cozy Core's Hearth Lantern does the quiet zone now, as a block you place where you want it.
- **Keeping your hotbar, armour and off-hand on death**: Cozy Core's `keep_hotbar_on_death` rule. Worn café charms still stay with you.
- Install Cozy Core alongside Espresso Flavoured Core for these; the Espresso Flavoured pack already does.

### Still here
- **Your home bed** is still your home: crouch and use a bed with an empty hand. It's where Drowsy clears and Cozy heals.
- Longer days, tougher nights, healing only when fed, roomier bundles, the block merchant, and all of the café.
- Old settings for the removed rules (`creeperGriefing`, `endermanGriefing`, `phantoms`, `safeHomeRadius`, `keepHotbarOnDeath`) are ignored and can be deleted from `config/espresso_flavoured.properties`; the settings screen no longer shows them.
- Worlds from 0.3.0 carry over: no blocks, items or saved data change. A player who died in 0.3.0 and updates before respawning still gets their things back.

## 0.3.0: Bean Coins and Café Charms

### New mod ID
- The mod ID is now `espresso_flavoured` (it was `espresso`, which another mod could easily share). Every block, item, recipe and advancement moves with it, and the settings file is now `config/espresso_flavoured.properties`.
- Worlds from 0.2.0 or earlier don't carry over: their Espresso blocks and items are dropped when the world opens, and homes and settings start fresh. Start a new world for 0.3.0.

### Gone
- **The clock and dusk warning.** Nightfall Clock, which is in the Espresso Flavoured pack, does that job: it reads the sun, so it stays right on the longer days, and it no longer hides itself when Espresso is installed.
- **Lusher rivers, rest spots and water lilies.** Espresso keeps to the café and the survival rules. Worlds from 0.2.0 don't carry over anyway (see above), so nothing already built is lost.

### Bean Coins
- **Bean Coins** replace the loyalty card. Every Espresso advancement pays you one, straight into your inventory. A master barista takes them: eight for the **Golden Mug**, one for three Barista's Reserve, two for sixteen bottles o' enchanting. A master always offers these three.
- Advancements you earned before this update pay out when you next play, and the old loyalty card is gone (its stamps are paid for in coins). A world from 0.2.0 shows Minecraft's "Missing content detected" screen once when it opens; choose "Create Backup and Load" and carry on, since the card is the only thing it drops.
- The wandering trader no longer trades the Golden Mug.

### Café charms
- Eight small charms, each with one gentle perk: the **Lucky Bean** (+1 Luck), the **Thermos** (coffee and cocoa effects last a quarter longer), the **Pocket Hearth** (powder snow can't freeze you, and you can walk on it), **Reading Socks** (step up a full block without jumping), the **Café Parasol** (fall three more blocks before it hurts), the **Knit Beanie** (Cozy heals you anywhere, not just near home), the **Ember Glove** (ores, sand, cobblestone and logs drop smelted as you mine them; crouch to mine normally, and there's no furnace XP) and the **Copper Tamper** (15% more damage to hostile mobs, and only them).
- With **Trinkets Updated** installed (it's in the Espresso Flavoured pack), they're worn in trinket slots: right-click one to put it on. The Pocket Hearth and Copper Tamper share a new two-deep Pocket slot. The Lucky Bean, Thermos, Parasol and Beanie show on your character. Without Trinkets, a charm works in your off-hand, so you pick one at a time, and All Dressed Up asks you to carry all eight instead.
- Each master barista sells one cosy charm for 2 Bean Coins and one of the Glove or Tamper for 3, picked when they reach master, so different cafés have different charms. Café chests hold one about one time in five, and village houses now and then.
- A new advancement, **All Dressed Up**: wear all eight at once. The field guide has a page on them.

### Baristas
- Baristas bake and decorate now. They buy wheat, eggs and sugar cane, and sell bread, cookies, pumpkin pie, cake, lanterns, candles, flower pots and paintings. Each barista still offers two trades per level, picked from a bigger mix, so no two cafés are quite the same.
- A master barista sells a music disc for the café's corner: Creator (Music Box).

### Advancements
- Six new ones, so there are now 20 Bean Coins to earn: **All Dressed Up** (see above), **Take a Seat** (sit on a bench or chair), **Local Favourite** (find a village café and open its supply chest), **Counter Culture** (place a coffee bar), **Stocked Up** (put something in a canister or jar) and **Your Usual** (trade with a barista).
- **House Blend** now needs a building block or decoration from the wandering trader, not just any trade.
- **Fair Trade** now needs you to actually sell roasted beans; selling the trader a water bottle no longer counts.
- **Pocket Shot** was free (the starter kit gives a bundle). Now it asks you to tuck a pickaxe and an axe into the same bundle, which only Espresso's roomier bundles allow.

### Trades
- The Barista's Reserve and bottles o' enchanting now come only from Bean Coins; the emerald versions are gone, since the coin trades were always the better deal.
- The wandering trader offers two building blocks instead of three, so its screen is less crowded.

### The café and the world
- **Coffee bars have a cupboard:** right-click one for two rows of storage, like a small chest. A hopper under a bar no longer collects drinks from the espresso machine on top; take them out by hand.
- **A gazebo at spawn:** new worlds start in a little round stone gazebo, built from whatever wood grows nearby, with the starter kit waiting in its chest. It's [Spawn Gazebo](https://github.com/gabmaybelle/spawn-gazebo), Gabby's own mod, included inside Espresso so there's nothing extra to install: what it builds, what it protects and its settings are described there. Its settings are one button away from Espresso's (or in `config/spawn_gazebo.properties`).
- Village cafés keep a little potted cactus on their chest.
- You can sit on a bench with a tool or anything else that isn't a block in your hand, not only with an empty hand.
- **The espresso machine looks busy while it brews:** wisps of steam rise under the brew head, it hisses softly now and then, and it gives off a little light, so a café at night has a glow behind the counter.

### Smaller things
- The title screen keeps the Minecraft logo, with "Espresso Flavoured" underneath where "Java Edition" usually sits.
- Coffee turns up in Towns and Towers' own village chests too (jungle, swamp, badlands, mushroom and Mediterranean houses).
- The field guide has a Bean Coins page, and says your hotbar and armour stay with you when you fall.
- With shaders (Iris), the sky now keeps time with any day length, not just Espresso's: another mod's or datapack's timeline is scaled to a normal day for the shader too.
- With **JEI**: a page for what the espresso machine brews (beans, decaf beans, the Barista's Reserve and cocoa mix, with the grounds they leave).
- The espresso machine's recipe now also shows up in the recipe book when you pick up a lightning rod.
- Canisters and glass jars have their own sounds: a soft rustle as things go in and out, with a ceramic tink (or a glass clink) of the lid.

### Looking after yourself
- Drowsy is simpler: three days away from home makes you Drowsy, and coffee holds it off for as long as it lasts. The caffeine crash, and Hungry from not eating, now only happen with the **Caffeine Crash** setting, which is off by default (and switched off once in existing settings files).

### Fixes
- A full hunger bar with no saturation left no longer locks you out of both eating and healing (the game refuses food at a full bar, and the "regen needs saturation" rule refused to heal). A full bar now counts as fed: you heal slowly, that costs hunger as in vanilla, and a notch soon frees up to eat.
- Safer settings: the settings file can't be left half-written or empty if the game crashes while saving it; a typo in a setting ("ture") keeps its default instead of quietly switching it off, with a note in the log saying which setting; and settings the mod doesn't know about, or notes you've added, are no longer wiped each time the game starts.
- A number typed into the settings file beyond its slider's range (sugar cane height, bundle room, safe home radius) is brought back to the range when the game loads, instead of going past what the screen can show.
- Breaking a ripe coffee bush no longer drops its cherries twice.
- Decaf drinks keep their extras: a decaf pumpkin spice latte still makes you Warm, decaf iced coffee still gives Fire Resistance, and so on. Only the caffeine is swapped for Cozy.
- The Golden Mug now stretches Cozy too, and hot chocolate calms a caffeine crash like decaf does.
- Breaking a canister or glass jar in creative mode spills what was inside instead of deleting it.
- The espresso machine waits until its knock box has room for all of a brew's grounds instead of losing some, and cocoa (which leaves no grounds) brews even when the box is full.
- Turning Longer Days on or off no longer changes the Drowsy timer until the restart that changes the days.
- Without Iris, the log no longer shows an "Error loading class" line for the shader time fix: the mod only applies it when Iris is there.
- Drowsy and Hungry count days by the world's actual day length, read from the overworld's day timeline, so another mod's or datapack's longer days count too.
- Cinnamon no longer replaces vanilla's axe data to drop from jungle and acacia logs; it watches the stripping instead. Other mods' logs now strip as they should alongside Espresso, and a new block tag, `espresso_flavoured:cinnamon_bark`, says which logs give cinnamon.
- On someone else's server, the settings screen greys out the settings that are the server's to decide and says so.
- Mod Menu's Website button now works (it opens the GitHub page).
- With Trinkets, the charms you're wearing now stay with you on death, like your hotbar and armour (Curse of Vanishing still vanishes them). They go back into their slots when you respawn.
- Bean Coin payouts check only Espresso's own advancements each second, picked out once when the server starts or reloads, instead of scanning every advancement for every player.

## 0.2.0: The Café

### Coffee
- **Espresso machine.** The only way to brew espresso now. It has a brewing-stand style menu: beans on top, up to three mugs below, and a ten-second brew. Hoppers work, and comparators read it.
- **Coffee bars**, in every wood (including poplar). The machine brews only when it sits on one, and a hopper under the bar collects the finished drinks.
- **Mugs.** Shape them from clay, fire them, and dye them any colour. Coffee keeps its mug from espresso to latte to mocha, and you get the mug back when you drink. Crouch to set a mug down, empty or full: it sits on a saucer, and every drink has its own look.
- **Vanilla latte**, the house special: honey and sugar make vanilla syrup.
- **Decaf.** Wash roasted beans in water. Decaf makes you **Cozy**: you heal slowly near home, wake up **Well Rested** with two golden hearts, and it softens a caffeine crash.
- **Ristretto**, brewed from the Barista's Reserve: the only Speed II and Haste II.
- **More drinks:** a Pumpkin Spice Latte (pumpkin spice is pumpkin, sugar and cinnamon, which you get by stripping jungle and acacia logs) that makes you **Warm**, so powder snow can't freeze you; Iced Coffee with a little Fire Resistance; Night Shift, a glow berry cold brew with Night Vision; and Hot Chocolate, brewed from cocoa mix, with no coffee at all, just Cozy.
- **Café favourites:** a Honey Latte (a moment of Regeneration), a Caramel Macchiato (caramel is smelted sugar; a minute of golden hearts) and Cold Brew (espresso over packed ice: no extras, but it lasts longest).
- **Pastries:** croissants, biscotti and berry muffins. Eat one with a coffee (or a cocoa) for a **pairing**: a little extra saturation and a puff of hearts. Baristas sell them, and café chests have them.
- **Coffee grounds.** Each shot leaves its used grounds in the machine's knock box (a hopper below takes them out too). Like the gardener's trick, they help crops, stems and bushes grow, and they compost.

### The café
- **Barista villagers** use the espresso machine as their workstation. They trade beans, mugs and drinks, and a master sells name tags, bottles o' enchanting, golden apples and the Barista's Reserve.
- **Loyalty card.** It comes with the Barista advancement and collects a stamp for each café milestone, wherever it is: in a chest, an ender chest or a bundle. Swap a full card with the wandering trader for the **Golden Mug** (coffee lasts longer from it).
- **Canisters and glass jars.** They hold four stacks of anything that stacks and show what's inside. Dye the canisters to match your mugs. Fill a jar with honey bottles for a honey jar.
- **Village cafés.** A cottage café can turn up in villages, built in the village's own woods and stone: a covered porch with little tables and lanterns, flower boxes under the windows, a chimney with a wisp of smoke, and inside, a fire in the hearth, a cake and an espresso machine on the counter for a barista to claim and a chest of café supplies. With Towns and Towers, its villages get them too, and its Japanese villages get a teahouse café instead.
- **Coffee in chests.** Village houses, jungle temples and shipwrecks sometimes hold coffee cherries or roasted beans, and a jungle temple might even have the Barista's Reserve.
- **Taller coffee bushes.** A planted cherry starts as a short seedling and grows into a two-block shrub, like a lilac, whose cherries turn yellow and then red. Pick from either half; wild bushes are already tall. Bushes planted before this update grow their top half on their own.
- **Rest spots.** Now and then in meadows, plains and forests: a bench with a lamp post, under a shady tree or looking over a little pond.
- **Lusher rivers.** Thicker seagrass, with kelp in the deeper stretches; tall grass, ferns, bushes and drifts of cornflowers, bluets and daisies on the banks; more firefly bushes and lily pads; and at the waterline a little moss, clay, mud on low earthy banks, and the odd mossy boulder. It all keeps to the water's edge, so clearing land is no harder. There's a "Lusher Rivers" setting; it's off by default alongside Terralith, which dresses the world itself.
- **Fair trade.** The wandering trader always sells coffee cherries to plant and buys your roasted beans at a fair price. It also sells a few rare decorations.

### Looking after yourself
- Drowsy comes from two days away from **home**: only a night in your own bed clears it. Coffee buys up to a day, and then you crash. Drowsy also stops you sprinting.
- Going those two days without eating makes you Hungry.
- Drowsy has no screen effects any more.

### Smaller things
- **Sit down** on any bench or chair (upright stairs) by using it with an empty hand; crouch to stand.
- **A clock** in the top-right corner ("Day 4 · 2:35 PM"), right for the longer days; it turns gold in the hour before night (there's a setting).
- With shaders (Iris), the sky now keeps time with the longer days instead of running ahead.
- With **Jade**: placed mugs show their drink, coffee bushes how ripe they are, and the espresso machine how its brew is going.
- New recipes appear in the recipe book as you pick up their ingredients.
- The field guide has a Coffee Corner page.
- Sugar cane grows 5 blocks tall, so a few plants keep you in sugar (there's a setting).
- New settings: "Machine Needs a Coffee Bar", "Caffeine Crash", "Sugar Cane Height", "Lusher Rivers" and "Clock".
- On a server, everyone's bundles follow the server's "Tools per Bundle" setting.
- New advancements: Barista, Mug Shot, House Special, Sweet Dreams, Perfect Pairing, and On the House.

## 0.1.0

- The first play-test build: a safe home radius, creepers and endermen that leave builds alone, no phantoms, longer days and tougher nights, keep your hotbar and armour on death, easier bundles, an Overworld shulker box recipe, the block merchant, a starter kit and field guide, and coffee from wild coffee bushes.

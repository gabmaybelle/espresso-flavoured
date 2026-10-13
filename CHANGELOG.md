# Changelog

## 0.3.0: Bean Coins, Café Charms and Lush Shores

### New mod ID
- The mod ID is now `espresso_flavoured` (it was `espresso`, which another mod could easily share). Every block, item, recipe and advancement moves with it, and the settings file is now `config/espresso_flavoured.properties`.
- Worlds from 0.2.0 or earlier don't carry over: their Espresso blocks and items are dropped when the world opens, and homes and settings start fresh. Start a new world for 0.3.0.

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
- Seven new ones, so there are now 21 Bean Coins to earn: **All Dressed Up** (see above), **Take a Seat** (sit on a bench or chair), **Scenic Route** (stop at a rest spot in the wild), **Local Favourite** (find a village café and open its supply chest), **Counter Culture** (place a coffee bar), **Stocked Up** (put something in a canister or jar) and **Your Usual** (trade with a barista).
- **House Blend** now needs a building block or decoration from the wandering trader, not just any trade.
- **Fair Trade** now needs you to actually sell roasted beans; selling the trader a water bottle no longer counts.
- **Pocket Shot** was free (the starter kit gives a bundle). Now it asks you to tuck a pickaxe and an axe into the same bundle, which only Espresso's roomier bundles allow.

### Trades
- The Barista's Reserve and bottles o' enchanting now come only from Bean Coins; the emerald versions are gone, since the coin trades were always the better deal.
- The wandering trader offers two building blocks instead of three, so its screen is less crowded.

### The café and the world
- **Coffee bars have a cupboard:** right-click one for two rows of storage, like a small chest. A hopper under a bar no longer collects drinks from the espresso machine on top; take them out by hand.
- **Lusher water, like a lush cave come up to the light:** moss patches spread along the banks, sprouting moss carpet, grass and azaleas (some flowering); little clay pools of big and small dripleaf sit at the water's edge; there's more moss at the waterline, and the odd small cluster of lily pads (not so many that they get in the way of boats). Where there's clay in the water, the bank turns to mud and fades out through patchy mud, packed mud and rooted dirt to coarse dirt among the grass.
- **Lusher Rivers is on with Terralith too** (it used to switch itself off). Like all of it, this only changes land you haven't explored yet.
- **Water lilies:** a lily pad with a cactus flower blooming on it, right on the water. Bone meal a lily pad to make one bloom, and look for the odd one among the lily pads by rivers and ponds in new land.
- **A gazebo at spawn:** new worlds start in a little round stone gazebo, built from whatever wood grows nearby (sandstone in deserts and badlands): eight stripped-log pillars of that wood on a round plinth, a ring of arches, a weathered copper dome with azaleas creeping over it and a lightning rod on top, and lanterns hanging under the eaves and flanking the steps. A spore blossom hangs over a glowstone floor, flower pots hold saplings and a cactus for your first garden, four decorated pots hide mangrove propagules, bamboo and seeds, and the starter kit waits in the chest. It's protected: outside creative it can't be broken or built on, and explosions, fire and pistons leave it alone. Switch off "Protect the Gazebo" in the settings to remodel it. Building it also moves the world spawn to the middle of the gazebo and sets the `spawn_radius` game rule to 0, so everyone starts inside it.
- **Spawn Gazebo:** with the standalone Spawn Gazebo mod installed, Espresso leaves the gazebo and its protection to it (its two gazebo settings are greyed out) and hands the starter kit straight to each player.
- Rest spots no longer have a dirt path, and they keep to dry land (one could end up standing in a lake). Village cafés keep a little potted cactus on their chest.
- **The espresso machine looks busy while it brews:** wisps of steam rise under the brew head, it hisses softly now and then, and it gives off a little light, so a café at night has a glow behind the counter.

### Smaller things
- The title screen keeps the Minecraft logo, with "Espresso Flavoured" underneath where "Java Edition" usually sits.
- Coffee turns up in Towns and Towers' own village chests too (jungle, swamp, badlands, mushroom and Mediterranean houses).
- The field guide has a Bean Coins page, and says your hotbar and armour stay with you when you fall.
- Lusher Rivers is switched on once for everyone on this update (older settings files had it off alongside Terralith by default, not by choice); turn it off again any time.
- With shaders (Iris), the sky now keeps time with any day length, not just Espresso's: another mod's or datapack's timeline is scaled to a normal day for the shader too.
- With **JEI**: a page for what the espresso machine brews (beans, decaf beans, the Barista's Reserve and cocoa mix, with the grounds they leave), and a note on where water lilies come from.
- The espresso machine's recipe now also shows up in the recipe book when you pick up a lightning rod.
- Canisters and glass jars have their own sounds: a soft rustle as things go in and out, with a ceramic tink (or a glass clink) of the lid.

### Looking after yourself
- Drowsy is simpler: three days away from home makes you Drowsy, and coffee holds it off for as long as it lasts. The caffeine crash, and Hungry from not eating, now only happen with the **Caffeine Crash** setting, which is off by default (and switched off once in existing settings files).

### Fixes
- Building the spawn gazebo no longer litters the ground: the parts of felled trees' canopies that hung outside its footprint used to rot away afterwards, scattering sticks and saplings, and two-block flowers dropped as they were cleared. Both go cleanly now.
- A full hunger bar with no saturation left no longer locks you out of both eating and healing (the game refuses food at a full bar, and the "regen needs saturation" rule refused to heal). A full bar now counts as fed: you heal slowly, that costs hunger as in vanilla, and a notch soon frees up to eat.
- Breaking a ripe coffee bush no longer drops its cherries twice.
- Decaf drinks keep their extras: a decaf pumpkin spice latte still makes you Warm, decaf iced coffee still gives Fire Resistance, and so on. Only the caffeine is swapped for Cozy.
- The Golden Mug now stretches Cozy too, and hot chocolate calms a caffeine crash like decaf does.
- Breaking a canister or glass jar in creative mode spills what was inside instead of deleting it.
- The espresso machine waits until its knock box has room for all of a brew's grounds instead of losing some, and cocoa (which leaves no grounds) brews even when the box is full.
- Turning Longer Days on or off no longer changes the Drowsy timer until the restart that changes the days.
- Without Iris, the log no longer shows an "Error loading class" line for the shader time fix: the mod only applies it when Iris is there.
- Placing a block in the protected spawn gazebo no longer shows the block for a moment before it vanishes: each player's game is told where the gazebo is and refuses the placement itself.
- A "Clock Position" setting: the clock can sit in any corner (bottom corners step up on narrow screens and hide while chat is open).
- Drowsy and Hungry count days by the world's actual day length, read from the overworld's day timeline, so another mod's or datapack's longer days count too.
- Cinnamon no longer replaces vanilla's axe data to drop from jungle and acacia logs; it watches the stripping instead. Other mods' logs now strip as they should alongside Espresso, and a new block tag, `espresso_flavoured:cinnamon_bark`, says which logs give cinnamon.
- On someone else's server, the settings screen greys out the settings that are the server's to decide and says so; only the clock and the dusk warning (which are yours) can be changed there.
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

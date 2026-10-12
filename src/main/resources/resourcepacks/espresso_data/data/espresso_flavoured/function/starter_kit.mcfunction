# When the spawn gazebo was built (SpawnGazebo.java sets storage espresso_flavoured:spawn {chest:1b}), the kit is
# in its chest. With Lootr every player gets their own copy from it ({lootr:1b}); without it the chest
# is the first player's, and everyone after them is handed the kit as before.
execute unless data storage espresso_flavoured:spawn {chest:1b} run function espresso_flavoured:starter_kit/give
execute if data storage espresso_flavoured:spawn {chest:1b} unless data storage espresso_flavoured:spawn {lootr:1b} if data storage espresso_flavoured:spawn {claimed:1b} run function espresso_flavoured:starter_kit/give
execute if data storage espresso_flavoured:spawn {chest:1b} run data merge storage espresso_flavoured:spawn {claimed:1b}

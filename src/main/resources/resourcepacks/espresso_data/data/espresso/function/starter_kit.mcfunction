# When the spawn gazebo was built (SpawnGazebo.java sets storage espresso:spawn {chest:1b}), the kit is
# in its chest. With Lootr every player gets their own copy from it ({lootr:1b}); without it the chest
# is the first player's, and everyone after them is handed the kit as before.
execute unless data storage espresso:spawn {chest:1b} run function espresso:starter_kit/give
execute if data storage espresso:spawn {chest:1b} unless data storage espresso:spawn {lootr:1b} if data storage espresso:spawn {claimed:1b} run function espresso:starter_kit/give
execute if data storage espresso:spawn {chest:1b} run data merge storage espresso:spawn {claimed:1b}

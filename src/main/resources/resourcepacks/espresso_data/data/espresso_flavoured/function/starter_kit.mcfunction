# The gazebo at spawn is Spawn Gazebo's (bundled with Espresso). When it built the gazebo with its chest it
# sets storage spawn_gazebo:spawn {chest:1b}; the chest holds this kit (fabric.mod.json names it as the starter
# kit) and Spawn Gazebo hands later players a roll of it. Without the chest, everyone is handed the kit here.
execute unless data storage spawn_gazebo:spawn {chest:1b} run function espresso_flavoured:starter_kit/give

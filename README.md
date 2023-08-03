# Creeper Healing
This customizable mod allows the world to automatically heal Creeper explosions. It has support for configuring a custom block-replace list, for teling the mod what blocks to use to replace a previously broken one, allowing for balancing and preventing potential abuse of this mod.

## Configuration
When the server is started, the mod will look for an existing `creeper-healing.json` file for the configs. If it exists, it will read the values from there. If not, it will create a new config file in `/config/creeper-healing.json`. You can then edit this file to configure the mod, and restart the server to apply the changes.

- `"explosion_heal_delay"`: This settinga allows you to change the delay in seconds between each creeper explosion and its corresponding healing process. This is 3 by default.
- `"block_placement_delay"`: This setting allows you to change the delya in seconds between each block placement during the creeper explosion healing process. This is 1 by default.
- `"replace_list"`: This field allows you to add your own replace settings for choosing what block to use to heal another block. By default, there is one entry for using a Stone block to heal a Diamond block. However, you can add your own adding new entries here. Inside the array (bounded by the `{}`), insert new entries as follows
```
"replace_list" : {

"minecraft:gold_block" : "minecraft:stone",
"minecraft:stone" : "minecraft:cobblestone",
...
"minecraft:stone_bricks" : "minecraft:cracked_stone_bricks"

}
```
Specify the namespace (in this case `minecraft:`), then the name of the block. The blocks on the right are the blocks that will be used to heal the blocks on the left.

## Building

Clone this repository on your PC, then open your command line prompt on the main directory of the mod, and run the command: `gradlew build`. Once the build is successful, you can find the mod under `/creeper-healing/build/libs`. Use the .jar file without the `"sources"`.

## Credits

Thanks to @_jacg on the Fabric Discord server for helping me out with setting up the custom config file, and thanks to @sulpherstaer for giving me the idea for this mod. This is my first Minecraft server-side mod, so things are probably not going to be best quality. I'm looking to improve on this, so please report any issues you might have :).

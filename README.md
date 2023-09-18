# Creeper Healing

This server and client side, customizable mod allows the world to automatically heal Creeper explosions. It has support for configuring a custom block-replace list, for telling the mod what blocks to use to replace a previously broken one, allowing for balancing and preventing potential abuse of this mod.

## Configuration
When the server or game is started, the mod will look for an existing `creeper-healing.json` file for the configs. If it exists, it will read the values from there. If not, it will create a new config file in `/config/creeper-healing.json`. You can then edit this file to configure the mod, and restart the server or game to apply the changes, or use the `/creeper-healing reload_config` in-game command.

- `"daytime_healing_mode"`: Whether or not daytime healing mode should be enabled. Explosions will wait until the next sunrise to start healing, and they will finish healing at nighttime. This is `false` by default. Note that this only applies for explosions that ocurred while this setting was enabled.
- `"explosion_heal_delay"`: This setting allows you to change the delay in seconds between each creeper explosion and its corresponding healing process. This is 3 by default.
- `"block_placement_delay"`: This setting allows you to change the delay in seconds between each block placement during the creeper explosion healing process. This is 1 by default.
- `heal_on_flowing_water`: Change whether or not the mod should heal blocks where there is currently flowing water. Change between `true` and `false`. This setting is `true` by default.
- `heal_on_flowing_lava`: Change whether or not the mod should heal blocks where there is currently flowing lava. Change between `true` and `false`. This setting is `true` by default.
- `block_placement_sound_effect`: Change whether or not a block heal should play a sound effect. Change between `true` and `false`. This setting is `true` by default.
- `"drop_items_on_creeper_explosions"`: Change whether or not creeper explosions should drop items. This is `true` by default.
- `"requires_light"`: Change whether or not explosions need light to be able to heal. This is `false` by default. Note that this requirement is only tested once at the beginning of the healing process. 
- `"replace_list"`: This field allows you to add your own replace settings for choosing what block to use to heal another block. By default, there is one entry for using a Stone block to heal a Diamond block. However, you can add your own entries here. Inside the array (bounded by the `{}`), insert new entries as follows:
```
"replace_list" : {

"minecraft:gold_block" : "minecraft:stone",
"minecraft:stone" : "minecraft:cobblestone",
...
"minecraft:stone_bricks" : "minecraft:cracked_stone_bricks"

}
```
Specify the namespace (in this case `minecraft:`), then the name of the block. The blocks on the right are the blocks that will be used to heal the blocks on the left. 

**Note**: Attempting to set a delay to 0, a negative value, or a very small decimal value, will result in a value of 1 being used for that delay instead.

</details>

<details>

<summary>Commands</summary>

You can also edit the mod's settings (except the replace list) via in-game commands:

 - `/creeper-healing settings explosion_heal_delay [decimal argument]`: Change the delay in seconds between each creeper explosion and its corresponding healing process. The change will only apply for explosions that occur after this command was executed. Use this command without passing a value to see the current value in the config.
 - `/creeper-healing settings block_placement_delay [decimal argument]`: Change the delay in seconds between each block placement during the creeper explosion healing process. The change will only apply for explosions that occur after this command was executed. Use this command without passing a value to see the current value in the config.
 - `/creeper-healing settings heal_on_flowing_water [true or false]`: Change whether or not the mod should heal blocks where there is currently flowing water. Use this command without passing a value to see the current value in the config.
 - `/creeper-healing settings heal_on_flowing_lava [true or false]`: Change whether or not the mod should heal blocks where there is currently flowing lava. Use this command without passing a value to see the current value in the config.
- `/creeper-healing settings block_placement_sound_effect [true or false]`: Change whether or not a block heal should play a sound effect. Use this command without passing a value to see the current value in the config.
- `/creeper-healing settings drop_items_on_creeper_explosions [true or false]`: Change whether or not creeper explosions should drop items. Use this command without passing a vlauea to see the current value in the config.
- `/creeper-healing settings requires_light [true or false]`: Change whether or not explosions need light to be able to heal. Use this command without passing a value to see the curernt value in the config.
- `/creeper-healing settings reload`: Allows you to change the values of the config file and apply them to the game or server without having to restart. Note that the reloaded changes will only apply for explosions that occur after the command was executed, except for `heal_on_flowing_water`, `heal_on_flowing_lava`, and `block_placement_sound_effect`.

- `/creeper-healing mode daytime_healing_mode [true or false]`: Whether or not daytime healing mode should be enabled. Explosions will wait until the next sunrise to start healing, and they will finish healing at nighttime. Use this command without passing a value to see the current value in the config.

**Note**: All of these commands require operator permissions.

</details>

## Building

Clone this repository on your PC, then open your command line prompt on the main directory of the mod, and run the command: `gradlew build`. Once the build is successful, you can find the mod under `/creeper-healing/build/libs`. Use the .jar file without the `"sources"`.

## Credits

- Thanks to @sulpherstaer for giving me the idea and inspiration for making this mod.
- Thansk to @_jacg on the Fabric Discord server helping me out with setting up the custom config file.
- Thanks to @dale8699 for helping me improve and give me ideas for the mod.

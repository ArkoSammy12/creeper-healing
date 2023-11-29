# Creeper Healing
This server and client side, customizable mod allows the world to automatically heal Creeper explosions. It has support for configuring a custom block-replace list, for telling the mod what blocks to use to replace a previously broken one, allowing for balancing and preventing potential abuse of this mod.

## Features

### Explosion healing modes

Special modes that customize the way explosions are healed even further:

  - **Daytime Healing Mode**: Makes explosions wait until sunrise to begin healing. When they do, they will need a source of light to be able to heal.
 - **Difficulty-based Healing Mode**: Speeds up or slows down the healing of explosions depending on the difficulty of the world or server.
 - **Blast-resistance based Healing Mode**: Blocks with a higher blast resistance will take longer to heal. Their delays will also receive a randomized offset, causing blocks to heal in bursts.

### Different explosion sources

This mod also supports healing explosions of different sources, such as Ghasts and Withers. By default, the mod will only heal Creeper explosions, but you can toggle each explosion source individually.

### Toggle whether explosions from different sources should drop items

 You can individually configure whether explosions coming from different sources should drop their items. By default, all of the explosion source types are allowed to drop their items, but you can configure these settings individually via the configu file or via commands.

### Configurable delays

Configure the amount of time it takes for an explosion to start healing, and the amount of time between each block placement.

- **Warning**: Both delays have a minimum value of 0.05 seconds. Attempting to force a lower value by setting it manually in the config will make the mod use the default values instead.

### Whitelist

You can  configure an optional whitelist that allows you to specify the blocks that are allowed to heal in an explosion. To do this, you can open the mod's configuration file, and find the following section:

```toml
#Use an optional whitelist to customize which blocks are allowed to heal. To add an entry, specify the block's namespace
#along with its identifier, separated by a colon, and add it in-between the square brackets below. Separate each entry with a comma.
#Example entries:
#whitelist_entries = ["minecraft:grass",  "minecraft:stone", "minecraft:sand"]
[whitelist]
	whitelist_entries = ["minecraft:placeholder"]
```

To add entries to the whitelist, you can add a string containing the block's namespace and identifier separated by a colon to the array, like shown in the example above. Separate each entry with a comma.
You can also find a preference setting named "enable_whitelist" to enable or disable the usage of the whitelist. 


### Replace map

In the mod's config file, you can customize a "replace map". This is used if you would like a certain block to be healed with another one, instead of using the same block. If a block is healed with another one, the properties of the original block will be carried over to the new block, preserving things like the block's orientation. 

To add entries to the replace map, you can open the configuration file (located in your config folder and named `creeper-healing.toml`), scroll down and add entries below the `[replace_map]` section, using the following format, and separating each entry by skipping a line:

```toml
"minecraft:name_of_old_block" = "minecraft:name_of_new_block"
```

By default, the mod includes the following entry in the replace map:

```toml
"minecraft:diamond_block" = "minecraft:stone"
```

- **Warning**: Do not set the same block to be replaced with multiple blocks, as this will cause a crash upon game startup. That is, do not use the same key twice or more in the replace map.

### Extra settings

You can also toggle different preference settings to further customize the behavior of the healing of explosions:
 
 - Whether blocks should be healed where there is currently flowing water or lava.
 - Whether explosions should drop items.
 - Enable or disable the block placement sound effect when a block is healed.
 - Heal explosions faster via splash potions of Healing or Regeneration.

### Commands

All of the mod's settings can also be modified in-game via commands. Access all of them via the `/creeper-healing` parent command. The config file also supports being reloaded in-game via `/creeper-healing reload_config` to avoid having to restart the server or world. Note that all commands require operator permission.

## Configuration  File
When the server or game is started, the mod will look for an existing `creeper-healing.toml` file for the config folder of your game. If it exists, it will read the values from there. If not, it will create a new config file in `/config/creeper-healing.toml`. You can then edit this file to configure the mod, and restart the server or game to apply the changes, or use the `/creeper-healing reload_config` in-game command. 

The following is the default configuration file generated upon first mod initialization or whenever the mod fails to find the config file during server or world shutdown.

```toml
#Choose between different special modes for explosion healing.
[explosion_healing_mode]
	#(Default = "default_mode") Select between any of the following healing modes by copying the string (the text enclosed by the double quotes along with the double quotes)
	#and pasting it into the value of the "mode" setting below:
	#["default_mode", "daytime_healing_mode", "difficulty_based_healing_mode", "blast_resistance_based_healing_mode"] 
	mode = "default_mode"
#Configure which explosions are allowed to heal.
[explosion_sources]
	#(Default = true) Heal explosions caused by Creepers.
	heal_creeper_explosions = true
	#(Default = false) Heal explosions caused by Ghasts.
	heal_ghast_explosions = false
	#(Default = false) Heal explosions caused by Withers.
	heal_wither_explosions = false
	#(Default = false) Heal explosions caused by TNT blocks.
	heal_tnt_explosions = false
	#(Default = false) Heal explosions caused by TNT minecarts.
	heal_tnt_minecart_explosions = false
	#(Default = false) Heal explosions caused by beds and respawn anchors.
	heal_bed_and_respawn_anchor_explosions = false
	#(Default = false) Heal explosions caused by end crystals.
	heal_end_crystal_explosions = false
#These settings allow you to configure whether explosions from specific sources are allowed to drop items or not.
[explosion_item_drops]
	#(Default = true) Explosions caused by Creepers will drop items.
	drop_items_on_creeper_explosions = true
	#(Default = true) Explosions caused by Ghasts will drop items.
	drop_items_on_ghast_explosions = true
	#(Default = true) Explosions caused by Withers will drop items.
	drop_items_on_wither_explosions = true
	#(Default = true) Explosions caused by TNT will drop items.
	drop_items_on_tnt_explosions = true
	#(Default = true) Explosions caused by TNT minecarts will drop items.
	drop_items_on_tnt_minecart_explosions = true
	#(Default = true) Explosions caused by beds and respawn anchors will drop items.
	drop_items_on_bed_and_respawn_anchor_explosions = true
	#(Default = true) Explosions caused by end crystals will drop items.
	drop_items_on_end_crystal_explosions = true
#Configure the delays related to the healing of explosions.
[delays]
	#(Default = 3) Change the delay in seconds between each explosion and its corresponding healing process.
	explosion_heal_delay = 3.0
	#(Default = 1) Change the delay in seconds between each block placement during the explosion healing process.
	block_placement_delay = 1.0
#Toggleable settings to customize the healing of explosions.
[preferences]
	#(Default = true) Whether or not blocks should be healed where there is currently flowing water.
	heal_on_flowing_water = true
	#(Default = false) Whether or not blocks should healed where there is currently a source water block.
	heal_on_source_water = false
	#(Default = true) Whether or not blocks should be healed where there is currently flowing lava.
	heal_on_flowing_lava = true
	#(Default = false) Whether or not blocks should be healed where there is currently a source lava block.
	heal_on_source_lava = false
	#(Default = true) Whether or not a block heal should play a sound effect.
	block_placement_sound_effect = true
	#(Default = true) Makes explosion heal immediately upon throwing a splash potion of Healing on them.
	heal_on_healing_potion_splash = true
	#(Default = true) Makes explosion start their healing process upon throwing a splash potion of Regeneration of them.
	#This option only modifies the heal delay of the explosion and only affects explosions created with the default healing mode.
	heal_on_regeneration_potion_splash = true
	#(Default = false) Enable or disable the usage of the whitelist
	enable_whitelist = false
#Use an optional whitelist to customize which blocks are allowed to heal. To add an entry, specify the block's namespace
#along with its identifier, separated by a colon, and add it in-between the square brackets below. Separate each entry with a comma.
#Example entries:
#whitelist_entries = ["minecraft:grass",  "minecraft:stone", "minecraft:sand"]
[whitelist]
	whitelist_entries = ["minecraft:placeholder"]
#Add your own replace settings to configure which blocks should be used to heal other blocks. The block on the right will be used to heal the block on the left.
#Specify the block's namespace along with the block's name identifier, separated by a colon.
#Example entry:
#"minecraft:gold_block" = "minecraft:stone"
#Warning, the same key cannot appear more than once in the replace map! For example, the following will cause an error:
#"minecraft:diamond_block" = "minecraft:stone"
#"minecraft:diamond_block" = "minecraft:air" 
[replace_map]
	"minecraft:diamond_block" = "minecraft:stone"

```

## Support

If you would like to report a bug, or make a suggestion, you can do so via the mod's [issue tracker](https://github.com/ArkoSammy12/creeper-healing/issues) or join the Creeper Healing mod's [Discord server](https://discord.gg/UKr8n3b3ze). 

## Building

Clone this repository on your PC, then open your command line prompt on the main directory of the mod, and run the command: `gradlew build`. Once the build is successful, you can find the mod under `/creeper-healing/build/libs`. Use the .jar file without the `"sources"`.

## Credits

- Thanks to @sulpherstaer for giving me the idea and inspiration for making this mod.
- Thansk to @_jacg on the Fabric Discord server helping me out with setting up the custom config file.
- Thanks to @dale8699 for helping me improve and give me ideas for the mod.

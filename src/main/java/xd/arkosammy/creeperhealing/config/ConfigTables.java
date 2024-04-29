package xd.arkosammy.creeperhealing.config;

import java.util.Arrays;
import java.util.List;

public enum ConfigTables {
    DELAYS_CONFIG_TABLE(new SimpleConfigTable("delays", """
            Configure the delays related to the healing of explosions.""")),
    EXPLOSION_ITEM_DROPS_TABLE(new SimpleConfigTable("explosion_item_drops", """
            Toggle whether certain explosion should drop items. Does not include items stored in container blocks.""")),
    EXPLOSION_SOURCE_TABLE(new SimpleConfigTable("explosion_sources", """
            Configure which explosions are allowed to heal.""")),
    HEALING_MODE_TABLE(new SimpleConfigTable("explosion_healing_mode", """
            Choose between different special modes for explosion healing. Note that certain healing modes will not follow the explosion delay and block delay settings.""")),
    PREFERENCES_TABLE(new SimpleConfigTable("preferences", """
            Toggleable settings for extra features.""")),
    WHITELIST_TABLE(new SimpleConfigTable("whitelist", """
             Use an optional whitelist to customize which blocks are allowed to heal. To add an entry, specify the block's namespace
            along with its identifier, separated by a colon and enclosed in double quotes, and add it in-between the square brackets below. Separate each entry with a comma.
            Example entries:
            whitelist_entries = ["minecraft:grass",  "minecraft:stone", "minecraft:sand"]""")),
    REPLACE_MAP_TABLE(new ReplaceMapTable());


    private final ConfigTable configTable;

    public static List<ConfigTable> getConfigTables() {
        return Arrays.stream(ConfigTables.values()).map(table -> table.configTable).toList();
    }

    public String getName() {
        return this.configTable.getName();
    }

    ConfigTables(ConfigTable configTable) {
        this.configTable = configTable;
    }

}

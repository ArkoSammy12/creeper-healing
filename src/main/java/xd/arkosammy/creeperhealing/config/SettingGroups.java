package xd.arkosammy.creeperhealing.config;

import xd.arkosammy.creeperhealing.config.groups.MutableDelaysGroup;
import xd.arkosammy.monkeyconfig.groups.DefaultMutableSettingGroup;
import xd.arkosammy.monkeyconfig.groups.MutableSettingGroup;
import xd.arkosammy.monkeyconfig.groups.maps.MutableStringMapSettingGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public enum SettingGroups {
    DELAYS(new MutableDelaysGroup("delays", """
            Configure the delays related to the healing of explosions.""")),
    EXPLOSION_ITEM_DROPS(new DefaultMutableSettingGroup("explosion_item_drops", """
            Toggle whether certain explosion should drop items. Does not include items stored in container blocks.""")),
    EXPLOSION_SOURCES(new DefaultMutableSettingGroup("explosion_sources", """
            Configure which explosions are allowed to heal.""")),
    HEALING_MODE(new DefaultMutableSettingGroup("explosion_healing_mode", """
            Choose between different special modes for explosion healing. Note that certain healing modes will not follow the explosion delay and block delay settings.""")),
    PREFERENCES(new DefaultMutableSettingGroup("preferences", """
            Toggleable settings for extra features.""")),
    WHITELIST(new DefaultMutableSettingGroup("whitelist", """
            Use an optional whitelist to customize which blocks are allowed to heal. To add an entry, specify the block's namespace
            along with its identifier, separated by a colon and enclosed in double quotes, and add it in-between the square brackets below. Separate each entry with a comma.
            Example entries:
            whitelist_entries = ["minecraft:grass",  "minecraft:stone", "minecraft:sand"]""", new ArrayList<>(), true)),
    REPLACE_MAP(new MutableStringMapSettingGroup("replace_map", """
            Add your own replace entries to configure which blocks should be used to heal other blocks. The block on the right will be used to heal the block on the left.
            Specify the block's namespace along with the block's name identifier, separated by a colon and enclosed in double quotes.
            Example entry:
            "minecraft:gold_block" = "minecraft:stone"
            Warning, the same key cannot appear more than once in the replace map! For example, the following will cause an error:
            "minecraft:diamond_block" = "minecraft:stone"
            "minecraft:diamond_block" = "minecraft:air"\s""", Map.of("minecraft:diamond_block", "minecraft:stone")));


    private final MutableSettingGroup settingGroup;

    SettingGroups(MutableSettingGroup settingGroup) {
        this.settingGroup = settingGroup;
    }

    public String getName() {
        return this.settingGroup.getName();
    }

    public static List<MutableSettingGroup> getSettingGroups() {
        return Arrays.stream(SettingGroups.values()).map(e -> e.settingGroup).collect(Collectors.toList());
    }

}

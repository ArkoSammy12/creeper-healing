package xd.arkosammy.creeperhealing.config.enums;

import xd.arkosammy.creeperhealing.config.settings.BlockPlacementDelaySetting;
import xd.arkosammy.creeperhealing.config.settings.ConfigSetting;
import xd.arkosammy.creeperhealing.config.settings.HealDelaySetting;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum DelaysSettings {
    EXPLOSION_HEAL_DELAY(new HealDelaySetting("explosion_heal_delay", 3.0, 0d, null, """
                (Default = 3) How much time in seconds should an explosion wait for to begin healing.""")),
    BLOCK_PLACEMENT_DELAY(new BlockPlacementDelaySetting("block_placement_delay", 1.0, 0d, null ,"""
                (Default = 1) The time in seconds that a block takes to heal."""));

    private final ConfigSetting<?> configSetting;

    static List<ConfigSetting<?>> getConfigSettings() {
        return Arrays.stream(DelaysSettings.values()).map(value -> value.configSetting).collect(Collectors.toList());
    }

    public String getName() {
        return this.configSetting.getName();
    }

    DelaysSettings(ConfigSetting<?> configSetting) {
        this.configSetting = configSetting;
    }

}

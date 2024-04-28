package xd.arkosammy.creeperhealing.config.settings.enums;

import xd.arkosammy.creeperhealing.config.settings.ConfigSetting;
import xd.arkosammy.creeperhealing.config.settings.StringListSetting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum WhitelistSettings {
    WHITELIST(new StringListSetting("whitelist", new ArrayList<>(Arrays.asList("minecraft:placeholder"))));

    private final ConfigSetting<?> configSetting;

    public static List<ConfigSetting<?>> getConfigSettings() {
        return Arrays.stream(WhitelistSettings.values()).map(value -> value.configSetting).collect(Collectors.toList());
    }

    public String getName() {
        return this.configSetting.getName();
    }

    WhitelistSettings(ConfigSetting<?> configSetting) {
        this.configSetting = configSetting;
    }

}

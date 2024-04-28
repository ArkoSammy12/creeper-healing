package xd.arkosammy.creeperhealing.config.settings.enums;

import xd.arkosammy.creeperhealing.explosions.ExplosionHealingMode;
import xd.arkosammy.creeperhealing.config.settings.ConfigSetting;
import xd.arkosammy.creeperhealing.config.settings.StringSetting;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum HealingModeSettings {
    MODE(new StringSetting("mode", ExplosionHealingMode.DEFAULT_MODE.getName(), """
                (Default = "default_mode") Choose any of the following healing modes by copying one of the strings and pasting it into the value of the "mode" setting below:
                - "%s", "%s", "%s", "%s"\s""".formatted(ExplosionHealingMode.DEFAULT_MODE.getName(), ExplosionHealingMode.DAYTIME_HEALING_MODE.getName(), ExplosionHealingMode.DIFFICULTY_BASED_HEALING_MODE.getName(), ExplosionHealingMode.BLAST_RESISTANCE_BASED_HEALING_MODE.getName())));

    private final ConfigSetting<?> configSetting;

    public static List<ConfigSetting<?>> getConfigSettings() {
        return Arrays.stream(HealingModeSettings.values()).map(value -> value.configSetting).collect(Collectors.toList());
    }

    public String getName() {
        return this.configSetting.getName();
    }

    HealingModeSettings(ConfigSetting<?> configSetting) {
        this.configSetting = configSetting;
    }

}

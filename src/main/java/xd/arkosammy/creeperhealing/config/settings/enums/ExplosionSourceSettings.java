package xd.arkosammy.creeperhealing.config.settings.enums;

import xd.arkosammy.creeperhealing.config.settings.BooleanSetting;
import xd.arkosammy.creeperhealing.config.settings.ConfigSetting;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum ExplosionSourceSettings {
    HEAL_CREEPER_EXPLOSIONS(new BooleanSetting("heal_creeper_explosions", true, """
                (Default = true) Heal explosions caused by Creepers.""")),
    HEAL_GHAST_EXPLOSIONS(new BooleanSetting("heal_ghast_explosions", false, """
                (Default = false) Heal explosions caused by Ghasts.""")),
    HEAL_WITHER_EXPLOSIONS(new BooleanSetting("heal_wither_explosions", false, """
                (Default = false) Heal explosions caused by Withers.""")),
    HEAL_TNT_EXPLOSIONS(new BooleanSetting("heal_tnt_explosions", false, """
                (Default = false) Heal explosions caused by TNT blocks.""")),
    HEAL_TNT_MINECART_EXPLOSIONS(new BooleanSetting("heal_tnt_minecart_explosions", false, """
                (Default = false) Heal explosions caused by TNT minecarts.""")),
    HEAL_BED_AND_RESPAWN_ANCHOR_EXPLOSIONS(new BooleanSetting("heal_bed_and_respawn_anchor_explosions", false, """
                (Default = false) Heal explosions caused by beds and respawn anchors.""")),
    HEAL_END_CRYSTAL_EXPLOSIONS(new BooleanSetting("heal_end_crystal_explosions", false, """
                (Default = false) Heal explosions caused by End Crystals."""));

    private final ConfigSetting<?> configSetting;

    public static List<ConfigSetting<?>> getConfigSettings() {
        return Arrays.stream(ExplosionSourceSettings.values()).map(value -> value.configSetting).collect(Collectors.toList());
    }

    public String getName() {
        return this.configSetting.getName();
    }

    ExplosionSourceSettings(ConfigSetting<?> configSetting) {
        this.configSetting = configSetting;
    }

}

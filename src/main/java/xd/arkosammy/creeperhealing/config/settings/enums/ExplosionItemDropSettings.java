package xd.arkosammy.creeperhealing.config.settings.enums;

import xd.arkosammy.creeperhealing.config.settings.BooleanSetting;
import xd.arkosammy.creeperhealing.config.settings.ConfigSetting;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum ExplosionItemDropSettings {
    DROP_ITEMS_ON_CREEPER_EXPLOSIONS(new BooleanSetting("drop_items_on_creeper_explosions", true, """
                (Default = true) Explosions caused by Creepers will drop items.""")),
    DROP_ITEMS_ON_GHAST_EXPLOSIONS(new BooleanSetting("drop_items_on_ghast_explosions", true, """
                (Default = true) Explosions caused by Ghasts will drop items.""")),
    DROP_ITEMS_ON_WITHER_EXPLOSIONS(new BooleanSetting("drop_items_on_wither_explosions", true, """
                (Default = true) Explosions caused by Withers will drop items.""")),
    DROP_ITEMS_ON_TNT_EXPLOSIONS(new BooleanSetting("drop_items_on_tnt_explosions", true, """
                (Default = true) Explosions caused by TNT will drop items.""")),
    DROP_ITEMS_ON_TNT_MINECART_EXPLOSIONS(new BooleanSetting("drop_items_on_tnt_minecart_explosions", true, """
                (Default = true) Explosions caused by TNT minecarts will drop items.""")),
    DROP_ITEMS_ON_BED_AND_RESPAWN_ANCHOR_EXPLOSIONS(new BooleanSetting("drop_items_on_bed_and_respawn_anchor_explosions", true, """
                (Default = true) Explosions caused by beds and respawn anchors will drop items.""")),
    DROP_ITEMS_ON_END_CRYSTAL_EXPLOSIONS(new BooleanSetting("drop_items_on_end_crystal_explosions", true, """
                (Default = true) Explosions caused by end crystals will drop items."""));


    private final ConfigSetting<?> configSetting;

    public static List<ConfigSetting<?>> getConfigSettings() {
        return Arrays.stream(ExplosionItemDropSettings.values()).map(value -> value.configSetting).collect(Collectors.toList());
    }

    public String getName() {
        return this.configSetting.getName();
    }

    ExplosionItemDropSettings(ConfigSetting<?> configSetting) {
        this.configSetting = configSetting;
    }

}

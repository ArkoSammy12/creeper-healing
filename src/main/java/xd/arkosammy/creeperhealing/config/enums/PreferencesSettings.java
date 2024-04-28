package xd.arkosammy.creeperhealing.config.enums;

import xd.arkosammy.creeperhealing.config.settings.BooleanSetting;
import xd.arkosammy.creeperhealing.config.settings.ConfigSetting;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum PreferencesSettings {

    RESTORE_BLOCK_NBT(new BooleanSetting("restore_block_nbt", false, """
            (Default = false) Whether to restore block nbt data upon healing. This option prevents container blocks like chests from dropping their inventories. Does not apply when the healed block is different from the destroyed block due to a replace map entry.""")),
    MAKE_FALLING_BLOCKS_FALL(new BooleanSetting("make_falling_blocks_fall", true, """
            (Default = true) Allows for a falling block, like sand or gravel, to fall when healed. Disabling this option makes the falling block have to receive a neighbor update before falling.""")),
    BLOCK_PLACEMENT_SOUND_EFFECT(new BooleanSetting("block_placement_sound_effect", true, """
                (Default = true) Whether a block placement sound effect should be played when a block is healed.""")),
    HEAL_ON_HEALING_POTION_SPLASH(new BooleanSetting("heal_on_healing_potion_splash", true, """
                (Default = true) Makes explosion heal immediately when a potion of Healing is thrown on them.""")),
    HEAL_ON_REGENERATION_POTION_SPLASH(new BooleanSetting("heal_on_regeneration_potion_splash", true, """
                (Default = true) Makes explosions begin their healing process when a potion of Regeneration is thrown on them.""")),
    ENABLE_WHITELIST(new BooleanSetting("enable_whitelist", false, """
                (Default = false) Toggle the usage of the whitelist."""));

    private final ConfigSetting<?> configSetting;

    static List<ConfigSetting<?>> getConfigSettings() {
        return Arrays.stream(PreferencesSettings.values()).map(value -> value.configSetting).collect(Collectors.toList());
    }

    public String getName() {
        return this.configSetting.getName();
    }

    PreferencesSettings(ConfigSetting<?> configSetting) {
        this.configSetting = configSetting;
    }

}

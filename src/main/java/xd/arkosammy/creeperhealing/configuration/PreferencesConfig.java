package xd.arkosammy.creeperhealing.configuration;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import xd.arkosammy.creeperhealing.CreeperHealing;

import java.util.Arrays;

public enum PreferencesConfig {

    HEAL_ON_FLOWING_WATER(new ConfigEntry<>("heal_on_flowing_water", true, """
                (Default = true) Whether or not blocks should be healed where there is currently flowing water.""")),
    HEAL_ON_SOURCE_WATER(new ConfigEntry<>("heal_on_source_water", false, """
                (Default = false) Whether or not blocks should healed where there is currently a source water block.""")),
    HEAL_ON_FLOWING_LAVA(new ConfigEntry<>("heal_on_flowing_lava", true, """
                (Default = true) Whether or not blocks should be healed where there is currently flowing lava.""")),
    HEAL_ON_SOURCE_LAVA(new ConfigEntry<>("heal_on_source_lava", false, """
                (Default = false) Whether or not blocks should be healed where there is currently a source lava block.""")),
    BLOCK_PLACEMENT_SOUND_EFFECT(new ConfigEntry<>("block_placement_sound_effect", true, """
                (Default = true) Whether or not a block heal should play a sound effect.""")),
    HEAL_ON_HEALING_POTION_SPLASH(new ConfigEntry<>("heal_on_healing_potion_splash", true, """
                (Default = true) Makes explosion heal immediately upon throwing a splash potion of Healing on them.""")),
    HEAL_ON_REGENERATION_POTION_SPLASH(new ConfigEntry<>("heal_on_regeneration_potion_splash", true, """
                (Default = true) Makes explosion start their healing process upon throwing a splash potion of Regeneration of them.""")),
    ENABLE_WHITELIST(new ConfigEntry<>("enable_whitelist", false, """
                (Default = false) Enable or disable the usage of the whitelist"""));

    private final ConfigEntry<Boolean> entry;

    PreferencesConfig(ConfigEntry<Boolean> entry){
        this.entry = entry;
    }

    public ConfigEntry<Boolean> getEntry(){
        return this.entry;
    }

    private static final String TABLE_NAME = "preferences";
    private static final String TABLE_COMMENT = """
            Toggleable settings to customize the healing of explosions.""";

    static void saveToFileWithDefaultValues(CommentedFileConfig fileConfig){

        for(ConfigEntry<Boolean> configEntry : Arrays.stream(PreferencesConfig.values()).map(PreferencesConfig::getEntry).toList()){
            configEntry.resetValue();
        }

        saveSettingsToFile(fileConfig);

    }

    static void saveSettingsToFile(CommentedFileConfig fileConfig){
        for(ConfigEntry<Boolean> entry : Arrays.stream(PreferencesConfig.values()).map(PreferencesConfig::getEntry).toList()){
            fileConfig.set(TABLE_NAME + "." + entry.getName(), entry.getValue());
            String entryComment = entry.getComment();
            if(entryComment != null) fileConfig.setComment(TABLE_NAME + "." + entry.getName(), entryComment);
        }
        fileConfig.setComment(TABLE_NAME, TABLE_COMMENT);
    }

    static void loadSettingsToMemory(CommentedFileConfig fileConfig){
        for(ConfigEntry<Boolean> configEntry : Arrays.stream(PreferencesConfig.values()).map(PreferencesConfig::getEntry).toList()){
            Object value = fileConfig.getOrElse(TABLE_NAME + "." + configEntry.getName(), configEntry.getDefaultValue());
            if(value instanceof Boolean boolValue){
                configEntry.setValue(boolValue);
            } else {
                CreeperHealing.LOGGER.error("Invalid value in config file for setting: " + configEntry.getName());
            }
        }
    }

}

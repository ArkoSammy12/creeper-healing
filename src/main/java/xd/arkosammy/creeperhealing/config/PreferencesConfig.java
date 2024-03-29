package xd.arkosammy.creeperhealing.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import xd.arkosammy.creeperhealing.CreeperHealing;

import java.util.Arrays;

public enum PreferencesConfig {

    RESTORE_BLOCK_NBT(new ConfigEntry<>("restore_block_nbt", false, """
            (Default = false) Whether to restore block nbt data upon healing. This option prevents container blocks like chests from dropping their inventories. Does not apply when the healed block is different from the destroyed block due to a replace map entry.""")),
    MAKE_FALLING_BLOCKS_FALL(new ConfigEntry<>("make_falling_blocks_fall", true, """
            (Default = true) Allows for a falling block, like sand or gravel, to fall when healed. Disabling this option makes the falling block have to receive a neighbor update before falling.""")),
    BLOCK_PLACEMENT_SOUND_EFFECT(new ConfigEntry<>("block_placement_sound_effect", true, """
                (Default = true) Whether a block placement sound effect should be played when a block is healed.""")),
    HEAL_ON_HEALING_POTION_SPLASH(new ConfigEntry<>("heal_on_healing_potion_splash", true, """
                (Default = true) Makes explosion heal immediately when a potion of Healing is thrown on them.""")),
    HEAL_ON_REGENERATION_POTION_SPLASH(new ConfigEntry<>("heal_on_regeneration_potion_splash", true, """
                (Default = true) Makes explosions begin their healing process when a potion of Regeneration is thrown on them.""")),
    ENABLE_WHITELIST(new ConfigEntry<>("enable_whitelist", false, """
                (Default = false) Toggle the usage of the whitelist."""));

    private final ConfigEntry<Boolean> entry;

    PreferencesConfig(ConfigEntry<Boolean> entry){
        this.entry = entry;
    }

    public ConfigEntry<Boolean> getEntry(){
        return this.entry;
    }

    private static final String TABLE_NAME = "preferences";
    private static final String TABLE_COMMENT = """
            Toggleable settings for extra features.""";

    static void setDefaultValues(CommentedFileConfig fileConfig){

        for(ConfigEntry<Boolean> configEntry : Arrays.stream(PreferencesConfig.values()).map(PreferencesConfig::getEntry).toList()){
            configEntry.resetValue();
        }

        setValues(fileConfig);

    }

    static void setValues(CommentedFileConfig fileConfig){
        for(ConfigEntry<Boolean> entry : Arrays.stream(PreferencesConfig.values()).map(PreferencesConfig::getEntry).toList()){
            fileConfig.set(TABLE_NAME + "." + entry.getName(), entry.getValue());
            String entryComment = entry.getComment();
            if(entryComment != null) fileConfig.setComment(TABLE_NAME + "." + entry.getName(), entryComment);
        }
        fileConfig.setComment(TABLE_NAME, TABLE_COMMENT);
        fileConfig.<CommentedConfig>get(TABLE_NAME).entrySet().removeIf(entry -> !isEntryKeyInEnum(entry.getKey()));
    }

    static void getValues(CommentedFileConfig fileConfig){
        for(ConfigEntry<Boolean> configEntry : Arrays.stream(PreferencesConfig.values()).map(PreferencesConfig::getEntry).toList()){
            Object value = fileConfig.getOrElse(TABLE_NAME + "." + configEntry.getName(), configEntry.getDefaultValue());
            if(value instanceof Boolean boolValue){
                configEntry.setValue(boolValue);
            } else {
                CreeperHealing.LOGGER.error("Invalid value in config file for setting: " + configEntry.getName());
            }
        }
    }

    private static boolean isEntryKeyInEnum(String key){
        return Arrays.stream(PreferencesConfig.values()).anyMatch(configEntry -> configEntry.getEntry().getName().equals(key));
    }

}

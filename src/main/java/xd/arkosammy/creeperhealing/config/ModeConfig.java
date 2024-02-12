package xd.arkosammy.creeperhealing.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import xd.arkosammy.creeperhealing.CreeperHealing;
import xd.arkosammy.creeperhealing.explosions.ExplosionHealingMode;
import java.util.Arrays;

public enum ModeConfig {
    MODE(new ConfigEntry<>("mode", ExplosionHealingMode.DEFAULT_MODE.getName(), """
                (Default = "default_mode") Choose any of the following healing modes by copying one of the strings and pasting it into the value of the "mode" setting below:
                - "%s", "%s", "%s", "%s"\s""".formatted(ExplosionHealingMode.DEFAULT_MODE.getName(), ExplosionHealingMode.DAYTIME_HEALING_MODE.getName(), ExplosionHealingMode.DIFFICULTY_BASED_HEALING_MODE.getName(), ExplosionHealingMode.BLAST_RESISTANCE_BASED_HEALING_MODE.getName())));

    private final ConfigEntry<String> entry;

    ModeConfig(ConfigEntry<String> entry){
        this.entry = entry;
    }

    public ConfigEntry<String> getEntry(){
        return this.entry;
    }

    private static final String TABLE_NAME = "explosion_healing_mode";
    private static final String TABLE_COMMENT = """
            Choose between different special modes for explosion healing. Note that certain healing modes will not follow the explosion delay and block delay settings.""";

    static void setDefaultValues(CommentedFileConfig fileConfig){
        for(ConfigEntry<String> configEntry : Arrays.stream(ModeConfig.values()).map(ModeConfig::getEntry).toList()){
            configEntry.resetValue();
        }
        setValues(fileConfig);
    }

    static void setValues(CommentedFileConfig fileConfig){
        for(ConfigEntry<String> entry : Arrays.stream(ModeConfig.values()).map(ModeConfig::getEntry).toList()){
            fileConfig.set(TABLE_NAME + "." + entry.getName(), entry.getValue());
            String entryComment = entry.getComment();
            if(entryComment != null) fileConfig.setComment(TABLE_NAME + "." + entry.getName(), entryComment);
        }
        fileConfig.setComment(TABLE_NAME, TABLE_COMMENT);
        fileConfig.<CommentedConfig>get(TABLE_NAME).entrySet().removeIf(entry -> !isEntryKeyInEnum(entry.getKey()));
    }

    static void getValues(CommentedFileConfig fileConfig){
        for(ConfigEntry<String> configEntry : Arrays.stream(ModeConfig.values()).map(ModeConfig::getEntry).toList()){
            Object value = fileConfig.getOrElse(TABLE_NAME + "." + configEntry.getName(), configEntry.getDefaultValue());
            if(value instanceof String stringValue){
                configEntry.setValue(stringValue);
            } else {
                CreeperHealing.LOGGER.error("Invalid value in config file for setting: " + configEntry.getName());
            }
        }
    }

    private static boolean isEntryKeyInEnum(String key){
        return Arrays.stream(ModeConfig.values()).anyMatch(configEntry -> configEntry.getEntry().getName().equals(key));
    }

}

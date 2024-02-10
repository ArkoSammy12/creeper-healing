package xd.arkosammy.creeperhealing.configuration;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import xd.arkosammy.creeperhealing.CreeperHealing;
import xd.arkosammy.creeperhealing.explosions.ExplosionHealingMode;
import java.util.Arrays;

public enum ModeConfig {
    MODE(new ConfigEntry<>("mode", ExplosionHealingMode.DEFAULT_MODE.getName(), """
                (Default = "default_mode") Select between any of the following healing modes by copying the string (the text enclosed by the double quotes along with the double quotes)
                and pasting it into the value of the "mode" setting below:
                ["%s", "%s", "%s", "%s"]\s""".formatted(ExplosionHealingMode.DEFAULT_MODE.getName(), ExplosionHealingMode.DAYTIME_HEALING_MODE.getName(), ExplosionHealingMode.DIFFICULTY_BASED_HEALING_MODE.getName(), ExplosionHealingMode.BLAST_RESISTANCE_BASED_HEALING_MODE.getName())));

    private final ConfigEntry<String> entry;

    ModeConfig(ConfigEntry<String> entry){
        this.entry = entry;
    }

    public ConfigEntry<String> getEntry(){
        return this.entry;
    }

    private static final String TABLE_NAME = "explosion_healing_mode";
    private static final String TABLE_COMMENT = """
            Choose between different special modes for explosion healing.""";

    static void saveToFileWithDefaultValues(CommentedFileConfig fileConfig){
        for(ConfigEntry<String> configEntry : Arrays.stream(ModeConfig.values()).map(ModeConfig::getEntry).toList()){
            configEntry.resetValue();
        }
        saveSettingsToFile(fileConfig);
    }

    static void saveSettingsToFile(CommentedFileConfig fileConfig){
        for(ConfigEntry<String> entry : Arrays.stream(ModeConfig.values()).map(ModeConfig::getEntry).toList()){
            fileConfig.set(TABLE_NAME + "." + entry.getName(), entry.getValue());
            String entryComment = entry.getComment();
            if(entryComment != null) fileConfig.setComment(TABLE_NAME + "." + entry.getName(), entryComment);
        }
        fileConfig.setComment(TABLE_NAME, TABLE_COMMENT);
    }

    static void loadSettingsToMemory(CommentedFileConfig fileConfig){
        for(ConfigEntry<String> configEntry : Arrays.stream(ModeConfig.values()).map(ModeConfig::getEntry).toList()){
            Object value = fileConfig.getOrElse(TABLE_NAME + "." + configEntry.getName(), configEntry.getDefaultValue());
            if(value instanceof String stringValue){
                configEntry.setValue(stringValue);
            } else {
                CreeperHealing.LOGGER.error("Invalid value in config file for setting: " + configEntry.getName());
            }
        }
    }

}

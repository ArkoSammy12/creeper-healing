package xd.arkosammy.configuration.tables;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import xd.arkosammy.CreeperHealing;
import xd.arkosammy.configuration.ConfigEntry;
import xd.arkosammy.explosions.ExplosionHealingMode;

import java.util.ArrayList;
import java.util.List;

public final class ModeConfig {

    private ModeConfig(){}
    private static final List<ConfigEntry<String>> modeEntryList = new ArrayList<>();
    private static final String TABLE_NAME = "explosion_healing_mode";
    private static final String TABLE_COMMENT = """
            Choose between different special modes for explosion healing.""";

    static {

        modeEntryList.add(new ConfigEntry<>("mode", ExplosionHealingMode.DEFAULT_MODE.getName(), """
                (Default = "default_mode") Select between any of the following healing modes by copying the string (the text enclosed by the double quotes along with the double quotes)
                and pasting it into the value of the "mode" setting below:
                ["default_mode", "daytime_healing_mode", "difficulty_based_healing_mode", "blast_resistance_based_healing_mode"]\s"""));

    }

    private static List<ConfigEntry<String>> getModeEntryList(){
        return modeEntryList;
    }

    public static void setHealingMode(String explosionModeName){
        for(ConfigEntry<String> configEntry : getModeEntryList()){
            if(configEntry.getName().equals("mode")){
                configEntry.setValue(explosionModeName);
            }
        }
    }

    public static String getHealingMode(){
        String stringToReturn = getValueForNameFromMemory("mode");
        if(stringToReturn == null) return ExplosionHealingMode.DEFAULT_MODE.getName();
        return stringToReturn;
    }

    public static void saveDefaultSettingsToFile(CommentedFileConfig fileConfig){
        for(ConfigEntry<String> configEntry : getModeEntryList()){
            configEntry.resetValue();
        }
        saveSettingsToFile(fileConfig);
    }

    public static void saveSettingsToFile(CommentedFileConfig fileConfig){
        for(ConfigEntry<String> entry : getModeEntryList()){
            fileConfig.set(TABLE_NAME + "." + entry.getName(), entry.getValue());
            String entryComment = entry.getComment();
            if(entryComment != null) fileConfig.setComment(TABLE_NAME + "." + entry.getName(), entryComment);
        }
        fileConfig.setComment(TABLE_NAME, TABLE_COMMENT);
    }

    public static void loadSettingsToMemory(CommentedFileConfig fileConfig){
        for(ConfigEntry<String> configEntry : getModeEntryList()){
            Object value = fileConfig.getOrElse(TABLE_NAME + "." + configEntry.getName(), configEntry.getDefaultValue());
            if(value instanceof String stringValue){
                configEntry.setValue(stringValue);
            } else {
                CreeperHealing.LOGGER.error("Invalid value in config file for setting: " + configEntry.getName());
            }
        }
    }

    private static String getValueForNameFromMemory(String settingName){
        for(ConfigEntry<String> entry : getModeEntryList()){
            if(entry.getName().equals(settingName)){
                return entry.getValue();
            }
        }
        return null;
    }
}

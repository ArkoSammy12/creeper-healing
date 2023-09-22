package xd.arkosammy.configuration.tables;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import xd.arkosammy.CreeperHealing;
import xd.arkosammy.configuration.ConfigEntry;

import java.util.ArrayList;
import java.util.List;

public abstract class ModeConfig {

    private ModeConfig(){}
    private static final List<ConfigEntry<Boolean>> modeEntryList = new ArrayList<>();
    private static final String TABLE_NAME = "modes";
    private static final String TABLE_COMMENT = """
            Toggle different special modes for explosion healing.""";

    static {

        modeEntryList.add(new ConfigEntry<>("daytime_healing_mode", false, """
                (Default = false) Whether or not daytime healing mode should be enabled.
                Explosions will wait until the next sunrise to start healing, and they will finish healing at nighttime.
                Note that this only applies for explosions that occurred while this setting was enabled."""));

    }

    public static void setDaytimeHealingMode(boolean daytimeHealingMode){

        for(ConfigEntry<Boolean> configEntry : getModeEntryList()){

            if(configEntry.getName().equals("daytime_healing_mode")){

                configEntry.setValue(daytimeHealingMode);

            }

        }

    }

    public static Boolean getDayTimeHealingMode(){

        Boolean boolToReturn = getValueForNameFromMemory("daytime_healing_mode");

        if(boolToReturn == null) return false;

        return boolToReturn;

    }

    private static List<ConfigEntry<Boolean>> getModeEntryList(){
        return modeEntryList;
    }

    public static void saveDefaultSettingsToFile(CommentedFileConfig fileConfig){

        for(ConfigEntry<Boolean> configEntry : getModeEntryList()){
            configEntry.resetValue();
        }

        saveSettingsToFile(fileConfig);

    }

    public static void saveSettingsToFile(CommentedFileConfig fileConfig){

        for(ConfigEntry<Boolean> entry : getModeEntryList()){

            fileConfig.set(
                    TABLE_NAME + "." + entry.getName(),
                    entry.getValue()
            );

            String entryComment = entry.getComment();

            if(entryComment != null)
                fileConfig.setComment(
                        TABLE_NAME + "." + entry.getName(),
                        entryComment
                );

        }

        fileConfig.setComment(TABLE_NAME, TABLE_COMMENT);

    }

    public static void loadSettingsToMemory(CommentedFileConfig fileConfig){

        for(ConfigEntry<Boolean> configEntry : getModeEntryList()){

            Object value = fileConfig.getOrElse(TABLE_NAME + "." + configEntry.getName(), configEntry.getDefaultValue());

            if(value instanceof Boolean boolValue){

                configEntry.setValue(boolValue);

            } else {

                CreeperHealing.LOGGER.warn("Invalid value in config file for setting: " + configEntry.getName());

            }

        }

    }

    private static Boolean getValueForNameFromMemory(String settingName){

        for(ConfigEntry<Boolean> entry : getModeEntryList()){

            if(entry.getName().equals(settingName)){

                return entry.getValue();

            }

        }

        return null;

    }
}

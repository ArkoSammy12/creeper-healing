package xd.arkosammy.configuration.tables;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import xd.arkosammy.CreeperHealing;
import xd.arkosammy.configuration.ConfigEntry;

import java.util.ArrayList;
import java.util.List;

public class ModeConfig {

    private static final List<ConfigEntry<Boolean>> modeEntryList = new ArrayList<>();

    public static final String COMMENT = """
            Toggle different special modes for explosion healing.""";

    public static final String NAME = "modes";

    static {

        modeEntryList.add(new ConfigEntry<>("daytime_healing_mode", false, """
                Whether or not daytime healing mode should be enabled.
                Explosions will wait until the next sunrise to start healing, and they will finish healing at nighttime.
                This is false by default.
                Note that this only applies for explosions that occurred while this setting was enabled."""));

    }

    public static Boolean getDayTimeHealingMode(){

        Boolean boolToReturn = getValueForEntry("daytime_healing_mode");

        if(boolToReturn == null) return false;

        return boolToReturn;

    }

    public static void setDaytimeHealingMode(boolean daytimeHealingMode){

        for(ConfigEntry<Boolean> configEntry : getModeEntryList()){

            if(configEntry.getName().equals("daytime_healing_mode")){

                configEntry.setValue(daytimeHealingMode);

            }

        }

    }

    private static List<ConfigEntry<Boolean>> getModeEntryList(){
        return modeEntryList;
    }

    public static void saveDefaultEntries(CommentedFileConfig fileConfig){

        for(ConfigEntry<Boolean> configEntry : getModeEntryList()){
            configEntry.resetValue();
        }

        saveEntries(fileConfig);

    }

    public static void saveEntries(CommentedFileConfig fileConfig){

        for(ConfigEntry<Boolean> entry : getModeEntryList()){

            fileConfig.set(
                    NAME + "." + entry.getName(),
                    entry.getValue()
            );

            fileConfig.setComment(
                    NAME + "." + entry.getName(),
                    entry.getComment()
            );

        }

        fileConfig.setComment(NAME, COMMENT);

    }

    public static void loadEntries(CommentedFileConfig fileConfig){

        for(ConfigEntry<Boolean> configEntry : getModeEntryList()){

            configEntry.setValue(fileConfig.getOrElse(NAME + "." + configEntry.getName(), configEntry.getDefaultValue()));

            CreeperHealing.LOGGER.info("Loaded entry: " + configEntry.getName() + " with entry : " + configEntry.getValue());

        }

    }

    public static Boolean getValueForEntry(String path){

        for(ConfigEntry<Boolean> entry : getModeEntryList()){

            if(entry.getName().equals(path)){

                return entry.getValue();

            }

        }

        return null;

    }
}

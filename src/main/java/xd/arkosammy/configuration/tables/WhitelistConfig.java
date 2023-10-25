package xd.arkosammy.configuration.tables;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import xd.arkosammy.CreeperHealing;
import xd.arkosammy.configuration.ConfigEntry;

import java.util.ArrayList;
import java.util.List;

public final class WhitelistConfig {

    private WhitelistConfig(){}

    private static final List<ConfigEntry<Boolean>> whitelistPreferences = new ArrayList<>();
    private static final List<String> whitelist = new ArrayList<>();

    private static final String TABLE_NAME = "whitelist";

    private static final String TABLE_COMMENT = """
            Use an optional whitelist to customize which blocks are allowed to heal. To add an entry, specify the block's namespace
            along with its identifier and add it in-between the square brackets below. Separate each entry with a comma.
            Example entries:
            whitelist_entries = ["minecraft:grass",  "minecraft:stone", "minecraft:sand"]""";


    static {

        getWhitelistPreferences().add(new ConfigEntry<>("enable_whitelist", false, """
                (Default = false) Enable or disable the usage of the whitelist"""));

    }

    public static List<String> getWhitelist(){
        return  whitelist;
    }

    public static List<ConfigEntry<Boolean>> getWhitelistPreferences(){
        return whitelistPreferences;
    }

    public static void setEnableWhitelist(boolean enableWhitelist){
        for(ConfigEntry<Boolean> configEntry : getWhitelistPreferences()){
            if(configEntry.getName().equals("enable_whitelist")){
                configEntry.setValue(enableWhitelist);
            }
        }
    }

    public static Boolean getEnableWhitelist(){
        Boolean boolToReturn = getValueForNameFromMemory("enable_whitelist");
        if(boolToReturn == null) return false;
        return boolToReturn;
    }

    public static void saveToFileWithDefaultValues(CommentedFileConfig fileConfig){
        for(ConfigEntry<Boolean> configEntry : getWhitelistPreferences()){
            configEntry.resetValue();
        }
        getWhitelist().clear();
        getWhitelist().add("minecraft:placeholder");
        saveWhitelistToFile(fileConfig);
    }

    public static void saveWhitelistToFile(CommentedFileConfig fileConfig){

         for(ConfigEntry<Boolean> entry : getWhitelistPreferences()){
             fileConfig.set(TABLE_NAME + "." + entry.getName(), entry.getValue());
             String entryComment = entry.getComment();
             if(entryComment != null){
                 fileConfig.setComment(TABLE_NAME + "." + entry.getName(), entryComment);
             }
         }

         if(!getWhitelist().isEmpty()){
             fileConfig.set(TABLE_NAME + "."+ "whitelist_entries", getWhitelist());
         } else {
             fileConfig.set(TABLE_NAME + "." + "whitelist_entries", List.of("minecraft:placeholder"));
         }

        fileConfig.setComment(TABLE_NAME, TABLE_COMMENT);

    }

    public static void loadWhitelistToMemory(CommentedFileConfig fileConfig){
        for(ConfigEntry<Boolean> configEntry : getWhitelistPreferences()){
            Object value = fileConfig.getOrElse(TABLE_NAME + "." + configEntry.getName(), configEntry.getDefaultValue());
            if(value instanceof Boolean boolValue){
                configEntry.setValue(boolValue);
            } else {
                CreeperHealing.LOGGER.error("Invalid value in config file for setting : " + configEntry.getName());
            }
        }
        List<String> config = fileConfig.get(TABLE_NAME);
        if(config != null){
            List<String> tempWhitelist = new ArrayList<>(config);
            getWhitelist().clear();
            getWhitelist().addAll(tempWhitelist);
        } else {
            CreeperHealing.LOGGER.error("Error attempting to read the whitelist from the config.");
        }
    }

    private static Boolean getValueForNameFromMemory(String settingName){
        for(ConfigEntry<Boolean> entry : getWhitelistPreferences()){
            if(entry.getName().equals(settingName)){
                return entry.getValue();
            }
        }
        return null;
    }

}

package xd.arkosammy.configuration.tables;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import xd.arkosammy.CreeperHealing;
import xd.arkosammy.configuration.ConfigEntry;
import java.util.ArrayList;
import java.util.List;

public final class DelaysConfig {

    private DelaysConfig(){}
    private static final List<ConfigEntry<Double>> delaysEntryList = new ArrayList<>();
    private static final String TABLE_NAME = "delays";
    private static final String TABLE_COMMENT = """
            Configure the delays related to the healing of explosions.""";

    static {

        delaysEntryList.add(new ConfigEntry<>("explosion_heal_delay", 3.0, """
                (Default = 3) Change the delay in seconds between each explosion and its corresponding healing process."""));

        delaysEntryList.add(new ConfigEntry<>("block_placement_delay", 1.0, """
                (Default = 1) Change the delay in seconds between each block placement during the explosion healing process."""));

    }

    private static List<ConfigEntry<Double>> getDelayEntryList(){
        return delaysEntryList;
    }

    public static void setExplosionHealDelay(double delay){
        for(ConfigEntry<Double> configEntry : getDelayEntryList()){
            if(configEntry.getName().equals("explosion_heal_delay")){
                configEntry.setValue(delay);
            }
        }
    }

    public static void setBlockPlacementDelay(double delay){
        for(ConfigEntry<Double> configEntry : getDelayEntryList()){
            if(configEntry.getName().equals("block_placement_delay")){
                configEntry.setValue(delay);
            }
        }
    }

    public static long getExplosionHealDelay(){
        Double explosionHealDelayToReturn = ConfigEntry.getValueForNameFromMemory("explosion_heal_delay", getDelayEntryList());
        if(explosionHealDelayToReturn == null) return 60;
        long rounded = Math.round(Math.max(explosionHealDelayToReturn, 0) * 20L);
        return rounded == 0 ? 20L : rounded;
    }

    public static long getBlockPlacementDelay(){
        Double blockPlacementDelayToReturn = ConfigEntry.getValueForNameFromMemory("block_placement_delay", getDelayEntryList());
        if(blockPlacementDelayToReturn == null) return 20;
        long rounded = Math.round(Math.max(blockPlacementDelayToReturn, 0) * 20L);
        return rounded == 0 ? 20L : rounded;
    }

    public static double getExplosionHealDelayRaw(){
        Double explosionHealDelayToReturn = ConfigEntry.getValueForNameFromMemory("explosion_heal_delay", getDelayEntryList());
        if(explosionHealDelayToReturn == null) return 3;
        return explosionHealDelayToReturn;
    }

    public static double getBlockPlacementDelayRaw(){
        Double blockPlacementDelayToReturn = ConfigEntry.getValueForNameFromMemory("block_placement_delay", getDelayEntryList());
        if(blockPlacementDelayToReturn == null) return 1;
        return blockPlacementDelayToReturn;
    }

    public static void saveToFileWithDefaultValues(CommentedFileConfig fileConfig){
        for(ConfigEntry<Double> configEntry : getDelayEntryList()){
            configEntry.resetValue();
        }
        saveSettingsToFile(fileConfig);
    }

    public static void saveSettingsToFile(CommentedFileConfig fileConfig){
        for(ConfigEntry<Double> entry : getDelayEntryList()){
            fileConfig.set(TABLE_NAME + "." + entry.getName(), entry.getValue());
            String entryComment = entry.getComment();
            if(entryComment != null) fileConfig.setComment(TABLE_NAME + "." + entry.getName(), entryComment);
        }
        fileConfig.setComment(TABLE_NAME, TABLE_COMMENT);
    }

    public static void loadSettingsToMemory(CommentedFileConfig fileConfig){
        for(ConfigEntry<Double> configEntry : getDelayEntryList()){
            Object value = fileConfig.getOrElse(TABLE_NAME + "." + configEntry.getName(), configEntry.getDefaultValue());
            if(value instanceof Number numberValue){
                configEntry.setValue(numberValue.doubleValue());
            } else {
                CreeperHealing.LOGGER.error("Invalid value in config file for setting: " + configEntry.getName());
            }
        }
    }

}

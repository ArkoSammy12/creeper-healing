package xd.arkosammy.configuration.tables;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import xd.arkosammy.CreeperHealing;
import xd.arkosammy.configuration.ConfigEntry;
import java.util.ArrayList;
import java.util.List;

public abstract class DelaysConfig {

    private DelaysConfig(){}
    private static final List<ConfigEntry<Double>> delaysEntryList = new ArrayList<>();
    private static final String TABLE_COMMENT = """
            Configure the delays related to the healing of explosions.""";
    private static final String TABLE_NAME = "delays";

    static {

        delaysEntryList.add(new ConfigEntry<>("explosion_heal_delay", 3.0, """
                (Default = 3) Change the delay in seconds between each explosion and its corresponding healing process."""));

        delaysEntryList.add(new ConfigEntry<>("block_placement_delay", 1.0, """
                (Default = 1) Change the delay in seconds between each block placement during the explosion healing process."""));

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

        Double explosionHealDelayToReturn = getValueForNameFromMemory("explosion_heal_delay");

        if(explosionHealDelayToReturn == null) return 60;

        long rounded = Math.round(Math.max(explosionHealDelayToReturn, 0) * 20L);
        return rounded == 0 ? 20L : rounded;

    }

    public static long getBlockPlacementDelay(){

        Double blockPlacementDelayToReturn = getValueForNameFromMemory("block_placement_delay");

        if(blockPlacementDelayToReturn == null) return 20;

        long rounded = Math.round(Math.max(blockPlacementDelayToReturn, 0) * 20L);
        return rounded == 0 ? 20L : rounded;

    }

    public static double getExplosionHealDelayRaw(){

        Double explosionHealDelayToReturn = getValueForNameFromMemory("explosion_heal_delay");

        if(explosionHealDelayToReturn == null) return 3;

        return explosionHealDelayToReturn;

    }

    public static double getBlockPlacementDelayRaw(){

        Double blockPlacementDelayToReturn = getValueForNameFromMemory("block_placement_delay");

        if(blockPlacementDelayToReturn == null) return 1;

        return blockPlacementDelayToReturn;

    }

    public static List<ConfigEntry<Double>> getDelayEntryList(){
        return delaysEntryList;
    }

    public static void saveDefaultSettingsToFile(CommentedFileConfig fileConfig){

        for(ConfigEntry<Double> configEntry : getDelayEntryList()){
            configEntry.resetValue();
        }

        saveSettingsToFile(fileConfig);

    }

    public static void saveSettingsToFile(CommentedFileConfig fileConfig){

        for(ConfigEntry<Double> entry : getDelayEntryList()){

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

        for(ConfigEntry<Double> configEntry : getDelayEntryList()){

            Object num = fileConfig.getOrElse(TABLE_NAME + "." + configEntry.getName(), configEntry.getDefaultValue());

            if(num instanceof Number numberToSet){

                configEntry.setValue(numberToSet.doubleValue());
                CreeperHealing.LOGGER.info("Loaded entry: " + configEntry.getName() + " with value : " + configEntry.getValue());

            } else {

                CreeperHealing.LOGGER.warn("Invalid value in config file for setting: " + configEntry.getName());

            }

        }

    }

    public static Double getValueForNameFromMemory(String settingName){

        for(ConfigEntry<Double> entry : getDelayEntryList()){

            if(entry.getName().equals(settingName)){

                return entry.getValue();

            }

        }

        return null;

    }



}

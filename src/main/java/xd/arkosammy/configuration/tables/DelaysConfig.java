package xd.arkosammy.configuration.tables;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import xd.arkosammy.CreeperHealing;
import xd.arkosammy.configuration.ConfigEntry;
import java.util.ArrayList;
import java.util.List;

public class DelaysConfig {

    private static final List<ConfigEntry<Double>> delaysEntryList = new ArrayList<>();

    public static final String COMMENT = """
            Configure the delays related to the healing of explosions.""";

    public static final String NAME = "delays";



    static {

        delaysEntryList.add(new ConfigEntry<>("explosion_heal_delay", 3.0, """
                Change the delay in seconds between each creeper explosion and its corresponding healing process.
                This is 3 by default."""));

        delaysEntryList.add(new ConfigEntry<>("block_placement_delay", 1.0, """
                Change the delay in seconds between each block placement during the explosion healing process.
                This is 1 by default."""));

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

        Double explosionHealDelayToReturn = getValueForEntry("explosion_heal_delay");

        if(explosionHealDelayToReturn == null) return 60;

        long rounded = Math.round(Math.max(explosionHealDelayToReturn, 0) * 20L);
        return rounded == 0 ? 20L : rounded;

    }

    public static long getBlockPlacementDelay(){

        Double blockPlacementDelayToReturn = getValueForEntry("block_placement_delay");

        if(blockPlacementDelayToReturn == null) return 20;

        long rounded = Math.round(Math.max(blockPlacementDelayToReturn, 0) * 20L);
        return rounded == 0 ? 20L : rounded;

    }

    public static double getExplosionHealDelayRaw(){

        Double explosionHealDelayToReturn = getValueForEntry("explosion_heal_delay");

        if(explosionHealDelayToReturn == null) return 3;

        return explosionHealDelayToReturn;

    }

    public static double getBlockPlacementDelayRaw(){

        Double blockPlacementDelayToReturn = getValueForEntry("block_placement_delay");

        if(blockPlacementDelayToReturn == null) return 1;

        return blockPlacementDelayToReturn;

    }

    public static List<ConfigEntry<Double>> getDelayEntryList(){
        return delaysEntryList;
    }

    public static void saveDefaultEntries(CommentedFileConfig fileConfig){

        for(ConfigEntry<Double> configEntry : getDelayEntryList()){
            configEntry.resetValue();
        }

        saveEntries(fileConfig);

    }

    public static void saveEntries(CommentedFileConfig fileConfig){

        for(ConfigEntry<Double> entry : getDelayEntryList()){

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

        for(ConfigEntry<Double> configEntry : getDelayEntryList()){

            configEntry.setValue((double)fileConfig.getOrElse(NAME + "." + configEntry.getName(), configEntry.getDefaultValue()));

            CreeperHealing.LOGGER.info("Loaded entry: " + configEntry.getName() + " with entry : " + configEntry.getValue());

        }

    }

    public static Double getValueForEntry(String name){

        for(ConfigEntry<Double> entry : getDelayEntryList()){

            if(entry.getName().equals(name)){

                return entry.getValue();

            }

        }

        return null;

    }



}

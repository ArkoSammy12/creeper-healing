package xd.arkosammy.creeperhealing.configuration;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import xd.arkosammy.creeperhealing.CreeperHealing;

import java.util.Arrays;

public enum DelaysConfig {

    EXPLOSION_HEAL_DELAY(new ConfigEntry<>("explosion_heal_delay", 3.0, """
                (Default = 3) Change the delay in seconds between each explosion and its corresponding healing process.""")),
    BLOCK_PLACEMENT_DELAY(new ConfigEntry<>("block_placement_delay", 1.0, """
                (Default = 1) Change the delay in seconds between each block placement during the explosion healing process."""));

    private final ConfigEntry<Double> entry;

    DelaysConfig(ConfigEntry<Double> entry){
        this.entry = entry;
    }

    public ConfigEntry<Double> getEntry(){
        return this.entry;
    }

    private static final String TABLE_NAME = "delays";
    private static final String TABLE_COMMENT = """
            Configure the delays related to the healing of explosions.""";

    public static long getExplosionHealDelayAsTicks(){
        long rounded = Math.round(Math.max(EXPLOSION_HEAL_DELAY.getEntry().getValue(), 0) * 20L);
        return rounded == 0 ? 20L : rounded;
    }

    public static long getBlockPlacementDelayAsTicks(){
        long rounded = Math.round(Math.max(BLOCK_PLACEMENT_DELAY.getEntry().getValue(), 0) * 20L);
        return rounded == 0 ? 20L : rounded;
    }


     static void saveToFileWithDefaultValues(CommentedFileConfig fileConfig){
        for(ConfigEntry<Double> configEntry : Arrays.stream(DelaysConfig.values()).map(DelaysConfig::getEntry).toList()){
            configEntry.resetValue();
        }
        saveSettingsToFile(fileConfig);
    }

     static void saveSettingsToFile(CommentedFileConfig fileConfig){
        for(ConfigEntry<Double> entry : Arrays.stream(DelaysConfig.values()).map(DelaysConfig::getEntry).toList()){
            fileConfig.set(TABLE_NAME + "." + entry.getName(), entry.getValue());
            String entryComment = entry.getComment();
            if(entryComment != null) fileConfig.setComment(TABLE_NAME + "." + entry.getName(), entryComment);
        }
        fileConfig.setComment(TABLE_NAME, TABLE_COMMENT);
    }

     static void loadSettingsToMemory(CommentedFileConfig fileConfig){
        for(ConfigEntry<Double> configEntry : Arrays.stream(DelaysConfig.values()).map(DelaysConfig::getEntry).toList()){
            Object value = fileConfig.getOrElse(TABLE_NAME + "." + configEntry.getName(), configEntry.getDefaultValue());
            if(value instanceof Number numberValue){
                configEntry.setValue(numberValue.doubleValue());
            } else {
                CreeperHealing.LOGGER.error("Invalid value in config file for setting: " + configEntry.getName());
            }
        }
    }

}

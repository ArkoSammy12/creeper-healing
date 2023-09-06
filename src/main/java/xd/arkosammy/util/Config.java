package xd.arkosammy.util;

import xd.arkosammy.CreeperHealing;

import java.util.HashMap;
import java.util.Map;

import static xd.arkosammy.CreeperHealing.LOGGER;
import static xd.arkosammy.CreeperHealing.CONFIG;

public class Config {


    private static boolean daytimeHealing = false;
    private static double explosionHealDelay = 3;
    private static double blockPlacementDelay = 1;
    private static boolean requiresLight = false;
    private static boolean shouldHealOnFlowingWater = true;
    private static boolean shouldHealOnFlowingLava = true;
    private static boolean shouldPlaySoundOnBlockPlacement = true;
    private static HashMap<String, String> replaceMap = new HashMap<>();

    public static void setExplosionHealDelay(double explosionHealDelay){
        Config.explosionHealDelay = explosionHealDelay;
        CONFIG.set("delays.explosion_heal_delay", explosionHealDelay);
    }

    public static void setBlockPlacementDelay(double blockPlacementDelay){
        Config.blockPlacementDelay = blockPlacementDelay;
        CONFIG.set("delays.block_placement_delay", blockPlacementDelay);
    }

    public static void setRequiresLight(boolean requiresLight){
        Config.requiresLight = requiresLight;
        CONFIG.set("preferences.requires_light", requiresLight);
    }

    public static void setShouldHealOnFlowingWater(boolean shouldHealOnFlowingWater){
        Config.shouldHealOnFlowingWater = shouldHealOnFlowingWater;
        CONFIG.set("preferences.heal_on_flowing_water", shouldHealOnFlowingWater);
    }

    public static void setShouldHealOnFlowingLava(boolean shouldHealOnFlowingLava){
        Config.shouldHealOnFlowingLava = shouldHealOnFlowingLava;
        CONFIG.set("preferences.heal_on_flowing_lava", shouldHealOnFlowingLava);
    }

    public static void setShouldPlaySoundOnBlockPlacement(boolean shouldPlaySoundOnBlockPlacement) {
        Config.shouldPlaySoundOnBlockPlacement = shouldPlaySoundOnBlockPlacement;
        CONFIG.set("preferences.block_placement_sound_effect", shouldPlaySoundOnBlockPlacement);
    }

    public static void setDaytimeHealing(boolean daytimeHealing){
        Config.daytimeHealing = daytimeHealing;
        CONFIG.set("mode.daytime_healing_mode", daytimeHealing);
    }

    public static long getExplosionDelay(){
        return Math.round(Math.max(explosionHealDelay, 0) * 20L) == 0 ? 20L : Math.round(Math.max(explosionHealDelay, 0) * 20L);
    }

    public static long getBlockPlacementDelay(){
        return Math.round(Math.max(blockPlacementDelay, 0) * 20L) == 0 ? 20L : Math.round(Math.max(blockPlacementDelay, 0) * 20L);
    }

    public static HashMap<String, String> getReplaceList(){
        return replaceMap;
    }

    public static boolean getRequiresLight(){
        return requiresLight;
    }

    public static boolean shouldHealOnFlowingWater(){
        return shouldHealOnFlowingWater;
    }

    public static boolean shouldHealOnFlowingLava(){
        return shouldHealOnFlowingLava;
    }

    public static boolean shouldPlaySoundOnBlockPlacement(){
        return shouldPlaySoundOnBlockPlacement;

    }

    public static boolean isDaytimeHealingEnabled(){
        return daytimeHealing;
    }

    public static void writeFreshConfig(){

        LOGGER.info("Writing a default configuration file...");

        explosionHealDelay = 3;
        blockPlacementDelay = 1;
        shouldHealOnFlowingWater = true;
        shouldHealOnFlowingLava = true;
        shouldPlaySoundOnBlockPlacement = true;
        requiresLight = false;
        daytimeHealing = false;
        replaceMap.clear();
        replaceMap.put("minecraft:diamond_block", "minecraft:stone");

        writeAllNewToConfig(); // Populate the FileConfig with your configuration values


    }

    public static void readConfig() {

        readAllFromConfig(); // Extract your configuration values from FileConfig

        if(Math.round(Math.max(Config.getExplosionDelay(), 0) * 20L) == 0) LOGGER.warn("Explosion heal delay set to a very low value in the config file. A value of 1 second will be used instead. Please set a valid value in the config file");
        if(Math.round(Math.max(Config.getBlockPlacementDelay(), 0) * 20L) == 0) LOGGER.warn("Block placement delay set to a very low value in the config file. A value of 1 second will be used instead. Please set a valid value in the config file");

        LOGGER.info("Applied custom configs");

    }

    public static void updateConfig() {

        if(CreeperHealing.CONFIG_FILE_PATH.exists()) {

            setAllToConfig(); // Extract your configuration values from FileConfig

        } else {

            writeFreshConfig();

        }

    }

    public static boolean reloadConfig() {

        if (CreeperHealing.CONFIG_FILE_PATH.exists()) {

            readAllFromConfig(); // Extract your configuration values from FileConfig
            return true;

        }

        writeFreshConfig();

        return false;
    }

    private static void writeAllNewToConfig() {

        CONFIG.add("mode.daytime_healing_mode", daytimeHealing);

        CONFIG.add("delays.explosion_heal_delay", explosionHealDelay);
        CONFIG.add("delays.block_placement_delay", blockPlacementDelay);

        CONFIG.add("preferences.requires_light", requiresLight);
        CONFIG.add("preferences.heal_on_flowing_water", shouldHealOnFlowingWater);
        CONFIG.add("preferences.heal_on_flowing_lava", shouldHealOnFlowingLava);
        CONFIG.add("preferences.block_placement_sound_effect", shouldPlaySoundOnBlockPlacement);

        // To handle the replace_list hashmap, iterate through it and set individual entries
        for (Map.Entry<String, String> entry : replaceMap.entrySet()) {
            CONFIG.add("replace_list." + entry.getKey(), entry.getValue());
        }

        CONFIG.save();
    }

    private static void setAllToConfig() {

        CONFIG.set("mode.daytime_healing_mode", daytimeHealing);

        CONFIG.set("delays.explosion_heal_delay", explosionHealDelay);
        CONFIG.set("delays.block_placement_delay", blockPlacementDelay);

        CONFIG.set("preferences.requires_light", requiresLight);
        CONFIG.set("preferences.heal_on_flowing_water", shouldHealOnFlowingWater);
        CONFIG.set("preferences.heal_on_flowing_lava", shouldHealOnFlowingLava);
        CONFIG.set("preferences.block_placement_sound_effect", shouldPlaySoundOnBlockPlacement);

        // To handle the replace_list hashmap, iterate through it and set individual entries
        for (Map.Entry<String, String> entry : replaceMap.entrySet()) {
            CONFIG.set("replace_list." + entry.getKey(), entry.getValue());
        }

        CONFIG.save();
    }

    private static void readAllFromConfig() {
        CONFIG.load();
        daytimeHealing = CONFIG.getOrElse("daytime_healing_mode", false);
        explosionHealDelay = CONFIG.getOrElse("explosion_heal_delay", 3);
        blockPlacementDelay = CONFIG.getOrElse("block_placement_delay", 1);
        requiresLight = CONFIG.getOrElse("requires_light", false);
        shouldHealOnFlowingWater = CONFIG.getOrElse("heal_on_flowing_water", true);
        shouldHealOnFlowingLava = CONFIG.getOrElse("heal_on_flowing_lava", true);
        shouldPlaySoundOnBlockPlacement = CONFIG.getOrElse("block_placement_sound_effect", true);

        if(CONFIG.contains("replace_list")) {

            // Create a temporary map to store new entries
            Map<String, String> tempReplaceMap = new HashMap<>();

            // To handle the replace_list hashmap, iterate through the entries and populate the temporary map
            com.electronwill.nightconfig.core.Config replaceListConfig = CONFIG.get("replace_list");

            for (com.electronwill.nightconfig.core.Config.Entry entry : replaceListConfig.entrySet()) {
                if (entry.getValue() instanceof String) {
                    tempReplaceMap.put(entry.getKey(), entry.getValue());

                    LOGGER.info("Key: " + entry.getKey() + ". " + "Value: " + entry.getValue());

                }
            }

            // Update the replaceMap after the iteration is complete
            replaceMap.clear();
            replaceMap.putAll(tempReplaceMap);

        } else {

            replaceMap.clear();
            replaceMap.put("minecraft:diamond_block", "minecraft:stone");

            for (Map.Entry<String, String> entry : replaceMap.entrySet()) {
                CONFIG.set("replace_list." + entry.getKey(), entry.getValue());
            }

        }

        CONFIG.save();

    }
}


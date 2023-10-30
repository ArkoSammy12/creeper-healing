package xd.arkosammy.configuration.tables;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import xd.arkosammy.CreeperHealing;
import xd.arkosammy.configuration.ConfigEntry;
import java.util.ArrayList;
import java.util.List;

public final class PreferencesConfig {

    private PreferencesConfig(){}
    private static final List<ConfigEntry<Boolean>> preferencesEntryList = new ArrayList<>();
    private static final String TABLE_NAME = "preferences";
    private static final String TABLE_COMMENT = """
            Toggleable settings to customize the healing of explosions.""";

    static {

        preferencesEntryList.add(new ConfigEntry<>("heal_on_flowing_water", true, """
                (Default = true) Whether or not blocks should be healed where there is currently flowing water."""));

        preferencesEntryList.add(new ConfigEntry<>("heal_on_source_water", false, """
                (Default = false) Whether or not blocks should healed where there is currently a source water block."""));

        preferencesEntryList.add(new ConfigEntry<>("heal_on_flowing_lava", true, """
                (Default = true) Whether or not blocks should be healed where there is currently flowing lava."""));

        preferencesEntryList.add(new ConfigEntry<>("heal_on_source_lava", false, """
                (Default = false) Whether or not blocks should be healed where there is currently a source lava block."""));

        preferencesEntryList.add(new ConfigEntry<>("block_placement_sound_effect", true, """
                (Default = true) Whether or not a block heal should play a sound effect."""));

        preferencesEntryList.add(new ConfigEntry<>("heal_on_healing_potion_splash", true, """
                (Default = true) Makes explosion heal immediately upon throwing a splash potion of Healing on them."""));

        preferencesEntryList.add(new ConfigEntry<>("heal_on_regeneration_potion_splash", true, """
                (Default = true) Makes explosion start their healing process upon throwing a splash potion of Regeneration of them.
                This option only modifies the heal delay of the explosion and only affects explosions created with the default healing mode."""));

        preferencesEntryList.add(new ConfigEntry<>("enable_whitelist", false, """
                (Default = false) Enable or disable the usage of the whitelist"""));

    }

    private static List<ConfigEntry<Boolean>> getPreferencesEntryList(){
        return preferencesEntryList;
    }

    public static void setHealOnFlowingWater(boolean healOnFlowingWater){
        for(ConfigEntry<Boolean> configEntry : getPreferencesEntryList()){
            if(configEntry.getName().equals("heal_on_flowing_water")){
                configEntry.setValue(healOnFlowingWater);
            }
        }
    }

    public static void setHealOnSourceWater(boolean healOnSourceWater){
        for(ConfigEntry<Boolean> configEntry : getPreferencesEntryList()){
            if(configEntry.getName().equals("heal_on_source_water")){
                configEntry.setValue(healOnSourceWater);
            }
        }
    }

    public static void setHealOnFlowingLava(boolean healOnFlowingLava){
        for(ConfigEntry<Boolean> configEntry : getPreferencesEntryList()){
            if(configEntry.getName().equals("heal_on_flowing_lava")){
                configEntry.setValue(healOnFlowingLava);
            }
        }
    }

    public static void setHealOnSourceLava(boolean healOnSourceLava){
        for(ConfigEntry<Boolean> configEntry : getPreferencesEntryList()){
            if(configEntry.getName().equals("heal_on_source_lava")){
                configEntry.setValue(healOnSourceLava);
            }
        }
    }

    public static void setBlockPlacementSoundEffect(boolean blockPlacementSoundEffect){
        for(ConfigEntry<Boolean> configEntry : getPreferencesEntryList()){
            if(configEntry.getName().equals("block_placement_sound_effect")){
                configEntry.setValue(blockPlacementSoundEffect);
            }
        }
    }

    public static void setHealOnHealingPotionSplash(boolean healOnHealingPotionSplash){
        for(ConfigEntry<Boolean> configEntry : getPreferencesEntryList()){
            if(configEntry.getName().equals("heal_on_healing_potion_splash")){
                configEntry.setValue(healOnHealingPotionSplash);
            }
        }
    }

    public static void setHealOnRegenerationPotionSplash(boolean healOnRegenerationPotionSplash){
        for(ConfigEntry<Boolean> configEntry : getPreferencesEntryList()){
            if(configEntry.getName().equals("heal_on_regeneration_potion_splash")){
                configEntry.setValue(healOnRegenerationPotionSplash);
            }
        }
    }

    public static void setEnableWhitelist(boolean enableWhitelist){
        CreeperHealing.LOGGER.info("Incoming value: " + enableWhitelist);
        for(ConfigEntry<Boolean> configEntry : getPreferencesEntryList()){
            if(configEntry.getName().equals("enable_whitelist")){
                configEntry.setValue(enableWhitelist);
                CreeperHealing.LOGGER.info("Set " + configEntry.getValue() + " for " + configEntry.getName());
            }
        }
    }

    public static Boolean getHealOnFlowingWater(){
        Boolean boolToReturn = ConfigEntry.getValueForNameFromMemory("heal_on_flowing_water", getPreferencesEntryList());
        if(boolToReturn == null) return true;
        return boolToReturn;
    }

    public static Boolean getHealOnSourceWater(){
        Boolean boolToReturn = ConfigEntry.getValueForNameFromMemory("heal_on_source_water", getPreferencesEntryList());
        if(boolToReturn == null) return false;
        return boolToReturn;
    }

    public static Boolean getHealOnFlowingLava(){
        Boolean boolToReturn = ConfigEntry.getValueForNameFromMemory("heal_on_flowing_lava", getPreferencesEntryList());
        if(boolToReturn == null) return true;
        return boolToReturn;
    }

    public static Boolean getHealOnSourceLava(){
        Boolean boolToReturn = ConfigEntry.getValueForNameFromMemory("heal_on_source_lava", getPreferencesEntryList());
        if(boolToReturn == null) return false;
        return boolToReturn;
    }

    public static Boolean getBlockPlacementSoundEffect(){
        Boolean boolToReturn = ConfigEntry.getValueForNameFromMemory("block_placement_sound_effect", getPreferencesEntryList());
        if(boolToReturn == null) return true;
        return boolToReturn;
    }

    public static Boolean getHealOnHealingPotionSplash(){
        Boolean boolToReturn = ConfigEntry.getValueForNameFromMemory("heal_on_healing_potion_splash", getPreferencesEntryList());
        if(boolToReturn == null) return true;
        return boolToReturn;
    }

    public static Boolean getHealOnRegenerationPotionSplash(){
        Boolean boolToReturn = ConfigEntry.getValueForNameFromMemory("heal_on_regeneration_potion_splash", getPreferencesEntryList());
        if(boolToReturn == null) return true;
        return boolToReturn;
    }

    public static Boolean getEnableWhitelist(){
        Boolean boolToReturn = ConfigEntry.getValueForNameFromMemory("enable_whitelist", getPreferencesEntryList());
        if(boolToReturn == null) return false;
        return boolToReturn;
    }

    public static void saveToFileWithDefaultValues(CommentedFileConfig fileConfig){

        for(ConfigEntry<Boolean> configEntry : getPreferencesEntryList()){
            configEntry.resetValue();
        }

        saveSettingsToFile(fileConfig);

    }

    public static void saveSettingsToFile(CommentedFileConfig fileConfig){

        for(ConfigEntry<Boolean> entry : getPreferencesEntryList()){

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
        for(ConfigEntry<Boolean> configEntry : getPreferencesEntryList()){
            Object value = fileConfig.getOrElse(TABLE_NAME + "." + configEntry.getName(), configEntry.getDefaultValue());
            if(value instanceof Boolean boolValue){
                configEntry.setValue(boolValue);
            } else {
                CreeperHealing.LOGGER.error("Invalid value in config file for setting: " + configEntry.getName());
            }
        }
    }

}

package xd.arkosammy.configuration.tables;


import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import xd.arkosammy.CreeperHealing;
import xd.arkosammy.configuration.ConfigEntry;

import java.util.ArrayList;
import java.util.List;

public final class ExplosionSourceConfig {

    private ExplosionSourceConfig(){}
    private static final List<ConfigEntry<Boolean>> explosionSourceEntryList = new ArrayList<>();
    private static final String TABLE_NAME = "explosion_sources";
    private static final String TABLE_COMMENT = """
            Configure which explosions are allowed to heal.""";

    static {

        explosionSourceEntryList.add(new ConfigEntry<>("heal_creeper_explosions", true, """
                (Default = true) Heal explosions caused by Creepers."""));

        explosionSourceEntryList.add(new ConfigEntry<>("heal_ghast_explosions", false, """
                (Default = false) Heal explosions caused by Ghasts."""));

        explosionSourceEntryList.add(new ConfigEntry<>("heal_wither_explosions", false, """
                (Default = false) Heal explosions caused by Withers."""));

        explosionSourceEntryList.add(new ConfigEntry<>("heal_tnt_explosions", false, """
                (Default = false) Heal explosions caused by TNT blocks."""));

        explosionSourceEntryList.add(new ConfigEntry<>("heal_tnt_minecart_explosions", false, """
                (Default = false) Heal explosions caused by TNT minecarts."""));

        explosionSourceEntryList.add(new ConfigEntry<>("heal_bed_and_respawn_anchor_explosions", false, """
                (Default = false) Heal explosions caused by beds and respawn anchors."""));

        explosionSourceEntryList.add(new ConfigEntry<>("heal_end_crystal_explosions", false, """
                (Default = false) Heal explosions caused by end crystals."""));

    }

    private static List<ConfigEntry<Boolean>> getExplosionSourceEntryList(){
        return explosionSourceEntryList;
    }

    public static void setHealCreeperExplosions(boolean healCreeperExplosions){
        for(ConfigEntry<Boolean> configEntry : getExplosionSourceEntryList()){
            if(configEntry.getName().equals("heal_creeper_explosions")){
                configEntry.setValue(healCreeperExplosions);
            }
        }
    }

    public static void setHealGhastExplosions(boolean healGhastExplosions){
        for(ConfigEntry<Boolean> configEntry : getExplosionSourceEntryList()){
            if(configEntry.getName().equals("heal_ghast_explosions")){
                configEntry.setValue(healGhastExplosions);
            }
        }
    }

    public static void setHealWitherExplosions(boolean healWitherExplosions){
        for(ConfigEntry<Boolean> configEntry : getExplosionSourceEntryList()){
            if(configEntry.getName().equals("heal_wither_explosions")){
                configEntry.setValue(healWitherExplosions);
            }
        }
    }

    public static void setHealTNTExplosions(boolean healTNTExplosions){
        for(ConfigEntry<Boolean> configEntry : getExplosionSourceEntryList()){
            if(configEntry.getName().equals("heal_tnt_explosions")){
                configEntry.setValue(healTNTExplosions);
            }
        }
    }

    public static void setHealTNTMinecartExplosions(boolean healTNTMinecartExplosions){
        for(ConfigEntry<Boolean> configEntry : getExplosionSourceEntryList()){
            if(configEntry.getName().equals("heal_tnt_minecart_explosions")){
                configEntry.setValue(healTNTMinecartExplosions);
            }
        }
    }

    public static void setHealBedAndRespawnAnchorExplosions(boolean healBedAndRespawnAnchorExplosions){
        for(ConfigEntry<Boolean> configEntry : getExplosionSourceEntryList()){
            if(configEntry.getName().equals("heal_bed_and_respawn_anchor_explosions")){
                configEntry.setValue(healBedAndRespawnAnchorExplosions);
            }
        }
    }

    public static void setHealEndCrystalExplosions(boolean healEndCrystalExplosions){
        for(ConfigEntry<Boolean> configEntry : getExplosionSourceEntryList()){
        if(configEntry.getName().equals("heal_end_crystal_explosions")) {
                configEntry.setValue(healEndCrystalExplosions);
            }
        }
    }

    public static Boolean getHealCreeperExplosions(){
        Boolean boolToReturn = ConfigEntry.getValueForNameFromMemory("heal_creeper_explosions", getExplosionSourceEntryList());
        if(boolToReturn == null) return true;
        return boolToReturn;
    }

    public static Boolean getHealGhastExplosions(){
        Boolean boolToReturn = ConfigEntry.getValueForNameFromMemory("heal_ghast_explosions", getExplosionSourceEntryList());
        if(boolToReturn == null) return false;
        return boolToReturn;
    }

    public static Boolean getHealWitherExplosions(){
        Boolean boolToReturn = ConfigEntry.getValueForNameFromMemory("heal_wither_explosions", getExplosionSourceEntryList());
        if(boolToReturn == null) return false;
        return boolToReturn;
    }

    public static Boolean getHealTNTExplosions(){
        Boolean boolToReturn = ConfigEntry.getValueForNameFromMemory("heal_tnt_explosions", getExplosionSourceEntryList());
        if(boolToReturn == null) return false;
        return boolToReturn;
    }

    public static Boolean getHealTNTMinecartExplosions(){
        Boolean boolToReturn = ConfigEntry.getValueForNameFromMemory("heal_tnt_minecart_explosions", getExplosionSourceEntryList());
        if(boolToReturn == null) return false;
        return boolToReturn;
    }

    public static Boolean getHealBedAndRespawnAnchorExplosions(){
        Boolean boolToReturn = ConfigEntry.getValueForNameFromMemory("heal_bed_and_respawn_anchor_explosions", getExplosionSourceEntryList());
        if(boolToReturn == null) return false;
        return boolToReturn;
    }

    public static Boolean getHealEndCrystalExplosions(){
        Boolean boolToReturn = ConfigEntry.getValueForNameFromMemory("heal_end_crystal_explosions", getExplosionSourceEntryList());
        if(boolToReturn == null) return false;
        return boolToReturn;
    }

    public static void saveToFileWithDefaultValues(CommentedFileConfig fileConfig){
        for(ConfigEntry<Boolean> configEntry : getExplosionSourceEntryList()){
            configEntry.resetValue();
        }
        saveSettingsToFile(fileConfig);
    }

    public static void saveSettingsToFile(CommentedFileConfig fileConfig){
        for(ConfigEntry<Boolean> entry : getExplosionSourceEntryList()){
            fileConfig.set(TABLE_NAME + "." + entry.getName(), entry.getValue());
            String entryComment = entry.getComment();
            if(entryComment != null) fileConfig.setComment(TABLE_NAME + "." + entry.getName(), entryComment);
        }
        fileConfig.setComment(TABLE_NAME, TABLE_COMMENT);
    }

    public static void loadSettingsToMemory(CommentedFileConfig fileConfig){
        for(ConfigEntry<Boolean> configEntry : getExplosionSourceEntryList()){
            Object value = fileConfig.getOrElse(TABLE_NAME + "." + configEntry.getName(), configEntry.getDefaultValue());
            if(value instanceof Boolean boolValue){
                configEntry.setValue(boolValue);
            } else {
                CreeperHealing.LOGGER.error("Invalid value in config file for setting: " + configEntry.getName());
            }
        }
    }

}

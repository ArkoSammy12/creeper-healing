package xd.arkosammy.configuration.tables;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import xd.arkosammy.CreeperHealing;
import xd.arkosammy.configuration.ConfigEntry;

import java.util.ArrayList;
import java.util.List;

public final class ExplosionItemDropConfig{

    private ExplosionItemDropConfig(){}
    private static final List<ConfigEntry<Boolean>> explosionItemDropEntries = new ArrayList<>();
    private static final String TABLE_NAME = "explosion_item_drops";
    private static final String TABLE_COMMENT = """
            These settings allow you to configure whether explosions from specific sources are allowed to drop items or not.""";

    static {

        explosionItemDropEntries.add(new ConfigEntry<>("drop_items_on_creeper_explosions", true, """
                (Default = true) Explosions caused by Creepers will drop items."""));

        explosionItemDropEntries.add(new ConfigEntry<>("drop_items_on_ghast_explosions", true, """
                (Default = true) Explosions caused by Ghasts will drop items."""));

        explosionItemDropEntries.add(new ConfigEntry<>("drop_items_on_wither_explosions", true, """
                (Default = true) Explosions caused by Withers will drop items."""));

        explosionItemDropEntries.add(new ConfigEntry<>("drop_items_on_tnt_explosions", true, """
                (Default = true) Explosions caused by TNT will drop items."""));

        explosionItemDropEntries.add(new ConfigEntry<>("drop_items_on_tnt_minecart_explosions", true, """
                (Default = true) Explosions caused by TNT minecarts will drop items."""));

        explosionItemDropEntries.add(new ConfigEntry<>("drop_items_on_bed_and_respawn_anchor_explosions", true, """
                (Default = true) Explosions caused by beds and respawn anchors will drop items."""));

        explosionItemDropEntries.add(new ConfigEntry<>("drop_items_on_end_crystal_explosions", true, """
                (Default = true) Explosions caused by end crystals will drop items."""));

    }

    private static List<ConfigEntry<Boolean>> getExplosionItemDropEntries(){
        return explosionItemDropEntries;
    }

    public static void setDropItemsOnCreeperExplosions(boolean dropItemsOnCreeperExplosions){
        for(ConfigEntry<Boolean> configEntry : getExplosionItemDropEntries()){
            if(configEntry.getName().equals("drop_items_on_creeper_explosions")){
                configEntry.setValue(dropItemsOnCreeperExplosions);
            }
        }
    }

    public static void setDropItemsOnGhastExplosions(boolean dropItemsOnGhastExplosions){
        for(ConfigEntry<Boolean> configEntry : getExplosionItemDropEntries()){
            if(configEntry.getName().equals("drop_items_on_ghast_explosions")){
                configEntry.setValue(dropItemsOnGhastExplosions);
            }
        }
    }

    public static void setDropItemsOnWitherExplosions(boolean dropItemsOnWitherExplosions){
        for(ConfigEntry<Boolean> configEntry : getExplosionItemDropEntries()){
            if(configEntry.getName().equals("drop_items_on_wither_explosions")){
                configEntry.setValue(dropItemsOnWitherExplosions);
            }
        }
    }

    public static void setDropItemsOnTNTExplosions(boolean dropItemsOnTNTExplosions){
        for(ConfigEntry<Boolean> configEntry : getExplosionItemDropEntries()){
            if(configEntry.getName().equals("drop_items_on_tnt_explosions")){
                configEntry.setValue(dropItemsOnTNTExplosions);
            }
        }
    }

    public static void setDropItemsOnTNTMinecartExplosions(boolean dropItemsOnTNTMinecartExplosions){
        for(ConfigEntry<Boolean> configEntry : getExplosionItemDropEntries()){
            if(configEntry.getName().equals("drop_items_on_tnt_minecart_explosions")){
                configEntry.setValue(dropItemsOnTNTMinecartExplosions);
            }
        }
    }

    public static void setDropItemsOnBedAndRespawnAnchorExplosions(boolean dropItemsOnBedAndRespawnAnchorExplosions){
        for(ConfigEntry<Boolean> configEntry : getExplosionItemDropEntries()){
            if(configEntry.getName().equals("drop_items_on_bed_and_respawn_anchor_explosions")){
                configEntry.setValue(dropItemsOnBedAndRespawnAnchorExplosions);
            }
        }
    }

    public static void setDropItemsOnEndCrystalExplosions(boolean dropItemsOnEndCrystalExplosions){
        for(ConfigEntry<Boolean> configEntry : getExplosionItemDropEntries()){
            if(configEntry.getName().equals("drop_items_on_end_crystal_explosions")){
                configEntry.setValue(dropItemsOnEndCrystalExplosions);
            }
        }
    }

    public static Boolean getDropItemsOnCreeperExplosions(){
        Boolean boolToReturn = ConfigEntry.getValueForNameFromMemory("drop_items_on_creeper_explosions", getExplosionItemDropEntries());
        if(boolToReturn == null) return true;
        return boolToReturn;
    }

    public static Boolean getDropItemsOnGhastExplosions(){
        Boolean boolToReturn = ConfigEntry.getValueForNameFromMemory("drop_items_on_ghast_explosions", getExplosionItemDropEntries());
        if(boolToReturn == null) return true;
        return boolToReturn;
    }

    public static Boolean getDropItemsOnWitherExplosions(){
        Boolean boolToReturn = ConfigEntry.getValueForNameFromMemory("drop_items_on_wither_explosions", getExplosionItemDropEntries());
        if(boolToReturn == null) return true;
        return boolToReturn;
    }

    public static Boolean getDropItemsOnTNTExplosions(){
        Boolean boolToReturn = ConfigEntry.getValueForNameFromMemory("drop_items_on_tnt_explosions", getExplosionItemDropEntries());
        if(boolToReturn == null) return true;
        return boolToReturn;
    }

    public static Boolean getDropItemsOnTNTMinecartExplosions(){
        Boolean boolToReturn = ConfigEntry.getValueForNameFromMemory("drop_items_on_tnt_minecart_explosions", getExplosionItemDropEntries());
        if(boolToReturn == null) return true;
        return boolToReturn;
    }

    public static Boolean getDropItemsOnBedAndRespawnAnchorExplosions(){
        Boolean boolToReturn = ConfigEntry.getValueForNameFromMemory("drop_items_on_bed_and_respawn_anchor_explosions", getExplosionItemDropEntries());
        if(boolToReturn == null) return true;
        return boolToReturn;
    }

    public static Boolean getDropItemsOnEndCrystalExplosions(){
        Boolean boolToReturn = ConfigEntry.getValueForNameFromMemory("drop_items_on_end_crystal_explosions", getExplosionItemDropEntries());
        if(boolToReturn == null) return true;
        return boolToReturn;
    }

    public static void saveToFileWithDefaultValues(CommentedFileConfig fileConfig){
        for(ConfigEntry<Boolean> configEntry : getExplosionItemDropEntries()){
            configEntry.resetValue();
        }
        saveSettingsToFile(fileConfig);
    }

    public static void saveSettingsToFile(CommentedFileConfig fileConfig){
        for(ConfigEntry<Boolean> entry : getExplosionItemDropEntries()){
            fileConfig.set(TABLE_NAME + "." + entry.getName(), entry.getValue());
            String entryComment = entry.getComment();
            if(entryComment != null) fileConfig.setComment(TABLE_NAME + "." + entry.getName(), entryComment);
        }
        fileConfig.setComment(TABLE_NAME, TABLE_COMMENT);
    }

    public static void loadSettingsToMemory(CommentedFileConfig fileConfig){
        for(ConfigEntry<Boolean> configEntry : getExplosionItemDropEntries()){
            Object value = fileConfig.getOrElse(TABLE_NAME + "." + configEntry.getName(), configEntry.getDefaultValue());
            if(value instanceof Boolean boolValue){
                configEntry.setValue(boolValue);
            } else {
                CreeperHealing.LOGGER.error("Invalid value in config file for setting: " + configEntry.getName());
            }
        }
    }

}

package xd.arkosammy.creeperhealing.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import xd.arkosammy.creeperhealing.CreeperHealing;

import java.util.Arrays;

public enum ExplosionItemDropConfig{

    DROP_ITEMS_ON_CREEPER_EXPLOSIONS(new ConfigEntry<>("drop_items_on_creeper_explosions", true, """
                (Default = true) Explosions caused by Creepers will drop items.""")),
    DROP_ITEMS_ON_GHAST_EXPLOSIONS(new ConfigEntry<>("drop_items_on_ghast_explosions", true, """
                (Default = true) Explosions caused by Ghasts will drop items.""")),
    DROP_ITEMS_ON_WITHER_EXPLOSIONS(new ConfigEntry<>("drop_items_on_wither_explosions", true, """
                (Default = true) Explosions caused by Withers will drop items.""")),
    DROP_ITEMS_ON_TNT_EXPLOSIONS(new ConfigEntry<>("drop_items_on_tnt_explosions", true, """
                (Default = true) Explosions caused by TNT will drop items.""")),
    DROP_ITEMS_ON_TNT_MINECART_EXPLOSIONS(new ConfigEntry<>("drop_items_on_tnt_minecart_explosions", true, """
                (Default = true) Explosions caused by TNT minecarts will drop items.""")),
    DROP_ITEMS_ON_BED_AND_RESPAWN_ANCHOR_EXPLOSIONS(new ConfigEntry<>("drop_items_on_bed_and_respawn_anchor_explosions", true, """
                (Default = true) Explosions caused by beds and respawn anchors will drop items.""")),
    DROP_ITEMS_ON_END_CRYSTAL_EXPLOSIONS(new ConfigEntry<>("drop_items_on_end_crystal_explosions", true, """
                (Default = true) Explosions caused by end crystals will drop items."""));

    private final ConfigEntry<Boolean> entry;

    ExplosionItemDropConfig(ConfigEntry<Boolean> entry){
        this.entry = entry;
    }

    public ConfigEntry<Boolean> getEntry(){
        return this.entry;
    }
    private static final String TABLE_NAME = "explosion_item_drops";
    private static final String TABLE_COMMENT = """
            Toggle whether certain explosion should drop items.""";

    static void setDefaultValues(CommentedFileConfig fileConfig){
        for(ConfigEntry<Boolean> configEntry : Arrays.stream(ExplosionItemDropConfig.values()).map(ExplosionItemDropConfig::getEntry).toList()){
            configEntry.resetValue();
        }
        setValues(fileConfig);
    }

    static void setValues(CommentedFileConfig fileConfig){
        for(ConfigEntry<Boolean> entry : Arrays.stream(ExplosionItemDropConfig.values()).map(ExplosionItemDropConfig::getEntry).toList()){
            fileConfig.set(TABLE_NAME + "." + entry.getName(), entry.getValue());
            String entryComment = entry.getComment();
            if(entryComment != null) fileConfig.setComment(TABLE_NAME + "." + entry.getName(), entryComment);
        }
        fileConfig.setComment(TABLE_NAME, TABLE_COMMENT);
        fileConfig.<CommentedConfig>get(TABLE_NAME).entrySet().removeIf(entry -> !isEntryKeyInEnum(entry.getKey()));
    }

    static void getValues(CommentedFileConfig fileConfig){
        for(ConfigEntry<Boolean> configEntry : Arrays.stream(ExplosionItemDropConfig.values()).map(ExplosionItemDropConfig::getEntry).toList()){
            Object value = fileConfig.getOrElse(TABLE_NAME + "." + configEntry.getName(), configEntry.getDefaultValue());
            if(value instanceof Boolean boolValue){
                configEntry.setValue(boolValue);
            } else {
                CreeperHealing.LOGGER.error("Invalid value in config file for setting: " + configEntry.getName());
            }
        }
    }

    private static boolean isEntryKeyInEnum(String key){
        return Arrays.stream(ExplosionItemDropConfig.values()).anyMatch(configEntry -> configEntry.getEntry().getName().equals(key));
    }
}

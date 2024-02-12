package xd.arkosammy.creeperhealing.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import xd.arkosammy.creeperhealing.CreeperHealing;

import java.util.Arrays;

public enum ExplosionSourceConfig {

    HEAL_CREEPER_EXPLOSIONS(new ConfigEntry<>("heal_creeper_explosions", true, """
                (Default = true) Heal explosions caused by Creepers.""")),
    HEAL_GHAST_EXPLOSIONS(new ConfigEntry<>("heal_ghast_explosions", false, """
                (Default = false) Heal explosions caused by Ghasts.""")),
    HEAL_WITHER_EXPLOSIONS(new ConfigEntry<>("heal_wither_explosions", false, """
                (Default = false) Heal explosions caused by Withers.""")),
    HEAL_TNT_EXPLOSIONS(new ConfigEntry<>("heal_tnt_explosions", false, """
                (Default = false) Heal explosions caused by TNT blocks.""")),
    HEAL_TNT_MINECART_EXPLOSIONS(new ConfigEntry<>("heal_tnt_minecart_explosions", false, """
                (Default = false) Heal explosions caused by TNT minecarts.""")),
    HEAL_BED_AND_RESPAWN_ANCHOR_EXPLOSIONS(new ConfigEntry<>("heal_bed_and_respawn_anchor_explosions", false, """
                (Default = false) Heal explosions caused by beds and respawn anchors.""")),
    HEAL_END_CRYSTAL_EXPLOSIONS(new ConfigEntry<>("heal_end_crystal_explosions", false, """
                (Default = false) Heal explosions caused by End Crystals."""));

    private final ConfigEntry<Boolean> entry;

    ExplosionSourceConfig(ConfigEntry<Boolean> entry){
        this.entry = entry;
    }

    public ConfigEntry<Boolean> getEntry(){
        return this.entry;
    }

    private static final String TABLE_NAME = "explosion_sources";
    private static final String TABLE_COMMENT = """
            Configure which explosions are allowed to heal.""";

    static void setDefaultValues(CommentedFileConfig fileConfig){
        for(ConfigEntry<Boolean> configEntry : Arrays.stream(ExplosionSourceConfig.values()).map(ExplosionSourceConfig::getEntry).toList()){
            configEntry.resetValue();
        }
        setValues(fileConfig);
    }

    static void setValues(CommentedFileConfig fileConfig){
        for(ConfigEntry<Boolean> entry : Arrays.stream(ExplosionSourceConfig.values()).map(ExplosionSourceConfig::getEntry).toList()){
            fileConfig.set(TABLE_NAME + "." + entry.getName(), entry.getValue());
            String entryComment = entry.getComment();
            if(entryComment != null) fileConfig.setComment(TABLE_NAME + "." + entry.getName(), entryComment);
        }
        fileConfig.setComment(TABLE_NAME, TABLE_COMMENT);
        fileConfig.<CommentedConfig>get(TABLE_NAME).entrySet().removeIf(entry -> !isEntryKeyInEnum(entry.getKey()));
    }

    static void getValues(CommentedFileConfig fileConfig){
        for(ConfigEntry<Boolean> configEntry : Arrays.stream(ExplosionSourceConfig.values()).map(ExplosionSourceConfig::getEntry).toList()){
            Object value = fileConfig.getOrElse(TABLE_NAME + "." + configEntry.getName(), configEntry.getDefaultValue());
            if(value instanceof Boolean boolValue){
                configEntry.setValue(boolValue);
            } else {
                CreeperHealing.LOGGER.error("Invalid value in config file for setting: " + configEntry.getName());
            }
        }
    }

    private static boolean isEntryKeyInEnum(String key){
        return Arrays.stream(ExplosionSourceConfig.values()).anyMatch(configEntry -> configEntry.getEntry().getName().equals(key));
    }

}

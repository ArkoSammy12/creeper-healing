package xd.arkosammy.configuration.tables;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import xd.arkosammy.CreeperHealing;
import xd.arkosammy.configuration.ConfigEntry;

import java.util.ArrayList;
import java.util.List;

public class PreferencesConfig {

    private static final List<ConfigEntry<Boolean>> preferencesEntryList = new ArrayList<>();

    public static final String COMMENT = """
            Toggleable settings to customize the healing of explosions.""";

    public static final String NAME = "preferences";

    static {

        preferencesEntryList.add(new ConfigEntry<>("heal_on_flowing_water", true, """
                Whether or not blocks should be healed where there is currently flowing water.
                This is true by default."""));

        preferencesEntryList.add(new ConfigEntry<>("heal_on_flowing_lava", true, """
                Whether or not blocks should be healed where there is currently flowing lava.
                This is true by default."""));

        preferencesEntryList.add(new ConfigEntry<>("block_placement_sound_effect", true, """
                Whether or not a block heal should play a sound effect.
                This is true by default."""));

        preferencesEntryList.add(new ConfigEntry<>("drop_items_on_explosions", true, """
                Whether or not creeper explosions should drop items.
                This is true by default."""));

        preferencesEntryList.add(new ConfigEntry<>("requires_light", false, """
                Whether or not explosions will need light to heal.
                This is false by default."""));

    }

    public static void setHealOnFlowingWater(boolean healOnFlowingWater){

        for(ConfigEntry<Boolean> configEntry : getPreferencesEntryList()){

            if(configEntry.getName().equals("heal_on_flowing_water")){

                configEntry.setValue(healOnFlowingWater);

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

    public static void setBlockPlacementSoundEffect(boolean blockPlacementSoundEffect){

        for(ConfigEntry<Boolean> configEntry : getPreferencesEntryList()){

            if(configEntry.getName().equals("block_placement_sound_effect")){

                configEntry.setValue(blockPlacementSoundEffect);

            }

        }

    }

    public static void setDropItemsOnExplosions(boolean dropItemsOnExplosions){

        for(ConfigEntry<Boolean> configEntry : getPreferencesEntryList()){

            if(configEntry.getName().equals("drop_items_on_explosions")){

                configEntry.setValue(dropItemsOnExplosions);

            }

        }

    }

    public static void setRequiresLight(boolean requiresLight){

        for(ConfigEntry<Boolean> configEntry : getPreferencesEntryList()){

            if(configEntry.getName().equals("requires_light")){

                configEntry.setValue(requiresLight);

            }

        }

    }

    public static Boolean getHealOnFlowingWater(){

        Boolean boolToReturn = getValueForEntry("heal_on_flowing_water");

        if(boolToReturn == null) return true;

        return boolToReturn;

    }

    public static Boolean getHealOnFlowingLava(){

        Boolean boolToReturn = getValueForEntry("heal_on_flowing_lava");

        if(boolToReturn == null) return true;

        return boolToReturn;

    }

    public static Boolean getBlockPlacementSoundEffect(){

        Boolean boolToReturn = getValueForEntry("block_placement_sound_effect");

        if(boolToReturn == null) return true;

        return boolToReturn;

    }

    public static Boolean getDropItemsOnExplosions(){

        Boolean boolToReturn = getValueForEntry("drop_items_on_explosions");

        if(boolToReturn == null) return true;

        return boolToReturn;

    }

    public static Boolean getRequiresLight(){

        Boolean boolToReturn = getValueForEntry("requires_light");

        if(boolToReturn == null) return false;

        return boolToReturn;

    }

    public static List<ConfigEntry<Boolean>> getPreferencesEntryList(){
        return preferencesEntryList;
    }

    public static void saveDefaultEntries(CommentedFileConfig fileConfig){

        for(ConfigEntry<Boolean> configEntry : getPreferencesEntryList()){
            configEntry.resetValue();
        }

        saveEntries(fileConfig);

    }

    public static void saveEntries(CommentedFileConfig fileConfig){

        for(ConfigEntry<Boolean> entry : getPreferencesEntryList()){

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

        for(ConfigEntry<Boolean> configEntry : getPreferencesEntryList()){

            configEntry.setValue(fileConfig.getOrElse(NAME + "." + configEntry.getName(), configEntry.getDefaultValue()));

            CreeperHealing.LOGGER.info("Loaded entry: " + configEntry.getName() + " with entry : " + configEntry.getValue());


        }

    }

    public static Boolean getValueForEntry(String entryName){

        for(ConfigEntry<Boolean> entry : getPreferencesEntryList()){

            if(entry.getName().equals(entryName)){

                return entry.getValue();

            }

        }

        return null;

    }

}

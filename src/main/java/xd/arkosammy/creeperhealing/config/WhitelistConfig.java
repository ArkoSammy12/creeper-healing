package xd.arkosammy.creeperhealing.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import xd.arkosammy.creeperhealing.CreeperHealing;

import java.util.ArrayList;
import java.util.List;

public final class WhitelistConfig {

    private WhitelistConfig(){}

    private static final List<String> whitelist = new ArrayList<>();

    private static final String TABLE_NAME = "whitelist";

    private static final String TABLE_COMMENT = """
            Use an optional whitelist to customize which blocks are allowed to heal. To add an entry, specify the block's namespace
            along with its identifier, separated by a colon and enclosed in double quotes, and add it in-between the square brackets below. Separate each entry with a comma.
            Example entries:
            whitelist_entries = ["minecraft:grass",  "minecraft:stone", "minecraft:sand"]""";


    public static List<String> getWhitelist(){
        return whitelist;
    }

    static void setDefaultValues(CommentedFileConfig fileConfig){
        getWhitelist().clear();
        getWhitelist().add("minecraft:placeholder");
        setValues(fileConfig);
    }

    static void setValues(CommentedFileConfig fileConfig){
         if(!getWhitelist().isEmpty()){
             fileConfig.set(TABLE_NAME + "." + "whitelist_entries", getWhitelist());
         } else {
             fileConfig.set(TABLE_NAME + "." + "whitelist_entries", List.of("minecraft:placeholder"));
         }
        fileConfig.setComment(TABLE_NAME, TABLE_COMMENT);
    }

     static void getValues(CommentedFileConfig fileConfig){
        List<Object> list = fileConfig.getOrElse(TABLE_NAME + "." + "whitelist_entries", List.of("minecraft:placeholder"));
        List<String> tempWhitelist = new ArrayList<>();
        if(list != null){
            for(Object entry : list){
                if(entry instanceof String stringEntry){
                    tempWhitelist.add(stringEntry);
                } else {
                    CreeperHealing.LOGGER.error("Found non-string entry in the whitelist. Please make sure to only include string entries in the whitelist.");
                }
            }
            getWhitelist().clear();
            getWhitelist().addAll(tempWhitelist);

        } else {
            CreeperHealing.LOGGER.error("Error attempting to read the whitelist from the config. Whitelist config section not found");
        }
    }

}

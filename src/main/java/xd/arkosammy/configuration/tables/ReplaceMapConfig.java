package xd.arkosammy.configuration.tables;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import xd.arkosammy.CreeperHealing;
import java.util.HashMap;
import java.util.Map;

public class ReplaceMapConfig {

    public static final String COMMENT = """
            Add your own replace settings to configure which blocks should be used to heal other blocks.
            The block on the right will be used to heal the block on the left. Specify the block's namespace along with the block's name identifier.
            Example entry:
            "minecraft:diamond_block" = "minecraft:stone"\s""";

    public static final String NAME = "replace_map";
    private static final HashMap<String, String> replaceMap = new HashMap<>();

    public static HashMap<String, String> getReplaceMap(){
        return  replaceMap;
    }

    public static void saveDefaultEntries(CommentedFileConfig fileConfig){

        replaceMap.clear();

        replaceMap.put("minecraft:diamond_block", "minecraft:stone");

        saveEntries(fileConfig);


    }

    public static void saveEntries(CommentedFileConfig fileConfig){

        for(Map.Entry<String, String> entry : replaceMap.entrySet()){

            fileConfig.set(NAME + "." + entry.getKey(),  entry.getValue());

        }

        fileConfig.setComment(NAME, COMMENT);

    }

    public static void loadEntries(CommentedFileConfig fileConfig){

        CommentedConfig replaceMapConfig = fileConfig.get(NAME);

        HashMap<String, String> tempReplaceMap = new HashMap<>();

        for(CommentedConfig.Entry entry : replaceMapConfig.entrySet()){

            if(entry.getValue() instanceof String) {
                tempReplaceMap.put(entry.getKey(), entry.getValue());
                CreeperHealing.LOGGER.info("Loaded entry: " + entry.getKey() + " with entry : " + tempReplaceMap.get(entry.getKey()));

            }

        }

        replaceMap.clear();
        replaceMap.putAll(tempReplaceMap);

    }


}

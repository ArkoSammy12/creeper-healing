package xd.arkosammy.configuration.tables;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import xd.arkosammy.CreeperHealing;
import java.util.HashMap;
import java.util.Map;

public abstract class ReplaceMapConfig {

    private ReplaceMapConfig(){}
    private static final HashMap<String, String> replaceMap = new HashMap<>();
    private static final String TABLE_NAME = "replace_map";
    private static final String TABLE_COMMENT = """
            Add your own replace settings to configure which blocks should be used to heal other blocks. The block on the right will be used to heal the block on the left.
            Specify the block's namespace along with the block's name identifier, separated by a colon.
            Example entry:
            "minecraft:gold_block" = "minecraft:stone"
            Warning, do not set a block to be replaced with more than one block! For example, the following will cause an error:
            "minecraft:diamond_block" = "minecraft:stone"
            "minecraft:diamond_block" = "minecraft:air"\s""";


    public static HashMap<String, String> getReplaceMap(){
        return  replaceMap;
    }

    public static void saveToFileWithDefaultValues(CommentedFileConfig fileConfig){
        replaceMap.clear();
        replaceMap.put("minecraft:diamond_block", "minecraft:stone");
        saveReplaceMapToFile(fileConfig);
    }

    public static void saveReplaceMapToFile(CommentedFileConfig fileConfig){

        for(Map.Entry<String, String> entry : replaceMap.entrySet()){
            fileConfig.set(TABLE_NAME + "." + entry.getKey(),  entry.getValue());
        }
        fileConfig.setComment(TABLE_NAME, TABLE_COMMENT);

    }

    public static void loadReplaceMapToMemory(CommentedFileConfig fileConfig){

        CommentedConfig replaceMapConfig = fileConfig.get(TABLE_NAME);
        HashMap<String, String> tempReplaceMap = new HashMap<>();

        for(CommentedConfig.Entry entry : replaceMapConfig.entrySet()){

            if(entry.getValue() instanceof String && entry.getKey() != null) {

                tempReplaceMap.put(entry.getKey(), entry.getValue());
                CreeperHealing.LOGGER.info("Loaded entry: " + entry.getKey() + " with value: " + tempReplaceMap.get(entry.getKey()));

            } else if (!(entry.getValue() instanceof String)) {

                CreeperHealing.LOGGER.warn("Invalid value in replace map for key: " + entry.getKey());

            } else if (entry.getKey() == null) {

                CreeperHealing.LOGGER.warn("Invalid key found in replace map.");

            }

        }

        replaceMap.clear();
        replaceMap.putAll(tempReplaceMap);

    }


}

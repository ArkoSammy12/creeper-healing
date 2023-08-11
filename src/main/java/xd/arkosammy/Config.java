package xd.arkosammy;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

//Huge thanks to @_jacg on the Fabric Discord Server for helping me out with setting the config
public class Config {
    @SerializedName("explosion_heal_delay")
    public int explosionHealDelay = 3;

    @SerializedName("block_placement_delay")
    public int blockPlacementDelay = 1;

    @SerializedName("replace_list")
    public HashMap<String, String> replaceMap = new HashMap<>();
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public boolean  writeConfig() {

        Path configPath = FabricLoader.getInstance().getConfigDir().resolve("creeper-healing.json");

        //If no config file is found, write a new one
        if(!Files.exists(configPath)) {

            try {

                //Put a default value into the map then write a new config using the fields of the Config class
                replaceMap.put("minecraft:diamond_block", "minecraft:stone");

                Files.writeString(configPath, GSON.toJson(this));

            } catch (IOException e) {

                throw new RuntimeException(e);

            }

            CreeperHealing.LOGGER.info("Found no preexisting configuration file. Creating a new one and setting default values.");
            CreeperHealing.LOGGER.info("Change the values in the configuration and restart the server to apply them.");

            return true; //Return true if the config file doesn't already exist

        } else {

            return false; //Return false if config file already exists

        }

    }


    public void readConfig(File file) throws IOException{

        FileReader reader = new FileReader(file);

        Gson gson = new Gson();

        //Deserialize our Json file and turn it into a JsonObject
        JsonObject obj = gson.fromJson(reader, JsonObject.class);
        //Set the config fields to the values read from our config
        explosionHealDelay = getIntOrDefault(obj, "explosion_heal_delay", explosionHealDelay);
        blockPlacementDelay = getIntOrDefault(obj, "block_placement_delay", blockPlacementDelay);

        //Parse the JsonObject into a Hashmap
        JsonObject replaceListJson = getJsonObjectOrDefault(obj, "replace_list", new JsonObject());
        replaceMap = gson.fromJson(replaceListJson, HashMap.class);

        reader.close();

    }

    private Integer getIntOrDefault(@NotNull JsonObject obj, String name, Integer def){

        return obj.has(name) ? obj.get(name).getAsInt() : def;

    }

    private JsonObject getJsonObjectOrDefault(@NotNull JsonObject obj, String name, JsonObject def){

        JsonElement element = obj.get(name);

        return element != null && element.isJsonObject() ? element.getAsJsonObject() : def;

    }

}

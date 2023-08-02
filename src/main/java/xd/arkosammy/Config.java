package xd.arkosammy;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Config {

    //TODO: Get Json Array config working

    @SerializedName("explosion_heal_delay")
    public int explosionHealDelay = 5;

    @SerializedName("block_placement_delay")
    public int blockPlacementDelay = 2;

    @SerializedName("replace_list")
    public HashMap<String, String> replaceList;

    private static JsonArray customReplaceArray;

    public void writeConfig(File file) throws IOException {

        JsonObject jsonObject = new JsonObject();



        jsonObject.addProperty("explosion_heal_delay", explosionHealDelay);
        jsonObject.addProperty("block_placement_delay", blockPlacementDelay);

        jsonObject.add("custom_replacements", customReplaceArray);

        GsonBuilder builder = new GsonBuilder();

        builder.setPrettyPrinting();

        Gson gson = builder.create();

        FileWriter writer = new FileWriter(file);

        gson.toJson(jsonObject, writer);

        writer.close();

    }

    public void readConfig(File file) throws IOException{

        FileReader reader = new FileReader(file);

        Gson gson = new Gson();

        JsonObject obj = gson.fromJson(reader, JsonObject.class);

        explosionHealDelay = getIntOrDefault(obj, "explosion_heal_delay", explosionHealDelay);
        blockPlacementDelay = getIntOrDefault(obj, "block_placement_delay", blockPlacementDelay);

        customReplaceArray = getJsonArrayOrDefault(obj, "custom_replacements", new JsonArray());

        reader.close();

    }

    private Integer getIntOrDefault(JsonObject obj, String name, Integer def){

        return obj.has(name) ? obj.get(name).getAsInt() : def;

    }

    private JsonArray getJsonArrayOrDefault(JsonObject obj, String name, JsonArray def){

        JsonElement element = obj.get(name);

        return element != null && element.isJsonArray() ? element.getAsJsonArray() : def;

    }

    public void addReplaceEntry(String originalBlockName, String replacementBlockName, File file) throws IOException {

        replaceList.put(originalBlockName, replacementBlockName);

        customReplaceArray = new JsonArray();

        for (Map.Entry<String, String> entry : replaceList.entrySet()) {

            JsonObject entryObject = new JsonObject();

            entryObject.addProperty("original", entry.getKey());

            entryObject.addProperty("replacement", entry.getValue());

            customReplaceArray.add(entryObject);

        }

        writeConfig(file);
    }

    public void readCustomBlockReplacements(File file) throws IOException {

        JsonParser parser = new JsonParser();
        JsonObject rootObject = parser.parse(new FileReader(file)).getAsJsonObject();

        customReplaceArray = getJsonArrayOrDefault(rootObject, "custom_replacements", new JsonArray());

        for (JsonElement entryElement : customReplaceArray) {
            if (entryElement.isJsonObject()) {
                JsonObject entryObject = entryElement.getAsJsonObject();
                String originalBlockName = entryObject.get("original").getAsString();
                String replacementBlockName = entryObject.get("replacement").getAsString();
                addReplaceEntry(originalBlockName, replacementBlockName, file);
            }
        }

    }


}

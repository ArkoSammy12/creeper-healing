package xd.arkosammy.util;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import xd.arkosammy.CreeperHealing;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

//Huge thanks to @_jacg on the Fabric Discord Server for helping me out with setting the config
public class Config {
    @SerializedName("explosion_heal_delay")
    private double explosionHealDelay = 3;

    @SerializedName("block_placement_delay")
    private double blockPlacementDelay = 1;

    @SerializedName("heal_on_flowing_water")
    private boolean shouldHealOnFlowingWater = true;

    @SerializedName("heal_on_flowing_lava")
    private boolean shouldHealOnFlowingLava = true;

    @SerializedName("block_placement_sound_effect")
    private boolean shouldPlaySoundOnBlockPlacement = true;

    @SerializedName("replace_list")
    private HashMap<String, String> replaceMap = new HashMap<>();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public void setExplosionHealDelay(double explosionHealDelay){

        this.explosionHealDelay = explosionHealDelay;

    }

    public void setBlockPlacementDelay(double blockPlacementDelay){

        this.blockPlacementDelay = blockPlacementDelay;

    }

    public void setShouldHealOnFlowingWater(boolean shouldHealOnFlowingWater){

        this.shouldHealOnFlowingWater = shouldHealOnFlowingWater;

    }

    public void setShouldHealOnFlowingLava(boolean shouldHealOnFlowingLava){

        this.shouldHealOnFlowingLava = shouldHealOnFlowingLava;

    }

    public void setShouldPlaySoundOnBlockPlacement(boolean shouldPlaySoundOnBlockPlacement) {

        this.shouldPlaySoundOnBlockPlacement = shouldPlaySoundOnBlockPlacement;

    }

    public long getExplosionDelay(){

        return Math.round(Math.max(this.explosionHealDelay, 0) * 20L) == 0 ? 20L : Math.round(Math.max(this.explosionHealDelay, 0) * 20L);

    }

    public long getBlockPlacementDelay(){

        return Math.round(Math.max(this.blockPlacementDelay, 0) * 20L) == 0 ? 20L : Math.round(Math.max(this.blockPlacementDelay, 0) * 20L);

    }

    public HashMap<String, String> getReplaceList(){

        return this.replaceMap;

    }

    public boolean shouldHealOnFlowingWater(){

        return this.shouldHealOnFlowingWater;

    }

    public boolean shouldHealOnFlowingLava(){

        return this.shouldHealOnFlowingLava;

    }

    public boolean shouldPlaySoundOnBlockPlacement(){

        return this.shouldPlaySoundOnBlockPlacement;

    }

    private Double getDoubleOrDefault(@NotNull JsonObject obj, String name, Double def){

        return obj.has(name) ? obj.get(name).getAsDouble() : def;

    }

    private boolean getBooleanOrDefault(@NotNull JsonObject obj, String name, Boolean def){

        return obj.has(name) ? obj.get(name).getAsBoolean() : def;

    }

    private JsonObject getJsonObjectOrDefault(@NotNull JsonObject obj, String name, JsonObject def){

        JsonElement element = obj.get(name);

        return element != null && element.isJsonObject() ? element.getAsJsonObject() : def;

    }
    public boolean writeConfig() {

        Path configPath = FabricLoader.getInstance().getConfigDir().resolve("creeper-healing.json");

        //If no config file is found, write a new one
        if(!Files.exists(configPath)) {

            try {

                //Put a default value into the replace list then write a new config using the fields of the Config class
                replaceMap.put("minecraft:diamond_block", "minecraft:stone");

                Files.writeString(configPath, GSON.toJson(this));

            } catch (IOException e) {

                throw new RuntimeException(e);

            }

            CreeperHealing.LOGGER.info("Found no preexisting configuration file. Creating a new one with default values.");
            CreeperHealing.LOGGER.info("Change the values in the config file and restart the server or game to apply them, or use the /creeper-healing reload_config command in-game.");

            //Return true if the config file doesn't already exist
            return true;

        } else {

            //Return false if config file already exists
            return false;

        }

    }


    public void readConfig(File file) throws IOException{

        FileReader reader = new FileReader(file);

        Gson gson = new Gson();

        //Deserialize our Json file and turn it into a JsonObject
        JsonObject obj = gson.fromJson(reader, JsonObject.class);

        //Set the config fields to the values read from our config file
        explosionHealDelay = getDoubleOrDefault(obj, "explosion_heal_delay", explosionHealDelay);
        blockPlacementDelay = getDoubleOrDefault(obj, "block_placement_delay", blockPlacementDelay);
        shouldHealOnFlowingWater = getBooleanOrDefault(obj, "heal_on_flowing_water", shouldHealOnFlowingWater);
        shouldHealOnFlowingLava = getBooleanOrDefault(obj, "heal_on_flowing_lava", shouldHealOnFlowingLava);
        shouldPlaySoundOnBlockPlacement = getBooleanOrDefault(obj, "block_placement_sound_effect", shouldPlaySoundOnBlockPlacement);

        //Parse the JsonObject into a Hashmap
        JsonObject replaceListJson = getJsonObjectOrDefault(obj, "replace_list", new JsonObject());
        replaceMap = gson.fromJson(replaceListJson, HashMap.class);

        reader.close();

    }

    //Called upon server shutdown
    public void updateConfig(File file) throws IOException {

        Path configPath = FabricLoader.getInstance().getConfigDir().resolve("creeper-healing.json");

        if(Files.exists(configPath)){

            FileReader reader = new FileReader(file);
            Gson gson = new Gson();

            //Don't override the current replace-list in the config file
            JsonObject obj = gson.fromJson(reader, JsonObject.class);
            JsonObject tempReplaceListJson = getJsonObjectOrDefault(obj, "replace_list", new JsonObject());
            replaceMap = gson.fromJson(tempReplaceListJson, HashMap.class);

            reader.close();

        } else {

            //If the config doesn't exist already, write a new one with default values
            explosionHealDelay = 3;
            blockPlacementDelay = 1;
            shouldHealOnFlowingWater = true;
            shouldHealOnFlowingLava = true;
            shouldPlaySoundOnBlockPlacement = true;
            replaceMap.clear();
            replaceMap.put("minecraft:diamond_block", "minecraft:stone");

            CreeperHealing.LOGGER.info("Found no preexisting configuration file. Creating a new one with default values.");
            CreeperHealing.LOGGER.info("Change the values in the config file and restart the server or game to apply them, or use the /creeper-healing reload_config command in-game.");

        }

        //Update all values of the config with new ones if the config exists
        Files.writeString(configPath, GSON.toJson(this));

    }

    public boolean reloadConfig(CommandContext<ServerCommandSource> serverCommandSourceCommandContext) throws IOException {

        File file = new File(FabricLoader.getInstance().getConfigDir() + "/creeper-healing.json");

        Path configPath = FabricLoader.getInstance().getConfigDir().resolve("creeper-healing.json");

        //If the config file exists, read the config again.
        // Remember to update the "isExplosionHandlingUnlocked" flag accordingly
        if(Files.exists(configPath)){

            CreeperHealing.setHealerHandlerLock(false);

            this.readConfig(file);

            CreeperHealing.setHealerHandlerLock(true);

            //Warn the user if these delays were set to 0 or fewer seconds
            if(Math.round(Math.max(this.explosionHealDelay, 0) * 20L) == 0) serverCommandSourceCommandContext.getSource().sendMessage(Text.literal("Explosion heal delay set to a very low value in the config file. A value of 1 second will be used instead. Please set a valid value in the config file").formatted(Formatting.YELLOW));
            if(Math.round(Math.max(this.blockPlacementDelay, 0) * 20L) == 0) serverCommandSourceCommandContext.getSource().sendMessage(Text.literal("Block placement delay set to a very low value in the config file. A value of 1 second will be used instead. Please set a valid value in the config file").formatted(Formatting.YELLOW));

            return true;

        }

        return false;

    }

}

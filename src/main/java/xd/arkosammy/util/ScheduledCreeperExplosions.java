package xd.arkosammy.util;

import com.google.gson.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import org.jetbrains.annotations.NotNull;
import xd.arkosammy.CreeperHealing;
import xd.arkosammy.events.CreeperExplosionEvent;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ScheduledCreeperExplosions implements Serializable {

    @Serial
    private static final long serialVersionUID = 1212L;

    //This private list will receive the actual list of CreeperExplosionEvent instances to be used for storage.
    private List<CreeperExplosionEvent> scheduledCreeperExplosionsForStoring;

    //Codec for our ScheduledCreeperExplosion instance that gets created upon server shutdown, and holds
    //the current CreeperExplosionEvents in the list for usage, and stores them in our private list.
    public static final Codec<ScheduledCreeperExplosions> CODEC = RecordCodecBuilder.create(scheduledCreeperExplosionsInstance -> scheduledCreeperExplosionsInstance.group(

            Codec.list(CreeperExplosionEvent.CODEC).fieldOf("Scheduled_Creeper_Explosions").forGetter(ScheduledCreeperExplosions::getScheduledCreeperExplosionsForStoring)

    ).apply(scheduledCreeperExplosionsInstance, ScheduledCreeperExplosions::new));

    public ScheduledCreeperExplosions(List<CreeperExplosionEvent> events){

        setScheduledCreeperExplosionsForStoring(events);

    }

    private void setScheduledCreeperExplosionsForStoring(List<CreeperExplosionEvent> events){

        this.scheduledCreeperExplosionsForStoring = events;

    }

    private List<CreeperExplosionEvent> getScheduledCreeperExplosionsForStoring() {

        return this.scheduledCreeperExplosionsForStoring;

    }

    //Reschedule the CreeperExplosionEvents read from our file
    public static void reScheduleCreeperExplosionEvents(MinecraftServer server) throws IOException {

        List<CreeperExplosionEvent> explosionEvents = readCreeperExplosionEvents(server);

        if (!explosionEvents.isEmpty()) {

            //Add our read CreeperExplosionEvents back to the list
            CreeperExplosionEvent.getExplosionEventsForUsage().addAll(explosionEvents);

            CreeperHealing.LOGGER.info("Rescheduled " + explosionEvents.size() + " creeper explosion events.");

        }

    }

    //Reads the JSON data from our file, then we return a list of the decoded CreeperExplosionEvents
    private static List<CreeperExplosionEvent> readCreeperExplosionEvents(@NotNull MinecraftServer server) throws IOException {

        //Obtain the path to the server's world directory
        Path scheduledExplosionsFilePath = server.getSavePath(WorldSavePath.ROOT).resolve("scheduled-explosions.json");

        List<CreeperExplosionEvent> scheduledExplosionEvents = new ArrayList<>();

        if (Files.exists(scheduledExplosionsFilePath)) { //If our file does not exist, create it and return an empty list.

            try (BufferedReader reader = Files.newBufferedReader(scheduledExplosionsFilePath)) {

                JsonElement scheduledExplosionsAsJson = JsonParser.parseReader(reader); //Parse the contents of our file into a JsonElement

                //CreeperHealing.LOGGER.info("Read json: {}", scheduledExplosionsAsJson);

                DataResult<ScheduledCreeperExplosions> decodedScheduledExplosions = CODEC.parse(JsonOps.INSTANCE, scheduledExplosionsAsJson); //Decode our JsonElement into a DataResult

                if (decodedScheduledExplosions.result().isPresent()) {

                    return decodedScheduledExplosions.resultOrPartial(error -> CreeperHealing.LOGGER.error("Error reading json: " + error)).orElseThrow().getScheduledCreeperExplosionsForStoring(); //If the DataResult does contain our result, then obtain our instance of ScheduledCreeperExplosion, then obtain its list of CreeperExplosionEvents

                } else {

                    CreeperHealing.LOGGER.error("Error reading scheduled creeper explosions: " + decodedScheduledExplosions.error());

                }

            } catch (IOException | JsonParseException e) {

                CreeperHealing.LOGGER.error("Error reading scheduled creeper explosions: " + e);

            }

        } else {

            Files.createFile(scheduledExplosionsFilePath);

        }

        return scheduledExplosionEvents;
    }

    //We encode our singular instance of CreeperExplosionEvent object, and we write it to a file
    public void storeBlockPlacements(@NotNull MinecraftServer server) {

        //Obtain the path to the server's world directory
        Path scheduledExplosionsFilePath = server.getSavePath(WorldSavePath.ROOT).resolve("scheduled-explosions.json");

        //Obtain the result of trying to encode our instance of ScheduledCreeperExplosions into a JsonOps
        //DataResult<JsonElement> encodedScheduledExplosions = CODEC.encodeStart(JsonOps.INSTANCE, new ScheduledCreeperExplosions(SCHEDULED_CREEPER_EXPLOSIONS.getScheduledCreeperExplosions()));

        DataResult<JsonElement> encodedScheduledExplosions = CODEC.encodeStart(JsonOps.INSTANCE, this);

        if (encodedScheduledExplosions.result().isPresent()){

            //If the previous step didn't fail, obtain the JsonElement, then turn it into a JsonString, to then write to our file
            JsonElement scheduledExplosionsAsJson = encodedScheduledExplosions.resultOrPartial(CreeperHealing.LOGGER::error).orElseThrow();

            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            String scheduledExplosionsAsJsonString = gson.toJson(scheduledExplosionsAsJson);

            try (BufferedWriter writer = Files.newBufferedWriter(scheduledExplosionsFilePath)) {

                writer.write(scheduledExplosionsAsJsonString);

            } catch (IOException e) {

                CreeperHealing.LOGGER.error("Error storing creeper explosions: " + e.getMessage());

            }

            CreeperHealing.LOGGER.info("Stored scheduled creeper explosion events to " + scheduledExplosionsFilePath);

        } else {

            CreeperHealing.LOGGER.warn("Failed to encode scheduled creeper explosion events.");

        }

    }

}

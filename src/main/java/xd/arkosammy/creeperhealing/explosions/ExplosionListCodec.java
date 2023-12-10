package xd.arkosammy.creeperhealing.explosions;

import com.google.gson.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import org.jetbrains.annotations.NotNull;
import xd.arkosammy.creeperhealing.CreeperHealing;
import xd.arkosammy.creeperhealing.handlers.ExplosionListHandler;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ExplosionListCodec {

    private final List<ExplosionEvent> storedExplosionEvents;

    public static final Codec<ExplosionListCodec> CODEC = RecordCodecBuilder.create(scheduledCreeperExplosionsInstance -> scheduledCreeperExplosionsInstance.group(
            Codec.list(ExplosionEvent.getCodec()).fieldOf("Scheduled_Creeper_Explosions").forGetter(ExplosionListCodec::getStoredExplosionEvents)
    ).apply(scheduledCreeperExplosionsInstance, ExplosionListCodec::new));

    public ExplosionListCodec(List<ExplosionEvent> events){
        this.storedExplosionEvents = new CopyOnWriteArrayList<>(events);
    }

    private List<ExplosionEvent> getStoredExplosionEvents() {
        return this.storedExplosionEvents;
    }

    public static void rescheduleExplosionEvents(MinecraftServer server) throws IOException {
        List<ExplosionEvent> explosionEvents = deserializeExplosionEvents(server);
        if (!explosionEvents.isEmpty()) {
            ExplosionListHandler.getExplosionEventList().addAll(explosionEvents);
            CreeperHealing.LOGGER.info("Rescheduled " + explosionEvents.size() + " explosion event(s).");
        }
    }

    //We encode our singular ExplosionEventSerializer object, and we write it to a file
    public void serializeExplosionEvents(@NotNull MinecraftServer server) {

        Path scheduledExplosionsFilePath = server.getSavePath(WorldSavePath.ROOT).resolve("scheduled-explosions.json");
        DataResult<JsonElement> encodedScheduledExplosions = CODEC.encodeStart(JsonOps.INSTANCE, this);

        if (encodedScheduledExplosions.result().isPresent()){

            JsonElement scheduledExplosionsAsJson = encodedScheduledExplosions.resultOrPartial(CreeperHealing.LOGGER::error).orElseThrow();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String scheduledExplosionsAsJsonString = gson.toJson(scheduledExplosionsAsJson);

            try (BufferedWriter writer = Files.newBufferedWriter(scheduledExplosionsFilePath)) {
                writer.write(scheduledExplosionsAsJsonString);
                CreeperHealing.LOGGER.info("Stored scheduled creeper explosion event(s) to " + scheduledExplosionsFilePath);
            } catch (IOException e) {
                CreeperHealing.LOGGER.error("Error storing creeper explosion(s): " + e.getMessage());
            }

        } else {
            CreeperHealing.LOGGER.error("Error storing explosions event(s) to " + scheduledExplosionsFilePath);
        }

    }

    //Read the JSON data from our file, then we return a list of the decoded ExplosionEvents
    private static List<ExplosionEvent> deserializeExplosionEvents(@NotNull MinecraftServer server) throws IOException {

        Path scheduledExplosionsFilePath = server.getSavePath(WorldSavePath.ROOT).resolve("scheduled-explosions.json");
        List<ExplosionEvent> scheduledExplosionEvents = new ArrayList<>();

        if (Files.exists(scheduledExplosionsFilePath)) {

            try (BufferedReader reader = Files.newBufferedReader(scheduledExplosionsFilePath)) {

                JsonElement scheduledExplosionsAsJson = JsonParser.parseReader(reader);
                DataResult<ExplosionListCodec> decodedScheduledExplosions = CODEC.parse(JsonOps.INSTANCE, scheduledExplosionsAsJson); //Decode our JsonElement into a DataResult

                if (decodedScheduledExplosions.result().isPresent()) {
                    return decodedScheduledExplosions.resultOrPartial(error -> CreeperHealing.LOGGER.error("Error reading scheduled-explosions.json: " + error)).orElseThrow().getStoredExplosionEvents();
                } else {
                    CreeperHealing.LOGGER.error("Error reading scheduled explosions: " + decodedScheduledExplosions.error());
                }

            } catch (IOException | JsonParseException e) {
                CreeperHealing.LOGGER.error("Error reading scheduled explosions: " + e);
            }
        } else {
            Files.createFile(scheduledExplosionsFilePath);
        }
        return scheduledExplosionEvents;
    }
}

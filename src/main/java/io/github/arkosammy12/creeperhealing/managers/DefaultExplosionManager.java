package io.github.arkosammy12.creeperhealing.managers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.arkosammy12.creeperhealing.explosions.AbstractExplosionEvent;
import io.github.arkosammy12.creeperhealing.explosions.ExplosionEvent;
import io.github.arkosammy12.creeperhealing.explosions.SerializedExplosionEvent;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import io.github.arkosammy12.creeperhealing.CreeperHealing;
import io.github.arkosammy12.creeperhealing.blocks.AffectedBlock;
import io.github.arkosammy12.creeperhealing.blocks.SingleAffectedBlock;
import io.github.arkosammy12.creeperhealing.config.ConfigUtils;
import io.github.arkosammy12.creeperhealing.explosions.factories.DefaultExplosionFactory;
import io.github.arkosammy12.creeperhealing.explosions.factories.ExplosionEventFactory;
import io.github.arkosammy12.creeperhealing.util.ExplosionContext;
import io.github.arkosammy12.creeperhealing.util.ExplosionUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultExplosionManager implements ExplosionManager {

    public static final Identifier ID = Identifier.of(CreeperHealing.MOD_ID, "default_explosion_manager");
    private static final Function<ExplosionContext, ExplosionEventFactory<?>> explosionContextToFactoryFunction = explosionContext -> {
        List<BlockPos> indirectlyExplodedPositions = explosionContext.indirectlyAffectedPositions();
        Map<BlockPos, Pair<BlockState, BlockEntity>> affectedStatesAndBlockEntities = explosionContext.affectedStatesAndBlockEntities();
        DefaultExplosionFactory explosionFactory = new DefaultExplosionFactory(
                affectedStatesAndBlockEntities,
                explosionContext.vanillaAffectedPositions(),
                indirectlyExplodedPositions,
                explosionContext.world()
        );
        return explosionFactory;
    };
    private static final String SCHEDULED_EXPLOSIONS_FILE = "scheduled-explosions.json";

    private final Codec<DefaultExplosionManager> codec;
    private final List<ExplosionEvent> explosionEvents = new ArrayList<>();

    @Override
    public Identifier getId() {
        return ID;
    }

    public DefaultExplosionManager(Codec<SerializedExplosionEvent> explosionSerializer) {
        this.codec = RecordCodecBuilder.create(instance -> instance.group(
                Codec.list(explosionSerializer).fieldOf("scheduled_explosions").forGetter(DefaultExplosionManager::getSerializedExplosionEvents)
        ).apply(instance, (serializedExplosionEvents) -> new DefaultExplosionManager(serializedExplosionEvents, explosionSerializer)));
    }

    private DefaultExplosionManager(List<SerializedExplosionEvent> serializedExplosionEvents, Codec<SerializedExplosionEvent> explosionSerializer) {
        this(explosionSerializer);
        this.explosionEvents.addAll(serializedExplosionEvents.stream().map(SerializedExplosionEvent::asDeserialized).toList());
    }

    private List<SerializedExplosionEvent> getSerializedExplosionEvents() {
        return this.explosionEvents.stream().map(ExplosionEvent::asSerialized).collect(Collectors.toList());
    }

    @Override
    public Stream<ExplosionEvent> getExplosionEvents() {
        return this.explosionEvents.stream();
    }

    @Override
    public void onServerStarting(MinecraftServer server) {
        this.readExplosionEvents(server);
        this.updateAffectedBlocksTimers();
    }

    @Override
    public void onServerStopping(MinecraftServer server) {
        this.storeExplosionEvents(server);
        this.explosionEvents.clear();
    }

    @Override
    public Function<ExplosionContext, ExplosionEventFactory<?>> getExplosionContextToEventFactoryFunction() {
        return explosionContextToFactoryFunction;
    }

    @Override
    public void tick(MinecraftServer server) {
        if (this.explosionEvents.isEmpty() || !server.getTickManager().shouldTick()) {
            return;
        }
        for (ExplosionEvent explosionEvent : this.explosionEvents) {
            explosionEvent.tick(server);
        }
        explosionEvents.removeIf(ExplosionEvent::isFinished);
    }

    @Override
    public <T extends ExplosionEvent> void addExplosionEvent(ExplosionEventFactory<T> explosionEventFactory) {
        ExplosionEvent explosionEvent = explosionEventFactory.createExplosionEvent();
        if (explosionEvent == null) {
            return;
        }
        Set<ExplosionEvent> collidingExplosions = this.getCollidingExplosions(explosionEvent, explosionEventFactory.getAffectedPositions());
        if (collidingExplosions.isEmpty()) {
            this.explosionEvents.add(explosionEvent);
        } else {
            this.explosionEvents.removeIf(collidingExplosions::contains);
            collidingExplosions.add(explosionEvent);
            this.explosionEvents.add(combineCollidingExplosions(explosionEvent, collidingExplosions, explosionEventFactory));
        }

    }

    @Override
    public void storeExplosionEvents(MinecraftServer server) {
        Path savedExplosionsFilePath = server.getSavePath(WorldSavePath.ROOT).resolve(SCHEDULED_EXPLOSIONS_FILE);
        DataResult<JsonElement> encodedExplosions = this.codec.encodeStart(server.getRegistryManager().getOps(JsonOps.COMPRESSED), this);
        if (encodedExplosions.isError()) {
            CreeperHealing.LOGGER.error("Error storing creeper healing explosion(s): No value present!");
            return;
        }
        JsonElement encodedExplosionsJson = encodedExplosions.getPartialOrThrow();
        Gson gson = new Gson();
        String jsonString = gson.toJson(encodedExplosionsJson);
        try (BufferedWriter bf = Files.newBufferedWriter(savedExplosionsFilePath)) {
            bf.write(jsonString);
            CreeperHealing.LOGGER.info("Stored {} explosion event(s) to {}: ", this.explosionEvents.size(), savedExplosionsFilePath);
        } catch (Exception e) {
            CreeperHealing.LOGGER.error("Error storing explosion event(s): {}", e.toString());
        }
    }

    @Override
    public void readExplosionEvents(MinecraftServer server) {
        Path savedExplosionsFilePath = server.getSavePath(WorldSavePath.ROOT).resolve(SCHEDULED_EXPLOSIONS_FILE);
        try {
            if (!Files.exists(savedExplosionsFilePath)) {
                CreeperHealing.LOGGER.warn("Scheduled explosions file not found! Creating new one at {}", savedExplosionsFilePath);
                Files.createFile(savedExplosionsFilePath);
                return;
            }
            try (BufferedReader br = Files.newBufferedReader(savedExplosionsFilePath)) {
                JsonElement jsonElement = JsonParser.parseReader(br);
                DataResult<DefaultExplosionManager> decodedExplosionManager = this.codec.parse(server.getRegistryManager().getOps(JsonOps.COMPRESSED), jsonElement);
                decodedExplosionManager
                        .resultOrPartial(error -> CreeperHealing.LOGGER.error("Error reading scheduled explosions from file {}: {}", savedExplosionsFilePath, error))
                        .ifPresent(decodedManager -> {
                            this.explosionEvents.addAll(decodedManager.explosionEvents);
                            CreeperHealing.LOGGER.info("Rescheduled {} explosion event(s)", decodedManager.explosionEvents.size());
                        });
            }
        } catch (Exception e) {
            CreeperHealing.LOGGER.error("Error reading scheduled explosions from file {}: {}", savedExplosionsFilePath, e.toString());
        }
    }

    // An explosion collides with another if the square of the distance between their centers is less than or equal to the sum of their radii
    private Set<ExplosionEvent> getCollidingExplosions(ExplosionEvent newExplosionEvent, List<BlockPos> affectedPositions) {
        Set<ExplosionEvent> collidingExplosions = new LinkedHashSet<>();
        BlockPos newExplosionCenter;
        int newExplosionRadius;
        if (newExplosionEvent instanceof AbstractExplosionEvent abstractExplosionEvent) {
            newExplosionCenter = abstractExplosionEvent.getCenter();
            newExplosionRadius = abstractExplosionEvent.getRadius();
        } else {
            newExplosionRadius = ExplosionUtils.getMaxExplosionRadius(affectedPositions);
            newExplosionCenter = ExplosionUtils.calculateCenter(affectedPositions);
        }

        for (ExplosionEvent explosionEvent : this.explosionEvents) {
            boolean hasStartedHealing = explosionEvent.getHealTimer() <= 0;
            if (hasStartedHealing) {
                continue;
            }
            BlockPos currentExplosionCenter;
            int currentExplosionRadius;
            if (explosionEvent instanceof AbstractExplosionEvent abstractExplosionEvent) {
                currentExplosionCenter = abstractExplosionEvent.getCenter();
                currentExplosionRadius = abstractExplosionEvent.getRadius();
            } else {
                List<BlockPos> currentAffectedPositions = explosionEvent.getAffectedBlocks().map(AffectedBlock::getBlockPos).toList();
                currentExplosionRadius = ExplosionUtils.getMaxExplosionRadius(currentAffectedPositions);
                currentExplosionCenter = ExplosionUtils.calculateCenter(currentAffectedPositions);
            }
            int combinedRadius = newExplosionRadius + currentExplosionRadius;
            double distanceBetweenCenters = Math.floor(Math.sqrt(newExplosionCenter.getSquaredDistance(currentExplosionCenter)));
            if (distanceBetweenCenters <= combinedRadius) {
                collidingExplosions.add(explosionEvent);
            }
        }
        return collidingExplosions;
    }

    // Combine the list of affected blocks and use the attributes of the newest explosion as the attributes of the combined explosion
    private ExplosionEvent combineCollidingExplosions(ExplosionEvent newestExplosion, Set<ExplosionEvent> collidingExplosions, ExplosionEventFactory<?> explosionEventFactory) {
        List<AffectedBlock> combinedAffectedBlocks = collidingExplosions.stream().flatMap(ExplosionEvent::getAffectedBlocks).collect(Collectors.toList());
        ExplosionEvent combinedExplosionEvent = explosionEventFactory.createExplosionEvent(combinedAffectedBlocks, newestExplosion.getHealTimer(), ConfigUtils.getBlockPlacementDelay());
        return combinedExplosionEvent;
    }

    public void updateAffectedBlocksTimers() {
        for (ExplosionEvent explosionEvent : this.explosionEvents) {
            if (!(explosionEvent instanceof AbstractExplosionEvent abstractExplosionEvent)) {
                continue;
            }
            List<AffectedBlock> affectedBlocks = explosionEvent.getAffectedBlocks().toList();
            for (int i = abstractExplosionEvent.getBlockCounter() + 1; i < affectedBlocks.size(); i++) {
                AffectedBlock currentAffectedBlock = affectedBlocks.get(i);
                if (!(currentAffectedBlock instanceof SingleAffectedBlock singleAffectedBlock)) {
                    continue;
                }
                singleAffectedBlock.setTimer(ConfigUtils.getBlockPlacementDelay());
            }

        }
    }

}

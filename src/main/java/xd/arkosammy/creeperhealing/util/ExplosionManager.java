package xd.arkosammy.creeperhealing.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import xd.arkosammy.creeperhealing.CreeperHealing;
import xd.arkosammy.creeperhealing.blocks.AffectedBlock;
import xd.arkosammy.creeperhealing.blocks.SingleAffectedBlock;
import xd.arkosammy.creeperhealing.config.ConfigSettings;
import xd.arkosammy.creeperhealing.config.ConfigUtils;
import xd.arkosammy.creeperhealing.explosions.*;
import xd.arkosammy.creeperhealing.explosions.ducks.ExplosionAccessor;
import xd.arkosammy.monkeyconfig.settings.BooleanSetting;
import xd.arkosammy.monkeyconfig.settings.list.StringListSetting;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class ExplosionManager {

    private ExplosionManager() {}

    private static final Codec<ExplosionManager> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(SerializedExplosionEvent.CODEC).fieldOf("scheduled_explosions").forGetter(explosionManager -> explosionManager.explosionEvents.stream().map(ExplosionEvent::asSerialized).toList())
    ).apply(instance, ExplosionManager::new));
    private static ExplosionManager instance;
    private final List<ExplosionEvent> explosionEvents = new CopyOnWriteArrayList<>();

    public static ExplosionManager getInstance(){
        if(instance == null){
            instance = new ExplosionManager();
        }
        return instance;
    }

    private ExplosionManager(List<SerializedExplosionEvent> serializedExplosionEvents){
        final List<ExplosionEvent> explosionEvents = serializedExplosionEvents.stream().map(SerializedExplosionEvent::toDeserialized).toList();
        this.explosionEvents.clear();
        this.explosionEvents.addAll(explosionEvents);
        CreeperHealing.LOGGER.info("Rescheduled {} explosion event(s)", explosionEvents.size());
    }

    public List<ExplosionEvent> getExplosionEvents(){
        return this.explosionEvents;
    }

    public void addExplosion(Explosion explosion){
        if(!((ExplosionAccessor)explosion).creeperhealing$shouldHeal()){
            return;
        }
        final World explosionWorld = ((ExplosionAccessor) explosion).creeperhealing$getWorld();
        final List<AffectedBlock> affectedBlocks = new ArrayList<>();
        final boolean whitelistEnabled = ConfigUtils.getSettingValue(ConfigSettings.ENABLE_WHITELIST.getSettingLocation(), BooleanSetting.class);
        final List<? extends String> whitelist = ConfigUtils.getSettingValue(ConfigSettings.WHITELIST.getSettingLocation(), StringListSetting.class);
        for(BlockPos affectedPosition : explosion.getAffectedBlocks()){
            // Hardcoded exception. Place before all logic
            final BlockState affectedState = explosionWorld.getBlockState(affectedPosition);
            if(ExcludedBlocks.isExcluded(affectedState)) {
                continue;
            }
            final boolean isStateUnhealable = affectedState.isAir() || affectedState.getBlock().equals(Blocks.TNT) || affectedState.isIn(BlockTags.FIRE);
            if (isStateUnhealable) {
                continue;
            }
            final String affectedBlockIdentifier = Registries.BLOCK.getId(affectedState.getBlock()).toString();
            final boolean whitelistContainsIdentifier = whitelist.contains(affectedBlockIdentifier);
            if (!whitelistEnabled || whitelistContainsIdentifier) {
                affectedBlocks.add(AffectedBlock.newInstance(affectedPosition, affectedState, explosionWorld));
            }
        }
        if(affectedBlocks.isEmpty()){
            return;
        }
        final List<AffectedBlock> sortedAffectedBlocks = ExplosionUtils.sortAffectedBlocks(affectedBlocks, explosionWorld);
        final ExplosionEvent newExplosionEvent = ExplosionEvent.newInstance(sortedAffectedBlocks, explosionWorld);
        final Set<ExplosionEvent> collidingExplosions = this.getCollidingExplosions(affectedBlocks.stream().map(AffectedBlock::getBlockPos).toList());
        if(collidingExplosions.isEmpty()){
            this.explosionEvents.add(newExplosionEvent);
        } else {
            this.explosionEvents.removeIf(collidingExplosions::contains);
            collidingExplosions.add(newExplosionEvent);
            this.explosionEvents.add(combineCollidingExplosions(collidingExplosions, newExplosionEvent, explosionWorld));
        }
    }

    // An explosion collides with another if the square of the distance between their centers is less than or equal to the sum of their radii
    private Set<ExplosionEvent> getCollidingExplosions(List<BlockPos> affectedPositions){
        final Set<ExplosionEvent> collidingExplosions = new LinkedHashSet<>();
        final BlockPos centerOfNewExplosion = new BlockPos(ExplosionUtils.getCenterXCoordinate(affectedPositions), ExplosionUtils.getCenterYCoordinate(affectedPositions), ExplosionUtils.getCenterZCoordinate(affectedPositions));
        final int newExplosionMaxRadius = ExplosionUtils.getMaxExplosionRadius(affectedPositions);
        for(ExplosionEvent explosionEvent : this.explosionEvents){
            // Don't combine explosions that have started healing already
            if (explosionEvent.getHealTimer() <= 0) {
                continue;
            }
            final List<BlockPos> affectedBlocksAsPositions = explosionEvent.getAffectedBlocks().map(AffectedBlock::getBlockPos).toList();
            final BlockPos centerOfCurrentExplosion = new BlockPos(ExplosionUtils.getCenterXCoordinate(affectedBlocksAsPositions), ExplosionUtils.getCenterYCoordinate(affectedBlocksAsPositions), ExplosionUtils.getCenterZCoordinate(affectedBlocksAsPositions));
            final int currentExplosionMaxRadius = ExplosionUtils.getMaxExplosionRadius(affectedBlocksAsPositions);
            final int combinedRadius = newExplosionMaxRadius + currentExplosionMaxRadius;
            final double distanceBetweenCenters = Math.floor(Math.sqrt(centerOfNewExplosion.getSquaredDistance(centerOfCurrentExplosion)));
            if(distanceBetweenCenters <= combinedRadius){
                collidingExplosions.add(explosionEvent);
            }
        }
        return collidingExplosions;
    }

    // Combine the list of affected blocks and use the attributes of the newest explosion as the attributes of the combined explosion
    private ExplosionEvent combineCollidingExplosions(Set<ExplosionEvent> collidingExplosions, ExplosionEvent newestExplosion, World world){
        final List<AffectedBlock> combinedAffectedBlockList = collidingExplosions.stream().flatMap(ExplosionEvent::getAffectedBlocks).collect(Collectors.toList());
        final List<AffectedBlock> sortedAffectedBlocks = ExplosionUtils.sortAffectedBlocks(combinedAffectedBlockList, world);
        final ExplosionEvent combinedExplosionEvent = switch (newestExplosion) {
            case DaytimeExplosionEvent ignored -> new DaytimeExplosionEvent(sortedAffectedBlocks, newestExplosion.getHealTimer(), newestExplosion.getBlockCounter());
            case DifficultyBasedExplosionEvent ignored -> new DifficultyBasedExplosionEvent(sortedAffectedBlocks, newestExplosion.getHealTimer(), newestExplosion.getBlockCounter());
            case BlastResistanceBasedExplosionEvent ignored -> new BlastResistanceBasedExplosionEvent(sortedAffectedBlocks, newestExplosion.getHealTimer(), newestExplosion.getBlockCounter());
            default -> new DefaultExplosionEvent(sortedAffectedBlocks, newestExplosion.getHealTimer(), newestExplosion.getBlockCounter());
        };
        combinedExplosionEvent.getAffectedBlocks().forEach(affectedBlock -> {
            if (affectedBlock instanceof SingleAffectedBlock singleAffectedBlock) {
              singleAffectedBlock.setTimer(ConfigUtils.getBlockPlacementDelay());
            }
        });
        combinedExplosionEvent.setup(world);
        return combinedExplosionEvent;
    }

    public void tick(MinecraftServer server){
        if(this.explosionEvents.isEmpty() || !server.getTickManager().shouldTick()){
            return;
        }
        this.explosionEvents.forEach(explosionEvent -> explosionEvent.tick(server));
        this.explosionEvents.removeIf(explosionEvent -> !explosionEvent.shouldKeepHealing(explosionEvent.getWorld(server)));
    }

    public void storeExplosions(MinecraftServer server){
        final Path scheduledExplosionsFilePath = server.getSavePath(WorldSavePath.ROOT).resolve("scheduled-explosions.json");
        final DataResult<JsonElement> encodedScheduledExplosions = CODEC.encodeStart(JsonOps.COMPRESSED, this);
        if (encodedScheduledExplosions.result().isEmpty()) {
            CreeperHealing.LOGGER.error("Error storing creeper explosion(s): No value present!");
            return;
        }
        final JsonElement scheduledExplosionsJson = encodedScheduledExplosions.resultOrPartial(CreeperHealing.LOGGER::error).orElseThrow();
        final Gson gson = new GsonBuilder().create();
        final String jsonString = gson.toJson(scheduledExplosionsJson);
        try(BufferedWriter bf = Files.newBufferedWriter(scheduledExplosionsFilePath)){
            bf.write(jsonString);
            CreeperHealing.LOGGER.info("Stored {} explosion event(s) to {}", this.explosionEvents.size(), scheduledExplosionsFilePath);
        } catch (IOException e){
            CreeperHealing.LOGGER.error("Error storing explosion event(s): {}", e.toString());
        }
    }

    public void readExplosionEvents(MinecraftServer server){
        final Path scheduledExplosionsFilePath = server.getSavePath(WorldSavePath.ROOT).resolve("scheduled-explosions.json");
        try {
            if (!Files.exists(scheduledExplosionsFilePath)) {
                CreeperHealing.LOGGER.warn("Scheduled explosions file not found. Creating new one at {}", scheduledExplosionsFilePath);
                Files.createFile(scheduledExplosionsFilePath);
                return;
            }
            try(BufferedReader br = Files.newBufferedReader(scheduledExplosionsFilePath)){
                final JsonElement scheduledExplosionsJson = JsonParser.parseReader(br);
                final DataResult<ExplosionManager> decodedExplosionManger = CODEC.parse(JsonOps.COMPRESSED, scheduledExplosionsJson);
                decodedExplosionManger.resultOrPartial(error -> CreeperHealing.LOGGER.error("Error reading decoded scheduled explosions: {}", error)).ifPresent(explosionManager -> instance = explosionManager);
            }
        } catch (IOException e){
            CreeperHealing.LOGGER.error("Error reading scheduled explosions: {}", e.toString());
        }

    }

    public void updateAffectedBlocksTimers(){
        for(ExplosionEvent explosionEvent : this.explosionEvents){
            if(explosionEvent instanceof DefaultExplosionEvent) {
                List<AffectedBlock> affectedBlocks = explosionEvent.getAffectedBlocks().toList();
                for (int i = explosionEvent.getBlockCounter() + 1; i < affectedBlocks.size(); i++) {
                    AffectedBlock currentAffectedBlock = affectedBlocks.get(i);
                    if (!(currentAffectedBlock instanceof SingleAffectedBlock singleAffectedBlock)) {
                        continue;
                    }
                    singleAffectedBlock.setTimer(ConfigUtils.getBlockPlacementDelay());
                }
            }
        }
    }

}

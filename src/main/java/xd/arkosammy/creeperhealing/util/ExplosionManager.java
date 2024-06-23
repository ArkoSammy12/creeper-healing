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

    private static final Codec<ExplosionManager> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(SerializedExplosionEvent.CODEC).fieldOf("scheduled_explosions").forGetter(explosionManager -> explosionManager.explosionEvents.stream().map(AbstractExplosionEvent::toSerialized).toList())
    ).apply(instance, ExplosionManager::new));
    private static ExplosionManager instance;
    private final List<AbstractExplosionEvent> explosionEvents = new CopyOnWriteArrayList<>();

    public static ExplosionManager getInstance(){
        if(instance == null){
            instance = new ExplosionManager();
        }
        return instance;
    }

    private ExplosionManager(){
    }

    private ExplosionManager(List<SerializedExplosionEvent> serializedExplosionEvents){
        List<AbstractExplosionEvent> explosionEvents = serializedExplosionEvents.stream().map(SerializedExplosionEvent::toDeserialized).toList();
        this.explosionEvents.clear();
        this.explosionEvents.addAll(explosionEvents);
        CreeperHealing.LOGGER.info("Rescheduled {} explosion event(s)", explosionEvents.size());
    }

    public List<AbstractExplosionEvent> getExplosionEvents(){
        return this.explosionEvents;
    }

    public void processExplosion(Explosion explosion){
        if(!((ExplosionAccessor)explosion).creeper_healing$shouldHeal()){
            return;
        }
        World world = ((ExplosionAccessor) explosion).creeper_healing$getWorld();
        List<AffectedBlock> affectedBlocks = new ArrayList<>();
        for(BlockPos affectedPos : explosion.getAffectedBlocks()){
            // Hardcoded exception. Place before all logic
            BlockState affectedState = world.getBlockState(affectedPos);
            if(ExcludedBlocks.isExcluded(affectedState)) {
                continue;
            }
            if (affectedState.isAir() || affectedState.getBlock().equals(Blocks.TNT) || affectedState.isIn(BlockTags.FIRE)) {
                continue; // Skip the current iteration if the block affectedState is air, TNT, or fire
            }
            String blockIdentifier = Registries.BLOCK.getId(affectedState.getBlock()).toString();
            boolean whitelistEnabled = ConfigUtils.getSettingValue(ConfigSettings.ENABLE_WHITELIST.getSettingLocation(), BooleanSetting.class);
            List<? extends String> whitelist = ConfigUtils.getSettingValue(ConfigSettings.WHITELIST.getSettingLocation(), StringListSetting.class);
            boolean whitelistContainsIdentifier = whitelist.contains(blockIdentifier);
            if (!whitelistEnabled || whitelistContainsIdentifier) {
                affectedBlocks.add(AffectedBlock.newAffectedBlock(affectedPos, affectedState, world));
            }
        }
        if(affectedBlocks.isEmpty()){
            return;
        }
        List<AffectedBlock> sortedAffectedBlocks = ExplosionUtils.sortAffectedBlocksList(affectedBlocks, world);
        AbstractExplosionEvent explosionEvent = AbstractExplosionEvent.newExplosionEvent(sortedAffectedBlocks, world);
        Set<AbstractExplosionEvent> collidingExplosions = this.getCollidingExplosions(affectedBlocks.stream().map(AffectedBlock::getPos).toList());
        if(collidingExplosions.isEmpty()){
            this.explosionEvents.add(explosionEvent);
        } else {
            this.explosionEvents.removeIf(collidingExplosions::contains);
            collidingExplosions.add(explosionEvent);
            this.explosionEvents.add(combineCollidingExplosions(collidingExplosions, explosionEvent, world));
        }
    }

    // An explosion collides with another if the square of the distance between their centers is less than or equal to the sum of their radii
    private Set<AbstractExplosionEvent> getCollidingExplosions(List<BlockPos> affectedPositions){
        Set<AbstractExplosionEvent> collidingExplosions = new LinkedHashSet<>();
        BlockPos centerOfNewExplosion = new BlockPos(ExplosionUtils.getCenterXCoordinate(affectedPositions), ExplosionUtils.getCenterYCoordinate(affectedPositions), ExplosionUtils.getCenterZCoordinate(affectedPositions));
        int newExplosionAverageRadius = ExplosionUtils.getMaxExplosionRadius(affectedPositions);
        for(AbstractExplosionEvent explosionEvent : this.explosionEvents){
            if(explosionEvent.getHealTimer() > 0){
                List<BlockPos> affectedBlocksAsPositions = explosionEvent.getAffectedBlocks().stream().map(AffectedBlock::getPos).toList();
                BlockPos centerOfCurrentExplosion = new BlockPos(ExplosionUtils.getCenterXCoordinate(affectedBlocksAsPositions), ExplosionUtils.getCenterYCoordinate(affectedBlocksAsPositions), ExplosionUtils.getCenterZCoordinate(affectedBlocksAsPositions));
                int currentExplosionAverageRadius = ExplosionUtils.getMaxExplosionRadius(affectedBlocksAsPositions);
                if(Math.floor(Math.sqrt(centerOfNewExplosion.getSquaredDistance(centerOfCurrentExplosion))) <= newExplosionAverageRadius + currentExplosionAverageRadius){
                    collidingExplosions.add(explosionEvent);
                }
            }
        }
        return collidingExplosions;
    }

    // Combine the list of affected blocks and use the attributes of the newest explosion as the attributes of the combined explosion
    private AbstractExplosionEvent combineCollidingExplosions(Set<AbstractExplosionEvent> collidingExplosions, AbstractExplosionEvent newestExplosion, World world){
        List<AffectedBlock> combinedAffectedBlockList = collidingExplosions.stream().flatMap(explosionEvent -> explosionEvent.getAffectedBlocks().stream()).collect(Collectors.toList());
        List<AffectedBlock> sortedAffectedBlocks = ExplosionUtils.sortAffectedBlocksList(combinedAffectedBlockList, world);
        AbstractExplosionEvent combinedExplosionEvent;
        if(newestExplosion instanceof DaytimeExplosionEvent){
            combinedExplosionEvent = new DaytimeExplosionEvent(sortedAffectedBlocks, newestExplosion.getHealTimer(), newestExplosion.getBlockCounter());
        } else if (newestExplosion instanceof DifficultyBasedExplosionEvent){
            combinedExplosionEvent = new DifficultyBasedExplosionEvent(sortedAffectedBlocks, newestExplosion.getHealTimer(), newestExplosion.getBlockCounter());
        } else if (newestExplosion instanceof BlastResistanceBasedExplosionEvent){
            combinedExplosionEvent = new BlastResistanceBasedExplosionEvent(sortedAffectedBlocks, newestExplosion.getHealTimer(), newestExplosion.getBlockCounter());
        } else {
            combinedExplosionEvent = new DefaultExplosionEvent(sortedAffectedBlocks, newestExplosion.getHealTimer(), newestExplosion.getBlockCounter());
        }
        combinedExplosionEvent.getAffectedBlocks().forEach(affectedBlock -> affectedBlock.setTimer(ConfigUtils.getBlockPlacementDelay()));
        combinedExplosionEvent.setupExplosion(world);
        return combinedExplosionEvent;
    }

    public void tick(MinecraftServer server){
        if(this.explosionEvents.isEmpty() || !server.getTickManager().shouldTick()){
            return;
        }
        this.explosionEvents.forEach(AbstractExplosionEvent::tick);
        for(AbstractExplosionEvent explosionEvent : this.explosionEvents){
            if(explosionEvent.getHealTimer() < 0){
                this.onExplosionEventFinishedTimer(explosionEvent, server);
            }
        }
    }

    private void onExplosionEventFinishedTimer(AbstractExplosionEvent currentExplosion, MinecraftServer server){
        Optional<AffectedBlock> optionalAffectedBlock = currentExplosion.getCurrentAffectedBlock();
        if(optionalAffectedBlock.isEmpty()){
            this.explosionEvents.remove(currentExplosion);
            return;
        }
        AffectedBlock affectedBlock = optionalAffectedBlock.get();
        if(affectedBlock.isPlaced()){
            currentExplosion.incrementCounter();
            return;
        }
        if(!affectedBlock.canBePlaced(server)){
            currentExplosion.delayAffectedBlock(affectedBlock, server);
            return;
        }
        affectedBlock.tickAffectedBlock();
        if(affectedBlock.getTimer() < 0){
            this.onAffectedBlockFinishedTimer(affectedBlock, currentExplosion, server);
        }
    }

    private void onAffectedBlockFinishedTimer(AffectedBlock currentedAffectedBlock, AbstractExplosionEvent currentExplosion, MinecraftServer server){
        if(!currentExplosion.shouldKeepHealing(currentedAffectedBlock.getWorld(server))){
            this.explosionEvents.remove(currentExplosion);
            return;
        }
        currentedAffectedBlock.tryHealing(server, currentExplosion);
        currentedAffectedBlock.setPlaced();
        currentExplosion.incrementCounter();
    }

    public void storeExplosions(MinecraftServer server){
        Path scheduledExplosionsFilePath = server.getSavePath(WorldSavePath.ROOT).resolve("scheduled-explosions.json");
        DataResult<JsonElement> encodedScheduledExplosions = CODEC.encodeStart(JsonOps.COMPRESSED, this);
        if(encodedScheduledExplosions.result().isPresent()){
            JsonElement scheduledExplosionsJson = encodedScheduledExplosions.resultOrPartial(CreeperHealing.LOGGER::error).orElseThrow();
            Gson gson = new GsonBuilder().create();
            String jsonString = gson.toJson(scheduledExplosionsJson);
            try(BufferedWriter bf = Files.newBufferedWriter(scheduledExplosionsFilePath)){
                bf.write(jsonString);
                CreeperHealing.LOGGER.info("Stored {} explosion event(s) to {}", this.explosionEvents.size(), scheduledExplosionsFilePath);
            } catch (IOException e){
                CreeperHealing.LOGGER.error("Error storing explosion event(s): {}", e.toString());
            }
        } else {
            CreeperHealing.LOGGER.error("Error storing creeper explosion(s): No value present");
        }
    }

    public void readExplosionEvents(MinecraftServer server){
        Path scheduledExplosionsFilePath = server.getSavePath(WorldSavePath.ROOT).resolve("scheduled-explosions.json");
        try {
            if (Files.exists(scheduledExplosionsFilePath)) {
                try(BufferedReader br = Files.newBufferedReader(scheduledExplosionsFilePath)){
                    JsonElement scheduledExplosionsJson = JsonParser.parseReader(br);
                    DataResult<ExplosionManager> decodedExplosionManger = CODEC.parse(JsonOps.COMPRESSED, scheduledExplosionsJson);
                    decodedExplosionManger.resultOrPartial(error -> CreeperHealing.LOGGER.error("Error reading decoded scheduled explosions: {}", error)).ifPresent(explosionManager -> instance = explosionManager);
                }
            } else {
                CreeperHealing.LOGGER.warn("Scheduled explosions file not found. Creating new one at {}", scheduledExplosionsFilePath);
                Files.createFile(scheduledExplosionsFilePath);
            }
        } catch (IOException e){
            CreeperHealing.LOGGER.error("Error reading scheduled explosions: {}", e.toString());
        }

    }

    public void updateAffectedBlocksTimers(){
        for(AbstractExplosionEvent explosionEvent : this.explosionEvents){
            if(explosionEvent instanceof DefaultExplosionEvent) {
                for (int i = explosionEvent.getBlockCounter() + 1; i < explosionEvent.getAffectedBlocks().size(); i++) {
                    explosionEvent.getAffectedBlocks().get(i).setTimer(ConfigUtils.getBlockPlacementDelay());
                }
            }
        }
    }

}

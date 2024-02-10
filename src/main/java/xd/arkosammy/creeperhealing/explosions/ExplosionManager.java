package xd.arkosammy.creeperhealing.explosions;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import xd.arkosammy.creeperhealing.CreeperHealing;
import xd.arkosammy.creeperhealing.configuration.*;
import xd.arkosammy.creeperhealing.explosions.ducks.ExplosionAccessor;

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
        if(explosion.getAffectedBlocks().isEmpty()){
            return;
        }
        if(!shouldHealExplosionFromSource(explosion.getCausingEntity(), explosion.getEntity(), ((ExplosionAccessor) explosion).creeper_healing$getDamageSource())){
            return;
        }
        World world = ((ExplosionAccessor) explosion).creeper_healing$getWorld();
        List<AffectedBlock> affectedBlocks = new ArrayList<>();
        for(BlockPos pos : explosion.getAffectedBlocks()){
            if (world.getBlockState(pos).isAir() || world.getBlockState(pos).getBlock().equals(Blocks.TNT) || world.getBlockState(pos).isIn(BlockTags.FIRE)) {
                continue; // Skip the current iteration if the block state is air, TNT, or fire
            }
            String blockIdentifier = Registries.BLOCK.getId(world.getBlockState(pos).getBlock()).toString();
            if (!PreferencesConfig.ENABLE_WHITELIST.getEntry().getValue() || WhitelistConfig.getWhitelist().contains(blockIdentifier)) {
                affectedBlocks.add(AffectedBlock.newAffectedBlock(pos, world));
            }
        }
        if(affectedBlocks.isEmpty()){
            return;
        }
        ExplosionHealingMode explosionHealingMode = ExplosionHealingMode.getFromName(ModeConfig.MODE.getEntry().getValue());
        List<AffectedBlock> sortedAffectedBlocks = ExplosionUtils.sortAffectedBlocksList(affectedBlocks, world.getServer());
        AbstractExplosionEvent explosionEvent = switch (explosionHealingMode) {
            case DAYTIME_HEALING_MODE -> new DaytimeExplosionEvent(sortedAffectedBlocks);
            case DIFFICULTY_BASED_HEALING_MODE -> new DifficultyBasedExplosionEvent(sortedAffectedBlocks);
            case BLAST_RESISTANCE_BASED_HEALING_MODE -> new BlastResistanceBasedExplosionEvent(sortedAffectedBlocks);
            default -> new DefaultExplosionEvent(sortedAffectedBlocks);
        };
        Set<AbstractExplosionEvent> collidingExplosions = this.getCollidingExplosions(affectedBlocks.stream().map(AffectedBlock::getPos).toList());
        if(collidingExplosions.isEmpty()){
            explosionEvent.setupExplosion(world);
            this.explosionEvents.add(explosionEvent);
        } else {
            this.explosionEvents.removeIf(collidingExplosions::contains);
            collidingExplosions.add(explosionEvent);
            this.explosionEvents.add(combineCollidingExplosions(collidingExplosions, explosionEvent, world));
        }

    }

    private AbstractExplosionEvent combineCollidingExplosions(Set<AbstractExplosionEvent> collidingExplosions, AbstractExplosionEvent newestExplosion, World world){
        List<AffectedBlock> combinedAffectedBlockList = collidingExplosions.stream()
                .flatMap(explosionEvent -> explosionEvent.getAffectedBlocks().stream())
                .collect(Collectors.toList());
        List<AffectedBlock> sortedAffectedBlocks = ExplosionUtils.sortAffectedBlocksList(combinedAffectedBlockList, world.getServer());
        AbstractExplosionEvent explosionEvent;
        if(newestExplosion instanceof DaytimeExplosionEvent){
            explosionEvent = new DaytimeExplosionEvent(sortedAffectedBlocks, newestExplosion.getHealTimer(), newestExplosion.getBlockCounter());
        } else if (newestExplosion instanceof DifficultyBasedExplosionEvent){
            explosionEvent = new DifficultyBasedExplosionEvent(sortedAffectedBlocks, newestExplosion.getHealTimer(), newestExplosion.getBlockCounter());
        } else if (newestExplosion instanceof BlastResistanceBasedExplosionEvent){
            explosionEvent = new BlastResistanceBasedExplosionEvent(sortedAffectedBlocks, newestExplosion.getHealTimer(), newestExplosion.getBlockCounter());
        } else {
            explosionEvent = new DefaultExplosionEvent(sortedAffectedBlocks, newestExplosion.getHealTimer(), newestExplosion.getBlockCounter());
        }
        long newestExplosionBlockTimers = DelaysConfig.getBlockPlacementDelayAsTicks();
        explosionEvent.getAffectedBlocks().forEach(affectedBlock -> affectedBlock.setAffectedBlockTimer(newestExplosionBlockTimers));
        explosionEvent.setupExplosion(world);
        return explosionEvent;
    }

    private Set<AbstractExplosionEvent> getCollidingExplosions(List<BlockPos> affectedPositions){
        Set<AbstractExplosionEvent> collidingExplosions = new LinkedHashSet<>();
        BlockPos centerOfNewExplosion = new BlockPos(ExplosionUtils.getCenterXCoordinate(affectedPositions), ExplosionUtils.getCenterYCoordinate(affectedPositions), ExplosionUtils.getCenterZCoordinate(affectedPositions));
        int newExplosionAverageRadius = ExplosionUtils.getMaxExplosionRadius(affectedPositions);
        for(AbstractExplosionEvent explosionEvent : this.explosionEvents){
            if(explosionEvent.getHealTimer() > 0){
                BlockPos centerOfCurrentExplosion = new BlockPos(ExplosionUtils.getCenterXCoordinate(explosionEvent.getAffectedBlocks().stream().map(AffectedBlock::getPos).toList()), ExplosionUtils.getCenterYCoordinate(explosionEvent.getCurrentAffectedBlock().stream().map(AffectedBlock::getPos).toList()), ExplosionUtils.getCenterZCoordinate(explosionEvent.getAffectedBlocks().stream().map(AffectedBlock::getPos).toList()));
                int currentExplosionAverageRadius = ExplosionUtils.getMaxExplosionRadius(explosionEvent.getAffectedBlocks().stream().map(AffectedBlock::getPos).toList());
                if(Math.floor(Math.sqrt(centerOfNewExplosion.getSquaredDistance(centerOfCurrentExplosion))) <= newExplosionAverageRadius + currentExplosionAverageRadius){
                    collidingExplosions.add(explosionEvent);
                }
            }
        }
        return collidingExplosions;
    }

    private boolean shouldHealExplosionFromSource(LivingEntity causingLivingEntity, Entity causingEntity, DamageSource damageSource){
        return (causingLivingEntity instanceof CreeperEntity && ExplosionSourceConfig.HEAL_CREEPER_EXPLOSIONS.getEntry().getValue())
                || (causingLivingEntity instanceof GhastEntity && ExplosionSourceConfig.HEAL_GHAST_EXPLOSIONS.getEntry().getValue())
                || (causingLivingEntity instanceof WitherEntity && ExplosionSourceConfig.HEAL_WITHER_EXPLOSIONS.getEntry().getValue())
                || (causingEntity instanceof TntEntity && ExplosionSourceConfig.HEAL_TNT_EXPLOSIONS.getEntry().getValue())
                || (causingEntity instanceof TntMinecartEntity && ExplosionSourceConfig.HEAL_TNT_MINECART_EXPLOSIONS.getEntry().getValue())
                || (damageSource.isOf(DamageTypes.BAD_RESPAWN_POINT) && ExplosionSourceConfig.HEAL_BED_AND_RESPAWN_ANCHOR_EXPLOSIONS.getEntry().getValue())
                || (causingEntity instanceof EndCrystalEntity && ExplosionSourceConfig.HEAL_END_CRYSTAL_EXPLOSIONS.getEntry().getValue());
    }

    public void tick(MinecraftServer server){
        if(this.explosionEvents.isEmpty() || !server.getTickManager().shouldTick()){
            return;
        }
        this.explosionEvents.forEach(AbstractExplosionEvent::tick);
        this.explosionEvents.forEach(explosionEvent -> {
            if(explosionEvent.getHealTimer() < 0){
                this.onExplosionEventFinishedTimer(explosionEvent, server);
            }
        });
    }

    private void onExplosionEventFinishedTimer(AbstractExplosionEvent currentExplosion, MinecraftServer server){
        Optional<AffectedBlock> optionalAffectedBlock = currentExplosion.getCurrentAffectedBlock();
        if(optionalAffectedBlock.isEmpty()){
            this.explosionEvents.remove(currentExplosion);
            return;
        }
        AffectedBlock affectedBlock = optionalAffectedBlock.get();
        if(affectedBlock.isAlreadyPlaced()){
            currentExplosion.incrementCounter();
            return;
        }
        if(!affectedBlock.canBePlaced(server)){
            currentExplosion.delayAffectedBlock(affectedBlock, server);
            return;
        }
        affectedBlock.tickAffectedBlock();
        if(affectedBlock.getAffectedBlockTimer() < 0){
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
        DataResult<JsonElement> encodedScheduledExplosions = CODEC.encodeStart(JsonOps.INSTANCE, this);
        if(encodedScheduledExplosions.result().isPresent()){
            JsonElement scheduledExplosionsJson = encodedScheduledExplosions.resultOrPartial(CreeperHealing.LOGGER::error).orElseThrow();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonString = gson.toJson(scheduledExplosionsJson);
            try(BufferedWriter bf = Files.newBufferedWriter(scheduledExplosionsFilePath)){
                bf.write(jsonString);
                CreeperHealing.LOGGER.info("Stored {} explosion event(s) to {}", this.explosionEvents.size(), scheduledExplosionsFilePath);
            } catch (IOException e){
                CreeperHealing.LOGGER.error("Error storing explosion event(s): " + e);
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
                    DataResult<ExplosionManager> decodedExplosionManger = CODEC.parse(JsonOps.INSTANCE, scheduledExplosionsJson);
                    decodedExplosionManger.resultOrPartial(error -> CreeperHealing.LOGGER.error("Error reading scheduled explosions: {}", error)).ifPresent(explosionManager -> instance = explosionManager);
                }
            } else {
                CreeperHealing.LOGGER.warn("Scheduled explosions file not found. Creating new one at {}", scheduledExplosionsFilePath);
                Files.createFile(scheduledExplosionsFilePath);
            }
        } catch (IOException e){
            CreeperHealing.LOGGER.error("Error reading scheduled explosions: " + e);
        }

    }

    public void updateAffectedBlocksTimers(){
        for(AbstractExplosionEvent explosionEvent : this.explosionEvents){
            if(explosionEvent instanceof DefaultExplosionEvent) {
                for (int i = explosionEvent.getBlockCounter() + 1; i < explosionEvent.getAffectedBlocks().size(); i++) {
                    explosionEvent.getAffectedBlocks().get(i).setAffectedBlockTimer(DelaysConfig.getBlockPlacementDelayAsTicks());
                }
            }
        }
    }

}

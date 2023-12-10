package xd.arkosammy.creeperhealing.explosions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import xd.arkosammy.creeperhealing.CreeperHealing;
import xd.arkosammy.creeperhealing.configuration.DelaysConfig;
import xd.arkosammy.creeperhealing.configuration.ModeConfig;
import xd.arkosammy.creeperhealing.handlers.ExplosionListHandler;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class ExplosionEvent {

    private final List<AffectedBlock> affectedBlocksList;
    private long explosionTimer;
    private int affectedBlockCounter;
    private final ExplosionHealingMode explosionMode;

    // Codec to serialize and deserialize ExplosionEvent instances.
    private static final Codec<ExplosionEvent> EXPLOSION_EVENT_CODEC = RecordCodecBuilder.create(creeperExplosionEventInstance -> creeperExplosionEventInstance.group(
            Codec.list(AffectedBlock.getCodec()).fieldOf("Affected_Blocks_List").forGetter(ExplosionEvent::getAffectedBlocksList),
            Codec.STRING.optionalFieldOf("Explosion_Mode", ExplosionHealingMode.DEFAULT_MODE.getName()).forGetter(explosionEvent -> explosionEvent.getExplosionMode().getName()),
            Codec.LONG.fieldOf("Explosion_Timer").forGetter(ExplosionEvent::getExplosionTimer),
            Codec.INT.fieldOf("Current_Block_Counter").forGetter(ExplosionEvent::getCurrentAffectedBlockCounter)
    ).apply(creeperExplosionEventInstance, ExplosionEvent::new));

    private ExplosionEvent(List<AffectedBlock> affectedBlocksList, String explosionModeName, long creeperExplosionTimer, int currentIndex){
        this.affectedBlockCounter = currentIndex;
        this.affectedBlocksList = new CopyOnWriteArrayList<>(affectedBlocksList);
        setExplosionTimer(creeperExplosionTimer);
        this.explosionMode = ExplosionHealingMode.getFromName(explosionModeName);
    }

    public static ExplosionEvent newExplosionEvent(List<AffectedBlock> affectedBlocksList, World world) {
        ExplosionEvent explosionEvent = new ExplosionEvent(ExplosionUtils.sortAffectedBlocksList(affectedBlocksList, world.getServer()), ModeConfig.MODE.getEntry().getValue(), DelaysConfig.getExplosionHealDelayAsTicks(), 0);
        explosionEvent.setUpExplosionHealingMode(world);

        Set<ExplosionEvent> collidingExplosions =  ExplosionUtils.getCollidingWaitingExplosions(affectedBlocksList.stream().map(AffectedBlock::getPos).toList());
        if(collidingExplosions.isEmpty()){
            return explosionEvent;
        } else {
            ExplosionListHandler.getExplosionEventList().removeIf(collidingExplosions::contains);
            collidingExplosions.add(explosionEvent);
            return combineCollidingExplosions(collidingExplosions, explosionEvent, world);
        }
    }

    public void setExplosionTimer(long delay){
        this.explosionTimer = delay;
    }

    public void incrementCounter() {
        this.affectedBlockCounter++;
    }

    public List<AffectedBlock> getAffectedBlocksList(){
        return this.affectedBlocksList;
    }

    public long getExplosionTimer(){
        return this.explosionTimer;
    }

    int getAffectedBlockCounter(){
        return this.affectedBlockCounter;
    }

    public ExplosionHealingMode getExplosionMode(){
        return this.explosionMode;
    }

    private int getCurrentAffectedBlockCounter(){
        return this.affectedBlockCounter;
    }

    static Codec<ExplosionEvent> getCodec(){
        return EXPLOSION_EVENT_CODEC;
    }


    //Iterate through all the CreeperExplosionEvents in our list and decrement each of their timers
    public static void tickExplosions(){
        for(ExplosionEvent explosionEvent : ExplosionListHandler.getExplosionEventList()){
            explosionEvent.explosionTimer--;
        }
    }

    public Optional<AffectedBlock> getCurrentAffectedBlock(){
        if(this.affectedBlockCounter < this.getAffectedBlocksList().size()){
            return Optional.of(this.getAffectedBlocksList().get(affectedBlockCounter));
        }
        return Optional.empty();
    }

    public void delayAffectedBlock(AffectedBlock affectedBlockToDelay, MinecraftServer server){
        int indexOfPostponed = this.getAffectedBlocksList().indexOf(affectedBlockToDelay);
        if(indexOfPostponed != -1) {
            Optional<Integer> indexOfNextPlaceableOptional = this.findNextPlaceableBlock(server);
            if (indexOfNextPlaceableOptional.isPresent()) {
                int indexOfNextPlaceable = indexOfNextPlaceableOptional.get();
                Collections.swap(this.getAffectedBlocksList(), indexOfPostponed, indexOfNextPlaceable);
            } else {
                this.incrementCounter();
                affectedBlockToDelay.setPlaced(true);
            }
        } else {
            this.incrementCounter();
            affectedBlockToDelay.setPlaced(true);
        }
    }

    private Optional<Integer> findNextPlaceableBlock(MinecraftServer server) {
        for (int i = this.getCurrentAffectedBlockCounter(); i < this.getAffectedBlocksList().size(); i++) {
            if (this.getAffectedBlocksList().get(i).canBePlaced(server)) {
                return Optional.of(i);
            }
        }
        return Optional.empty();
    }

    private void setUpExplosionHealingMode(World world){
        switch(this.getExplosionMode()){
            case DAYTIME_HEALING_MODE -> this.setupDayTimeHealingMode(world);
            case DIFFICULTY_BASED_HEALING_MODE -> this.setupDifficultyBasedHealingMode(world);
            case BLAST_RESISTANCE_BASED_HEALING_MODE -> this.setupBlastResistanceBasedHealingMode(world);
        }
    }

    private void setupDayTimeHealingMode(World world){
        this.setExplosionTimer(24000 - (world.getTimeOfDay() % 24000));
        int daylightBasedBlockPlacementDelay = 13000/Math.max(this.getAffectedBlocksList().size(), 1);
        for(AffectedBlock affectedBlock : this.getAffectedBlocksList()){
            affectedBlock.setAffectedBlockTimer(daylightBasedBlockPlacementDelay);
        }
    }
    public boolean hasEnoughLightIfDaytimeHealingMode(MinecraftServer server){

        //We return true if the current block counter is greater than 0,
        //since we want to allow explosions to heal completely if the light conditions were only met initially
        if (this.getAffectedBlockCounter() > 0 || this.getExplosionMode() != ExplosionHealingMode.DAYTIME_HEALING_MODE) return true;
        return this.getAffectedBlocksList().stream()
                .anyMatch(affectedBlock -> affectedBlock.getWorld(server).getLightLevel(LightType.BLOCK, affectedBlock.getPos()) > 0 || affectedBlock.getWorld(server).getLightLevel(LightType.SKY, affectedBlock.getPos()) > 0);
    }

    private void setupDifficultyBasedHealingMode(World world){
        int difficultyOffset = switch (world.getDifficulty()) {
            case PEACEFUL -> -2;
            case EASY -> -1;
            case NORMAL -> 1;
            case HARD -> 2;
        };
        long finalOffset = Math.max(1, (DelaysConfig.getBlockPlacementDelayAsTicks()) + (difficultyOffset * 20));
        long finalOffsetExplosion = Math.max(1, (DelaysConfig.getExplosionHealDelayAsTicks()) + (difficultyOffset * 20));
        this.setExplosionTimer(finalOffsetExplosion);
        this.getAffectedBlocksList().forEach(affectedBlock -> affectedBlock.setAffectedBlockTimer(finalOffset));
    }

    public boolean shouldKeepHealingIfDifficultyBasedHealingMode(World world){
        if (this.getExplosionMode() != ExplosionHealingMode.DIFFICULTY_BASED_HEALING_MODE || world.getDifficulty() != Difficulty.HARD) return true;
        Random random = world.getRandom();
        return random.nextBetween(0, 50) != 25;
    }

     private void setupBlastResistanceBasedHealingMode(World world){
         Random random = world.getRandom();
         this.getAffectedBlocksList().forEach(affectedBlock -> {
             double randomOffset = random.nextBetween(-2, 2);
             double affectedBlockBlastResistance = Math.min(affectedBlock.getState().getBlock().getBlastResistance(), 9);
             int offset = (int) (MathHelper.lerp(affectedBlockBlastResistance / 9, -2, 2) + randomOffset);
             long finalOffset = Math.max(1, DelaysConfig.getBlockPlacementDelayAsTicks() + (offset * 20L));
             affectedBlock.setAffectedBlockTimer(finalOffset);
         });
    }

    private static ExplosionEvent combineCollidingExplosions(Set<ExplosionEvent> collidingExplosions, ExplosionEvent newestExplosion, World world){
        List<AffectedBlock> combinedAffectedBlockList = collidingExplosions.stream()
                .flatMap(explosionEvent -> explosionEvent.getAffectedBlocksList().stream())
                .collect(Collectors.toList());
        ExplosionEvent explosionEvent = new ExplosionEvent(ExplosionUtils.sortAffectedBlocksList(combinedAffectedBlockList, world.getServer()), newestExplosion.getExplosionMode().getName(), newestExplosion.getExplosionTimer(), newestExplosion.getCurrentAffectedBlockCounter());
        long newestExplosionBlockTimers = DelaysConfig.getBlockPlacementDelayAsTicks();
        explosionEvent.getAffectedBlocksList().forEach(affectedBlock -> affectedBlock.setAffectedBlockTimer(newestExplosionBlockTimers));
        explosionEvent.setUpExplosionHealingMode(world);
        return explosionEvent;
    }

    public void markAffectedBlockAsPlaced(BlockState secondHalfState, BlockPos secondHalfPos, World world){
        CreeperHealing.setHealerHandlerLock(false);
        for(AffectedBlock affectedBlock : this.getAffectedBlocksList()) {
            if(affectedBlock.getState().equals(secondHalfState) && affectedBlock.getPos().equals(secondHalfPos) && affectedBlock.getWorldRegistryKey().equals(world.getRegistryKey())) {
                affectedBlock.setPlaced(true);
            }
        }
        CreeperHealing.setHealerHandlerLock(true);
    }

}

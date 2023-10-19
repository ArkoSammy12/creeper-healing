package xd.arkosammy.explosions;

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
import xd.arkosammy.CreeperHealing;
import xd.arkosammy.configuration.tables.DelaysConfig;
import xd.arkosammy.configuration.tables.ModeConfig;
import xd.arkosammy.handlers.ExplosionListHandler;

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
        ExplosionEvent explosionEvent = new ExplosionEvent(ExplosionUtils.sortAffectedBlocksList(affectedBlocksList, world.getServer()), ModeConfig.getHealingMode(), DelaysConfig.getExplosionHealDelay(), 0);
        explosionEvent.setUpExplosionHealingMode(world);

        Set<ExplosionEvent> collidingExplosions =  ExplosionUtils.getCollidingWaitingExplosions(affectedBlocksList.stream().map(AffectedBlock::getPos).collect(Collectors.toList()));
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

    public AffectedBlock getCurrentAffectedBlock(){
        if(this.affectedBlockCounter < this.getAffectedBlocksList().size()){
            return this.getAffectedBlocksList().get(affectedBlockCounter);
        }
        return null;
    }

    public void delayAffectedBlock(AffectedBlock blockToPostpone, MinecraftServer server){
        int indexOfPostponed = this.getAffectedBlocksList().indexOf(blockToPostpone);
        if(indexOfPostponed != -1) {
            Integer indexOfNextPlaceable = this.findNextPlaceableBlock(server);
            if (indexOfNextPlaceable != null) {
                Collections.swap(this.getAffectedBlocksList(), indexOfPostponed, indexOfNextPlaceable);
            } else {
                this.incrementCounter();
                blockToPostpone.setPlaced(true);
            }
        } else {
            this.incrementCounter();
            blockToPostpone.setPlaced(true);
        }
    }

    private Integer findNextPlaceableBlock(MinecraftServer server) {
        for (int i = this.getCurrentAffectedBlockCounter(); i < this.getAffectedBlocksList().size(); i++) {
            if (this.getAffectedBlocksList().get(i).canBePlaced(server)) {
                return i;
            }
        }
        return null;
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
        for(AffectedBlock affectedBlock : this.getAffectedBlocksList()){
            if (affectedBlock.getWorld(server).getLightLevel(LightType.BLOCK, affectedBlock.getPos()) > 0 || affectedBlock.getWorld(server).getLightLevel(LightType.SKY, affectedBlock.getPos()) > 0) {
                return true;
            }
        }
        return false;
    }

    private void setupDifficultyBasedHealingMode(World world){
        int difficultyOffset = 0;
        switch(world.getDifficulty()){
            case PEACEFUL -> difficultyOffset = -2;
            case EASY -> difficultyOffset = -1;
            case NORMAL -> difficultyOffset = 1;
            case HARD -> difficultyOffset = 2;
        }
        long finalOffset = Math.max(1, (DelaysConfig.getBlockPlacementDelay()) + (difficultyOffset * 20));
        long finalOffsetExplosion = Math.max(1, (DelaysConfig.getExplosionHealDelay()) + (difficultyOffset * 20));
        this.setExplosionTimer(finalOffsetExplosion);
        this.getAffectedBlocksList().forEach(affectedBlock -> affectedBlock.setAffectedBlockTimer(finalOffset));
    }

    public boolean shouldKeepHealingIfDifficultyBasedHealingMode(World world){
        if(this.getExplosionMode() != ExplosionHealingMode.DIFFICULTY_BASED_HEALING_MODE || world.getDifficulty() != Difficulty.HARD){
            return true;
        }
        Random random = world.getRandom();
        int randomNum = random.nextBetween(0, 50);
        return randomNum != 25;
    }

     private void setupBlastResistanceBasedHealingMode(World world){
        Random random = world.getRandom();
        this.getAffectedBlocksList().forEach(affectedBlock -> {
            double randomOffset = random.nextBetween(-2, 2);
            double affectedBlockBlastResistance = Math.min(affectedBlock.getState().getBlock().getBlastResistance(), 9);
            int offset = (int) (MathHelper.lerp(affectedBlockBlastResistance/9, -5, 5) + randomOffset);
            long finalOffset = Math.max(1, DelaysConfig.getBlockPlacementDelay() + (offset * 20L));
            affectedBlock.setAffectedBlockTimer(finalOffset);
        });
    }

    private static ExplosionEvent combineCollidingExplosions(Set<ExplosionEvent> collidingExplosions, ExplosionEvent newestExplosion, World world){
        List<AffectedBlock> combinedAffectedBlockList = collidingExplosions.stream()
                .flatMap(explosionEvent -> explosionEvent.getAffectedBlocksList().stream())
                .collect(Collectors.toList());
        ExplosionEvent explosionEvent = new ExplosionEvent(ExplosionUtils.sortAffectedBlocksList(combinedAffectedBlockList, world.getServer()), newestExplosion.getExplosionMode().getName(), newestExplosion.getExplosionTimer(), newestExplosion.getCurrentAffectedBlockCounter());
        long newestExplosionBlockTimers = DelaysConfig.getBlockPlacementDelay();
        explosionEvent.getAffectedBlocksList().forEach(affectedBlock -> affectedBlock.setAffectedBlockTimer(newestExplosionBlockTimers));
        explosionEvent.setUpExplosionHealingMode(world);
        return explosionEvent;
    }

    public void markAffectedBlockAsPlaced(BlockState secondHalfState, BlockPos secondHalfPos, World world){
        for(AffectedBlock affectedBlock : this.getAffectedBlocksList()) {
            if(affectedBlock.getState().equals(secondHalfState) && affectedBlock.getPos().equals(secondHalfPos) && affectedBlock.getWorldRegistryKey().equals(world.getRegistryKey())) {
                CreeperHealing.setHealerHandlerLock(false);
                affectedBlock.setPlaced(true);
                CreeperHealing.setHealerHandlerLock(true);
            }
        }
    }

}

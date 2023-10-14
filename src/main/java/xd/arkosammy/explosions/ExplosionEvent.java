package xd.arkosammy.explosions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import xd.arkosammy.CreeperHealing;
import xd.arkosammy.configuration.tables.DelaysConfig;
import xd.arkosammy.configuration.tables.ModeConfig;
import xd.arkosammy.configuration.tables.PreferencesConfig;
import xd.arkosammy.handlers.ExplosionListHandler;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class ExplosionEvent {

    private enum ExplosionMode{
        DEFAULT_MODE,
        DAYTIME_HEALING_MODE,
        DIFFICULTY_BASED_HEALING_MODE,
        WEATHER_BASED_HEALING_MODE,

    }

    private final List<AffectedBlock> affectedBlocksList;
    private long explosionTimer;
    private int affectedBlockCounter;
    private boolean dayTimeHealingMode;

    // Codec to serialize and deserialize ExplosionEvent instances.
    private static final Codec<ExplosionEvent> EXPLOSION_EVENT_CODEC = RecordCodecBuilder.create(creeperExplosionEventInstance -> creeperExplosionEventInstance.group(
            Codec.list(AffectedBlock.getCodec()).fieldOf("Affected_Blocks_List").forGetter(ExplosionEvent::getAffectedBlocksList),
            Codec.LONG.fieldOf("Explosion_Timer").forGetter(ExplosionEvent::getExplosionTimer),
            Codec.INT.fieldOf("Current_Block_Counter").forGetter(ExplosionEvent::getCurrentAffectedBlockCounter),
            Codec.BOOL.fieldOf("DayTime_Healing_Mode").forGetter(ExplosionEvent::isMarkedWithDayTimeHealingMode)
    ).apply(creeperExplosionEventInstance, ExplosionEvent::new));

    private ExplosionEvent(List<AffectedBlock> affectedBlocksList, long creeperExplosionTimer, int currentIndex, boolean dayTimeHealingMode){
        this.affectedBlockCounter = currentIndex;
        this.affectedBlocksList = new CopyOnWriteArrayList<>(affectedBlocksList);
        setExplosionTimer(creeperExplosionTimer);
        this.dayTimeHealingMode = dayTimeHealingMode;
    }

    public static ExplosionEvent newExplosionEvent(List<AffectedBlock> affectedBlocksList, World world) {
        ExplosionEvent explosionEvent = new ExplosionEvent(ExplosionUtils.sortAffectedBlocksList(affectedBlocksList, world.getServer()), DelaysConfig.getExplosionHealDelay(), 0, false);
        if (ModeConfig.getDayTimeHealingMode())
            explosionEvent.setupDayTimeHealing(world);

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

    public boolean isMarkedWithDayTimeHealingMode(){
        return this.dayTimeHealingMode;
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

    public boolean canHealIfRequiresLight(MinecraftServer server){

        //We return true if the current block counter is greater than 0,
        // since we want to allow explosions to heal completely if the light conditions were only met initially
        if (!PreferencesConfig.getRequiresLight() || this.getAffectedBlockCounter() > 0) return true;
        for(AffectedBlock affectedBlock : this.getAffectedBlocksList()){
            if (affectedBlock.getWorld(server).getLightLevel(LightType.BLOCK, affectedBlock.getPos()) > 0 || affectedBlock.getWorld(server).getLightLevel(LightType.SKY, affectedBlock.getPos()) > 0) {
                return true;
            }
        }
        return false;
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

    public void setupDayTimeHealing(World world){
        this.dayTimeHealingMode = true;
        this.setExplosionTimer(24000 - (world.getTimeOfDay() % 24000));
        int daylightBasedBlockPlacementDelay = 13000/Math.max(this.getAffectedBlocksList().size(), 1);
        for(AffectedBlock affectedBlock : this.getAffectedBlocksList()){
            affectedBlock.setAffectedBlockTimer(daylightBasedBlockPlacementDelay);
        }
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

    private Integer findNextPlaceableBlock(MinecraftServer server) {
        for (int i = this.getCurrentAffectedBlockCounter(); i < this.getAffectedBlocksList().size(); i++) {
            if (this.getAffectedBlocksList().get(i).canBePlaced(server)) {
                return i;
            }
        }
        return null;
    }

    private static ExplosionEvent combineCollidingExplosions(Set<ExplosionEvent> collidingExplosions, ExplosionEvent newestExplosion, World world){

        List<AffectedBlock> combinedAffectedBlockList = collidingExplosions.stream()
                .flatMap(explosionEvent -> explosionEvent.getAffectedBlocksList().stream())
                .collect(Collectors.toList());
        ExplosionEvent explosionEvent = new ExplosionEvent(ExplosionUtils.sortAffectedBlocksList(combinedAffectedBlockList, world.getServer()), newestExplosion.getExplosionTimer(), newestExplosion.getCurrentAffectedBlockCounter(), newestExplosion.isMarkedWithDayTimeHealingMode());
        long newestExplosionBlockTimers = DelaysConfig.getBlockPlacementDelay();
        explosionEvent.getAffectedBlocksList().forEach(affectedBlock -> affectedBlock.setAffectedBlockTimer(newestExplosionBlockTimers));
        if(explosionEvent.isMarkedWithDayTimeHealingMode())
            explosionEvent.setupDayTimeHealing(world);
        return explosionEvent;
    }

}

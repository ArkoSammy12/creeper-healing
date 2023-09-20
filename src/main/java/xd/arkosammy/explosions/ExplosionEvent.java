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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ExplosionEvent {

    private final List<AffectedBlock> affectedBlocksList;
    private long creeperExplosionTimer;
    private int affectedBlockCounter;
    private boolean dayTimeHealingMode;

    // Codec to serialize and deserialize ExplosionEvent instances.
    private static final Codec<ExplosionEvent> CREEPER_EXPLOSION_EVENT_CODEC = RecordCodecBuilder.create(creeperExplosionEventInstance -> creeperExplosionEventInstance.group(
            Codec.list(AffectedBlock.getCodec()).fieldOf("Affected_Blocks_List").forGetter(ExplosionEvent::getAffectedBlocksList),
            Codec.LONG.fieldOf("Creeper_Explosion_Timer").forGetter(ExplosionEvent::getCreeperExplosionTimer),
            Codec.INT.fieldOf("Current_Block_Counter").forGetter(ExplosionEvent::getCurrentAffectedBlockCounter),
            Codec.BOOL.fieldOf("DayTime_Healing_Mode").forGetter(ExplosionEvent::isMarkedWithDayTimeHealingMode)
    ).apply(creeperExplosionEventInstance, ExplosionEvent::new));

    private ExplosionEvent(List<AffectedBlock> affectedBlocksList, long creeperExplosionTimer, int currentIndex, boolean dayTimeHealingMode){
        this.affectedBlockCounter = currentIndex;
        this.affectedBlocksList = new CopyOnWriteArrayList<>(affectedBlocksList); //Gotta ensure mutability and thread safety
        setExplosionTimer(creeperExplosionTimer);
        this.dayTimeHealingMode = dayTimeHealingMode;
    }

    /**
     * Creates a new ExplosionEvent instance based on the given list of affected blocks and a world.
     *
     * @param affectedBlocksList The list of affected blocks.
     * @param world              The world in which the explosion occurred.
     * @return A new ExplosionEvent instance.
     */
    public static ExplosionEvent newExplosionEvent(List<AffectedBlock> affectedBlocksList, World world) {
        List<AffectedBlock> sortedAffectedBlockList = ExplosionUtils.sortAffectedBlocksList(affectedBlocksList, world.getServer());
        ExplosionEvent explosionEvent = new ExplosionEvent(sortedAffectedBlockList, DelaysConfig.getExplosionHealDelay(), 0, false);
        if (ModeConfig.getDayTimeHealingMode()) explosionEvent.setupDayTimeHealing(world);
        return explosionEvent;
    }

    public void setExplosionTimer(long delay){
        this.creeperExplosionTimer = delay;
    }

    public void incrementCounter() {
        this.affectedBlockCounter++;
    }

    public List<AffectedBlock> getAffectedBlocksList(){
        return this.affectedBlocksList;
    }

    public long getCreeperExplosionTimer(){
        return this.creeperExplosionTimer;
    }

    int getAffectedBlockCounter(){
        return this.affectedBlockCounter;
    }

    public boolean isMarkedWithDayTimeHealingMode(){
        return this.dayTimeHealingMode;
    }

    public int getCurrentAffectedBlockCounter(){
        return this.affectedBlockCounter;
    }

    static Codec<ExplosionEvent> getCodec(){
        return CREEPER_EXPLOSION_EVENT_CODEC;
    }


    //Iterate through all the CreeperExplosionEvents in our list and decrement each of their timers
    public static void tickExplosions(){
        for(ExplosionEvent explosionEvent : ExplosionListHandler.getExplosionEventList()){
            explosionEvent.creeperExplosionTimer--;
        }
    }

    public AffectedBlock getCurrentAffectedBlock(){
        if(this.affectedBlockCounter < this.getAffectedBlocksList().size()){
            return this.getAffectedBlocksList().get(affectedBlockCounter);
        }
        return null;
    }

    //Set up daytime healing mode for this explosion by making the explosion start healing at the next sunrise,
    // and make it finish healing when the next night falls
    public void setupDayTimeHealing(World world){

        this.dayTimeHealingMode = true;
        this.setExplosionTimer(24000 - (world.getTimeOfDay() % 24000));
        int daylightBasedBlockPlacementDelay = 13000/Math.max(this.getAffectedBlocksList().size(), 1);
        for(AffectedBlock affectedBlock : this.getAffectedBlocksList()){
            affectedBlock.setAffectedBlockTimer(daylightBasedBlockPlacementDelay);
        }

    }

    public void markSecondHalfAsPlaced(BlockState secondHalfState, BlockPos secondHalfPos, World world){
        for(AffectedBlock affectedBlock : this.getAffectedBlocksList()) {

            if(affectedBlock.getState().equals(secondHalfState) && affectedBlock.getPos().equals(secondHalfPos) && affectedBlock.getWorldRegistryKey().equals(world.getRegistryKey())) {
                CreeperHealing.setHealerHandlerLock(false);
                affectedBlock.setPlaced(true);
                CreeperHealing.setHealerHandlerLock(true);
            }

        }
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

    public void postponeBlock(AffectedBlock blockToPostpone, MinecraftServer server){

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

    private Integer findNextPlaceableBlock(MinecraftServer server){
        for(int i = this.getCurrentAffectedBlockCounter(); i < this.getAffectedBlocksList().size(); i++){

            if(this.getAffectedBlocksList().get(i).canBePlaced(server))
                return i;

        }
        return null;
    }

}

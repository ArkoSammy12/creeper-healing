package xd.arkosammy.events;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import xd.arkosammy.CreeperHealing;
import xd.arkosammy.handlers.ExplosionHealerHandler;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class CreeperExplosionEvent {

    private final List<AffectedBlock> affectedBlocksList;
    private long creeperExplosionTimer;
    private int affectedBlockCounter;
    private boolean dayTimeHealingMode;

    //Create codec for our CreeperExplosionEvent, which will contain a list of AffectedBlock codecs.
    public static final Codec<CreeperExplosionEvent> CODEC = RecordCodecBuilder.create(creeperExplosionEventInstance -> creeperExplosionEventInstance.group(

            Codec.list(AffectedBlock.CODEC).fieldOf("Affected_Blocks_List").forGetter(CreeperExplosionEvent::getAffectedBlocksList),
            Codec.LONG.fieldOf("Creeper_Explosion_Timer").forGetter(CreeperExplosionEvent::getCreeperExplosionTimer),
            Codec.INT.fieldOf("Current_Block_Counter").forGetter(CreeperExplosionEvent::getCurrentAffectedBlockCounter),
            Codec.BOOL.fieldOf("DayTime_Healing_Mode").forGetter(CreeperExplosionEvent::isMarkedWithDayTimeHealingMode)

    ).apply(creeperExplosionEventInstance, CreeperExplosionEvent::new));

    public CreeperExplosionEvent(List<AffectedBlock> affectedBlocksList, long creeperExplosionTimer, int currentIndex, boolean dayTimeHealingMode){

        //Sort our list of affected blocks according to their Y and transparency values
        this.affectedBlockCounter = currentIndex;
        this.affectedBlocksList = sortAffectedBlocksList(affectedBlocksList, CreeperHealing.getServerInstance());
        setCreeperExplosionTimer(creeperExplosionTimer);
        setDayTimeHealingMode(dayTimeHealingMode);

    }

    public void setCreeperExplosionTimer(long delay){
        this.creeperExplosionTimer = delay;
    }

    public void setDayTimeHealingMode(boolean dayTimeHealingMode) {
        this.dayTimeHealingMode = dayTimeHealingMode;
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

    public int getAffectedBlockCounter(){
        return this.affectedBlockCounter;
    }

    public boolean isMarkedWithDayTimeHealingMode(){
        return this.dayTimeHealingMode;
    }

    private int getCurrentAffectedBlockCounter(){
        return this.affectedBlockCounter;
    }


    public AffectedBlock getCurrentAffectedBlock(){

        if(this.affectedBlockCounter < this.getAffectedBlocksList().size()){

            return this.getAffectedBlocksList().get(affectedBlockCounter);

        }

        return null;

    }

    //Iterate through all the CreeperExplosionEvents in our list and decrement each of their delay counters
    public static void tickCreeperExplosionEvents(){

        for(CreeperExplosionEvent creeperExplosionEvent : ExplosionHealerHandler.getExplosionEventList()){

            creeperExplosionEvent.creeperExplosionTimer--;

        }

    }

    public void markSecondHalfAsPlaced(BlockState secondHalfState, BlockPos secondBlockPos, World world){

        for(AffectedBlock affectedBlock : this.getAffectedBlocksList()) {

            if(affectedBlock.getState().equals(secondHalfState) && affectedBlock.getPos().equals(secondBlockPos) && affectedBlock.getWorldRegistryKey().equals(world.getRegistryKey())) {

                CreeperHealing.setHealerHandlerLock(false);

                affectedBlock.setPlaced(true);

                CreeperHealing.setHealerHandlerLock(true);

            }

        }

    }

    //Set up daytime healing mode for this explosion by making the explosion start healing at the next sunrise,
    // and make it finish healing when the next night falls
    public void setupDayTimeHealing(World world){

        this.setDayTimeHealingMode(true);

        this.setCreeperExplosionTimer(24000 - (world.getTimeOfDay() % 24000));

        int daylightBasedBlockPlacementDelay = 13000/Math.max(this.getAffectedBlocksList().size(), 1);

        for(AffectedBlock affectedBlock : this.getAffectedBlocksList()){

            affectedBlock.setAffectedBlockTimer(daylightBasedBlockPlacementDelay);

        }

    }

    //TODO: Implement "requiresLight" feature

    private static @NotNull List<AffectedBlock> sortAffectedBlocksList(@NotNull List<AffectedBlock> affectedBlocksList, MinecraftServer server){

        List<AffectedBlock> sortedAffectedBlocks = new ArrayList<>(affectedBlocksList);

        Comparator<AffectedBlock> yLevelComparator = Comparator.comparingInt(affectedBlock -> affectedBlock.getPos().getY());

        sortedAffectedBlocks.sort(yLevelComparator);

        if(server != null) {

            Comparator<AffectedBlock> transparencyComparator = (affectedBlock1, affectedBlock2) -> {

                boolean isBlockInfo1Transparent = affectedBlock1.getState().isTransparent(affectedBlock1.getWorld(server), affectedBlock1.getPos());
                boolean isBlockInfo2Transparent = affectedBlock2.getState().isTransparent(affectedBlock2.getWorld(server), affectedBlock2.getPos());
                return Boolean.compare(isBlockInfo1Transparent, isBlockInfo2Transparent);

            };

            sortedAffectedBlocks.sort(transparencyComparator);

        }

        return sortedAffectedBlocks;

    }

}

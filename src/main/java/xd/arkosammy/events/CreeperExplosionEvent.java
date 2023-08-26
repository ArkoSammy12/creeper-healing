package xd.arkosammy.events;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * CreeperExplosionEvent instances will be created upon an explosion of a creeper.
 * It will contain a list of BlockInfo instances,
 * the delay that this event should wait in ticks before being processed, and
 * an internal counter to iterate through the list of BlockInfo instances as each
 * one of them is placed.
 */
public class CreeperExplosionEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1212L;
    private static List<CreeperExplosionEvent> explosionEventsForUsage = new CopyOnWriteArrayList<>();
    private List<BlockInfo> blockList;
    private long creeperExplosionDelay;
    private int currentCounter; //To let us know what block we are currently placing

    //Create codec for our CreeperExplosionEvent, which will contain a list of BlockInfo codecs.
    public static final Codec<CreeperExplosionEvent> CODEC = RecordCodecBuilder.create(creeperExplosionEventInstance -> creeperExplosionEventInstance.group(

            Codec.list(BlockInfo.CODEC).fieldOf("Block_Info_List").forGetter(CreeperExplosionEvent::getBlockList),
            Codec.LONG.fieldOf("Creeper_Explosion_Delay").forGetter(CreeperExplosionEvent::getCreeperExplosionDelay),
            Codec.INT.fieldOf("Current_Block_Counter").forGetter(CreeperExplosionEvent::getCurrentCounter)

    ).apply(creeperExplosionEventInstance, CreeperExplosionEvent::new));

    public CreeperExplosionEvent(List<BlockInfo> blockList, long creeperExplosionDelay, int currentCounter){

        setBlockList(blockList);

        setCreeperExplosionDelay(creeperExplosionDelay);

        setCurrentCounter(currentCounter);

    }

    private void setBlockList(List<BlockInfo> blockList){

        this.blockList = blockList;

    }

    private void setCreeperExplosionDelay(long delay){

        this.creeperExplosionDelay = delay;

    }

    private void setCurrentCounter(int currentCounter){

        this.currentCounter = currentCounter;

    }

    public List<BlockInfo> getBlockList(){

        return this.blockList;

    }

    public BlockInfo getCurrentBlockInfo(){

        if(this.currentCounter < this.getBlockList().size()){

            return this.getBlockList().get(currentCounter);

        }

        return null;

    }

    public long getCreeperExplosionDelay(){

        return this.creeperExplosionDelay;

    }

    private int getCurrentCounter(){

        return this.currentCounter;

    }

    public void incrementCounter(){

        this.currentCounter++;

    }

    public static List<CreeperExplosionEvent> getExplosionEventsForUsage(){

        return explosionEventsForUsage;

    }

    //Iterate through explosionEventsForUsage list and decrement each of their delay counters
    public static void tickCreeperExplosionEvents(){

        for(CreeperExplosionEvent creeperExplosionEvent : CreeperExplosionEvent.getExplosionEventsForUsage()){

            creeperExplosionEvent.tickSingleEvent();

        }

    }

    private void tickSingleEvent(){

        this.creeperExplosionDelay--;

    }

}

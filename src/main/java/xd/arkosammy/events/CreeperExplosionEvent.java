package xd.arkosammy.events;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import xd.arkosammy.CreeperHealing;
import xd.arkosammy.handlers.ExplosionHealerHandler;
import xd.arkosammy.util.BlockInfo;
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

    private int currentCounter;

    //Create codec for our CreeperExplosionEvent, which will contain a list of BlockInfo codecs.
    public static final Codec<CreeperExplosionEvent> CODEC = RecordCodecBuilder.create(creeperExplosionEventInstance -> creeperExplosionEventInstance.group(

            Codec.list(BlockInfo.CODEC).fieldOf("Block_Info_List").forGetter(CreeperExplosionEvent::getBlockList)

    ).apply(creeperExplosionEventInstance, CreeperExplosionEvent::new));

    public CreeperExplosionEvent(List<BlockInfo> blockList){

        setBlockList(blockList);

        setCreeperExplosionDelay(ExplosionHealerHandler.getExplosionDelay());

        setCurrentCounter();

    }

    private void setBlockList(List<BlockInfo> blockList){

        this.blockList = blockList;

    }

    private void setCreeperExplosionDelay(int delay){

        this.creeperExplosionDelay = delay * 20L;

    }

    private void setCurrentCounter(){

        this.currentCounter = 0;

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

    public void incrementCounter(){

        this.currentCounter++;

    }

    public static List<CreeperExplosionEvent> getExplosionEventsForUsage(){

        return explosionEventsForUsage;

    }

    //Iterate through explosionEventsForUsage list and increment each of their delay counters
    public static void tickCreeperExplosionEvents(){

        for(CreeperExplosionEvent creeperExplosionEvent : CreeperExplosionEvent.getExplosionEventsForUsage()){

            creeperExplosionEvent.tickSingleEvent();

        }

    }

    private void tickSingleEvent(){

        this.creeperExplosionDelay--;

    }

}

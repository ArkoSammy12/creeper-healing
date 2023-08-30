package xd.arkosammy.events;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import xd.arkosammy.CreeperHealing;
import java.io.Serial;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class CreeperExplosionEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1212L;
    private static List<CreeperExplosionEvent> explosionEventList = new CopyOnWriteArrayList<>();
    private List<AffectedBlock> blockList;
    private long creeperExplosionDelay;
    private int currentIndex;

    //Create codec for our CreeperExplosionEvent, which will contain a list of AffectedBlock codecs.
    public static final Codec<CreeperExplosionEvent> CODEC = RecordCodecBuilder.create(creeperExplosionEventInstance -> creeperExplosionEventInstance.group(

            Codec.list(AffectedBlock.CODEC).fieldOf("Block_Info_List").forGetter(CreeperExplosionEvent::getAffectedBlocks),
            Codec.LONG.fieldOf("Creeper_Explosion_Delay").forGetter(CreeperExplosionEvent::getCreeperExplosionDelay),
            Codec.INT.fieldOf("Current_Block_Counter").forGetter(CreeperExplosionEvent::getCurrentIndex)

    ).apply(creeperExplosionEventInstance, CreeperExplosionEvent::new));

    public CreeperExplosionEvent(List<AffectedBlock> blockList, long creeperExplosionDelay, int currentIndex){

        setBlockList(blockList);
        setCreeperExplosionDelay(creeperExplosionDelay);
        setCurrentIndex(currentIndex);

    }

    private void setBlockList(List<AffectedBlock> blockList){

        //Sort our list of affected blocks according to their Y and transparency values
        this.blockList = customSort(blockList, CreeperHealing.getServerInstance());

    }

    private void setCreeperExplosionDelay(long delay){

        this.creeperExplosionDelay = delay;

    }

    private void setCurrentIndex(int currentIndex){

        this.currentIndex = currentIndex;

    }

    public List<AffectedBlock> getAffectedBlocks(){

        return this.blockList;

    }

    public AffectedBlock getCurrentBlockInfo(){

        if(this.currentIndex < this.getAffectedBlocks().size()){

            return this.getAffectedBlocks().get(currentIndex);

        }

        return null;

    }

    public long getCreeperExplosionDelay(){

        return this.creeperExplosionDelay;

    }

    private int getCurrentIndex(){

        return this.currentIndex;

    }

    public static List<CreeperExplosionEvent> getExplosionEventList(){

        return explosionEventList;

    }

    //Iterate through all the CreeperExplosionEvents in our list and decrement each of their delay counters
    public static void tickCreeperExplosionEvents(){

        for(CreeperExplosionEvent creeperExplosionEvent : CreeperExplosionEvent.getExplosionEventList()){

            creeperExplosionEvent.creeperExplosionDelay--;

        }

    }

    public void incrementIndex() {

        this.currentIndex++;

    }

    public void markSecondHalfAsPlaced(BlockState secondHalfState, BlockPos secondBlockPos, World world){

        for(AffectedBlock affectedBlock : this.getAffectedBlocks()) {

            if(affectedBlock.getBlockState().equals(secondHalfState) && affectedBlock.getPos().equals(secondBlockPos) && affectedBlock.getWorldRegistryKey().equals(world.getRegistryKey())) {

                CreeperHealing.setHealerHandlerLock(false);

                affectedBlock.setHasBeenPlaced(true);

                CreeperHealing.setHealerHandlerLock(true);

            }

        }

    }

    private static @NotNull List<AffectedBlock> customSort(@NotNull List<AffectedBlock> affectedBlockList, MinecraftServer server){

        Comparator<AffectedBlock> yCoordComparator = Comparator.comparingInt(affectedBlock -> affectedBlock.getPos().getY());

        affectedBlockList.sort(yCoordComparator);

        if(server != null) {

            Comparator<AffectedBlock> transprencyComparator = (affectedBlock1, affectedBlock2) -> {

                boolean isBlockInfo1Transparent = affectedBlock1.getBlockState().isTransparent(affectedBlock1.getWorld(server), affectedBlock1.getPos());
                boolean isBlockInfo2Transparent = affectedBlock2.getBlockState().isTransparent(affectedBlock2.getWorld(server), affectedBlock2.getPos());

                return Boolean.compare(isBlockInfo1Transparent, isBlockInfo2Transparent);

            };

            affectedBlockList.sort(transprencyComparator);

        }

        return affectedBlockList;

    }

}

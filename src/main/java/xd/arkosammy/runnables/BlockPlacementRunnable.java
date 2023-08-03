package xd.arkosammy.runnables;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xd.arkosammy.util.BlockInfo;
import xd.arkosammy.handlers.ExplosionHealerHandler;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class BlockPlacementRunnable implements Runnable{

    private ArrayList<BlockInfo> blockInfoList;

    private World world;

    private int currentIndex = 0;

    //Constructor for the Runnable BlockPlacement class to give to the .schedule() function, to schedule a new tick event
    public BlockPlacementRunnable(World world, ArrayList<BlockInfo> blockInfoList){

        this.blockInfoList = blockInfoList;
        this.world = world;

    }

    //The run method of a Runnable gets called the executorService at the specified delay

    @Override
    public void run() {

        ArrayList<BlockInfo> blockInfoSorted = BlockInfo.getAsYSorted(blockInfoList); //Get the blockInfoList sorted from less to greater Y value

        if(currentIndex < blockInfoSorted.size()) {

            BlockInfo blockInfo = blockInfoSorted.get(currentIndex);

            BlockPos pos = blockInfo.getPos();

            BlockState state = blockInfo.getBlockState();

            //Bitwise shift to the right 4 times (divide by 16) to get actual chunk coordinates from the normal coordinates
            //It turns out it doesn't matter if we run a setBlock on an unloaded chunk but I'll keep this here just in case

            if (/*world.isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4) && */world.getBlockState(pos).equals(Blocks.AIR.getDefaultState())) { //Let's not override a block placed by a player

                ExplosionHealerHandler.placeBlock(world, pos, state);

            }
            //Increment our iterator index and recursively schedule ourselves to achieve our delay between block placements. Continue this until the current index is greater than the size of blockInfoSorted
            //This also ensures sequential processing in the order specified in blockInfoSorted
            currentIndex++;
            ExplosionHealerHandler.explosionExecutorService.schedule(this, ExplosionHealerHandler.getBlockPlacementDelay(), TimeUnit.SECONDS);

        }
    }

}

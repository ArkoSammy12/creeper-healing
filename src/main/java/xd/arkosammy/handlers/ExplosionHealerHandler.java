package xd.arkosammy.handlers;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xd.arkosammy.util.BlockHealReplaceList;
import xd.arkosammy.util.BlockInfo;
import xd.arkosammy.CreeperHealing;
import xd.arkosammy.events.CreeperExplosionEvent;
import xd.arkosammy.runnables.BlockPlacementRunnable;
import xd.arkosammy.runnables.ExplosionQueueRunnable;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ExplosionHealerHandler {

    public static ScheduledExecutorService explosionExecutorService = Executors.newSingleThreadScheduledExecutor();
    private static int explosionDelay = CreeperHealing.CONFIG.explosionHealDelay; // Adjust the delay between each explosion restoration (100 ticks = 5 seconds)
    private static int blockPlacementDelay = CreeperHealing.CONFIG.blockPlacementDelay; // Adjust the delay between each block placement (1 tick = 0.05 seconds)

    public static int getExplosionDelay(){

        return explosionDelay;

    }

    public static int getBlockPlacementDelay(){

        return blockPlacementDelay;

    }

    public static void setExplosionDelay(int delay){

        explosionDelay = delay;

    }

    public static void setBlockPlacementDelayTicks(int delay){

        blockPlacementDelay = delay;

    }
    public void processExplosionQueue(World world){

        //CreeperHealing.LOGGER.info(String.valueOf(explosionDelay));
        //CreeperHealing.LOGGER.info(String.valueOf(blockPlacementDelay));


        CreeperExplosionEvent explosionEvent = CreeperExplosionEvent.getExplosionEvents().poll();

        if(explosionEvent != null){

            ArrayList<BlockInfo> blockInfoList = explosionEvent.getBlockList();

            //Schedule a new TickSchedule to handle our blockInfoList, that is, healing one creeper explosion. Pass in the world, blockInfolist, the delay and our TimeUnit
            explosionExecutorService.schedule(new ExplosionQueueRunnable(world, blockInfoList), explosionDelay, TimeUnit.SECONDS);

        }

    }

    public static void handleBlockList(World world, ArrayList<BlockInfo> blockInfoList){

        //Schedule a new TickSchedule to place back our destroyed blocks. Pass in the world, the blockInfoList, our delay and the Time Unit
        explosionExecutorService.schedule(new BlockPlacementRunnable(world, blockInfoList), blockPlacementDelay, TimeUnit.SECONDS);

    }

    public static void placeBlock(World world, BlockPos pos, BlockState state){

        if(BlockHealReplaceList.getReplaceList().containsKey(state.getBlock())){

            state = BlockHealReplaceList.getReplaceList().get(state.getBlock()).getDefaultState();

        }

        world.setBlockState(pos, state);

    }

    //Register our new TickEventHandler, and call this function in our entrypoint over at CreeperHealing.class
    public void registerTickEventHandler(){

        ServerTickEvents.END_WORLD_TICK.register(this::processExplosionQueue);

    }

}

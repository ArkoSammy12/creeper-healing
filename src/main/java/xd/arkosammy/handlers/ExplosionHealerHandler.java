package xd.arkosammy.handlers;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xd.arkosammy.util.BlockInfo;
import xd.arkosammy.CreeperHealing;
import xd.arkosammy.events.CreeperExplosionEvent;
import xd.arkosammy.runnables.BlockPlacementRunnable;
import xd.arkosammy.runnables.ExplosionQueueRunnable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ExplosionHealerHandler {

    //Static ScheduledExecutorService to insure this is shared across all instances of the mod, allowing centralized control
    public static ScheduledExecutorService explosionExecutorService;
    private static int explosionDelay = CreeperHealing.CONFIG.explosionHealDelay; // Adjust the delay between each explosion restoration (100 ticks = 5 seconds)
    private static int blockPlacementDelay = CreeperHealing.CONFIG.blockPlacementDelay; // Adjust the delay between each block placement (1 tick = 0.05 seconds)

    private static HashMap<String, String> customReplaceList = CreeperHealing.CONFIG.replaceMap;
    public static int getExplosionDelay(){

        return explosionDelay;

    }

    public static int getBlockPlacementDelay(){

        return blockPlacementDelay;

    }

    public static void setExplosionDelay(int delay){

        //Do not let the user set this config below 1
        explosionDelay = Math.max(delay, 1);

    }

    public static void setBlockPlacementDelayTicks(int delay){

        //Do not let the user set this config below 1
        blockPlacementDelay = Math.max(delay, 1);

    }

    public static void setCustomReplaceList(HashMap<String, String> replaceList){

        customReplaceList = replaceList;

    }

    //Called by ServerTickEvents.END_WORLD_TICK and is responsible for processing the explosion event after the specified delay
    public void handleExplosionQueue(World world){

        //Poll a member of the CreeperExplosionEvent queue and check if it is null. If it's not, go ahead and handle it
        CreeperExplosionEvent explosionEvent = CreeperExplosionEvent.getExplosionEvents().poll();

        if(explosionEvent != null){

            //Get our blockInfoList and pass it to the constructor of the ExplosionQueueRunnable

            ArrayList<BlockInfo> blockInfoList = explosionEvent.getBlockList();

            //Schedule a new ExplosionQueueRunnable to handle our blockInfoList, that is, healing one creeper explosion. Pass in the world, blockInfolist, the delay and our TimeUnit
            explosionExecutorService.schedule(new ExplosionQueueRunnable(world, blockInfoList), getExplosionDelay(), TimeUnit.SECONDS);

        }

    }

    public static void handleBlockList(World world, ArrayList<BlockInfo> blockInfoList){

        //Schedule a new BlockPlacementRunnable to place back our destroyed blocks. Pass in the world, the blockInfoList, our delay and the Time Unit
        explosionExecutorService.schedule(new BlockPlacementRunnable(world, blockInfoList), getBlockPlacementDelay(), TimeUnit.SECONDS);

    }

    public static void placeBlock(World world, BlockPos pos, BlockState state){

        //Check if the block string of the block we are about to place is contained in our replaceMap. If it is, switch it for the corresponding block
        String blockString = Registries.BLOCK.getId(state.getBlock()).toString();

        if(customReplaceList.containsKey(blockString)){

            //Get BlockState via the block registries
            state = Registries.BLOCK.get(new Identifier(customReplaceList.get(blockString))).getDefaultState();

        }

        world.setBlockState(pos, state);

    }

    //Register our new ServerTickEvent, and call this function in our entrypoint over at CreeperHealing.class
    //At the end of each world tick, call the handleExplosionQueue() function.
    public void registerTickEventHandler(){

        explosionExecutorService = Executors.newSingleThreadScheduledExecutor();

        ServerTickEvents.END_WORLD_TICK.register(this::handleExplosionQueue);

    }

}

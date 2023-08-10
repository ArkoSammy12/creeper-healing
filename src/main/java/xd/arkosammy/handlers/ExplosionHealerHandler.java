package xd.arkosammy.handlers;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import xd.arkosammy.util.BlockInfo;
import xd.arkosammy.CreeperHealing;
import xd.arkosammy.events.CreeperExplosionEvent;

import java.util.HashMap;
import java.util.concurrent.ScheduledExecutorService;

public class ExplosionHealerHandler {

    //Static ScheduledExecutorService to insure this is shared across all instances of the mod, allowing centralized control
    public static ScheduledExecutorService explosionExecutorService;
    private static int explosionDelay = CreeperHealing.CONFIG.explosionHealDelay; // Adjust the delay between each explosion restoration (100 ticks = 5 seconds)
    private static int blockPlacementDelay = CreeperHealing.CONFIG.blockPlacementDelay; // Adjust the delay between each block placement (1 tick = 0.05 seconds)
    private static HashMap<String, String> customReplaceList = CreeperHealing.CONFIG.replaceMap;

    private static MinecraftServer minecraftServer;
    public static int getExplosionDelay(){

        return explosionDelay;

    }

    public static int getBlockPlacementDelay(){

        return blockPlacementDelay;

    }

    public static MinecraftServer getMinecraftServer(){

        return minecraftServer;

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
    public void handleExplosionQueue(MinecraftServer server){

        CreeperExplosionEvent.tickCreeperExplosionEvents();


        if(!CreeperExplosionEvent.getExplosionEventsForUsage().isEmpty()){

            for(CreeperExplosionEvent creeperExplosionEvent : CreeperExplosionEvent.getExplosionEventsForUsage()){

                CreeperHealing.LOGGER.info(String.valueOf(creeperExplosionEvent.getCreeperExplosionDelay()));

                if(creeperExplosionEvent.getCreeperExplosionDelay() < 0){

                    CreeperHealing.LOGGER.info("Found creeper explosion to heal");

                    BlockInfo currentBlock = creeperExplosionEvent.getCurrentBlockInfo();

                    if(currentBlock != null) {

                        currentBlock.tickSingleBlockInfo();

                        if (currentBlock.getBlockPlacementDelay() < 0) {

                            CreeperHealing.LOGGER.info("Placing block");
                            CreeperHealing.LOGGER.info("{} {}",currentBlock.getPos(),currentBlock.getBlockState());

                            placeBlock(currentBlock.getWorld(server), currentBlock.getPos(), currentBlock.getBlockState());

                            creeperExplosionEvent.incrementCounter();

                        }

                    } else {

                        CreeperHealing.LOGGER.info("Finished healing explosion");

                        CreeperExplosionEvent.getExplosionEventsForUsage().remove(creeperExplosionEvent);
                        CreeperHealing.SCHEDULED_CREEPER_EXPLOSIONS.getScheduledCreeperExplosionsForStoring().remove(creeperExplosionEvent);


                    }


                }


            }


        }

    }

    public static void placeBlock(World world, BlockPos pos, @NotNull BlockState state){

        //Check if the block string of the block we are about to place is contained in our replaceMap. If it is, switch it for the corresponding block
        String blockString = Registries.BLOCK.getId(state.getBlock()).toString();

        if(customReplaceList.containsKey(blockString)){

            //Get BlockState via the block registries
            state = Registries.BLOCK.get(new Identifier(customReplaceList.get(blockString))).getDefaultState();

        }

        if(world.getBlockState(pos).equals(Blocks.AIR.getDefaultState())) {

            world.setBlockState(pos, state);
            CreeperHealing.LOGGER.info("Placed block");

        }



    }

    //Register our new ServerTickEvent, and call this function in our entrypoint over at CreeperHealing.class
    //At the end of each world tick, call the handleExplosionQueue() function.
    public void registerTickEventHandler(MinecraftServer server){

        ServerTickEvents.END_SERVER_TICK.register(this::handleExplosionQueue);

    }

}

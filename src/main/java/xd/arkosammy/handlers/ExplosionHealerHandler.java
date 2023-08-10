package xd.arkosammy.handlers;

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

public class ExplosionHealerHandler {

    // Adjust the delay between each explosion restoration (100 ticks = 5 seconds)
    private static int explosionDelay = CreeperHealing.CONFIG.explosionHealDelay;

    // Adjust the delay between each block placement (1 tick = 0.05 seconds)
    private static int blockPlacementDelay = CreeperHealing.CONFIG.blockPlacementDelay;
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

    //Called at the end of each server tick
    public static void handleExplosionQueue(MinecraftServer server){

        //Tick each one of CreeperExplosionEvent instances in our list
        CreeperExplosionEvent.tickCreeperExplosionEvents();

        //Scary empty check
        if(!CreeperExplosionEvent.getExplosionEventsForUsage().isEmpty()){

            //Iterate through each our CreeperExplosionEvent instances in our list and find any whose delay counter has reached 0
            for(CreeperExplosionEvent creeperExplosionEvent : CreeperExplosionEvent.getExplosionEventsForUsage()){

                if(creeperExplosionEvent.getCreeperExplosionDelay() < 0){

                    //The currentBlock that we pick is controlled by the internal currentCounter variable,
                    // allowing us to iterate
                    BlockInfo currentBlock = creeperExplosionEvent.getCurrentBlockInfo();

                    if(currentBlock != null) {

                        //Tick the internal delay counter contained in this BlockInfo instance.
                        //Once it reaches 0, we can proceed to place it, and increment the internal currentCounter of this
                        //CreeperExplosionEvent instance to iterate to the next BlockInfo instance
                        currentBlock.tickSingleBlockInfo();

                        if (currentBlock.getBlockPlacementDelay() < 0) {

                            placeBlock(currentBlock.getWorld(server), currentBlock.getPos(), currentBlock.getBlockState());

                            creeperExplosionEvent.incrementCounter();

                        }

                    } else {

                        //If the BlockInfo instance that we get is null,
                        //that means that the internal counter has gone above the size of the BlockInfo list
                        //in our current creeperExplosionEvent,
                        //meaning this explosion has been fully healed, and we can remove it from the list.
                        CreeperExplosionEvent.getExplosionEventsForUsage().remove(creeperExplosionEvent);

                    }


                }


            }

        }

    }

    public static void placeBlock(World world, BlockPos pos, @NotNull BlockState state){

        //Check if the block string of the block we are about to place is contained in our replaceMap. If it is, switch it for the corresponding block
        String blockString = Registries.BLOCK.getId(state.getBlock()).toString();

        if(customReplaceList.containsKey(blockString)){

            //Get BlockState via the block registries.
            //Note that this gets the default state of the block, so stuff like the orientation of the block is lost.
            state = Registries.BLOCK.get(new Identifier(customReplaceList.get(blockString))).getDefaultState();

        }

        //Only place the block if where we are about to place it there is no other block.
        //This way we avoid overriding a block placed by a player.
        if(world.getBlockState(pos).equals(Blocks.AIR.getDefaultState())) {

            world.setBlockState(pos, state);

        }

    }

}

package xd.arkosammy.handlers;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import xd.arkosammy.util.BlockInfo;
import xd.arkosammy.CreeperHealing;
import xd.arkosammy.events.CreeperExplosionEvent;

import static xd.arkosammy.CreeperHealing.CONFIG;

//Thanks to @dale8689 for helping me figure out how to use tick timers instead of ScheduledFutures
public class ExplosionHealerHandler {

    //Called at the end of each server tick
    public static void handleExplosionQueue(MinecraftServer server){

        //Only start manipulating our list after we've made sure we've read our list. Let's not accidentally throw a
        //ConcurrentModificationException :v
        if(CreeperHealing.hasReadConfig()) {

            //Tick each one of CreeperExplosionEvent instances in our list
            CreeperExplosionEvent.tickCreeperExplosionEvents();

            //Scary empty check
            if (!CreeperExplosionEvent.getExplosionEventsForUsage().isEmpty()) {

                //Iterate through each our CreeperExplosionEvent instances in our list and find any whose delay counter has reached 0
                for (CreeperExplosionEvent creeperExplosionEvent : CreeperExplosionEvent.getExplosionEventsForUsage()) {

                    if (creeperExplosionEvent.getCreeperExplosionDelay() < 0) {

                        //The currentBlock that we pick is controlled by the internal currentCounter variable,
                        // allowing us to iterate
                        BlockInfo currentBlock = creeperExplosionEvent.getCurrentBlockInfo();

                        if (currentBlock != null) {

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

    }

    private static void placeBlock(World world, BlockPos pos, @NotNull BlockState state){

        //Check if the block string of the block we are about to place is contained in our replaceMap. If it is, switch it for the corresponding block
        String blockString = Registries.BLOCK.getId(state.getBlock()).toString();

        if(CONFIG.getReplaceMap().containsKey(blockString)){

            //Get BlockState via the block registries.
            //Note that this gets the default state of the block, so stuff like the orientation of the block is lost.
            state = Registries.BLOCK.get(new Identifier(CONFIG.getReplaceMap().get(blockString))).getDefaultState();

        }

        if(shouldPlaceBlock(world, pos)) {

            world.setBlockState(pos, state);

        }

    }

    private static boolean shouldPlaceBlock(@NotNull World world, BlockPos pos){

        //Place a block if the current coordinate contains an air block, to avoid overriding a player placed block
        if(world.getBlockState(pos).equals(Blocks.AIR.getDefaultState())) {

            return true;

            //Otherwise, place a block if the current coordinate contains a flowing water block,
            //and the corresponding setting is enabled
        } else if(world.getBlockState(pos).getFluidState().getFluid().equals(Fluids.FLOWING_WATER) && CONFIG.shouldHealOnFlowingWater()){

            return true;

            //Otherwise, place a block if the current coordinate contains a flowing laval block,
            //and the corresponding setting is enabled
        } else return world.getBlockState(pos).getFluidState().getFluid().equals(Fluids.FLOWING_LAVA) && CONFIG.shouldHealOnFlowingLava();

    }

}

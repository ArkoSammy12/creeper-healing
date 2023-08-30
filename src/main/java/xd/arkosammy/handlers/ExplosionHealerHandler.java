package xd.arkosammy.handlers;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import xd.arkosammy.events.AffectedBlock;
import xd.arkosammy.CreeperHealing;
import xd.arkosammy.events.CreeperExplosionEvent;

import static xd.arkosammy.CreeperHealing.CONFIG;

//Thanks to @dale8689 for helping me figure out how to use tick timers instead of ScheduledFutures
public class ExplosionHealerHandler {

    //Called at the end of each server tick
    public static void handleExplosionQueue(MinecraftServer server){

        //Safety lock for avoiding any potential issues with concurrency
        if(CreeperHealing.isExplosionHandlingUnlocked()) {

            //Tick each one of CreeperExplosionEvent instances in our list
            CreeperExplosionEvent.tickCreeperExplosionEvents();

            if (!CreeperExplosionEvent.getExplosionEventList().isEmpty()) {

                //Find a CreeperExplosionEvent in our list whose delay has reached 0
                for (CreeperExplosionEvent creeperExplosionEvent : CreeperExplosionEvent.getExplosionEventList()) {

                    if (creeperExplosionEvent.getCreeperExplosionTimer() < 0) {

                        //Get the current block to heal based on the event's internal block counter
                        AffectedBlock currentBlock = creeperExplosionEvent.getCurrentAffectedBlock();

                        //If the currentBlock is null, there are no more blocks to heal.
                        // We can remove this CreeperExplosionEvent from the list
                        if (currentBlock != null) {

                            //If the current block has already been placed, increment the counter to skip it
                            if(!currentBlock.hasBeenPlaced()) {

                                //Tick the current block place and check if its delay is less than 0
                                currentBlock.tickSingleBlockInfo();

                                if (currentBlock.getAffectedBlockTimer() < 0) {

                                    placeBlock(currentBlock.getWorld(server), currentBlock.getPos(), currentBlock.getBlockState(), creeperExplosionEvent);

                                    //Increment this event's internal counter to move on to the next block
                                    creeperExplosionEvent.incrementIndex();

                                    //Mark the block as placed
                                    currentBlock.setHasBeenPlaced(true);

                                }

                            } else {

                                creeperExplosionEvent.incrementIndex();

                            }

                        } else {

                            CreeperExplosionEvent.getExplosionEventList().remove(creeperExplosionEvent);

                        }

                    }

                }

            }

        }

    }

    private static void placeBlock(World world, BlockPos pos, @NotNull BlockState state, CreeperExplosionEvent creeperExplosionEvent){

        //Check if the block we are about to place is in the replace-list.
        //If it is, switch the state for the corresponding one in the replace-list.
        String blockString = Registries.BLOCK.getId(state.getBlock()).toString();

        if(CONFIG.getReplaceList().containsKey(blockString)){

            //The downside of this is that we only get the default state of the block
            state = Registries.BLOCK.get(new Identifier(CONFIG.getReplaceList().get(blockString))).getDefaultState();

        }

        //If the block we are about to place is "special", handle it separately
        if(!SpecialBlockHandler.isSpecialBlock(world, state, pos, creeperExplosionEvent)) {

            if(shouldPlaceBlock(world, pos)) {

                world.setBlockState(pos, state);

                //The first argument being null tells the server to play the sound to all nearby players
                if(shouldPlaySound(world, state)) world.playSound(null, pos, state.getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, state.getSoundGroup().getVolume(), state.getSoundGroup().getPitch());


            }

        }

    }

    private static boolean shouldPlaceBlock(@NotNull World world, BlockPos pos){

        //Place a block if the current coordinate contains an air block, to avoid overriding a block placed by a player
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

    static boolean shouldPlaySound(World world, BlockState state) {

        //Make sure we are on the logical server and avoid placing an air block,
        //since they too produce a sound effect when "placed"
        return !world.isClient && !state.equals(Blocks.AIR.getDefaultState()) && CONFIG.shouldPlaySoundOnBlockPlacement();

    }


}

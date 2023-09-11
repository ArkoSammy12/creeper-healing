package xd.arkosammy.handlers;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluids;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import xd.arkosammy.events.AffectedBlock;
import xd.arkosammy.CreeperHealing;
import xd.arkosammy.events.CreeperExplosionEvent;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import static xd.arkosammy.CreeperHealing.CONFIG;

//Thanks to @dale8689 for helping me figure out how to use tick timers instead of ScheduledFutures
public final class ExplosionHealerHandler {

    private ExplosionHealerHandler(){}
    private static final List<CreeperExplosionEvent> explosionEventList = new CopyOnWriteArrayList<>();
    public static List<CreeperExplosionEvent> getExplosionEventList(){
        return explosionEventList;
    }

    //Called at the end of each server tick
    public static void handleExplosionEventList(MinecraftServer server){

        //Safety lock for avoiding any potential issues with concurrency
        if(CreeperHealing.isExplosionHandlingUnlocked()) {

            if (!getExplosionEventList().isEmpty()) {

                //Tick each one of CreeperExplosionEvent instances in our list
                CreeperExplosionEvent.tickCreeperExplosionEvents();

                //Find a CreeperExplosionEvent in our list whose delay has reached 0
                for (CreeperExplosionEvent creeperExplosionEvent : getExplosionEventList()) {

                    if (creeperExplosionEvent.getCreeperExplosionTimer() < 0) {

                        //Get the current block to heal based on the event's internal block counter
                        AffectedBlock currentBlock = creeperExplosionEvent.getCurrentAffectedBlock();

                        if (currentBlock != null) {

                            if (!currentBlock.isPlaced()) {

                                //Tick the current block tryPlacing and check if its delay is less than 0
                                currentBlock.tickAffectedBlock();

                                if (currentBlock.getAffectedBlockTimer() < 0) {

                                    if(creeperExplosionEvent.canHealIfRequiresLight(server)) {

                                        if(currentBlock.canBePlaced(server)) {

                                            //Pass in the current creeperExplosionEvent
                                            // that this AffectedBlock instance belongs to
                                            currentBlock.tryPlacing(server, creeperExplosionEvent);

                                            currentBlock.setPlaced(true);

                                            creeperExplosionEvent.incrementCounter();

                                        } else {

                                            creeperExplosionEvent.postponeBlock(currentBlock, server);

                                        }

                                        CreeperHealing.LOGGER.info("Tried placing block: " + currentBlock.getState().getBlock().getName());

                                    } else {

                                        //Remove the current explosion if the light conditions are not met,
                                        // and the "requiresLight" setting is enabled
                                        getExplosionEventList().remove(creeperExplosionEvent);

                                    }

                                }

                            } else {

                                //If the current block has already been placed, increment the counter to skip it
                                creeperExplosionEvent.incrementCounter();

                            }

                        } else {

                            //If the currentBlock is null, there are no more blocks to heal.
                            // We can remove this CreeperExplosionEvent from the list
                            getExplosionEventList().remove(creeperExplosionEvent);

                        }

                    }

                }

            }

        }

    }

    //Only tryPlacing the block if the current coordinate contains an air block,
    // or whether we should tryPlacing one where there is currently flowing water or flowing lava
    public static boolean shouldPlaceBlock(@NotNull World world, BlockPos pos){

        if(world.isAir(pos)) return true;

        else if(world.getBlockState(pos).getFluidState().getFluid().equals(Fluids.FLOWING_WATER) && CONFIG.shouldHealOnFlowingWater()) return true;

        else return world.getBlockState(pos).getFluidState().getFluid().equals(Fluids.FLOWING_LAVA) && CONFIG.shouldHealOnFlowingLava();


    }

    //Make sure we are on the logical server and avoid placing an air block,
    //since they too produce a sound effect when "placed"
    public static boolean shouldPlaySound(World world, BlockState state) {
        return !world.isClient && !state.isAir() && CONFIG.shouldPlaySoundOnBlockPlacement();
    }

    public static void updateAffectedBlocksTimers(){
        CreeperHealing.setHealerHandlerLock(false);
        for(CreeperExplosionEvent creeperExplosionEvent : getExplosionEventList()){
            if(!creeperExplosionEvent.isMarkedWithDayTimeHealingMode()) {
                for (int i = creeperExplosionEvent.getAffectedBlockCounter() + 1; i < creeperExplosionEvent.getAffectedBlocksList().size(); i++) {
                    creeperExplosionEvent.getAffectedBlocksList().get(i).setAffectedBlockTimer(CONFIG.getBlockPlacementDelay());
                }
            }
        }
        CreeperHealing.setHealerHandlerLock(true);
    }

}

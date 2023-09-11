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
    public static void tickCreeperExplosions(MinecraftServer server){

        //Safety lock for avoiding any potential issues with concurrency
        if(!CreeperHealing.isExplosionHandlingUnlocked() || getExplosionEventList().isEmpty()) return;

        //Tick each one of CreeperExplosionEvent instances in our list
        CreeperExplosionEvent.tickCreeperExplosionEvents();

        //Find a CreeperExplosionEvent in our list whose delay has reached 0
        for (CreeperExplosionEvent currentCreeperExplosionEvent : getExplosionEventList()) {

            if (currentCreeperExplosionEvent.getCreeperExplosionTimer() < 0) {

                processExplosionEvent(currentCreeperExplosionEvent, server);

            }

        }

    }

    private static void processExplosionEvent(CreeperExplosionEvent currentCreeperExplosionEvent, MinecraftServer server){

        //Get the current block to heal based on the event's internal block counter
        AffectedBlock currentAffectedBlock = currentCreeperExplosionEvent.getCurrentAffectedBlock();

        //If the currentAffectedBlock is null, there are no more blocks to heal.
        // We can remove this CreeperExplosionEvent from the list
        if(currentAffectedBlock == null) {

            getExplosionEventList().remove(currentCreeperExplosionEvent);

            CreeperHealing.LOGGER.info("Explosion finished");

            return;

        }

        //If the current block has already been placed, increment the counter to skip it
        if(currentAffectedBlock.isAlreadyPlaced()){

            currentCreeperExplosionEvent.incrementCounter();

            return;

        }

        if(!currentAffectedBlock.canBePlaced(server)){

            //If the block isn't placeable, postpone its placement and find a placeable block
            currentCreeperExplosionEvent.postponeBlock(currentAffectedBlock, server);

            CreeperHealing.LOGGER.info("Postponed block: " + currentAffectedBlock.getState().getBlock().getName().toString());

            return;


        }


        //Tick the current block tryPlacing and check if its delay is less than 0
        currentAffectedBlock.tickAffectedBlock();

        if (currentAffectedBlock.getAffectedBlockTimer() < 0) {

            handleBlockPlacement(currentAffectedBlock, currentCreeperExplosionEvent, server);

        }

    }

    private static void handleBlockPlacement(AffectedBlock currentAffectedBlock, CreeperExplosionEvent currentCreeperExplosionEvent,MinecraftServer server){

        if (currentCreeperExplosionEvent.canHealIfRequiresLight(server)) {

            //Pass in the current currentCreeperExplosionEvent
            // that this AffectedBlock instance belongs to
            currentAffectedBlock.tryPlacing(server, currentCreeperExplosionEvent);

            //Set this block as placed
            currentAffectedBlock.setPlaced(true);

            //Increment this explosion's internal counter to move on to the next one
            currentCreeperExplosionEvent.incrementCounter();

        } else {

            //Remove the current explosion if the light conditions are not met,
            // and the "requiresLight" setting is enabled
            getExplosionEventList().remove(currentCreeperExplosionEvent);

            CreeperHealing.LOGGER.info("Explosion finished");

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

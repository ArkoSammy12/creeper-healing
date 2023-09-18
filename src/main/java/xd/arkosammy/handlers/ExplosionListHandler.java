package xd.arkosammy.handlers;

import net.minecraft.server.MinecraftServer;
import xd.arkosammy.explosions.AffectedBlock;
import xd.arkosammy.CreeperHealing;
import xd.arkosammy.explosions.ExplosionEvent;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

//Thanks to @dale8689 for helping me figure out how to use tick timers instead of ScheduledFutures
public final class ExplosionListHandler {

    private ExplosionListHandler(){}
    private static final List<ExplosionEvent> explosionEventList = new CopyOnWriteArrayList<>();
    public static List<ExplosionEvent> getExplosionEventList(){
        return explosionEventList;
    }

    //Called at each server tick
    public static void handleExplosionList(MinecraftServer server){

        //Safety lock for avoiding any potential issues with concurrency
        if(!CreeperHealing.isExplosionHandlingUnlocked() || getExplosionEventList().isEmpty()) return;

        //Tick each one of our ExplosionEvent instances in our list
        ExplosionEvent.tickExplosions();

        //Find an ExplosionEvent in our list whose delay has reached 0
        for (ExplosionEvent currentExplosionEvent : getExplosionEventList()) {

            if (currentExplosionEvent.getCreeperExplosionTimer() < 0) {

                processExplosionEvent(currentExplosionEvent, server);

            }

        }

    }

    private static void processExplosionEvent(ExplosionEvent currentExplosionEvent, MinecraftServer server){

        //Get the current block to heal based on the event's internal block counter
        AffectedBlock currentAffectedBlock = currentExplosionEvent.getCurrentAffectedBlock();

        //If the block we got is null, remove this ExplosionEvent from the list
        if(currentAffectedBlock == null) {

            getExplosionEventList().remove(currentExplosionEvent);

            return;

        }

        //If the current block has already been placed, increment the counter to skip it
        if(currentAffectedBlock.isAlreadyPlaced()){

            currentExplosionEvent.incrementCounter();

            return;

        }

        //If the block isn't placeable, postpone its placement and find a placeable block, and place that one instead
        if(!currentAffectedBlock.canBePlaced(server)){

            currentExplosionEvent.postponeBlock(currentAffectedBlock, server);

            return;

        }


        //Tick the current block and check if its delay is less than 0
        currentAffectedBlock.tickAffectedBlock();

        if (currentAffectedBlock.getAffectedBlockTimer() < 0) {

            handleBlockPlacement(currentAffectedBlock, currentExplosionEvent, server);

        }

    }

    private static void handleBlockPlacement(AffectedBlock currentAffectedBlock, ExplosionEvent currentExplosionEvent, MinecraftServer server){

        if (currentExplosionEvent.canHealIfRequiresLight(server)) {

            //Pass in the current currentExplosionEvent
            // that this AffectedBlock instance belongs to
            currentAffectedBlock.tryPlacing(server, currentExplosionEvent);

            //Set this block as placed
            currentAffectedBlock.setPlaced(true);

            //Increment this explosion's internal counter to move on to the next one
            currentExplosionEvent.incrementCounter();

        } else {

            //Remove the current explosion if the light conditions are not met,
            // and the "requiresLight" setting is enabled
            getExplosionEventList().remove(currentExplosionEvent);


        }

    }


}

package xd.arkosammy.handlers;

import net.minecraft.server.MinecraftServer;
import xd.arkosammy.explosions.AffectedBlock;
import xd.arkosammy.CreeperHealing;
import xd.arkosammy.explosions.ExplosionEvent;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

//Thanks to @dale8689 for helping me figure out how to use tick timers instead of ScheduledFutures
public final class ExplosionListHandler {

    private ExplosionListHandler(){}

    private static final List<ExplosionEvent> explosionEventList = new CopyOnWriteArrayList<>();

    public static List<ExplosionEvent> getExplosionEventList(){
        return explosionEventList;
    }

    public static void handleExplosionList(MinecraftServer server){

        if(!CreeperHealing.isExplosionHandlingUnlocked() || getExplosionEventList().isEmpty()) return;

        ExplosionEvent.tickExplosions();
        for (ExplosionEvent currentExplosionEvent : getExplosionEventList()) {
            if (currentExplosionEvent.getExplosionTimer() < 0) {
                processExplosionEvent(currentExplosionEvent, server);
            }
        }
    }

    private static void processExplosionEvent(ExplosionEvent currentExplosionEvent, MinecraftServer server){
        Optional<AffectedBlock> optionalAffectedBlock = currentExplosionEvent.getCurrentAffectedBlock();
        if(optionalAffectedBlock.isEmpty()) {
            getExplosionEventList().remove(currentExplosionEvent);
            return;
        }
        AffectedBlock currentAffectedBlock = optionalAffectedBlock.get();
        if(currentAffectedBlock.isAlreadyPlaced()){
            currentExplosionEvent.incrementCounter();
            return;
        }
        if(!currentAffectedBlock.canBePlaced(server)){
            currentExplosionEvent.delayAffectedBlock(currentAffectedBlock, server);
            return;
        }
        currentAffectedBlock.tickAffectedBlock();
        if (currentAffectedBlock.getAffectedBlockTimer() < 0) {
            handleBlockPlacement(currentAffectedBlock, currentExplosionEvent, server);
        }
    }

    private static void handleBlockPlacement(AffectedBlock currentAffectedBlock, ExplosionEvent currentExplosionEvent, MinecraftServer server){
        if (!currentExplosionEvent.hasEnoughLightIfDaytimeHealingMode(server)) {
            getExplosionEventList().remove(currentExplosionEvent);
            return;
        }
        if(!currentExplosionEvent.shouldKeepHealingIfDifficultyBasedHealingMode(currentAffectedBlock.getWorld(server))) {
            getExplosionEventList().remove(currentExplosionEvent);
            return;
        }
        currentAffectedBlock.tryHealing(server, currentExplosionEvent);
        currentAffectedBlock.setPlaced(true);
        currentExplosionEvent.incrementCounter();
    }

}

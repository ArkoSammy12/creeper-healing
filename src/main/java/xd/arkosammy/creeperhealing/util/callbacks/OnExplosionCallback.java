package xd.arkosammy.creeperhealing.util.callbacks;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.ActionResult;
import xd.arkosammy.creeperhealing.util.ExplosionContext;

public interface OnExplosionCallback {

    Event<OnExplosionCallback> EVENT = EventFactory.createArrayBacked(OnExplosionCallback.class,
            (listeners) -> (explosion) -> {
                for (OnExplosionCallback listener : listeners) {
                    ActionResult result = listener.onExplosion(explosion);
                    if (result != ActionResult.PASS) {
                        return result;
                    }
                }
                return ActionResult.PASS;
            });

    ActionResult onExplosion(ExplosionContext explosionContext);
}

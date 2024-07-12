package xd.arkosammy.creeperhealing.util.callbacks;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.explosion.Explosion;

public interface ExplosionCallbacks {

    Event<BeforeExplosion> BEFORE_EXPLOSION = EventFactory.createArrayBacked(BeforeExplosion.class,
            (listeners) -> (explosion) -> {
                for (BeforeExplosion listener : listeners) {
                    listener.beforeExplosion(explosion);
                }
            });

    Event<AfterExplosion> AFTER_EXPLOSION = EventFactory.createArrayBacked(AfterExplosion.class,
            (listeners) -> (explosion) -> {
                for (AfterExplosion listener : listeners) {
                    listener.afterExplosion(explosion);
                }
            });


    interface BeforeExplosion {

        void beforeExplosion(Explosion explosion);

    }

    interface AfterExplosion {

        void afterExplosion(Explosion explosion);

    }

}

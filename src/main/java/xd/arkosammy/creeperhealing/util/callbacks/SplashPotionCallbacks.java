package xd.arkosammy.creeperhealing.util.callbacks;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

public interface SplashPotionCallbacks {

    Event<OnCollision> ON_COLLISION = EventFactory.createArrayBacked(OnCollision.class,
            (listeners) -> ((potionEntity, potionContentsComponent, hitResult, world) -> {
                for (OnCollision listener : listeners) {
                    listener.onPotionCollide(potionEntity, potionContentsComponent, hitResult, world);
                }
            }));

    interface OnCollision {
        void onPotionCollide(PotionEntity potionEntity, PotionContentsComponent potionContentsComponent, HitResult hitResult, World world);
    }

}

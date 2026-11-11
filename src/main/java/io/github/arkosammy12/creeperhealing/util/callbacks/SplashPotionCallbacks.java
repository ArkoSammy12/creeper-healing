package io.github.arkosammy12.creeperhealing.util.callbacks;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.entity.projectile.throwableitemprojectile.AbstractThrownPotion;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.Level;

public interface SplashPotionCallbacks {

    Event<OnCollision> ON_COLLISION = EventFactory.createArrayBacked(OnCollision.class,
            (listeners) -> ((potionEntity, potionContentsComponent, hitResult, world) -> {
                for (OnCollision listener : listeners) {
                    listener.onPotionCollide(potionEntity, potionContentsComponent, hitResult, world);
                }
            }));

    interface OnCollision {
        void onPotionCollide(AbstractThrownPotion potionEntity, PotionContents potionContentsComponent, HitResult hitResult, Level world);
    }

}

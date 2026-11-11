package io.github.arkosammy12.creeperhealing.explosions.ducks;

import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public interface ExplosionImplDuck {

    boolean creeperhealing$shouldHeal();

    void creeperhealing$setExplosionInteraction(Level.ExplosionInteraction explosionSourceType);

    @Nullable
    Level.ExplosionInteraction creeperhealing$getExplosionInteraction();

}

package io.github.arkosammy12.creeperhealing.explosions.ducks;

import net.minecraft.world.World;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public interface ExplosionImplDuck {

    boolean creeperhealing$shouldHeal();

    void creeperhealing$setExplosionSourceType(World.ExplosionSourceType explosionSourceType);

    @Nullable
    World.ExplosionSourceType creeperhealing$getExplosionSourceType();

}

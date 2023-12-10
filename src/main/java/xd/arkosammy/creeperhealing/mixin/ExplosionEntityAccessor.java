package xd.arkosammy.creeperhealing.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Explosion.class)
public interface ExplosionEntityAccessor {
    @Accessor("entity")
    Entity getEntity();
}

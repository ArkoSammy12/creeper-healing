package xd.arkosammy.creeperhealing.explosions.ducks;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.world.World;

public interface ExplosionAccessor {

    World creeperhealing$getWorld();

    DamageSource creeperhealing$getDamageSource();

    boolean creeperhealing$shouldHeal();

    boolean creeperhealing$willBeHealed();

}

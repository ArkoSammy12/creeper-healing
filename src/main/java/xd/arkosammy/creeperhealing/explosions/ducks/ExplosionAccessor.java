package xd.arkosammy.creeperhealing.explosions.ducks;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.world.World;

public interface ExplosionAccessor {

    World creeper_healing$getWorld();

    DamageSource creeper_healing$getDamageSource();

    boolean creeper_healing$shouldHeal();

    boolean creeper_healing$willBeHealed();

}

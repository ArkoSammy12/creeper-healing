package xd.arkosammy.creeperhealing.explosions;

import net.minecraft.world.World;

import java.util.List;

public class DefaultExplosionEvent extends AbstractExplosionEvent {

    DefaultExplosionEvent(List<AffectedBlock> affectedBlocks, long healTimer, int blockCounter) {
        super(affectedBlocks, healTimer, blockCounter);
    }

    DefaultExplosionEvent(List<AffectedBlock> affectedBlocks){
        super(affectedBlocks);
    }

    @Override
    ExplosionHealingMode getHealingMode() {
        return ExplosionHealingMode.DEFAULT_MODE;
    }

    @Override
    void setupExplosion(World world) {
    }

    @Override
    boolean shouldKeepHealing(World world) {
        return true;
    }

}

package xd.arkosammy.creeperhealing.explosions;

import net.minecraft.world.World;
import xd.arkosammy.creeperhealing.blocks.AffectedBlock;

import java.util.List;

public class DefaultExplosionEvent extends AbstractExplosionEvent {

    public DefaultExplosionEvent(List<AffectedBlock> affectedBlocks, long healTimer, int blockCounter) {
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
    public void setupExplosion(World world) {
    }

    @Override
    public boolean shouldKeepHealing(World world) {
        return true;
    }

}

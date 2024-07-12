package xd.arkosammy.creeperhealing.explosions;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xd.arkosammy.creeperhealing.blocks.AffectedBlock;

import java.util.List;

public class DefaultExplosionEvent extends AbstractExplosionEvent {

    public DefaultExplosionEvent(List<AffectedBlock> affectedBlocks, int radius, BlockPos center) {
        super(affectedBlocks, radius, center);
    }

    public DefaultExplosionEvent(List<AffectedBlock> affectedBlocks, long healTimer, int blockCounter, int radius, BlockPos center) {
        super(affectedBlocks, healTimer, blockCounter, radius, center);
    }

    @Override
    protected ExplosionHealingMode getHealingMode() {
        return ExplosionHealingMode.DEFAULT_MODE;
    }


    @Override
    public void setup(World world) {

    }

}

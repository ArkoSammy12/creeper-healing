package io.github.arkosammy12.creeperhealing.explosions;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import io.github.arkosammy12.creeperhealing.blocks.AffectedBlock;

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
    public void setup(ServerWorld world) {

    }

}

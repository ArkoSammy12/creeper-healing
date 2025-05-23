package io.github.arkosammy12.creeperhealing.explosions;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import io.github.arkosammy12.creeperhealing.blocks.AffectedBlock;
import io.github.arkosammy12.creeperhealing.blocks.SingleAffectedBlock;
import io.github.arkosammy12.creeperhealing.config.ConfigUtils;

import java.util.List;

public class BlastResistanceBasedExplosionEvent extends AbstractExplosionEvent {


    public BlastResistanceBasedExplosionEvent(List<AffectedBlock> affectedBlocks, int radius, BlockPos center) {
        super(affectedBlocks, radius, center);
    }

    public BlastResistanceBasedExplosionEvent(List<AffectedBlock> affectedBlocks, long healTimer, int blockCounter, int radius, BlockPos center) {
        super(affectedBlocks, healTimer, blockCounter, radius, center);
    }

    @Override
    protected ExplosionHealingMode getHealingMode() {
        return ExplosionHealingMode.BLAST_RESISTANCE_BASED_HEALING_MODE;
    }

    // Change the timers of each affected block based on their blast resistance
    @Override
    public void setup(ServerWorld world) {
        Random random = world.getRandom();
        for (AffectedBlock affectedBlock : this.getAffectedBlocks().toList()) {
            if (!(affectedBlock instanceof SingleAffectedBlock singleAffectedBlock)) {
                continue;
            }
            double randomOffset = random.nextBetween(-2, 2);
            float blastResistance = singleAffectedBlock.getBlockState().getBlock().getBlastResistance();
            double blastResistanceMultiplier = Math.min(blastResistance, 9);
            int offset = (int) (MathHelper.lerp(blastResistanceMultiplier / 9, -2, 2) + randomOffset);
            long finalOffset = Math.max(1, ConfigUtils.getBlockPlacementDelay() + (offset * 20L));
            singleAffectedBlock.setTimer(finalOffset);
        }
    }

}

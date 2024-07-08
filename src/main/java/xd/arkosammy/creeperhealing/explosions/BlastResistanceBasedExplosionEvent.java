package xd.arkosammy.creeperhealing.explosions;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import xd.arkosammy.creeperhealing.blocks.AffectedBlock;
import xd.arkosammy.creeperhealing.blocks.SingleAffectedBlock;
import xd.arkosammy.creeperhealing.config.ConfigUtils;

import java.util.List;

public class BlastResistanceBasedExplosionEvent extends AbstractExplosionEvent {

    public BlastResistanceBasedExplosionEvent(List<AffectedBlock> affectedBlocks, long healTimer, int blockCounter) {
        super(affectedBlocks, healTimer, blockCounter);
    }

    BlastResistanceBasedExplosionEvent(List<AffectedBlock> affectedBlocks){
        super(affectedBlocks);
    }

    @Override
    public ExplosionHealingMode getHealingMode(){
        return ExplosionHealingMode.BLAST_RESISTANCE_BASED_HEALING_MODE;
    }

    // Change the timers of each affected block in this explosion event based on their blast resistance
    @Override
    public void setup(World world){
        final Random random = world.getRandom();
        for(AffectedBlock affectedBlock : this.getAffectedBlocks().toList()){
            if (!(affectedBlock instanceof SingleAffectedBlock singleAffectedBlock)) {
                continue;
            }
            final double randomOffset = random.nextBetween(-2, 2);
            final double blastResistanceMultiplier = Math.min(singleAffectedBlock.getBlockState().getBlock().getBlastResistance(), 9);
            final int offset = (int) (MathHelper.lerp(blastResistanceMultiplier / 9, -2, 2) + randomOffset);
            final long finalOffset = Math.max(1, ConfigUtils.getBlockPlacementDelay() + (offset * 20L));
            singleAffectedBlock.setTimer(finalOffset);
        }
    }

    @Override
    public boolean shouldKeepHealing(World world) {
        return super.shouldKeepHealing(world);
    }

}

package xd.arkosammy.creeperhealing.explosions;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import xd.arkosammy.creeperhealing.blocks.AffectedBlock;
import xd.arkosammy.creeperhealing.config.settings.BlockPlacementDelaySetting;

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
    public void setupExplosion(World world){
        Random random = world.getRandom();
        for(AffectedBlock affectedBlock : this.getAffectedBlocks()){
            double randomOffset = random.nextBetween(-2, 2);
            double blastResistanceMultiplier = Math.min(affectedBlock.getState().getBlock().getBlastResistance(), 9);
            int offset = (int) (MathHelper.lerp(blastResistanceMultiplier / 9, -2, 2) + randomOffset);
            long finalOffset = Math.max(1, BlockPlacementDelaySetting.getAsTicks() + (offset * 20L));
            affectedBlock.setTimer(finalOffset);
        }
    }

    @Override
    public boolean shouldKeepHealing(World world) {
        return true;
    }

}

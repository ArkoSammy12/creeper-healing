package xd.arkosammy.creeperhealing.explosions;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import xd.arkosammy.creeperhealing.blocks.AffectedBlock;
import xd.arkosammy.creeperhealing.blocks.SingleAffectedBlock;
import xd.arkosammy.creeperhealing.config.ConfigUtils;

import java.util.List;
import net.minecraft.util.math.random.Random;

public class DifficultyBasedExplosionEvent extends AbstractExplosionEvent {

    public DifficultyBasedExplosionEvent(List<AffectedBlock> affectedBlocks, int radius, BlockPos center) {
        super(affectedBlocks, radius, center);
    }

    public DifficultyBasedExplosionEvent(List<AffectedBlock> affectedBlocks, long healTimer, int blockCounter, int radius, BlockPos center) {
        super(affectedBlocks, healTimer, blockCounter, radius, center);
    }

    @Override
    protected ExplosionHealingMode getHealingMode() {
        return ExplosionHealingMode.DIFFICULTY_BASED_HEALING_MODE;
    }

    @Override
    public void setup(World world) {
        final int difficultyMultiplier = switch (world.getDifficulty()) {
            case PEACEFUL -> -2;
            case EASY -> -1;
            case NORMAL -> 1;
            case HARD -> 2;
        };
        long newBlockTimer = Math.max(1, ConfigUtils.getBlockPlacementDelay() + (difficultyMultiplier * 20));
        long newExplosionTimer = Math.max(1, ConfigUtils.getExplosionHealDelay() + (difficultyMultiplier * 20));
        this.healTimer = newExplosionTimer;
        for (AffectedBlock affectedBlock : this.getAffectedBlocks().toList()) {
            if (!(affectedBlock instanceof SingleAffectedBlock singleAffectedBlock)) {
                continue;
            }
            singleAffectedBlock.setTimer(newBlockTimer);
        }
    }

    // 1/50 chance of the explosion stopping its healing process if the difficulty is too hard
    @Override
    public void updateFinishedStatus(World world) {
        if (world.getDifficulty() != Difficulty.HARD) {
            return;
        }
        Random random = world.getRandom();
        int randInt = random.nextBetween(0, 50);
        if (randInt == 1) {
            this.finished = true;
        }
    }

}

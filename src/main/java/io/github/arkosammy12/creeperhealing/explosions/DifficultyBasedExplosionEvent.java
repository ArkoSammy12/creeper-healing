package io.github.arkosammy12.creeperhealing.explosions;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import io.github.arkosammy12.creeperhealing.blocks.AffectedBlock;
import io.github.arkosammy12.creeperhealing.blocks.SingleAffectedBlock;
import io.github.arkosammy12.creeperhealing.config.ConfigUtils;

import java.util.List;

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
    public void setup(ServerWorld world) {
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

}

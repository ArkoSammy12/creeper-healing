package io.github.arkosammy12.creeperhealing.explosions.factories;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import io.github.arkosammy12.creeperhealing.blocks.AffectedBlock;
import io.github.arkosammy12.creeperhealing.explosions.ExplosionEvent;

import java.util.List;

public interface ExplosionEventFactory<T extends ExplosionEvent> {

    @Nullable
    List<BlockPos> getAffectedPositions();

    ServerWorld getWorld();

    @Nullable
    T createExplosionEvent();

    @Nullable
    T createExplosionEvent(List<BlockPos> affectedPositions, ServerWorld world);

    @Nullable
    T createExplosionEvent(List<AffectedBlock> affectedBlocks, long healTimer);

    @Nullable
    T createExplosionEvent(List<AffectedBlock> affectedBlocks, long healTimer, long blockHealDelay);

}

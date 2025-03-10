package io.github.arkosammy12.creeperhealing.explosions.ducks;

import net.minecraft.util.math.BlockPos;

import java.util.Collection;

public interface ServerWorldDuck {

    void creeperhealing$addAffectedPositions(Collection<BlockPos> affectedPositions);

    void creeperhealing$clearAffectedPositions();

    boolean creeperhealing$isAffectedPosition(BlockPos pos);

}

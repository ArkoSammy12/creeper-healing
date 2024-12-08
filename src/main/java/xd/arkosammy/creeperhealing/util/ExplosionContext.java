package xd.arkosammy.creeperhealing.util;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Map;

public record ExplosionContext(
        List<BlockPos> vanillaAffectedPositions,
        List<BlockPos> indirectlyAffectedPositions,
        Map<BlockPos, Pair<BlockState, BlockEntity>> affectedStatesAndBlockEntities,
        ServerWorld world,
        World.ExplosionSourceType explosionSourceType
) {
}

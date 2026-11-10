package io.github.arkosammy12.creeperhealing.util;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.server.level.ServerLevel;
import kotlin.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Map;

public record ExplosionContext(
        List<BlockPos> vanillaAffectedPositions,
        List<BlockPos> indirectlyAffectedPositions,
        Map<BlockPos, Pair<BlockState, BlockEntity>> affectedStatesAndBlockEntities,
        ServerLevel world,
        Level.ExplosionInteraction explosionSourceType
) {
}

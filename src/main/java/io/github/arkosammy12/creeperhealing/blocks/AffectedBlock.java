package io.github.arkosammy12.creeperhealing.blocks;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import io.github.arkosammy12.creeperhealing.config.ConfigUtils;
import io.github.arkosammy12.creeperhealing.explosions.ExplosionEvent;

public interface AffectedBlock {

    BlockPos getBlockPos();

    BlockState getBlockState();

    ResourceKey<Level> getWorldRegistryKey();

    ServerLevel getWorld(MinecraftServer server);

    long getBlockTimer();

    void tick(ExplosionEvent currentExplosion, MinecraftServer server);

    void setPlaced();

    boolean isPlaced();

    boolean canBePlaced(MinecraftServer server);

    SerializedAffectedBlock asSerialized();

    static AffectedBlock newInstance(BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, Level world) {
        ResourceKey<Level> worldRegistryKey = world.dimension();
        long blockPlacementDelay = ConfigUtils.getBlockPlacementDelay();
        boolean restoreBlockNbt = ConfigUtils.getRawBooleanSetting(ConfigUtils.RESTORE_BLOCK_NBT);
        if (blockEntity != null && restoreBlockNbt) {
            return new SingleAffectedBlock(pos, state, worldRegistryKey, blockEntity.saveWithFullMetadata(world.registryAccess()), blockPlacementDelay, false);
        }
        return new SingleAffectedBlock(pos, state, worldRegistryKey, null, blockPlacementDelay, false);
    }

    static AffectedBlock newInstance(BlockPos firstHalfPos, BlockState firstHalfState, @Nullable BlockEntity firstHalfBlockEntity, BlockPos secondHalfPos, @Nullable BlockState secondHalfState, @Nullable BlockEntity secondHalfBlockEntity, Level world) {
        ResourceKey<Level> worldRegistryKey = world.dimension();
        long blockPlacementDelay = ConfigUtils.getBlockPlacementDelay();
        boolean restoreBlockNbt = ConfigUtils.getRawBooleanSetting(ConfigUtils.RESTORE_BLOCK_NBT);
        if ((firstHalfBlockEntity != null && secondHalfBlockEntity != null) && restoreBlockNbt) {
            return new DoubleAffectedBlock(firstHalfPos, firstHalfState, firstHalfBlockEntity.saveWithFullMetadata(world.registryAccess()), secondHalfPos, secondHalfState, secondHalfBlockEntity.saveWithFullMetadata(world.registryAccess()), worldRegistryKey, blockPlacementDelay, false);
        }
        return new DoubleAffectedBlock(firstHalfPos, firstHalfState, null, secondHalfPos, secondHalfState, null, worldRegistryKey, blockPlacementDelay, false);
    }
}

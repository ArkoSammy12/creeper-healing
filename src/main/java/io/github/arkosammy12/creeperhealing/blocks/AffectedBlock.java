package io.github.arkosammy12.creeperhealing.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import io.github.arkosammy12.creeperhealing.config.ConfigUtils;
import io.github.arkosammy12.creeperhealing.explosions.ExplosionEvent;

public interface AffectedBlock {

    BlockPos getBlockPos();

    BlockState getBlockState();

    RegistryKey<World> getWorldRegistryKey();

    ServerWorld getWorld(MinecraftServer server);

    long getBlockTimer();

    void tick(ExplosionEvent currentExplosion, MinecraftServer server);

    void setPlaced();

    boolean isPlaced();

    boolean canBePlaced(MinecraftServer server);

    SerializedAffectedBlock asSerialized();

    static AffectedBlock newInstance(BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, World world) {
        RegistryKey<World> worldRegistryKey = world.getRegistryKey();
        long blockPlacementDelay = ConfigUtils.getBlockPlacementDelay();
        boolean restoreBlockNbt = ConfigUtils.getRawBooleanSetting(ConfigUtils.RESTORE_BLOCK_NBT);
        if (blockEntity != null && restoreBlockNbt) {
            return new SingleAffectedBlock(pos, state, worldRegistryKey, blockEntity.createNbtWithIdentifyingData(world.getRegistryManager()), blockPlacementDelay, false);
        }
        return new SingleAffectedBlock(pos, state, worldRegistryKey, null, blockPlacementDelay, false);
    }

    static AffectedBlock newInstance(BlockPos firstHalfPos, BlockState firstHalfState, @Nullable BlockEntity firstHalfBlockEntity, BlockPos secondHalfPos, @Nullable BlockState secondHalfState, @Nullable BlockEntity secondHalfBlockEntity, World world) {
        RegistryKey<World> worldRegistryKey = world.getRegistryKey();
        long blockPlacementDelay = ConfigUtils.getBlockPlacementDelay();
        boolean restoreBlockNbt = ConfigUtils.getRawBooleanSetting(ConfigUtils.RESTORE_BLOCK_NBT);
        if ((firstHalfBlockEntity != null && secondHalfBlockEntity != null) && restoreBlockNbt) {
            return new DoubleAffectedBlock(firstHalfPos, firstHalfState, firstHalfBlockEntity.createNbtWithIdentifyingData(world.getRegistryManager()), secondHalfPos, secondHalfState, secondHalfBlockEntity.createNbtWithIdentifyingData(world.getRegistryManager()), worldRegistryKey, blockPlacementDelay, false);
        }
        return new DoubleAffectedBlock(firstHalfPos, firstHalfState, null, secondHalfPos, secondHalfState, null, worldRegistryKey, blockPlacementDelay, false);
    }
}

package io.github.arkosammy12.creeperhealing.blocks;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.Optional;

public interface SerializedAffectedBlock {

    String getAffectedBlockTypeName();

    BlockPos getBlockPos();

    BlockState getBlockState();

    ResourceKey<Level> getWorldRegistryKey();

    long getBlockTimer();

    boolean isPlaced();

    <T> Optional<T> getCustomData(String name, Class<T> clazz);

    AffectedBlock asDeserialized();

}

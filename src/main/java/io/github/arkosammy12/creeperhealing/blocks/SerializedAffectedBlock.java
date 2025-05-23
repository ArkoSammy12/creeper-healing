package io.github.arkosammy12.creeperhealing.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;

public interface SerializedAffectedBlock {

    String getAffectedBlockTypeName();

    BlockPos getBlockPos();

    BlockState getBlockState();

    RegistryKey<World> getWorldRegistryKey();

    long getBlockTimer();

    boolean isPlaced();

    <T> Optional<T> getCustomData(String name, Class<T> clazz);

    AffectedBlock asDeserialized();

}

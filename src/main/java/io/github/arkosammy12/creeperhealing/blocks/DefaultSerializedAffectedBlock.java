package io.github.arkosammy12.creeperhealing.blocks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import io.github.arkosammy12.creeperhealing.CreeperHealing;

import java.util.Optional;

public record DefaultSerializedAffectedBlock(
        String affectedBlockType,
        BlockPos pos,
        BlockState state,
        @Nullable BlockPos secondHalfPos,
        @Nullable BlockState secondHalfState,
        RegistryKey<World> worldRegistryKey,
        @Nullable NbtCompound nbt,
        @Nullable NbtCompound secondHalfNbt,
        long blockTimer,
        boolean placed
) implements SerializedAffectedBlock {

    public static final Codec<SerializedAffectedBlock> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("affected_block_type").forGetter(SerializedAffectedBlock::getAffectedBlockTypeName),
            BlockPos.CODEC.fieldOf("block_pos").forGetter(SerializedAffectedBlock::getBlockPos),
            BlockState.CODEC.fieldOf("block_state").forGetter(SerializedAffectedBlock::getBlockState),
            BlockPos.CODEC.optionalFieldOf("second_half_pos").forGetter(serializedAffectedBlock -> serializedAffectedBlock.getCustomData("secondHalfPos", BlockPos.class)),
            BlockState.CODEC.optionalFieldOf("second_half_state").forGetter(serializedAffectedBlock -> serializedAffectedBlock.getCustomData("secondHalfState", BlockState.class)),
            World.CODEC.fieldOf("world").forGetter(SerializedAffectedBlock::getWorldRegistryKey),
            NbtCompound.CODEC.optionalFieldOf("nbt_data").forGetter(serializedAffectedBlock -> serializedAffectedBlock.getCustomData("nbt", NbtCompound.class)),
            NbtCompound.CODEC.optionalFieldOf("second_half_nbt_data").forGetter(serializedAffectedBlock -> serializedAffectedBlock.getCustomData("secondHalfNbt", NbtCompound.class)),
            Codec.LONG.fieldOf("affected_block_timer").forGetter(SerializedAffectedBlock::getBlockTimer),
            Codec.BOOL.fieldOf("is_placed").forGetter(SerializedAffectedBlock::isPlaced)
    ).apply(instance, (affectedBlockType, blockPos, blockState, secondHalfPos, secondHalfState, world, optionalNbt, optionalSecondHalfNbt, affectedBlockTimer, isPlaced) ->
            new DefaultSerializedAffectedBlock(affectedBlockType, blockPos, blockState, secondHalfPos.orElse(null), secondHalfState.orElse(null), world, optionalNbt.orElse(null), optionalSecondHalfNbt.orElse(null), affectedBlockTimer, isPlaced)));

    @Override
    public String getAffectedBlockTypeName() {
        return this.affectedBlockType;
    }

    @Override
    public BlockPos getBlockPos() {
        return this.pos;
    }

    @Override
    public BlockState getBlockState() {
        return this.state;
    }

    @Override
    public RegistryKey<World> getWorldRegistryKey() {
        return this.worldRegistryKey;
    }

    @Override
    public long getBlockTimer() {
        return this.blockTimer;
    }

    @Override
    public boolean isPlaced() {
        return this.placed;
    }

    // Unchecked cast done under the assumption that T is always the proper type of the custom data that we wish to access
    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getCustomData(String name, Class<T> clazz) {
        Object data = switch (name) {
            case "nbt" -> this.nbt;
            case "secondHalfPos" -> this.secondHalfPos;
            case "secondHalfState" -> this.secondHalfState;
            case "secondHalfNbt" -> this.secondHalfNbt;
            default -> {
                CreeperHealing.LOGGER.warn("Tried to get unexpected property of name \"{}\" while serializing an affected block!", name);
                yield null;
            }
        };
        if (clazz.isInstance(data)) {
            return Optional.of((T) data);
        } else if (data == null) {
            return Optional.empty();
        } else {
            CreeperHealing.LOGGER.error("Unsuccessfully tried to cast property with name \"{}\" of type \"{}\" to \"{}\" while serializing an affected block!", name, data.getClass().getSimpleName(), clazz.getSimpleName());
            return Optional.empty();
        }
    }

    @Override
    public AffectedBlock asDeserialized() {
        return switch (this.affectedBlockType) {
            case DoubleAffectedBlock.TYPE ->
                    new DoubleAffectedBlock(this.pos(), this.state(), this.nbt(), this.secondHalfPos(), this.secondHalfState(), this.secondHalfNbt(), this.worldRegistryKey(), this.getBlockTimer(), this.placed());
            default ->
                    new SingleAffectedBlock(this.pos(), this.state(), this.worldRegistryKey(), this.nbt(), this.getBlockTimer(), this.placed());
        };
    }
}

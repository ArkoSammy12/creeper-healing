package xd.arkosammy.creeperhealing.blocks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record DefaultSerializedAffectedBlock(
        String affectedBlockType,
        BlockPos pos,
        BlockState state,
        RegistryKey<World> worldRegistryKey,
        @Nullable NbtCompound nbt,
        long blockTimer,
        boolean placed
) implements SerializedAffectedBlock {

    public static final Codec<SerializedAffectedBlock> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("affected_block_type").forGetter(SerializedAffectedBlock::getAffectedBlockTypeName),
            BlockPos.CODEC.fieldOf("block_pos").forGetter(SerializedAffectedBlock::getBlockPos),
            BlockState.CODEC.fieldOf("block_state").forGetter(SerializedAffectedBlock::getBlockState),
            World.CODEC.fieldOf("world").forGetter(SerializedAffectedBlock::getWorldRegistryKey),
            NbtCompound.CODEC.optionalFieldOf("nbt_data").forGetter(serializedAffectedBlock -> Optional.ofNullable(serializedAffectedBlock.getCustomData("nbt", NbtCompound.class))),
            Codec.LONG.fieldOf("affected_block_timer").forGetter(SerializedAffectedBlock::getBlockTimer),
            Codec.BOOL.fieldOf("is_placed").forGetter(SerializedAffectedBlock::isPlaced)
    ).apply(instance, (affectedBlockType, blockPos, blockState, world, nbt, timer, placed) -> new DefaultSerializedAffectedBlock(affectedBlockType, blockPos, blockState, world, nbt.orElse(null), timer, placed)));

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
    public <T> T getCustomData(String name, Class<T> clazz) {
        Object data = switch (name) {
            case "nbt" -> this.nbt;
            default -> throw new IllegalArgumentException("Unknown serialized affected block property of name: " + name + "!");
        };
        if (clazz.isInstance(data)) {
            return (T) data;
        } else {
            throw new IllegalArgumentException("Property of serialized affected block with name " + name + " cannot be cased to " + clazz.getSimpleName());
        }
    }

    @Override
    public AffectedBlock asDeserialized() {
        return switch (this.affectedBlockType){
            case DoubleAffectedBlock.TYPE -> new DoubleAffectedBlock(this.pos, this.state(), this.worldRegistryKey(), this.getBlockTimer(), this.placed());
            default -> new SingleAffectedBlock(this.pos(), this.state(), this.worldRegistryKey(), this.nbt(), this.getBlockTimer(), this.placed());
        };
    }
}

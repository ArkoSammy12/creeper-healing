package xd.arkosammy.creeperhealing.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xd.arkosammy.creeperhealing.blocks.AffectedBlock;
import xd.arkosammy.creeperhealing.blocks.DoubleAffectedBlock;

import java.util.Optional;

public record SerializedAffectedBlock(String affectedBlockType, BlockPos blockPos, BlockState state, RegistryKey<World> worldRegistryKey, Optional<NbtCompound> optionalNbt, long affectedBlockTimer, boolean placed)  {

    static final Codec<SerializedAffectedBlock> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("affected_block_type").forGetter(SerializedAffectedBlock::affectedBlockType),
            BlockPos.CODEC.fieldOf("block_pos").forGetter(SerializedAffectedBlock::blockPos),
            BlockState.CODEC.fieldOf("block_state").forGetter(SerializedAffectedBlock::state),
            World.CODEC.fieldOf("world").forGetter(SerializedAffectedBlock::worldRegistryKey),
            NbtCompound.CODEC.optionalFieldOf("nbt_data").forGetter(SerializedAffectedBlock::optionalNbt),
            Codec.LONG.fieldOf("affected_block_timer").forGetter(SerializedAffectedBlock::affectedBlockTimer),
            Codec.BOOL.fieldOf("is_placed").forGetter(SerializedAffectedBlock::placed)
    ).apply(instance, SerializedAffectedBlock::new));

    AffectedBlock toDeserialized(){
        return switch (this.affectedBlockType){
            case DoubleAffectedBlock.TYPE -> new DoubleAffectedBlock(this.blockPos(), this.state(), this.worldRegistryKey(), this.affectedBlockTimer(), this.placed());
            default -> new AffectedBlock(this.blockPos(), this.state(), this.worldRegistryKey(), optionalNbt.orElse(null), this.affectedBlockTimer(), this.placed());
        };
    }

}

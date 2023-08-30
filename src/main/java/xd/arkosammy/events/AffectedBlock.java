package xd.arkosammy.events;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import java.io.Serial;
import java.io.Serializable;

public class AffectedBlock implements Serializable {

    @Serial
    private static final long serialVersionUID = 1212L;
    private BlockPos pos;
    private BlockState blockState;
    private RegistryKey<World> worldRegistryKey;
    private long affectedBlockTimer;
    private boolean hasBeenPlaced;

    //Create a CODEC for our AffectedBlock.
    //Each AffectedBlock CODEC will contain a field for BlockState, BlockPos,
    //and the World Registry Key, from which we will obtain the World instance.
    public static final Codec<AffectedBlock> CODEC = RecordCodecBuilder.create(blockInfoInstance -> blockInfoInstance.group(

            BlockPos.CODEC.fieldOf("Block_Position").forGetter(AffectedBlock::getPos),
            BlockState.CODEC.fieldOf("Block_State").forGetter(AffectedBlock::getBlockState),
            World.CODEC.fieldOf("World").forGetter(AffectedBlock::getWorldRegistryKey),
            Codec.LONG.fieldOf("Block_Timer").forGetter(AffectedBlock::getAffectedBlockTimer),
            Codec.BOOL.fieldOf("Placed").forGetter(AffectedBlock::hasBeenPlaced)

    ).apply(blockInfoInstance, AffectedBlock::new));

    public AffectedBlock(BlockPos pos, BlockState blockState, RegistryKey<World> registryKey, long affectedBlockTimer, boolean hasBeenPlaced){

        setPos(pos);
        setBlockState(blockState);
        setAffectedBlockTimer(affectedBlockTimer);
        setWorldRegistryKey(registryKey);
        setHasBeenPlaced(hasBeenPlaced);

    }

    public void setPos(BlockPos pos){

        this.pos = pos;

    }

    public void setBlockState(BlockState blockState){

        this.blockState = blockState;

    }

    public void setAffectedBlockTimer(long delay){

        this.affectedBlockTimer = delay;

    }

    public void setWorldRegistryKey(RegistryKey<World> registryKey){

        this.worldRegistryKey = registryKey;

    }

    public void setHasBeenPlaced(boolean hasBeenPlaced){

        this.hasBeenPlaced = hasBeenPlaced;

    }

    public RegistryKey<World> getWorldRegistryKey(){

        return this.worldRegistryKey;

    }

    public World getWorld(@NotNull MinecraftServer server){

        //Get the World instance from the stored World Registry Key
        return server.getWorld(this.getWorldRegistryKey());

    }

    public BlockPos getPos(){

        return this.pos;

    }

    public BlockState getBlockState(){

        return this.blockState;

    }

    public long getAffectedBlockTimer(){

        return this.affectedBlockTimer;

    }

    public boolean hasBeenPlaced(){

        return this.hasBeenPlaced;

    }

    public void tickSingleBlockInfo(){

        this.affectedBlockTimer--;

    }

}

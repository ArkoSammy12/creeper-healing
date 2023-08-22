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
import java.util.Comparator;
import java.util.List;

//This class serves as a way to store both the Position, the State and the World of a block as a single object,
//improving our quality of life
public class BlockInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1212L;
    private BlockPos pos;
    private BlockState blockState;
    private long blockPlacementDelay;
    private RegistryKey<World> worldRegistryKey;

    //Create a CODEC for our BlockInfo.
    //Each BlockInfo CODEC will contain a field for BlockState, BlockPos,
    //and the World Registry Key, from which we will obtain the World instance.
    public static final Codec<BlockInfo> CODEC = RecordCodecBuilder.create(blockInfoInstance -> blockInfoInstance.group(

            BlockPos.CODEC.fieldOf("Block_Position").forGetter(BlockInfo::getPos),
            BlockState.CODEC.fieldOf("Block_State").forGetter(BlockInfo::getBlockState),
            World.CODEC.fieldOf("World").forGetter(BlockInfo::getWorldRegistryKey),
            Codec.LONG.fieldOf("Block_Placement_Delay").forGetter(BlockInfo::getBlockPlacementDelay)

    ).apply(blockInfoInstance, BlockInfo::new));

    public BlockInfo(BlockPos pos, BlockState blockState, RegistryKey<World> registryKey, long blockPlacementDelay){

        setPos(pos);
        setBlockState(blockState);
        setBlockPlacementDelay(blockPlacementDelay);
        setWorldRegistryKey(registryKey);

    }

    public void setPos(BlockPos pos){

        this.pos = pos;

    }

    public void setBlockState(BlockState blockState){

        this.blockState = blockState;

    }

    public void setBlockPlacementDelay(long delay){

        this.blockPlacementDelay = delay;

    }

    public void setWorldRegistryKey(RegistryKey<World> registryKey){

        this.worldRegistryKey = registryKey;

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

    public long getBlockPlacementDelay(){

        return this.blockPlacementDelay;

    }

    public static @NotNull List<BlockInfo> getAsYSorted(@NotNull List<BlockInfo> blockInfoList){

        Comparator<BlockInfo> yCoordComparator = Comparator.comparingInt(blockInfo -> blockInfo.getPos().getY());

        blockInfoList.sort(yCoordComparator);

        return blockInfoList;

    }

    public void tickSingleBlockInfo(){

        this.blockPlacementDelay--;

    }

}

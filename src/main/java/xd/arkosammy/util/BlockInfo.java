package xd.arkosammy.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import xd.arkosammy.handlers.ExplosionHealerHandler;

import java.io.Serial;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

//This class serves as a way to store both the Position and the State of a block as a single object, improving our quality of life
public class BlockInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1212L;
    private BlockPos pos;
    private BlockState blockState;

    private long blockPlacementDelay;
    private World world;

    private RegistryKey<World> worldRegistryKey;


    //Create a CODEC for our BlockInfo. Each BlockInfo CODEC will contain a field for BlockState and BlockPos


    public static final Codec<BlockInfo> CODEC = RecordCodecBuilder.create(blockInfoInstance -> blockInfoInstance.group(

            BlockPos.CODEC.fieldOf("Block_Position").forGetter(BlockInfo::getPos),
            BlockState.CODEC.fieldOf("Block_State").forGetter(BlockInfo::getBlockState),
            World.CODEC.fieldOf("World_Reg_Key").forGetter(BlockInfo::getWorldRegistryKey)

    ).apply(blockInfoInstance, BlockInfo::new));

    public BlockInfo(BlockPos pos, BlockState blockState, RegistryKey<World> registryKey){

        setPos(pos);
        setBlockState(blockState);
        setBlockPlacementDelay(ExplosionHealerHandler.getBlockPlacementDelay());
        setWorldRegistryKey(registryKey);

    }

    public void setPos(BlockPos pos){

        this.pos = pos;

    }

    public void setBlockState(BlockState blockState){

        this.blockState = blockState;

    }

    public void setBlockPlacementDelay(long blockPlacementDelay){

        this.blockPlacementDelay = blockPlacementDelay * 40;

    }

    public void setWorldRegistryKey(RegistryKey<World> registryKey){

        this.worldRegistryKey = registryKey;

    }

    public RegistryKey<World> getWorldRegistryKey(){

        return this.worldRegistryKey;

    }

    public World getWorld(MinecraftServer server){

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

    public static List<BlockInfo> getAsYSorted(@NotNull List<BlockInfo> blockInfoList){

        Comparator<BlockInfo> yCoordComparator = Comparator.comparingInt(blockInfo -> blockInfo.getPos().getY());

        blockInfoList.sort(yCoordComparator);

        return blockInfoList;

    }

    public void tickSingleBlockInfo(){

        this.blockPlacementDelay--;

    }

}

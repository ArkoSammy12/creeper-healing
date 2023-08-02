package xd.arkosammy.util;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import java.util.ArrayList;
import java.util.Comparator;

//This class serves as a way to store both the Position and the State of a block as a single object, improving our quality of life
public class BlockInfo {

    private BlockPos pos;
    private BlockState blockState;

    public BlockInfo(BlockPos pos, BlockState blockState){

        setPos(pos);
        setBlockState(blockState);

    }

    public void setPos(BlockPos pos){

        this.pos = pos;

    }

    public void setBlockState(BlockState blockState){

        this.blockState = blockState;

    }

    public BlockPos getPos(){

        return this.pos;

    }

    public BlockState getBlockState(){

        return this.blockState;

    }

    public static ArrayList<BlockInfo> getAsYSorted(ArrayList<BlockInfo> blockInfoList){

        Comparator<BlockInfo> yCoordComparator = Comparator.comparingInt(blockInfo -> blockInfo.getPos().getY());

        blockInfoList.sort(yCoordComparator);

        return blockInfoList;

    }

}

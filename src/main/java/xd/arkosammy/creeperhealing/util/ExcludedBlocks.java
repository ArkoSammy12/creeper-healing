package xd.arkosammy.creeperhealing.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

import java.util.Arrays;

/**
 * This class is used to store the blocks that are excluded from the healing process,
 * either because their behavior is too out of the ordinary to be properly taken into account,
 * or because they are not meant to be healed.
 */
public enum ExcludedBlocks {
    SHULKER_BOX(Blocks.SHULKER_BOX);

    private final Block blockInstance;

    public static boolean isExcluded(Block block) {
        return Arrays.stream(ExcludedBlocks.values()).anyMatch(excludedBlock -> excludedBlock.blockInstance.equals(block));
    }

    public static boolean isExcluded(BlockState state) {
        return Arrays.stream(ExcludedBlocks.values()).anyMatch(excludedBlock -> state.isOf(excludedBlock.blockInstance));
    }


    ExcludedBlocks(Block blockInstance){
        this.blockInstance = blockInstance;
    }

}

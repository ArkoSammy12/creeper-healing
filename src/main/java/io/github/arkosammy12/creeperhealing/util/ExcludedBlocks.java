package io.github.arkosammy12.creeperhealing.util;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * This class is used to store the blocks that are excluded from the healing process,
 * either because their behavior is too out of the ordinary to be properly taken into account,
 * or because they are not meant to be healed.
 */
public enum ExcludedBlocks {
    SHULKER_BOX(Blocks.SHULKER_BOX, BlockTags.SHULKER_BOXES),
    NETHER_PORTAL(Blocks.NETHER_PORTAL, BlockTags.PORTALS),
    END_PORTAL(Blocks.END_PORTAL, BlockTags.PORTALS),
    END_GATEWAY(Blocks.END_GATEWAY, BlockTags.PORTALS);

    private final Block blockInstance;
    @Nullable
    private final TagKey<Block> blockTag;

    public static boolean isExcluded(@Nullable Block block) {
        if (block == null) {
            return false;
        }
        return Arrays.stream(ExcludedBlocks.values()).anyMatch(excludedBlock -> {
            if (block.defaultBlockState().is(excludedBlock.blockInstance)) {
                return true;
            }
            TagKey<Block> blockTag = excludedBlock.blockTag;
            return blockTag != null && block.defaultBlockState().is(blockTag);
        });
    }

    public static boolean isExcluded(@Nullable BlockState state) {
        if (state == null) {
            return false;
        }
        return Arrays.stream(ExcludedBlocks.values()).anyMatch(excludedBlock -> {
            if (state.is(excludedBlock.blockInstance)) {
                return true;
            }
            TagKey<Block> blockTag = excludedBlock.blockTag;
            return blockTag != null && state.is(blockTag);
        });
    }

    ExcludedBlocks(Block blockInstance, @Nullable TagKey<Block> blockTag) {
        this.blockInstance = blockInstance;
        this.blockTag = blockTag;
    }

}

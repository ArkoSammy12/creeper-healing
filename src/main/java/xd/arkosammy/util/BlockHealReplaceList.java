package xd.arkosammy.util;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

import java.util.HashMap;

public class BlockHealReplaceList {

    private static HashMap<Block, Block> replaceList = new HashMap<>();

    public static HashMap<Block, Block> getReplaceList(){

        replaceList.put(Blocks.DIAMOND_BLOCK, Blocks.STONE);


        return replaceList;
    }

}

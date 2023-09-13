package xd.arkosammy.handlers;

import net.minecraft.block.*;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import xd.arkosammy.explosions.ExplosionUtils;
import xd.arkosammy.explosions.ExplosionEvent;

public final class DoubleBlockHandler {

    private DoubleBlockHandler(){}

    public static boolean isDoubleBlock(BlockState state){
        return state.contains(Properties.DOUBLE_BLOCK_HALF) || state.contains(Properties.BED_PART);
    }

    public static void handleDoubleBlock(World world, BlockState state, BlockPos pos, ExplosionEvent currentExplosionEvent){

        if(state.contains(Properties.DOUBLE_BLOCK_HALF)) {

            handleDoubleBlocks(world, state, pos, currentExplosionEvent);

        } else if (state.contains(Properties.BED_PART)) {

            handleBedPart(world, state, pos, currentExplosionEvent);

        }

    }

    private static void handleDoubleBlocks(World world, BlockState firstHalfState, BlockPos firstHalfPos, ExplosionEvent currentExplosionEvent){

        //Get the opposite half
        DoubleBlockHalf secondHalf = firstHalfState.get(Properties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.UPPER) ? DoubleBlockHalf.LOWER : DoubleBlockHalf.UPPER;

        //Transfer over the same properties to the new half of the block
        BlockState secondHalfState = firstHalfState.getBlock().getStateWithProperties(firstHalfState).with(Properties.DOUBLE_BLOCK_HALF, secondHalf);

        //Obtain the opposite coordinate of the current half of the door
        BlockPos secondHalfPos = secondHalfState.get(Properties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.UPPER) ? firstHalfPos.offset(Direction.Axis.Y, 1) :  firstHalfPos.offset(Direction.Axis.Y, -1);

        if(ExplosionUtils.shouldPlaceDoubleBlock(world, firstHalfPos, secondHalfPos)) {

            BlockState stateToPushFrom = firstHalfState.get(Properties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.LOWER) ? firstHalfState : secondHalfState;
            BlockPos posToPushFrom = firstHalfState.get(Properties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.LOWER) ? firstHalfPos : firstHalfPos.offset(Direction.Axis.Y, -1);

            if(stateToPushFrom.isSolidBlock(world, posToPushFrom))
                ExplosionUtils.pushPlayersUpwards(world, posToPushFrom, true);

            world.setBlockState(firstHalfPos, firstHalfState);
            world.setBlockState(secondHalfPos, secondHalfState);

            //The first argument being null tells the server to play the sound to all nearby players
            if(ExplosionUtils.shouldPlaySound(world, firstHalfState))
                world.playSound(null, firstHalfPos, firstHalfState.getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, firstHalfState.getSoundGroup().getVolume(), firstHalfState.getSoundGroup().getPitch());

        }

        //To avoid potentially placing back the special block if it is broken before the upper half is reached
        currentExplosionEvent.markSecondHalfAsPlaced(secondHalfState, secondHalfPos, world);


    }

    private static void handleBedPart(World world, BlockState firstHalfState, BlockPos firstHalfPos, ExplosionEvent currentExplosionEvent) {

        //Get the opposite part of the current part of the bed
        BedPart secondBedPart = firstHalfState.get(Properties.BED_PART).equals(BedPart.HEAD) ? BedPart.FOOT : BedPart.HEAD;

        //Transfer over the same properties and the opposite part to the new part of the bed
        BlockState secondHalfState = firstHalfState.getBlock().getStateWithProperties(firstHalfState).with(Properties.BED_PART, secondBedPart);

        BlockPos secondHalfPos;

        //Get the position of the opposite part of the current part of the bed
        if(secondBedPart.equals(BedPart.HEAD)) {

            switch (firstHalfState.get(Properties.HORIZONTAL_FACING)) {

                case NORTH -> secondHalfPos = firstHalfPos.offset(Direction.Axis.Z, -1);
                case SOUTH -> secondHalfPos = firstHalfPos.offset(Direction.Axis.Z, 1);
                case EAST -> secondHalfPos = firstHalfPos.offset(Direction.Axis.X, 1);
                default -> secondHalfPos = firstHalfPos.offset(Direction.Axis.X, -1);

            }

        } else {

            switch (firstHalfState.get(Properties.HORIZONTAL_FACING)) {

                case NORTH -> secondHalfPos = firstHalfPos.offset(Direction.Axis.Z, 1);
                case SOUTH -> secondHalfPos = firstHalfPos.offset(Direction.Axis.Z, -1);
                case EAST -> secondHalfPos = firstHalfPos.offset(Direction.Axis.X, -1);
                default -> secondHalfPos = firstHalfPos.offset(Direction.Axis.X, 1);

            }

        }

        if (ExplosionUtils.shouldPlaceDoubleBlock(world, firstHalfPos, secondHalfPos)) {

            if(firstHalfState.isSolidBlock(world, firstHalfPos))
                ExplosionUtils.pushPlayersUpwards(world, firstHalfPos, false);

            if(secondHalfState.isSolidBlock(world, secondHalfPos))
                ExplosionUtils.pushPlayersUpwards(world, secondHalfPos, false);

            world.setBlockState(firstHalfPos, firstHalfState);
            world.setBlockState(secondHalfPos, secondHalfState);

            //The first argument being null tells the server to play the sound to all nearby players
            if (ExplosionUtils.shouldPlaySound(world, firstHalfState))
                world.playSound(null, firstHalfPos, firstHalfState.getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, firstHalfState.getSoundGroup().getVolume(), firstHalfState.getSoundGroup().getPitch());

        }

        //To avoid potentially placing back the special block if it is broken before the opposite half is reached
        currentExplosionEvent.markSecondHalfAsPlaced(secondHalfState, secondHalfPos, world);


    }

}

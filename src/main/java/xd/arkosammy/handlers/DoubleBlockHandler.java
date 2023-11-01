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

        DoubleBlockHalf secondHalf = firstHalfState.get(Properties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.UPPER) ? DoubleBlockHalf.LOWER : DoubleBlockHalf.UPPER;
        BlockState secondHalfState = firstHalfState.getBlock().getStateWithProperties(firstHalfState).with(Properties.DOUBLE_BLOCK_HALF, secondHalf);
        BlockPos secondHalfPos = secondHalfState.get(Properties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.UPPER) ? firstHalfPos.offset(Direction.Axis.Y, 1) :  firstHalfPos.offset(Direction.Axis.Y, -1);

        if(ExplosionUtils.shouldHealDoubleBlock(world, firstHalfPos, secondHalfPos)) {
            BlockState stateToPushFrom = firstHalfState.get(Properties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.LOWER) ? firstHalfState : secondHalfState;
            BlockPos posToPushFrom = firstHalfState.get(Properties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.LOWER) ? firstHalfPos : firstHalfPos.offset(Direction.Axis.Y, -1);

            if(stateToPushFrom.isSolidBlock(world, posToPushFrom))
                ExplosionUtils.pushEntitiesUpwards(world, posToPushFrom, true);

            world.setBlockState(firstHalfPos, firstHalfState);
            world.setBlockState(secondHalfPos, secondHalfState);

            if(ExplosionUtils.shouldPlaySoundOnBlockHeal(world, firstHalfState))
                world.playSound(null, firstHalfPos, firstHalfState.getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, firstHalfState.getSoundGroup().getVolume(), firstHalfState.getSoundGroup().getPitch());
        }
        currentExplosionEvent.markAffectedBlockAsPlaced(secondHalfState, secondHalfPos, world);
    }

    private static void handleBedPart(World world, BlockState firstHalfState, BlockPos firstHalfPos, ExplosionEvent currentExplosionEvent) {

        BedPart secondBedPart = firstHalfState.get(Properties.BED_PART).equals(BedPart.HEAD) ? BedPart.FOOT : BedPart.HEAD;
        BlockState secondHalfState = firstHalfState.getBlock().getStateWithProperties(firstHalfState).with(Properties.BED_PART, secondBedPart);
        BlockPos secondHalfPos;

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

        if (ExplosionUtils.shouldHealDoubleBlock(world, firstHalfPos, secondHalfPos)) {

            if(firstHalfState.isSolidBlock(world, firstHalfPos))
                ExplosionUtils.pushEntitiesUpwards(world, firstHalfPos, false);
            if(secondHalfState.isSolidBlock(world, secondHalfPos))
                ExplosionUtils.pushEntitiesUpwards(world, secondHalfPos, false);

            world.setBlockState(firstHalfPos, firstHalfState);
            world.setBlockState(secondHalfPos, secondHalfState);

            if (ExplosionUtils.shouldPlaySoundOnBlockHeal(world, firstHalfState))
                world.playSound(null, firstHalfPos, firstHalfState.getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, firstHalfState.getSoundGroup().getVolume(), firstHalfState.getSoundGroup().getPitch());

        }
        currentExplosionEvent.markAffectedBlockAsPlaced(secondHalfState, secondHalfPos, world);
    }

}

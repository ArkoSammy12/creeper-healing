package xd.arkosammy.handlers;

import net.minecraft.block.*;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.fluid.Fluids;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import xd.arkosammy.events.CreeperExplosionEvent;
import static xd.arkosammy.CreeperHealing.CONFIG;
import static xd.arkosammy.handlers.ExplosionHealerHandler.shouldPlaySound;

public final class DoubleBlockHandler {

    private DoubleBlockHandler(){}

    public static boolean isDoubleBlock(World world, BlockState state, BlockPos pos, CreeperExplosionEvent currentCreeperExplosionEvent){

        if(state.contains(Properties.DOUBLE_BLOCK_HALF)) {

            handleDoubleBlocks(world, state, pos, currentCreeperExplosionEvent);

            return true;

        } else if (state.contains(Properties.BED_PART)) {

            handleBedPart(world, state, pos, currentCreeperExplosionEvent);

            return true;

        }

        return false;

    }

    private static void handleDoubleBlocks(World world, BlockState firstHalfState, BlockPos firstHalfPos, CreeperExplosionEvent currentCreeperExplosionEvent){

        //Get the opposite half
        DoubleBlockHalf secondHalf = firstHalfState.get(Properties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.UPPER) ? DoubleBlockHalf.LOWER : DoubleBlockHalf.UPPER;

        //Transfer over the same properties to the new half of the block
        BlockState secondHalfState = firstHalfState.getBlock().getStateWithProperties(firstHalfState).with(Properties.DOUBLE_BLOCK_HALF, secondHalf);

        //Obtain the opposite coordinate of the current half of the door
        BlockPos secondHalfPos = secondHalfState.get(Properties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.UPPER) ? firstHalfPos.offset(Direction.Axis.Y, 1) :  firstHalfPos.offset(Direction.Axis.Y, -1);

        if(canPlaceBothHalves(world, firstHalfPos, secondHalfPos)) {

            world.setBlockState(firstHalfPos, firstHalfState);
            world.setBlockState(secondHalfPos, secondHalfState);

            //The first argument being null tells the server to play the sound to all nearby players
            if(shouldPlaySound(world, firstHalfState)) world.playSound(null, firstHalfPos, firstHalfState.getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, firstHalfState.getSoundGroup().getVolume(), firstHalfState.getSoundGroup().getPitch());

        }

        //To avoid potentially placing back the special block if it is broken before the upper half is reached
        currentCreeperExplosionEvent.markSecondHalfAsPlaced(secondHalfState, secondHalfPos, world);


    }

    private static void handleBedPart(World world, BlockState firstHalfState, BlockPos firstHalfPos, CreeperExplosionEvent currentCreeperExplosionEvent) {

        //Get the opposite part of the current part of the bed
        BedPart secondBedPart = firstHalfState.get(Properties.BED_PART).equals(BedPart.HEAD) ? BedPart.FOOT : BedPart.HEAD;

        //Transfer over the same properties and the opposite part to the new part of the bed
        BlockState secondHalfState = firstHalfState.getBlock().getStateWithProperties(firstHalfState).with(Properties.BED_PART, secondBedPart);

        BlockPos secondHalfPos;

        if(secondBedPart.equals(BedPart.HEAD)) {

            //Get the position where we should tryPlacing the head part of the bed
            switch (firstHalfState.get(Properties.HORIZONTAL_FACING)) {

                case NORTH -> secondHalfPos = firstHalfPos.offset(Direction.Axis.Z, -1);
                case SOUTH -> secondHalfPos = firstHalfPos.offset(Direction.Axis.Z, 1);
                case EAST -> secondHalfPos = firstHalfPos.offset(Direction.Axis.X, 1);
                default -> secondHalfPos = firstHalfPos.offset(Direction.Axis.X, -1);

            }

        } else {

            //Get the position where we should tryPlacing the foot part of the bed
            switch (firstHalfState.get(Properties.HORIZONTAL_FACING)) {

                case NORTH -> secondHalfPos = firstHalfPos.offset(Direction.Axis.Z, 1);
                case SOUTH -> secondHalfPos = firstHalfPos.offset(Direction.Axis.Z, -1);
                case EAST -> secondHalfPos = firstHalfPos.offset(Direction.Axis.X, -1);
                default -> secondHalfPos = firstHalfPos.offset(Direction.Axis.X, 1);

            }

        }

        if (canPlaceBothHalves(world, firstHalfPos, secondHalfPos)) {

            world.setBlockState(firstHalfPos, firstHalfState);
            world.setBlockState(secondHalfPos, secondHalfState);

            //The first argument being null tells the server to play the sound to all nearby players
            if (shouldPlaySound(world, firstHalfState))
                world.playSound(null, firstHalfPos, firstHalfState.getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, firstHalfState.getSoundGroup().getVolume(), firstHalfState.getSoundGroup().getPitch());

        }

        //To avoid potentially placing back the special block if it is broken before the opposite half is reached
        currentCreeperExplosionEvent.markSecondHalfAsPlaced(secondHalfState, secondHalfPos, world);


    }

    private static boolean canPlaceBothHalves(@NotNull World world, BlockPos firstHalfPos, BlockPos secondHalfPos){

        //Place both halves if both halves' coordinates are empty
        if(world.isAir(firstHalfPos) && world.isAir(secondHalfPos)) {

            return true;

            //Otherwise, tryPlacing both halves if:
            // - First half flowing water, second half air or,
            // - First half air, second half flowing water or,
            // - Both halves are flowing water
            //and the corresponding setting is enabled
        } else if (((world.getBlockState(firstHalfPos).getFluidState().getFluid().equals(Fluids.FLOWING_WATER) && world.isAir(secondHalfPos))
                 || (world.isAir(firstHalfPos) && world.getBlockState(secondHalfPos).getFluidState().getFluid().equals(Fluids.FLOWING_WATER))
                 || (world.getBlockState(firstHalfPos).getFluidState().getFluid().equals(Fluids.FLOWING_WATER) && world.getBlockState(secondHalfPos).getFluidState().getFluid().equals(Fluids.FLOWING_WATER)))
                 && CONFIG.shouldHealOnFlowingWater()){

            return true;

            //Otherwise, tryPlacing both halves if:
            // - First half flowing lava, second half air or,
            // - First half air, second half flowing lava or,
            // - Both halves are flowing lava
            //and the corresponding setting is enabled
        } else return ((world.getBlockState(firstHalfPos).getFluidState().getFluid().equals(Fluids.FLOWING_LAVA) && world.isAir(secondHalfPos))
                    || (world.isAir(firstHalfPos) && world.getBlockState(secondHalfPos).getFluidState().getFluid().equals(Fluids.FLOWING_LAVA))
                    || (world.getBlockState(firstHalfPos).getFluidState().getFluid().equals(Fluids.FLOWING_LAVA) && world.getBlockState(secondHalfPos).getFluidState().getFluid().equals(Fluids.FLOWING_LAVA)))
                    && CONFIG.shouldHealOnFlowingLava();

    }
}

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

public class SpecialBlockHandler {

    public static boolean isSpecialBlock(World world, BlockState state, BlockPos pos, CreeperExplosionEvent creeperExplosionEvent){

        if(state.contains(Properties.DOUBLE_BLOCK_HALF)) {

            return handleTallBlock(world, state, pos, creeperExplosionEvent);

        } else if (state.contains(Properties.BED_PART)) {

            return handleBedPart(world, state, pos, creeperExplosionEvent);

        }

        return false;

    }

    private static boolean handleTallBlock(World world, BlockState firstHalfState, BlockPos firstHalfPos, CreeperExplosionEvent creeperExplosionEvent){

        //Check for door block
        if(firstHalfState.getBlock() instanceof DoorBlock currentDoorHalf) {

            //Get the opposite half of the current half of the door
            DoubleBlockHalf secondHalf = firstHalfState.get(Properties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.UPPER) ? DoubleBlockHalf.LOWER : DoubleBlockHalf.UPPER;

            //Transfer over the same properties and the opposite half to the new half of the door
            BlockState secondHalfState = currentDoorHalf.getDefaultState()
                    .with(Properties.DOUBLE_BLOCK_HALF, secondHalf)
                    .with(Properties.OPEN, firstHalfState.get(Properties.OPEN))
                    .with(Properties.POWERED, firstHalfState.get(Properties.POWERED))
                    .with(Properties.DOOR_HINGE, firstHalfState.get(Properties.DOOR_HINGE))
                    .with(Properties.HORIZONTAL_FACING, firstHalfState.get(Properties.HORIZONTAL_FACING));

            //Obtain the opposite coordinate of the current half of the door
            BlockPos secondHalfPos = secondHalfState.get(Properties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.UPPER) ? firstHalfPos.offset(Direction.Axis.Y, 1) :  firstHalfPos.offset(Direction.Axis.Y, -1);

            if(canPlaceBothHalves(world, firstHalfPos, secondHalfPos)) {

                world.setBlockState(firstHalfPos, firstHalfState);
                world.setBlockState(secondHalfPos, secondHalfState);

                //The first argument being null tells the server to play the sound to all nearby players
                if(shouldPlaySound(world, firstHalfState)) world.playSound(null, firstHalfPos, firstHalfState.getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, firstHalfState.getSoundGroup().getVolume(), firstHalfState.getSoundGroup().getPitch());

            }

            //To avoid potentially placing back the special block if it is broken before the upper half is reached
            creeperExplosionEvent.markSecondHalfAsPlaced(secondHalfState, secondHalfPos, world);

            return true;

            //Check for tall flowers
        } else if (firstHalfState.getBlock() instanceof TallFlowerBlock currentFlowerHalf) {

            //Get the opposite half of the current half of the flower
            DoubleBlockHalf secondFlowerHalf = firstHalfState.get(Properties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.UPPER) ? DoubleBlockHalf.LOWER : DoubleBlockHalf.UPPER;

            //Transfer over the same properties and the opposite half to the new half of the flower
            BlockState secondHalfState = currentFlowerHalf.getDefaultState().with(Properties.DOUBLE_BLOCK_HALF, secondFlowerHalf);

            //Obtain the opposite coordinate of the current half of the flower
            BlockPos secondHalfPos = secondHalfState.get(Properties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.UPPER) ? firstHalfPos.offset(Direction.Axis.Y, 1) :  firstHalfPos.offset(Direction.Axis.Y, -1);

            if(canPlaceBothHalves(world, firstHalfPos, secondHalfPos)) {

                world.setBlockState(firstHalfPos, firstHalfState);
                world.setBlockState(secondHalfPos, secondHalfState);

                //The first argument being null tells the server to play the sound to all nearby players
                if(shouldPlaySound(world, firstHalfState)) world.playSound(null, firstHalfPos, firstHalfState.getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, firstHalfState.getSoundGroup().getVolume(), firstHalfState.getSoundGroup().getPitch());

            }

            //To avoid potentially placing back the special block if it is broken before the upper half is reached
            creeperExplosionEvent.markSecondHalfAsPlaced(secondHalfState, secondHalfPos, world);

            return true;

            //Check for tall plants
        } else if (firstHalfState.getBlock() instanceof TallPlantBlock originalPlant) {

            //Get the opposite half of the current half of the plant
            DoubleBlockHalf secondPlantHalf = firstHalfState.get(Properties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.UPPER) ? DoubleBlockHalf.LOWER : DoubleBlockHalf.UPPER;

            //Transfer over the same properties and the opposite half to the new half of the plant
            BlockState secondHalfState = originalPlant.getDefaultState().with(Properties.DOUBLE_BLOCK_HALF, secondPlantHalf);

            //Obtain the opposite coordinate of the current half of the plwant
            BlockPos secondHalfPos = secondHalfState.get(Properties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.UPPER) ? firstHalfPos.offset(Direction.Axis.Y, 1) :  firstHalfPos.offset(Direction.Axis.Y, -1);

            if(canPlaceBothHalves(world, firstHalfPos, secondHalfPos)) {

                world.setBlockState(firstHalfPos, firstHalfState);
                world.setBlockState(secondHalfPos, secondHalfState);

                //The first argument being null tells the server to play the sound to all nearby players
                if(shouldPlaySound(world, firstHalfState)) world.playSound(null, firstHalfPos, firstHalfState.getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, firstHalfState.getSoundGroup().getVolume(), firstHalfState.getSoundGroup().getPitch());

            }

            //To avoid potentially placing back the special block if it is broken before the upper half is reached
            creeperExplosionEvent.markSecondHalfAsPlaced(secondHalfState, secondHalfPos, world);

            return true;

        }

        return false;

    }

    private static boolean handleBedPart(World world, BlockState firstHalfState, BlockPos firstHalfPos, CreeperExplosionEvent creeperExplosionEvent) {

        if(firstHalfState.getBlock() instanceof BedBlock currentBedHalf) {

            //Get the opposite part of the current part of the bed
            BedPart newBedHalf = firstHalfState.get(Properties.BED_PART).equals(BedPart.HEAD) ? BedPart.FOOT : BedPart.HEAD;

            //Transfer over the same properties and the opposite part to the new part of the bed
            BlockState secondHalfState = currentBedHalf.getDefaultState()
                    .with(Properties.BED_PART, newBedHalf)
                    .with(Properties.HORIZONTAL_FACING, firstHalfState.get(Properties.HORIZONTAL_FACING));

            BlockPos secondHalfPos;

            if(newBedHalf.equals(BedPart.HEAD)) {

                //Get the position where we should tryPlacing the head part of the bed
                switch (firstHalfState.get(Properties.HORIZONTAL_FACING)) {

                    case NORTH -> secondHalfPos = firstHalfPos.offset(Direction.Axis.Z, -1);
                    case SOUTH -> secondHalfPos = firstHalfPos.offset(Direction.Axis.Z, 1);
                    case EAST -> secondHalfPos = firstHalfPos.offset(Direction.Axis.X, 1);
                    case WEST -> secondHalfPos = firstHalfPos.offset(Direction.Axis.X, -1);
                    default -> secondHalfPos = null;

                }

            } else {

                //Get the position where we should tryPlacing the foot part of the bed
                switch (firstHalfState.get(Properties.HORIZONTAL_FACING)) {

                    case NORTH -> secondHalfPos = firstHalfPos.offset(Direction.Axis.Z, 1);
                    case SOUTH -> secondHalfPos = firstHalfPos.offset(Direction.Axis.Z, -1);
                    case EAST -> secondHalfPos = firstHalfPos.offset(Direction.Axis.X, -1);
                    case WEST -> secondHalfPos = firstHalfPos.offset(Direction.Axis.X, 1);
                    default -> secondHalfPos = null;

                }

            }

            if(secondHalfPos != null) {

                if(canPlaceBothHalves(world, firstHalfPos, secondHalfPos)) {

                    world.setBlockState(firstHalfPos, firstHalfState);
                    world.setBlockState(secondHalfPos, secondHalfState);

                    //The first argument being null tells the server to play the sound to all nearby players
                    if(shouldPlaySound(world, firstHalfState)) world.playSound(null, firstHalfPos, firstHalfState.getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, firstHalfState.getSoundGroup().getVolume(), firstHalfState.getSoundGroup().getPitch());

                }

            }

            //To avoid potentially placing back the special block if it is broken before the opposite half is reached
            creeperExplosionEvent.markSecondHalfAsPlaced(secondHalfState, secondHalfPos, world);

            return true;

        }

        return false;

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

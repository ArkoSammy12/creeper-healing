package xd.arkosammy.handlers;

import net.minecraft.block.*;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.fluid.Fluids;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import static xd.arkosammy.CreeperHealing.CONFIG;
import static xd.arkosammy.handlers.ExplosionHealerHandler.shouldPlaySound;

public class SpecialBlockHandler {

    public static boolean isSpecialBlock(World world, BlockState state, BlockPos pos){

        if(state.contains(Properties.DOUBLE_BLOCK_HALF)) {

            //CreeperHealing.LOGGER.info("Block: " + state.getBlock().getName().toString() + " has the double block half property");

            //CreeperHealing.LOGGER.info("Properties: " + state);

            return handleTallBlock(world, state, pos);


        } else if (state.contains(Properties.BED_PART)) {

            //CreeperHealing.LOGGER.info("Block: " + state.getBlock().getName().toString() + " has the part properties");

            return handleBedPart(world, state, pos);


        }

        return false;

    }

    private static boolean handleTallBlock(World world, BlockState originalState, BlockPos originalPos){

        if(originalState.getBlock() instanceof DoorBlock originalDoor) {

            //Get the opposite half of the current half of the door
            DoubleBlockHalf newHalf = originalState.get(Properties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.UPPER)
                    ? DoubleBlockHalf.LOWER : DoubleBlockHalf.UPPER;

            //Transfer over the same properties and the opposite half to the new half of the door
            BlockState newState = originalDoor.getDefaultState()
                    .with(Properties.DOUBLE_BLOCK_HALF, newHalf)
                    .with(Properties.OPEN, originalState.get(Properties.OPEN))
                    .with(Properties.POWERED, originalState.get(Properties.POWERED))
                    .with(Properties.DOOR_HINGE, originalState.get(Properties.DOOR_HINGE))
                    .with(Properties.HORIZONTAL_FACING, originalState.get(Properties.HORIZONTAL_FACING));

            //Obtain the opposite coordinate of the current half of the door
            BlockPos newPos = newState.get(Properties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.UPPER)
                    ? new BlockPos(originalPos.getX(), originalPos.getY() + 1, originalPos.getZ()) :  new BlockPos(originalPos.getX(), originalPos.getY() - 1, originalPos.getZ());


            if(canPlaceBothHalves(world, originalPos, newPos)) {

                world.setBlockState(originalPos, originalState);
                world.setBlockState(newPos, newState);

                //The first argument being null tells the server to play the sound to all nearby players
                if(shouldPlaySound(world, originalState)) world.playSound(null, originalPos, originalState.getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, originalState.getSoundGroup().getVolume(), originalState.getSoundGroup().getPitch());

            }

            return true;

        } else if (originalState.getBlock() instanceof TallFlowerBlock originalFlower) {

            if(originalState.contains(Properties.DOUBLE_BLOCK_HALF)) {

                //Get the opposite half of the current half of the flower
                DoubleBlockHalf newHalf = originalState.get(Properties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.UPPER)
                        ? DoubleBlockHalf.LOWER : DoubleBlockHalf.UPPER;

                //Transfer over the same properties and the opposite half to the new half of the flower
                BlockState newState = originalFlower.getDefaultState()
                        .with(Properties.DOUBLE_BLOCK_HALF, newHalf);

                //Obtain the opposite coordinate of the current half of the flower
                BlockPos newPos = newState.get(Properties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.UPPER)
                        ? new BlockPos(originalPos.getX(), originalPos.getY() + 1, originalPos.getZ()) :  new BlockPos(originalPos.getX(), originalPos.getY() - 1, originalPos.getZ());

                if(canPlaceBothHalves(world, originalPos, newPos)) {

                    world.setBlockState(originalPos, originalState);
                    world.setBlockState(newPos, newState);

                    //The first argument being null tells the server to play the sound to all nearby players
                    if(shouldPlaySound(world, originalState)) world.playSound(null, originalPos, originalState.getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, originalState.getSoundGroup().getVolume(), originalState.getSoundGroup().getPitch());

                }

                return true;

            }

        } else if (originalState.getBlock() instanceof TallPlantBlock originalPlant) {

            if(originalState.contains(Properties.DOUBLE_BLOCK_HALF)) {

                //Get the opposite half of the current half of the plant
                DoubleBlockHalf newHalf = originalState.get(Properties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.UPPER)
                        ? DoubleBlockHalf.LOWER : DoubleBlockHalf.UPPER;

                //Transfer over the same properties and the opposite half to the new half of the plant
                BlockState newState = originalPlant.getDefaultState()
                        .with(Properties.DOUBLE_BLOCK_HALF, newHalf);

                //Obtain the opposite coordinate of the current half of the plwant
                BlockPos newPos = newState.get(Properties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.UPPER)
                        ? new BlockPos(originalPos.getX(), originalPos.getY() + 1, originalPos.getZ()) :  new BlockPos(originalPos.getX(), originalPos.getY() - 1, originalPos.getZ());

                if(canPlaceBothHalves(world, originalPos, newPos)) {

                    world.setBlockState(originalPos, originalState);
                    world.setBlockState(newPos, newState);

                    //The first argument being null tells the server to play the sound to all nearby players
                    if(shouldPlaySound(world, originalState)) world.playSound(null, originalPos, originalState.getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, originalState.getSoundGroup().getVolume(), originalState.getSoundGroup().getPitch());

                }

                return true;

            }

        }

        return false;

    }

    private static boolean handleBedPart(World world, BlockState originalState, BlockPos originalPos) {

        if(originalState.getBlock() instanceof BedBlock originalBed) {

            //Get the opposite part of the current part of the bed
            BedPart newBedPart = originalState.get(Properties.BED_PART).equals(BedPart.HEAD)
                    ? BedPart.FOOT : BedPart.HEAD;

            //Transfer over the same properties and the opposite part to the new part of the bed
            BlockState newState = originalBed.getDefaultState()
                    .with(Properties.BED_PART, newBedPart)
                    .with(Properties.HORIZONTAL_FACING, originalState.get(Properties.HORIZONTAL_FACING));

            BlockPos newPos;

            if(newBedPart.equals(BedPart.HEAD)) {

                //Get the position where we should place the head part of the bed
                switch (originalState.get(Properties.HORIZONTAL_FACING)) {

                    case NORTH -> newPos = new BlockPos(originalPos.getX(), originalPos.getY(), originalPos.getZ() - 1);
                    case SOUTH -> newPos = new BlockPos(originalPos.getX(), originalPos.getY(), originalPos.getZ() + 1);
                    case EAST -> newPos = new BlockPos(originalPos.getX() + 1, originalPos.getY(), originalPos.getZ());
                    case WEST -> newPos = new BlockPos(originalPos.getX() - 1, originalPos.getY(), originalPos.getZ());
                    default -> newPos = null;

                }

            } else {

                //Get the position where we should place the foot part of the bed
                switch (originalState.get(Properties.HORIZONTAL_FACING)) {

                    case NORTH -> newPos = new BlockPos(originalPos.getX(), originalPos.getY(), originalPos.getZ() + 1);
                    case SOUTH -> newPos = new BlockPos(originalPos.getX(), originalPos.getY(), originalPos.getZ() - 1);
                    case EAST -> newPos = new BlockPos(originalPos.getX() - 1, originalPos.getY(), originalPos.getZ());
                    case WEST -> newPos = new BlockPos(originalPos.getX() + 1, originalPos.getY(), originalPos.getZ());
                    default -> newPos = null;

                }

            }

            if(newPos != null) {

                if(canPlaceBothHalves(world, originalPos, newPos)) {

                    world.setBlockState(originalPos, originalState);
                    world.setBlockState(newPos, newState);

                    //The first argument being null tells the server to play the sound to all nearby players
                    if(shouldPlaySound(world, originalState)) world.playSound(null, originalPos, originalState.getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, originalState.getSoundGroup().getVolume(), originalState.getSoundGroup().getPitch());

                }

            }

            return true;

        }

        return false;

    }

    private static boolean canPlaceBothHalves(@NotNull World world, BlockPos oldPos, BlockPos newPos){

        //Place both halves if both halves' coordinates are empty
        if(world.getBlockState(oldPos).equals(Blocks.AIR.getDefaultState()) && world.getBlockState(newPos).equals(Blocks.AIR.getDefaultState())) {

            return true;

            //Otherwise, place both halves if:
            // - First half flowing water, second half air or,
            // - First half air, second half flowing water or,
            // - Both halves are flowing water
            //and the corresponding setting is enabled
        } else if(((world.getBlockState(oldPos).getFluidState().getFluid().equals(Fluids.FLOWING_WATER) && world.getBlockState(newPos).equals(Blocks.AIR.getDefaultState()))
                || (world.getBlockState(oldPos).equals(Blocks.AIR.getDefaultState()) && world.getBlockState(newPos).getFluidState().getFluid().equals(Fluids.FLOWING_WATER))
                || (world.getBlockState(oldPos).getFluidState().getFluid().equals(Fluids.FLOWING_WATER) && world.getBlockState(newPos).getFluidState().getFluid().equals(Fluids.FLOWING_WATER)))
                && CONFIG.shouldHealOnFlowingWater()){

            return true;

            //Otherwise, place both halves if:
            // - First half flowing lava, second half air or,
            // - First half air, second half flowing lava or,
            // - Both halves are flowing lava
            //and the corresponding setting is enabled
        } else return ((world.getBlockState(oldPos).getFluidState().getFluid().equals(Fluids.FLOWING_LAVA) && world.getBlockState(newPos).equals(Blocks.AIR.getDefaultState()))
                || (world.getBlockState(oldPos).equals(Blocks.AIR.getDefaultState()) && world.getBlockState(newPos).getFluidState().getFluid().equals(Fluids.FLOWING_LAVA))
                || (world.getBlockState(oldPos).getFluidState().getFluid().equals(Fluids.FLOWING_LAVA) && world.getBlockState(newPos).getFluidState().getFluid().equals(Fluids.FLOWING_LAVA)))
                && CONFIG.shouldHealOnFlowingLava();

    }
}

package xd.arkosammy.creeperhealing.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import xd.arkosammy.creeperhealing.configuration.PreferencesConfig;
import xd.arkosammy.creeperhealing.configuration.ReplaceMapConfig;
import xd.arkosammy.creeperhealing.explosions.AbstractExplosionEvent;
import xd.arkosammy.creeperhealing.util.ExplosionUtils;

public class DoubleAffectedBlock extends AffectedBlock {

    public static final String TYPE = "double_affected_block";

    public DoubleAffectedBlock(BlockPos pos, BlockState state, RegistryKey<World> registryKey, long affectedBlockTimer, boolean placed){
        super(pos, state, registryKey, affectedBlockTimer, placed);
    }

    @Override
    String getAffectedBlockType(){
        return TYPE;
    }

    @Override
    public void tryHealing(MinecraftServer server, AbstractExplosionEvent currentExplosionEvent){

        BlockState state = this.getState();
        String blockIdentifier = Registries.BLOCK.getId(state.getBlock()).toString();
        if(ReplaceMapConfig.getReplaceMap().containsKey(blockIdentifier)){
            super.tryHealing(server, currentExplosionEvent);
            return;
        }
        if(state.contains(Properties.DOUBLE_BLOCK_HALF)) {
            handleDoubleBlocks(server ,currentExplosionEvent);
        } else if (state.contains(Properties.BED_PART)) {
            handleBedPart(server, currentExplosionEvent);
        }

    }

    private void handleDoubleBlocks(MinecraftServer server, AbstractExplosionEvent currentExplosionEvent){

        BlockState firstHalfState = this.getState();
        BlockPos firstHalfPos = this.getPos();
        World world = this.getWorld(server);

        DoubleBlockHalf secondHalf = firstHalfState.get(Properties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.UPPER) ? DoubleBlockHalf.LOWER : DoubleBlockHalf.UPPER;
        BlockState secondHalfState = firstHalfState.getBlock().getStateWithProperties(firstHalfState).with(Properties.DOUBLE_BLOCK_HALF, secondHalf);
        BlockPos secondHalfPos = secondHalfState.get(Properties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.UPPER) ? firstHalfPos.offset(Direction.Axis.Y, 1) :  firstHalfPos.offset(Direction.Axis.Y, -1);

        if(this.shouldHealDoubleBlock(secondHalfState)) {
            BlockState stateToPushFrom = firstHalfState.get(Properties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.LOWER) ? firstHalfState : secondHalfState;
            BlockPos posToPushFrom = firstHalfState.get(Properties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.LOWER) ? firstHalfPos : firstHalfPos.offset(Direction.Axis.Y, -1);

            if(stateToPushFrom.isSolidBlock(world, posToPushFrom)) {
                ExplosionUtils.pushEntitiesUpwards(world, posToPushFrom, true);
            }

            world.setBlockState(firstHalfPos, firstHalfState);
            world.setBlockState(secondHalfPos, secondHalfState);

            if(ExplosionUtils.shouldPlaySoundOnBlockHeal(world, firstHalfState)) {
                world.playSound(null, firstHalfPos, firstHalfState.getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, firstHalfState.getSoundGroup().getVolume(), firstHalfState.getSoundGroup().getPitch());
            }
        }
        currentExplosionEvent.markAffectedBlockAsPlaced(secondHalfState, secondHalfPos, world);
    }

    private void handleBedPart(MinecraftServer server, AbstractExplosionEvent currentExplosionEvent) {

        BlockState firstHalfState = this.getState();
        BlockPos firstHalfPos = this.getPos();
        World world = this.getWorld(server);

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

        if (this.shouldHealDoubleBlock(secondHalfState)) {

            if(firstHalfState.isSolidBlock(world, firstHalfPos)) {
                ExplosionUtils.pushEntitiesUpwards(world, firstHalfPos, false);
            }
            if(secondHalfState.isSolidBlock(world, secondHalfPos)) {
                ExplosionUtils.pushEntitiesUpwards(world, secondHalfPos, false);
            }
            world.setBlockState(firstHalfPos, firstHalfState);
            world.setBlockState(secondHalfPos, secondHalfState);
            if (ExplosionUtils.shouldPlaySoundOnBlockHeal(world, firstHalfState)) {
                world.playSound(null, firstHalfPos, firstHalfState.getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, firstHalfState.getSoundGroup().getVolume(), firstHalfState.getSoundGroup().getPitch());
            }
        }
        currentExplosionEvent.markAffectedBlockAsPlaced(secondHalfState, secondHalfPos, world);
    }

    private boolean shouldHealDoubleBlock(BlockState secondHalfState){

        BlockState firstHalfState = this.getState();
        FluidState firstHalfFluidState = firstHalfState.getFluidState();
        FluidState secondHalfFluidState = secondHalfState.getFluidState();

        if (ExplosionUtils.isStateAirOrFire(firstHalfState) && ExplosionUtils.isStateAirOrFire(secondHalfState)) {
            return true;
        } else if (((firstHalfFluidState.getFluid().equals(Fluids.FLOWING_WATER) && ExplosionUtils.isStateAirOrFire(secondHalfState)) ||
                (ExplosionUtils.isStateAirOrFire(firstHalfState) && secondHalfFluidState.getFluid().equals(Fluids.FLOWING_WATER)) ||
                (firstHalfFluidState.getFluid().equals(Fluids.FLOWING_WATER) && secondHalfFluidState.getFluid().equals(Fluids.FLOWING_WATER)))
                && PreferencesConfig.HEAL_ON_FLOWING_WATER.getEntry().getValue()) {
            return true;
        } else if (((firstHalfFluidState.getFluid().equals(Fluids.WATER) && ExplosionUtils.isStateAirOrFire(secondHalfState)) ||
                (ExplosionUtils.isStateAirOrFire(firstHalfState) && secondHalfFluidState.getFluid().equals(Fluids.WATER)) ||
                (firstHalfFluidState.getFluid().equals(Fluids.WATER) && secondHalfFluidState.getFluid().equals(Fluids.WATER)))
                && PreferencesConfig.HEAL_ON_SOURCE_WATER.getEntry().getValue()) {
            return true;
        } else if (((firstHalfFluidState.getFluid().equals(Fluids.FLOWING_LAVA) && ExplosionUtils.isStateAirOrFire(secondHalfState)) ||
                (ExplosionUtils.isStateAirOrFire(firstHalfState) && secondHalfFluidState.getFluid().equals(Fluids.FLOWING_LAVA)) ||
                (firstHalfFluidState.getFluid().equals(Fluids.FLOWING_LAVA) && secondHalfFluidState.getFluid().equals(Fluids.FLOWING_LAVA)))
                && PreferencesConfig.HEAL_ON_FLOWING_LAVA.getEntry().getValue()) {
            return true;
        } else {
            return ((firstHalfFluidState.getFluid().equals(Fluids.LAVA) && ExplosionUtils.isStateAirOrFire(secondHalfState)) ||
                    (ExplosionUtils.isStateAirOrFire(firstHalfState) && secondHalfFluidState.getFluid().equals(Fluids.LAVA)) ||
                    (firstHalfFluidState.getFluid().equals(Fluids.LAVA) && secondHalfFluidState.getFluid().equals(Fluids.LAVA)))
                    && PreferencesConfig.HEAL_ON_SOURCE_LAVA.getEntry().getValue();
        }

    }

}

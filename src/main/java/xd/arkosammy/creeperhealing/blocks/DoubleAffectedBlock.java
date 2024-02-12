package xd.arkosammy.creeperhealing.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import xd.arkosammy.creeperhealing.config.ReplaceMapConfig;
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
        } else {
            super.tryHealing(server, currentExplosionEvent);
        }
    }

    private void handleDoubleBlocks(MinecraftServer server, AbstractExplosionEvent currentExplosionEvent){

        BlockState firstHalfState = this.getState();
        BlockPos firstHalfPos = this.getPos();
        World world = this.getWorld(server);

        DoubleBlockHalf secondHalf = firstHalfState.get(Properties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.UPPER) ? DoubleBlockHalf.LOWER : DoubleBlockHalf.UPPER;
        BlockState secondHalfState = firstHalfState.getBlock().getStateWithProperties(firstHalfState).with(Properties.DOUBLE_BLOCK_HALF, secondHalf);
        BlockPos secondHalfPos = secondHalfState.get(Properties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.UPPER) ? firstHalfPos.up() :  firstHalfPos.down();

        if(this.shouldHealBlock(world, secondHalfPos)) {
            BlockState stateToPushFrom = firstHalfState.get(Properties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.LOWER) ? firstHalfState : secondHalfState;
            BlockPos posToPushFrom = firstHalfState.get(Properties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.LOWER) ? firstHalfPos : firstHalfPos.down();

            if(stateToPushFrom.isSolidBlock(world, posToPushFrom)) {
                ExplosionUtils.pushEntitiesUpwards(world, posToPushFrom, true);
            }
            world.setBlockState(firstHalfPos, firstHalfState);
            world.setBlockState(secondHalfPos, secondHalfState);

            if(ExplosionUtils.shouldPlayBlockPlacementSound(world, firstHalfState)) {
                world.playSound(null, firstHalfPos, firstHalfState.getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, firstHalfState.getSoundGroup().getVolume(), firstHalfState.getSoundGroup().getPitch());
            }
        }
        currentExplosionEvent.markAsPlaced(secondHalfState, secondHalfPos, world);
    }

    private void handleBedPart(MinecraftServer server, AbstractExplosionEvent currentExplosionEvent) {
        BlockState firstHalfState = this.getState();
        BlockPos firstHalfPos = this.getPos();
        World world = this.getWorld(server);
        BedPart secondBedPart = firstHalfState.get(Properties.BED_PART).equals(BedPart.HEAD) ? BedPart.FOOT : BedPart.HEAD;
        BlockState secondHalfState = firstHalfState.getBlock().getStateWithProperties(firstHalfState).with(Properties.BED_PART, secondBedPart);
        BlockPos secondHalfPos;

        // Offset the location of the second bed part depending on the orientation of the first
        Direction firstBedPartOrientation = firstHalfState.get(Properties.HORIZONTAL_FACING);
        if(secondBedPart.equals(BedPart.HEAD)) {
            switch (firstBedPartOrientation) {
                case NORTH -> secondHalfPos = firstHalfPos.north();
                case SOUTH -> secondHalfPos = firstHalfPos.south();
                case EAST -> secondHalfPos = firstHalfPos.east();
                default -> secondHalfPos = firstHalfPos.west();
            }
        } else {
            switch (firstBedPartOrientation) {
                case NORTH -> secondHalfPos = firstHalfPos.south();
                case SOUTH -> secondHalfPos = firstHalfPos.north();
                case EAST -> secondHalfPos = firstHalfPos.west();
                default -> secondHalfPos = firstHalfPos.east();
            }
        }

        if (this.shouldHealBlock(world, secondHalfPos)) {
            if(firstHalfState.isSolidBlock(world, firstHalfPos)) {
                ExplosionUtils.pushEntitiesUpwards(world, firstHalfPos, false);
            }
            if(secondHalfState.isSolidBlock(world, secondHalfPos)) {
                ExplosionUtils.pushEntitiesUpwards(world, secondHalfPos, false);
            }
            world.setBlockState(firstHalfPos, firstHalfState);
            world.setBlockState(secondHalfPos, secondHalfState);
            if (ExplosionUtils.shouldPlayBlockPlacementSound(world, firstHalfState)) {
                world.playSound(null, firstHalfPos, firstHalfState.getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, firstHalfState.getSoundGroup().getVolume(), firstHalfState.getSoundGroup().getPitch());
            }
        }
        currentExplosionEvent.markAsPlaced(secondHalfState, secondHalfPos, world);
    }


    @Override
    boolean shouldHealBlock(World world, BlockPos secondBlockPos){
        return world.getBlockState(this.getPos()).isReplaceable() && world.getBlockState(secondBlockPos).isReplaceable();
    }

}

package xd.arkosammy.creeperhealing.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import xd.arkosammy.creeperhealing.config.ConfigUtils;
import xd.arkosammy.creeperhealing.config.SettingGroups;
import xd.arkosammy.creeperhealing.explosions.AbstractExplosionEvent;
import xd.arkosammy.creeperhealing.explosions.ExplosionEvent;
import xd.arkosammy.creeperhealing.util.ExplosionUtils;
import xd.arkosammy.monkeyconfig.groups.SettingGroup;
import xd.arkosammy.monkeyconfig.groups.maps.StringMapSettingGroup;

public class DoubleAffectedBlock extends SingleAffectedBlock {

    public static final String TYPE = "double_affected_block";

    protected DoubleAffectedBlock(BlockPos pos, BlockState state, RegistryKey<World> registryKey, long affectedBlockTimer, boolean placed){
        super(pos, state, registryKey, null, affectedBlockTimer, placed);
    }

    @Override
    String getAffectedBlockType(){
        return TYPE;
    }

    @Override
    public void tryHealing(MinecraftServer server, ExplosionEvent currentExplosionEvent){

        final BlockState state = this.getBlockState();
        final String blockIdentifier = Registries.BLOCK.getId(state.getBlock()).toString();

        final SettingGroup settingGroup = ConfigUtils.getSettingGroup(SettingGroups.REPLACE_MAP.getName());
        if (settingGroup instanceof StringMapSettingGroup replaceMapGroup) {
            String replaceMapValue = replaceMapGroup.get(blockIdentifier);
            if (replaceMapValue != null) {
                super.tryHealing(server, currentExplosionEvent);
                return;
            }
        }

        if(state.contains(Properties.DOUBLE_BLOCK_HALF)) {
            handleDoubleBlocks(server ,currentExplosionEvent);
        } else if (state.contains(Properties.BED_PART)) {
            handleBedPart(server, currentExplosionEvent);
        } else {
            super.tryHealing(server, currentExplosionEvent);
        }
    }

    private void handleDoubleBlocks(MinecraftServer server, ExplosionEvent currentExplosionEvent){

        final BlockState firstHalfState = this.getBlockState();
        final BlockPos firstHalfPos = this.getBlockPos();
        final World world = this.getWorld(server);

        final DoubleBlockHalf secondHalf = firstHalfState.get(Properties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.UPPER) ? DoubleBlockHalf.LOWER : DoubleBlockHalf.UPPER;
        final BlockState secondHalfState = firstHalfState.getBlock().getStateWithProperties(firstHalfState).with(Properties.DOUBLE_BLOCK_HALF, secondHalf);
        final BlockPos secondHalfPos = secondHalfState.get(Properties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.UPPER) ? firstHalfPos.up() :  firstHalfPos.down();

        if(!this.shouldHealBlock(world, secondHalfPos)){
            return;
        }
        final BlockState stateToPushFrom = firstHalfState.get(Properties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.LOWER) ? firstHalfState : secondHalfState;
        final BlockPos posToPushFrom = firstHalfState.get(Properties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.LOWER) ? firstHalfPos : firstHalfPos.down();

        if(stateToPushFrom.isSolidBlock(world, posToPushFrom)) {
            ExplosionUtils.pushEntitiesUpwards(world, posToPushFrom, true);
        }
        this.placeDoubleBlock(new Pair<>(firstHalfPos, firstHalfState), new Pair<>(secondHalfPos, secondHalfState), world);
        if (currentExplosionEvent instanceof AbstractExplosionEvent abstractExplosionEvent) {
            abstractExplosionEvent.findAndMarkPlaced(secondHalfPos, secondHalfState, world);
        }
    }

    private void handleBedPart(MinecraftServer server, ExplosionEvent currentExplosionEvent) {
        final BlockState firstHalfState = this.getBlockState();
        final BlockPos firstHalfPos = this.getBlockPos();
        final World world = this.getWorld(server);
        final BedPart secondBedPart = firstHalfState.get(Properties.BED_PART).equals(BedPart.HEAD) ? BedPart.FOOT : BedPart.HEAD;
        final BlockState secondHalfState = firstHalfState.getBlock().getStateWithProperties(firstHalfState).with(Properties.BED_PART, secondBedPart);
        final BlockPos secondHalfPos;

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

        if(!this.shouldHealBlock(world, secondHalfPos)) {
            return;
        }
        if(firstHalfState.isSolidBlock(world, firstHalfPos)) {
            ExplosionUtils.pushEntitiesUpwards(world, firstHalfPos, false);
        }
        if(secondHalfState.isSolidBlock(world, secondHalfPos)) {
            ExplosionUtils.pushEntitiesUpwards(world, secondHalfPos, false);
        }
        this.placeDoubleBlock(new Pair<>(firstHalfPos, firstHalfState), new Pair<>(secondHalfPos, secondHalfState), world);
        if (currentExplosionEvent instanceof AbstractExplosionEvent abstractExplosionEvent) {
            abstractExplosionEvent.findAndMarkPlaced(secondHalfPos, secondHalfState, world);
        }
    }

    private void placeDoubleBlock(Pair<BlockPos, BlockState> first, Pair<BlockPos, BlockState> second, World world) {
        world.setBlockState(first.getLeft(), first.getRight());
        world.setBlockState(second.getLeft(), second.getRight());
        if (ExplosionUtils.shouldPlayBlockPlacementSound(world, first.getRight())) {
            world.playSound(null, first.getLeft(), first.getRight().getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, first.getRight().getSoundGroup().getVolume(), first.getRight().getSoundGroup().getPitch());
        }
    }


    @Override
    protected boolean shouldHealBlock(World world, BlockPos secondBlockPos){
        return world.getBlockState(this.getBlockPos()).isReplaceable() && world.getBlockState(secondBlockPos).isReplaceable();
    }

}

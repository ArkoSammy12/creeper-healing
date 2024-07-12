package xd.arkosammy.creeperhealing.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
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

    protected DoubleAffectedBlock(BlockPos pos, BlockState state, RegistryKey<World> registryKey, long affectedBlockTimer, boolean placed) {
        super(pos, state, registryKey, null, affectedBlockTimer, placed);
    }

    @Override
    protected String getAffectedBlockType() {
        return TYPE;
    }

    @Override
    public void tryHealing(MinecraftServer server, ExplosionEvent currentExplosionEvent) {

        BlockState state = this.getBlockState();
        String blockIdentifier = Registries.BLOCK.getId(state.getBlock()).toString();

        SettingGroup settingGroup = ConfigUtils.getSettingGroup(SettingGroups.REPLACE_MAP.getName());
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

    private void handleDoubleBlocks(MinecraftServer server, ExplosionEvent currentExplosionEvent) {
        BlockState firstHalfState = this.getBlockState();
        BlockPos firstHalfPos = this.getBlockPos();
        World world = this.getWorld(server);

        DoubleBlockHalf secondHalf = firstHalfState.get(Properties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.UPPER) ? DoubleBlockHalf.LOWER : DoubleBlockHalf.UPPER;
        BlockState secondHalfState = firstHalfState.getBlock().getStateWithProperties(firstHalfState).with(Properties.DOUBLE_BLOCK_HALF, secondHalf);
        BlockPos secondHalfPos = secondHalfState.get(Properties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.UPPER) ? firstHalfPos.up() :  firstHalfPos.down();

        if(!this.shouldHealBlock(world, secondHalfPos)){
            return;
        }
        BlockState stateToPushFrom = firstHalfState.get(Properties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.LOWER) ? firstHalfState : secondHalfState;
        BlockPos posToPushFrom = firstHalfState.get(Properties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.LOWER) ? firstHalfPos : firstHalfPos.down();
        ExplosionUtils.pushEntitiesUpwards(world, posToPushFrom, stateToPushFrom, true);
        this.placeDoubleBlock(new Pair<>(firstHalfPos, firstHalfState), new Pair<>(secondHalfPos, secondHalfState), world);
        if (currentExplosionEvent instanceof AbstractExplosionEvent abstractExplosionEvent) {
            abstractExplosionEvent.findAndMarkPlaced(secondHalfPos, secondHalfState, world);
        }
    }

    private void handleBedPart(MinecraftServer server, ExplosionEvent currentExplosionEvent) {
        BlockState firstHalfState = this.getBlockState();
        BlockPos firstHalfPos = this.getBlockPos();
        World world = this.getWorld(server);
        BedPart secondBedPart = firstHalfState.get(Properties.BED_PART).equals(BedPart.HEAD) ? BedPart.FOOT : BedPart.HEAD;
        BlockState secondHalfState = firstHalfState.getBlock().getStateWithProperties(firstHalfState).with(Properties.BED_PART, secondBedPart);
        Direction firstBedPartOrientation = firstHalfState.get(Properties.HORIZONTAL_FACING);

        // Offset the location of the second bed part depending on the orientation of the first
        BlockPos secondHalfPos = switch (secondBedPart) {
            case HEAD -> switch (firstBedPartOrientation) {
                case NORTH -> firstHalfPos.north();
                case SOUTH -> firstHalfPos.south();
                case EAST -> firstHalfPos.east();
                default -> firstHalfPos.west();
            };
            case FOOT -> switch (firstBedPartOrientation) {
                case NORTH -> firstHalfPos.south();
                case SOUTH -> firstHalfPos.north();
                case EAST -> firstHalfPos.west();
                default -> firstHalfPos.east();
            };
        };

        if(!this.shouldHealBlock(world, secondHalfPos)) {
            return;
        }
        ExplosionUtils.pushEntitiesUpwards(world, firstHalfPos, firstHalfState, false);
        ExplosionUtils.pushEntitiesUpwards(world, secondHalfPos, secondHalfState, false);
        this.placeDoubleBlock(new Pair<>(firstHalfPos, firstHalfState), new Pair<>(secondHalfPos, secondHalfState), world);
        if (currentExplosionEvent instanceof AbstractExplosionEvent abstractExplosionEvent) {
            abstractExplosionEvent.findAndMarkPlaced(secondHalfPos, secondHalfState, world);
        }
    }

    private void placeDoubleBlock(Pair<BlockPos, BlockState> first, Pair<BlockPos, BlockState> second, World world) {
        world.setBlockState(first.getLeft(), first.getRight());
        world.setBlockState(second.getLeft(), second.getRight());
        ExplosionUtils.playBlockPlacementSoundEffect(world, first.getLeft(), first.getRight());
        ExplosionUtils.spawnParticles(world, first.getLeft());
    }

    @Override
    protected boolean shouldHealBlock(World world, BlockPos secondBlockPos) {
        return world.getBlockState(this.getBlockPos()).isReplaceable() && world.getBlockState(secondBlockPos).isReplaceable();
    }

    @Override
    public String toString() {
        return "DoubleAffectedBlock(pos=%s, state=%s, world=%s, timer=%s, placed=%s)"
                .formatted(this.getBlockPos(), this.getBlockState(), this.getWorldRegistryKey(), this.getBlockTimer(), this.isPlaced());
    }

}

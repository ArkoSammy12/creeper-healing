package io.github.arkosammy12.creeperhealing.blocks;

import io.github.arkosammy12.creeperhealing.config.ConfigSettings;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.enums.ChestType;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import io.github.arkosammy12.creeperhealing.config.ConfigUtils;
import io.github.arkosammy12.creeperhealing.config.SettingGroups;
import io.github.arkosammy12.creeperhealing.explosions.AbstractExplosionEvent;
import io.github.arkosammy12.creeperhealing.explosions.ExplosionEvent;
import io.github.arkosammy12.creeperhealing.util.ExplosionUtils;
import org.jetbrains.annotations.Nullable;
import xd.arkosammy.monkeyconfig.groups.SettingGroup;
import xd.arkosammy.monkeyconfig.groups.maps.StringMapSettingGroup;
import xd.arkosammy.monkeyconfig.settings.BooleanSetting;

public class DoubleAffectedBlock extends SingleAffectedBlock {

    private final BlockPos secondHalfPos;
    private final BlockState secondHalfState;
    @Nullable
    private final NbtCompound secondHalfNbt;

    public static final String TYPE = "double_affected_block";

    protected DoubleAffectedBlock(BlockPos firstHalfPos, BlockState firstHalfState, @Nullable NbtCompound firstHalfNbt, BlockPos secondHalfPos, @Nullable BlockState secondHalfState, @Nullable NbtCompound secondHalfNbt, RegistryKey<World> registryKey, long affectedBlockTimer, boolean placed) {
        super(firstHalfPos, firstHalfState, registryKey, null, affectedBlockTimer, placed);
        this.secondHalfNbt = secondHalfNbt;

        if (secondHalfState == null) {
            if (firstHalfState.contains(Properties.DOUBLE_BLOCK_HALF)) {
                DoubleBlockHalf secondHalf = firstHalfState.get(Properties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.UPPER) ? DoubleBlockHalf.LOWER : DoubleBlockHalf.UPPER;
                this.secondHalfState = firstHalfState.getBlock().getStateWithProperties(firstHalfState).with(Properties.DOUBLE_BLOCK_HALF, secondHalf);
            } else {
                BedPart secondBedPart = firstHalfState.get(Properties.BED_PART).equals(BedPart.HEAD) ? BedPart.FOOT : BedPart.HEAD;
                this.secondHalfState = firstHalfState.getBlock().getStateWithProperties(firstHalfState).with(Properties.BED_PART, secondBedPart);
            }
        } else {
            this.secondHalfState = secondHalfState;
        }

        if (secondHalfPos == null) {
            if (firstHalfState.contains(Properties.DOUBLE_BLOCK_HALF)) {
                this.secondHalfPos = this.secondHalfState.get(Properties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.UPPER) ? firstHalfPos.up() :  firstHalfPos.down();
            } else {
                BedPart secondBedPart = firstHalfState.get(Properties.BED_PART).equals(BedPart.HEAD) ? BedPart.FOOT : BedPart.HEAD;
                Direction firstBedPartOrientation = firstHalfState.get(Properties.HORIZONTAL_FACING);
                this.secondHalfPos = switch (secondBedPart) {
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
            }

        } else {
            this.secondHalfPos = secondHalfPos;
        }

    }

    @Override
    protected String getAffectedBlockType() {
        return TYPE;
    }

    @Override
    public SerializedAffectedBlock asSerialized() {
        return new DefaultSerializedAffectedBlock(this.getAffectedBlockType(), this.getBlockPos(), this.getBlockState(), this.secondHalfPos, this.secondHalfState, this.getWorldRegistryKey(), this.nbt, this.secondHalfNbt, this.getBlockTimer(), this.isPlaced());
    }

    @Override
    public void tryHealing(MinecraftServer server, ExplosionEvent currentExplosionEvent) {

        this.setPlaced();
        BlockState firstHalfState = this.getBlockState();
        BlockPos firstHalfPos = this.getBlockPos();
        World world = this.getWorld(server);

        BlockState secondHalfState = this.secondHalfState;
        BlockPos secondHalfPos = this.secondHalfPos;

        boolean stateReplaced = false;
        String blockIdentifier = Registries.BLOCK.getId(firstHalfState.getBlock()).toString();

        SettingGroup settingGroup = ConfigUtils.getSettingGroup(SettingGroups.REPLACE_MAP.getName());
        if (settingGroup instanceof StringMapSettingGroup replaceMapGroup) {
            String replaceMapValue = replaceMapGroup.get(blockIdentifier);
            if (replaceMapValue != null && !this.shouldForceHeal()) {
                firstHalfState = Registries.BLOCK.get(Identifier.of(replaceMapValue)).getStateWithProperties(firstHalfState);
                secondHalfState = Registries.BLOCK.get(Identifier.of(replaceMapValue)).getStateWithProperties(secondHalfState);
                stateReplaced = true;
            }
        }

        if (!this.shouldHealBlock(world)) {
            return;
        }

        ExplosionUtils.pushEntitiesUpwards(world, firstHalfPos, firstHalfState, firstHalfState.contains(Properties.DOUBLE_BLOCK_HALF));
        boolean makeFallingBlocksFall = ConfigUtils.getSettingValue(ConfigSettings.MAKE_FALLING_BLOCKS_FALL.getSettingLocation(), BooleanSetting.class);
        if (firstHalfState.getBlock() instanceof FallingBlock) {
            ExplosionUtils.FALLING_BLOCK_SCHEDULE_TICK.set(makeFallingBlocksFall);
        }
        if (secondHalfState.getBlock() instanceof FallingBlock) {
            ExplosionUtils.FALLING_BLOCK_SCHEDULE_TICK.set(makeFallingBlocksFall);
        }
        world.setBlockState(firstHalfPos, firstHalfState);
        world.setBlockState(secondHalfPos, secondHalfState);

        //this.handleChestBlockIfNeeded(currentExplosionEvent, state, pos, server);
        boolean healNbtFirst = this.nbt != null && !stateReplaced;
        if (healNbtFirst) {
            world.addBlockEntity(BlockEntity.createFromNbt(firstHalfPos, firstHalfState, this.nbt, world.getRegistryManager()));
        }
        boolean healNbtSecond = this.secondHalfNbt != null && !stateReplaced;
        if (healNbtSecond) {
            world.addBlockEntity(BlockEntity.createFromNbt(secondHalfPos, secondHalfState, this.secondHalfNbt, world.getRegistryManager()));
        }

        ExplosionUtils.playBlockPlacementSoundEffect(world, firstHalfPos, firstHalfState);
        ExplosionUtils.spawnParticles(world, firstHalfPos);

    }

    private void handleDoubleBlocks(MinecraftServer server, ExplosionEvent currentExplosionEvent) {
        BlockState firstHalfState = this.getBlockState();
        BlockPos firstHalfPos = this.getBlockPos();
        World world = this.getWorld(server);

        DoubleBlockHalf secondHalf = firstHalfState.get(Properties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.UPPER) ? DoubleBlockHalf.LOWER : DoubleBlockHalf.UPPER;
        BlockState secondHalfState = firstHalfState.getBlock().getStateWithProperties(firstHalfState).with(Properties.DOUBLE_BLOCK_HALF, secondHalf);
        BlockPos secondHalfPos = secondHalfState.get(Properties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.UPPER) ? firstHalfPos.up() :  firstHalfPos.down();

        /*
        if (!this.shouldHealBlock(world, secondHalfPos)) {
            return;
        }

         */
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
        /*
        if(!this.shouldHealBlock(world, secondHalfPos)) {
            return;
        }

         */
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
    protected boolean shouldHealBlock(World world) {
        return world.getBlockState(this.getBlockPos()).isReplaceable() && world.getBlockState(this.secondHalfPos).isReplaceable();
    }

    @Override
    public String toString() {
        return "DoubleAffectedBlock(firstHalfPos=%s, firstHalfState=%s, world=%s, timer=%s, placed=%s)"
                .formatted(this.getBlockPos(), this.getBlockState(), this.getWorldRegistryKey(), this.getBlockTimer(), this.isPlaced());
    }

}

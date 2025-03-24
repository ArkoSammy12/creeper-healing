package io.github.arkosammy12.creeperhealing.blocks;

import io.github.arkosammy12.monkeyconfig.base.Setting;
import io.github.arkosammy12.monkeyconfig.sections.maps.StringMapSection;
import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import io.github.arkosammy12.creeperhealing.config.ConfigUtils;
import io.github.arkosammy12.creeperhealing.explosions.ExplosionEvent;
import io.github.arkosammy12.creeperhealing.util.ExplosionUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class DoubleAffectedBlock extends SingleAffectedBlock {

    private final BlockPos secondHalfPos;
    private final BlockState secondHalfState;
    @Nullable
    private final NbtCompound secondHalfNbt;

    public static final String TYPE = "double_affected_block";

    protected DoubleAffectedBlock(BlockPos firstHalfPos, BlockState firstHalfState, @Nullable NbtCompound firstHalfNbt, @Nullable BlockPos secondHalfPos, @Nullable BlockState secondHalfState, @Nullable NbtCompound secondHalfNbt, RegistryKey<World> registryKey, long affectedBlockTimer, boolean placed) {
        super(firstHalfPos, firstHalfState, registryKey, firstHalfNbt, affectedBlockTimer, placed);
        this.secondHalfNbt = secondHalfNbt;
        if (secondHalfState == null) {
            if (firstHalfState.contains(Properties.DOUBLE_BLOCK_HALF)) {
                DoubleBlockHalf secondHalf = firstHalfState.get(Properties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.UPPER) ? DoubleBlockHalf.LOWER : DoubleBlockHalf.UPPER;
                this.secondHalfState = firstHalfState.getBlock().getStateWithProperties(firstHalfState).with(Properties.DOUBLE_BLOCK_HALF, secondHalf);
            } else if (firstHalfState.contains(Properties.BED_PART)) {
                BedPart secondBedPart = firstHalfState.get(Properties.BED_PART).equals(BedPart.HEAD) ? BedPart.FOOT : BedPart.HEAD;
                this.secondHalfState = firstHalfState.getBlock().getStateWithProperties(firstHalfState).with(Properties.BED_PART, secondBedPart);
            } else {
                this.secondHalfState = null;
            }
        } else {
            this.secondHalfState = secondHalfState;
        }
        if (secondHalfPos == null) {
            this.secondHalfPos = getOtherHalfPos(firstHalfPos, firstHalfState);
        } else {
            this.secondHalfPos = secondHalfPos;
        }
    }

    @Override
    protected String getAffectedBlockType() {
        return TYPE;
    }

    public BlockState getSecondHalfState() {
        return this.secondHalfState;
    }

    public BlockPos getSecondHalfPos() {
        return this.secondHalfPos;
    }

    @Nullable
    public NbtCompound getSecondHalfNbt() {
        return this.secondHalfNbt;
    }

    @Override
    public SerializedAffectedBlock asSerialized() {
        return new DefaultSerializedAffectedBlock(this.getAffectedBlockType(), this.getBlockPos(), this.getBlockState(), this.getNbt(), this.getSecondHalfPos(), this.getSecondHalfState(), this.getSecondHalfNbt(), this.getWorldRegistryKey(), this.getBlockTimer(), this.isPlaced());
    }

    @Override
    protected boolean shouldHealBlock(World world) {
        return world.getBlockState(this.getBlockPos()).isReplaceable() && world.getBlockState(this.secondHalfPos).isReplaceable();
    }

    @Override
    protected boolean shouldForceHeal() {
        boolean forceBlocksWithNbtToAlwaysHeal = ConfigUtils.getRawBooleanSetting(ConfigUtils.FORCE_BLOCKS_WITH_NBT_TO_ALWAYS_HEAL);
        return this.getNbt() != null && this.getSecondHalfNbt() != null && forceBlocksWithNbtToAlwaysHeal;
    }

    @Override
    public void tryHealing(MinecraftServer server, ExplosionEvent currentExplosionEvent) {
        if (this.secondHalfState == null) {
            super.tryHealing(server, currentExplosionEvent);
            return;
        }

        this.setPlaced();
        BlockState firstHalfState = this.getBlockState();
        BlockPos firstHalfPos = this.getBlockPos();
        BlockState secondHalfState = this.secondHalfState;
        BlockPos secondHalfPos = this.secondHalfPos;
        World world = this.getWorld(server);
        boolean stateReplaced = false;

        String blockIdentifier = Registries.BLOCK.getId(firstHalfState.getBlock()).toString();
        StringMapSection replaceMapSection = ConfigUtils.getRawStringMapSection(ConfigUtils.REPLACE_MAP);
        Setting<String, ?> replaceMapValue = replaceMapSection.get(blockIdentifier);
        // Hardcode an exception to allow beds to be replaced with other blocks despite them having an Nbt tag.
        if (replaceMapValue != null && (!this.shouldForceHeal() || firstHalfState.isIn(BlockTags.BEDS))) {
            firstHalfState = Registries.BLOCK.get(Identifier.of(replaceMapValue.getValue().getRaw())).getStateWithProperties(firstHalfState);
            secondHalfState = Registries.BLOCK.get(Identifier.of(replaceMapValue.getValue().getRaw())).getStateWithProperties(secondHalfState);
            stateReplaced = true;
        }


        // Prevent both halves of a double block from being replaced with two of a single regular block
        if (!firstHalfState.contains(Properties.DOUBLE_BLOCK_HALF) && !firstHalfState.contains(Properties.BED_PART)) {
            super.tryHealing(server, currentExplosionEvent);
            return;
        }

        if (!this.shouldHealBlock(world)) {
            return;
        }

        ExplosionUtils.pushEntitiesUpwards(world, firstHalfPos, firstHalfState, firstHalfState.contains(Properties.DOUBLE_BLOCK_HALF));
        boolean makeFallingBlocksFall = ConfigUtils.getRawBooleanSetting(ConfigUtils.MAKE_FALLING_BLOCKS_FALL);
        if (firstHalfState.getBlock() instanceof FallingBlock) {
            ExplosionUtils.FALLING_BLOCK_SCHEDULE_TICK.set(makeFallingBlocksFall);
        }
        if (secondHalfState.getBlock() instanceof FallingBlock) {
            ExplosionUtils.FALLING_BLOCK_SCHEDULE_TICK.set(makeFallingBlocksFall);
        }

        world.setBlockState(firstHalfPos, firstHalfState);
        world.setBlockState(secondHalfPos, secondHalfState);

        boolean healFirstHalfNbt = this.getNbt() != null && !stateReplaced;
        if (healFirstHalfNbt) {
            world.addBlockEntity(BlockEntity.createFromNbt(firstHalfPos, firstHalfState, this.getNbt(), world.getRegistryManager()));
        }
        boolean healSecondHalfNbt = this.secondHalfNbt != null && !stateReplaced;
        if (healSecondHalfNbt) {
            world.addBlockEntity(BlockEntity.createFromNbt(secondHalfPos, secondHalfState, this.secondHalfNbt, world.getRegistryManager()));
        }
        ExplosionUtils.playBlockPlacementSoundEffect(world, firstHalfPos, firstHalfState);
        ExplosionUtils.spawnParticles(world, firstHalfPos);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DoubleAffectedBlock that)) return false;
        return Objects.equals(getBlockPos(), that.getBlockPos()) && Objects.equals(getBlockState(), that.getBlockState()) && Objects.equals(this.getSecondHalfPos(), that.getSecondHalfPos()) && Objects.equals(this.getSecondHalfState(), that.getSecondHalfState()) && Objects.equals(getWorldRegistryKey(), that.getWorldRegistryKey());
    }

    @Override
    public String toString() {
        return "DoubleAffectedBlock(firstHalfPos=%s, firstHalfState=%s, firstHalfNbt=%s, secondHalfPos=%s, secondHalfState=%s, secondHalfNbt=%s, world=%s, timer=%s, placed=%s)"
                .formatted(this.getBlockPos(), this.getBlockState(), this.getNbt(), this.secondHalfPos, this.secondHalfState, this.secondHalfNbt, this.getWorldRegistryKey(), this.getBlockTimer(), this.isPlaced());
    }

    @Nullable
    public static BlockPos getOtherHalfPos(BlockPos pos, BlockState state) {
        if (state.contains(Properties.DOUBLE_BLOCK_HALF)) {
            return DoubleAffectedBlock.getSecondDoubleBlockHalfPos(pos, state);
        } else if (state.contains(Properties.BED_PART)) {
            return DoubleAffectedBlock.getSecondBedBlockHalfPos(pos, state);
        }
        return null;
    }

    public static BlockPos getSecondDoubleBlockHalfPos(BlockPos firstHalfPos, BlockState firstHalfState) {
        return firstHalfState.get(Properties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.UPPER) ? firstHalfPos.down() :  firstHalfPos.up();
    }

    public static BlockPos getSecondBedBlockHalfPos(BlockPos firstHalfPos, BlockState firstHalfState) {
        BedPart secondBedPart = firstHalfState.get(Properties.BED_PART).equals(BedPart.HEAD) ? BedPart.FOOT : BedPart.HEAD;
        Direction firstBedPartOrientation = firstHalfState.get(Properties.HORIZONTAL_FACING);
        return switch (secondBedPart) {
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

}

package io.github.arkosammy12.creeperhealing.blocks;

import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import io.github.arkosammy12.creeperhealing.config.ConfigUtils;
import io.github.arkosammy12.creeperhealing.explosions.ExplosionEvent;
import io.github.arkosammy12.creeperhealing.util.ExplosionUtils;

import java.util.List;
import java.util.Objects;

public class SingleAffectedBlock implements AffectedBlock {

    public static final String TYPE = "single_affected_block";
    private final BlockPos blockPos;
    private final BlockState blockState;
    private final ResourceKey<Level> worldRegistryKey;
    @Nullable
    private final CompoundTag nbt;
    private long timer;
    private boolean placed;

    protected SingleAffectedBlock(BlockPos blockPos, BlockState blockState, ResourceKey<Level> registryKey, @Nullable CompoundTag nbt, long timer, boolean placed) {
        this.blockPos = blockPos;
        this.blockState = blockState;
        this.worldRegistryKey = registryKey;
        this.nbt = nbt;
        this.placed = placed;
        this.timer = timer;
    }

    public void setTimer(long delay) {
        this.timer = delay;
    }

    @Override
    public ResourceKey<Level> getWorldRegistryKey() {
        return this.worldRegistryKey;
    }

    @Override
    public ServerLevel getWorld(@NotNull MinecraftServer server) {
        return server.getLevel(this.getWorldRegistryKey());
    }

    @Override
    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    @Override
    public BlockState getBlockState() {
        return this.blockState;
    }

    @Nullable
    public CompoundTag getNbt() {
        return this.nbt;
    }

    @Override
    public final void setPlaced() {
        this.placed = true;
    }

    @Override
    public boolean isPlaced() {
        return this.placed;
    }

    @Override
    public long getBlockTimer() {
        return this.timer;
    }

    @Override
    public void tick(ExplosionEvent explosionEvent, MinecraftServer server) {
        this.timer--;
        if (this.timer >= 0) {
            return;
        }
        this.tryHealing(server, explosionEvent);
    }

    @Override
    public boolean canBePlaced(MinecraftServer server) {
        if (shouldForceHeal()) {
            return true;
        }
        return this.getBlockState().canSurvive(this.getWorld(server), this.getBlockPos());
    }

    @Override
    public SerializedAffectedBlock asSerialized() {
        return new DefaultSerializedAffectedBlock(this.getAffectedBlockType(), this.blockPos, this.blockState, this.nbt, null, null, null, this.worldRegistryKey, this.timer, this.placed);
    }

    protected String getAffectedBlockType() {
        return TYPE;
    }

    protected void tryHealing(MinecraftServer server, ExplosionEvent currentExplosionEvent) {

        this.setPlaced();
        BlockState state = this.getBlockState();
        BlockPos pos = this.getBlockPos();
        Level world = this.getWorld(server);
        boolean stateReplaced = false;

        // Check if the block we are about to try placing is in the replace-map.
        // If it is, switch the state for the corresponding one in the replace-map.
        String blockIdentifier = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
        String replaceMapValue = ConfigUtils.getReplaceMapEntry(blockIdentifier);
        if (replaceMapValue != null && !this.shouldForceHeal()) {
            state = BuiltInRegistries.BLOCK.getValue(Identifier.parse(replaceMapValue)).withPropertiesOf(state);
            stateReplaced = true;
        }


        if (!this.shouldHealBlock(world)) {
            return;
        }

        ExplosionUtils.pushEntitiesUpwards(world, pos, state, false);
        boolean makeFallingBlocksFall = ConfigUtils.getRawBooleanSetting(ConfigUtils.MAKE_FALLING_BLOCKS_FALL);
        if (state.getBlock() instanceof FallingBlock) {
            ExplosionUtils.FALLING_BLOCK_SCHEDULE_TICK.set(makeFallingBlocksFall);
        }
        world.setBlock(pos, state, Block.UPDATE_ALL);
        this.handleChestBlockIfNeeded(currentExplosionEvent, state, pos, server);
        boolean healNbt = this.nbt != null && !stateReplaced;
        if (healNbt) {
            world.setBlockEntity(BlockEntity.loadStatic(pos, state, this.nbt, world.registryAccess()));
        }
        ExplosionUtils.playBlockPlacementSoundEffect(world, pos, state);
        ExplosionUtils.spawnParticles(world, pos);
    }

    protected boolean shouldHealBlock(Level world) {
        if (shouldForceHeal()) {
            return true;
        }
        return world.getBlockState(this.blockPos).canBeReplaced();
    }

    protected boolean shouldForceHeal() {
        boolean forceBlocksWithNbtToAlwaysHeal = ConfigUtils.getRawBooleanSetting(ConfigUtils.FORCE_BLOCKS_WITH_NBT_TO_ALWAYS_HEAL);
        return this.nbt != null && forceBlocksWithNbtToAlwaysHeal;
    }

    private void handleChestBlockIfNeeded(ExplosionEvent explosionEvent, BlockState blockState, BlockPos chestPos, MinecraftServer server) {
        if (!blockState.is(Blocks.CHEST)) {
            return;
        }
        ChestType chestType = blockState.getValue(ChestBlock.TYPE);
        Direction facing = blockState.getValue(ChestBlock.FACING);
        BlockPos otherHalfPos = switch (chestType) {
            case SINGLE -> null;
            case LEFT -> switch (facing) {
                case NORTH -> chestPos.east();
                case EAST -> chestPos.south();
                case SOUTH -> chestPos.west();
                case WEST -> chestPos.north();
                default -> null;
            };
            case RIGHT -> switch (facing) {
                case NORTH -> chestPos.west();
                case EAST -> chestPos.north();
                case SOUTH -> chestPos.east();
                case WEST -> chestPos.south();
                default -> null;
            };
        };
        if (otherHalfPos == null) {
            return;
        }

        List<AffectedBlock> affectedBlocks = explosionEvent.getAffectedBlocks().toList();
        for (AffectedBlock affectedBlock : affectedBlocks) {
            if (!(affectedBlock instanceof SingleAffectedBlock singleAffectedBlock)) {
                continue;
            }
            if (singleAffectedBlock.isPlaced()) {
                continue;
            }
            BlockState affectedState = singleAffectedBlock.getBlockState();
            BlockPos affectedPosition = singleAffectedBlock.getBlockPos();
            if (!affectedState.is(Blocks.CHEST) || !affectedPosition.equals(otherHalfPos)) {
                continue;
            }
            singleAffectedBlock.tryHealing(server, explosionEvent);
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SingleAffectedBlock that)) return false;
        return Objects.equals(getBlockPos(), that.getBlockPos()) && Objects.equals(getBlockState(), that.getBlockState()) && Objects.equals(getWorldRegistryKey(), that.getWorldRegistryKey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBlockPos(), getBlockState(), getWorldRegistryKey());
    }

    @Override
    public String toString() {
        return "SingleAffectedBlock(pos=%s, state=%s, world=%s, nbt=%s, timer=%s, placed=%s)"
                .formatted(this.blockPos, this.blockState, this.worldRegistryKey, this.nbt != null ? this.nbt : "null", this.timer, this.placed);
    }

}

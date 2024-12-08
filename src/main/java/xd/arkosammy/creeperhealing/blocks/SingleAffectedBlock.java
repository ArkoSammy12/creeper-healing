package xd.arkosammy.creeperhealing.blocks;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xd.arkosammy.creeperhealing.config.ConfigSettings;
import xd.arkosammy.creeperhealing.config.ConfigUtils;
import xd.arkosammy.creeperhealing.config.SettingGroups;
import xd.arkosammy.creeperhealing.explosions.ExplosionEvent;
import xd.arkosammy.creeperhealing.util.ExplosionUtils;
import xd.arkosammy.monkeyconfig.groups.SettingGroup;
import xd.arkosammy.monkeyconfig.groups.maps.StringMapSettingGroup;
import xd.arkosammy.monkeyconfig.settings.BooleanSetting;

import java.util.List;
import java.util.Objects;

public class SingleAffectedBlock implements AffectedBlock {

    public static final String TYPE = "single_affected_block";
    private final BlockPos blockPos;
    private final BlockState blockState;
    private final RegistryKey<World> worldRegistryKey;

    @Nullable
    private final NbtCompound nbt;
    private long timer;
    private boolean placed;

    protected SingleAffectedBlock(BlockPos blockPos, BlockState blockState, RegistryKey<World> registryKey, @Nullable NbtCompound nbt, long timer, boolean placed) {
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
    public RegistryKey<World> getWorldRegistryKey() {
        return this.worldRegistryKey;
    }

    @Override
    public ServerWorld getWorld(@NotNull MinecraftServer server) {
        return server.getWorld(this.getWorldRegistryKey());
    }

    @Override
    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    @Override
    public BlockState getBlockState() {
        return this.blockState;
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
        this.setPlaced();
    }

    @Override
    public boolean canBePlaced(MinecraftServer server) {
        if (shouldForceHeal()) {
            return true;
        }
        return this.getBlockState().canPlaceAt(this.getWorld(server), this.getBlockPos());
    }

    protected String getAffectedBlockType() {
        return TYPE;
    }

    protected void tryHealing(MinecraftServer server, ExplosionEvent currentExplosionEvent) {

        BlockState state = this.getBlockState();
        BlockPos pos = this.getBlockPos();
        World world = this.getWorld(server);
        boolean stateReplaced = false;

        // Check if the block we are about to try placing is in the replace-map.
        // If it is, switch the state for the corresponding one in the replace-map.
        String blockIdentifier = Registries.BLOCK.getId(state.getBlock()).toString();
        SettingGroup settingGroup = ConfigUtils.getSettingGroup(SettingGroups.REPLACE_MAP.getName());
        if (settingGroup instanceof StringMapSettingGroup replaceMapGroup) {
            String replaceMapValue = replaceMapGroup.get(blockIdentifier);
            if (replaceMapValue != null && !this.shouldForceHeal()) {
                state = Registries.BLOCK.get(Identifier.of(replaceMapValue)).getStateWithProperties(state);
                stateReplaced = true;
            }
        }

        if (!this.shouldHealBlock(world, this.blockPos)) {
            return;
        }

        ExplosionUtils.pushEntitiesUpwards(world, pos, state, false);
        boolean makeFallingBlocksFall = ConfigUtils.getSettingValue(ConfigSettings.MAKE_FALLING_BLOCKS_FALL.getSettingLocation(), BooleanSetting.class);
        if (state.getBlock() instanceof FallingBlock) {
            ExplosionUtils.FALLING_BLOCK_SCHEDULE_TICK.set(makeFallingBlocksFall);
        }
        world.setBlockState(pos, state);
        this.handleChestBlockIfNeeded(currentExplosionEvent, state, pos, server);
        boolean healNbt = this.nbt != null && !stateReplaced;
        if (healNbt) {
            world.addBlockEntity(BlockEntity.createFromNbt(pos, state, this.nbt, world.getRegistryManager()));
        }
        ExplosionUtils.playBlockPlacementSoundEffect(world, pos, state);
        ExplosionUtils.spawnParticles(world, pos);
    }

    protected boolean shouldHealBlock(World world, BlockPos pos) {
        if (shouldForceHeal()) {
            return true;
        }
        return world.getBlockState(pos).isReplaceable();
    }

    protected boolean shouldForceHeal() {
        boolean forceBlocksWithNbtToAlwaysHeal = ConfigUtils.getSettingValue(ConfigSettings.FORCE_BLOCKS_WITH_NBT_TO_ALWAYS_HEAL.getSettingLocation(), BooleanSetting.class);
        return this.nbt != null && forceBlocksWithNbtToAlwaysHeal;
    }

    private void handleChestBlockIfNeeded(ExplosionEvent explosionEvent, BlockState blockState, BlockPos chestPos, MinecraftServer server) {
        if (!blockState.isOf(Blocks.CHEST)) {
            return;
        }
        ChestType chestType = blockState.get(ChestBlock.CHEST_TYPE);
        Direction facing = blockState.get(ChestBlock.FACING);
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
            if (!affectedState.isOf(Blocks.CHEST) || !affectedPosition.equals(otherHalfPos)) {
                continue;
            }
            singleAffectedBlock.tryHealing(server, explosionEvent);
            singleAffectedBlock.setPlaced();
        }

    }

    @Override
    public SerializedAffectedBlock asSerialized() {
        return new DefaultSerializedAffectedBlock(this.getAffectedBlockType(), this.blockPos, this.blockState, this.worldRegistryKey, this.nbt, this.timer, this.placed);
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

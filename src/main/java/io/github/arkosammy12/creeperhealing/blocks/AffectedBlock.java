package io.github.arkosammy12.creeperhealing.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import io.github.arkosammy12.creeperhealing.config.ConfigSettings;
import io.github.arkosammy12.creeperhealing.config.ConfigUtils;
import io.github.arkosammy12.creeperhealing.explosions.ExplosionEvent;
import xd.arkosammy.monkeyconfig.settings.BooleanSetting;

public interface AffectedBlock {

    BlockPos getBlockPos();

    BlockState getBlockState();

    RegistryKey<World> getWorldRegistryKey();

    ServerWorld getWorld(MinecraftServer server);

    long getBlockTimer();

    void tick(ExplosionEvent currentExplosion, MinecraftServer server);

    void setPlaced();

    boolean isPlaced();

    boolean canBePlaced(MinecraftServer server);

    SerializedAffectedBlock asSerialized();

    static AffectedBlock newInstance(BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, World world) {
        RegistryKey<World> worldRegistryKey = world.getRegistryKey();
        long blockPlacementDelay = ConfigUtils.getBlockPlacementDelay();
        if (state.contains(Properties.DOUBLE_BLOCK_HALF) || state.contains(Properties.BED_PART)) {
            return new DoubleAffectedBlock(pos, state, worldRegistryKey, blockPlacementDelay, false);
        }
        boolean restoreBlockNbt = ConfigUtils.getSettingValue(ConfigSettings.RESTORE_BLOCK_NBT.getSettingLocation(), BooleanSetting.class);
        if (blockEntity != null && restoreBlockNbt) {
            return new SingleAffectedBlock(pos, state, worldRegistryKey, blockEntity.createNbtWithId(world.getRegistryManager()), blockPlacementDelay, false);
        }
        return new SingleAffectedBlock(pos, state, worldRegistryKey, null, blockPlacementDelay, false);
    }

}

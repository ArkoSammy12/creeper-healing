package xd.arkosammy.creeperhealing.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
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

import java.util.Objects;
import java.util.Optional;

public non-sealed class SingleAffectedBlock implements AffectedBlock {

    public static final String TYPE = "single_affected_block";
    private final BlockPos blockPos;
    private final BlockState blockState;
    private final RegistryKey<World> worldRegistryKey;

    @Nullable
    private final NbtCompound nbt;
    private long timer;
    private boolean placed;

    protected SingleAffectedBlock(BlockPos blockPos, BlockState blockState, RegistryKey<World> registryKey, @Nullable NbtCompound nbt, long timer, boolean placed){
        this.blockPos = blockPos;
        this.blockState = blockState;
        this.worldRegistryKey = registryKey;
        this.nbt = nbt;
        this.placed = placed;
        this.timer = timer;
    }

    public void setTimer(long delay){
        this.timer = delay;
    }

    @Override
    public RegistryKey<World> getWorldRegistryKey(){
        return this.worldRegistryKey;
    }

    @Override
    public World getWorld(@NotNull MinecraftServer server){
        return server.getWorld(this.getWorldRegistryKey());
    }

    @Override
    public BlockPos getBlockPos(){
        return this.blockPos;
    }

    @Override
    public BlockState getBlockState(){
        return this.blockState;
    }

    @Override
    public final void setPlaced(){
        this.placed = true;
    }

    @Override
    public boolean isPlaced(){
        return this.placed;
    }

    @Override
    public void tick(ExplosionEvent explosionEvent, MinecraftServer server){
        this.timer--;
        if (this.timer >= 0) {
            return;
        }
        if(!explosionEvent.shouldKeepHealing(this.getWorld(server))){
            return;
        }
        this.tryHealing(server, explosionEvent);
        this.setPlaced();
    }

    @Override
    public boolean canBePlaced(MinecraftServer server){
        if(shouldForceHeal()) {
            return true;
        }
        return this.getBlockState().canPlaceAt(this.getWorld(server), this.getBlockPos());
    }

    String getAffectedBlockType(){
        return TYPE;
    }

    @Override
    public SerializedAffectedBlock asSerialized(){
        return new SerializedAffectedBlock(this.getAffectedBlockType(), this.blockPos, this.blockState, this.worldRegistryKey, Optional.ofNullable(this.nbt), this.timer, this.placed);
    }

    protected void tryHealing(MinecraftServer server, ExplosionEvent currentExplosionEvent){

        BlockState state = this.getBlockState();
        final BlockPos pos = this.getBlockPos();
        final World world = this.getWorld(server);
        boolean stateReplaced = false;

        //Check if the block we are about to try placing is in the replace-map.
        //If it is, switch the state for the corresponding one in the replace-map.
        final String blockIdentifier = Registries.BLOCK.getId(state.getBlock()).toString();
        final SettingGroup settingGroup = ConfigUtils.getSettingGroup(SettingGroups.REPLACE_MAP.getName());
        if (settingGroup instanceof StringMapSettingGroup replaceMapGroup) {
            final String replaceMapValue = replaceMapGroup.get(blockIdentifier);
            if (replaceMapValue != null && !this.shouldForceHeal()) {
                state = Registries.BLOCK.get(Identifier.of(replaceMapValue)).getStateWithProperties(state);
                stateReplaced = true;
            }
        }

        if(!this.shouldHealBlock(world, this.blockPos)){
            return;
        }
        if(state.isSolidBlock(world, pos)) {
            ExplosionUtils.pushEntitiesUpwards(world, pos, false);
        }
        boolean makeFallingBlocksFall = ConfigUtils.getSettingValue(ConfigSettings.MAKE_FALLING_BLOCKS_FALL.getSettingLocation(), BooleanSetting.class);
        if(state.getBlock() instanceof FallingBlock){
            ExplosionUtils.FALLING_BLOCK_SCHEDULE_TICK.set(makeFallingBlocksFall);
        }
        world.setBlockState(pos, state);
        boolean healNbt = this.nbt != null && !stateReplaced;
        if(healNbt) {
            world.addBlockEntity(BlockEntity.createFromNbt(pos, state, this.nbt, world.getRegistryManager()));
        }
        if(ExplosionUtils.shouldPlayBlockPlacementSound(world, state)) {
            world.playSound(null, pos, state.getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, state.getSoundGroup().getVolume(), state.getSoundGroup().getPitch());
        }

    }

    protected boolean shouldHealBlock(World world, BlockPos pos) {
        if(shouldForceHeal()) {
            return true;
        }
        return world.getBlockState(pos).isReplaceable();
    }

    protected boolean shouldForceHeal() {
        final boolean forceBlocksWithNbtToAlwaysHeal = ConfigUtils.getSettingValue(ConfigSettings.FORCE_BLOCKS_WITH_NBT_TO_ALWAYS_HEAL.getSettingLocation(), BooleanSetting.class);
        return this.nbt != null && forceBlocksWithNbtToAlwaysHeal;
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

}

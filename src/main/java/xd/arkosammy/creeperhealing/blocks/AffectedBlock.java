package xd.arkosammy.creeperhealing.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xd.arkosammy.creeperhealing.config.DelaysConfig;
import xd.arkosammy.creeperhealing.config.PreferencesConfig;
import xd.arkosammy.creeperhealing.config.ReplaceMapConfig;
import xd.arkosammy.creeperhealing.explosions.AbstractExplosionEvent;
import xd.arkosammy.creeperhealing.util.ExplosionUtils;
import xd.arkosammy.creeperhealing.util.SerializedAffectedBlock;

import java.util.Objects;
import java.util.Optional;

public class AffectedBlock {

    public static final String TYPE = "single_affected_block";
    private final BlockPos pos;
    private final BlockState state;
    private final RegistryKey<World> worldRegistryKey;

    @Nullable
    private final NbtCompound nbt;
    private long timer;
    private boolean placed;

    public AffectedBlock(BlockPos pos, BlockState state, RegistryKey<World> registryKey, @Nullable NbtCompound nbt, long timer, boolean placed){
        this.pos = pos;
        this.state = state;
        this.worldRegistryKey = registryKey;
        this.nbt = nbt;
        this.placed = placed;
        this.timer = timer;
    }

    public static AffectedBlock newAffectedBlock(BlockPos pos, BlockState state, World world){
        if(state.contains(Properties.DOUBLE_BLOCK_HALF) || state.contains(Properties.BED_PART)){
            return new DoubleAffectedBlock(pos, state, world.getRegistryKey(), DelaysConfig.getBlockPlacementDelayAsTicks(), false);
        } else {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if(blockEntity != null && PreferencesConfig.RESTORE_BLOCK_NBT.getEntry().getValue()){
                return new AffectedBlock(pos, state, world.getRegistryKey(), blockEntity.createNbtWithId(), DelaysConfig.getBlockPlacementDelayAsTicks(), false);
            } else {
                return new AffectedBlock(pos, state, world.getRegistryKey(), null, DelaysConfig.getBlockPlacementDelayAsTicks(), false);
            }
        }
    }

    public void setTimer(long delay){
        this.timer = delay;
    }

    public RegistryKey<World> getWorldRegistryKey(){
        return this.worldRegistryKey;
    }

    public World getWorld(@NotNull MinecraftServer server){
        return server.getWorld(this.getWorldRegistryKey());
    }

    public BlockPos getPos(){
        return this.pos;
    }

    public BlockState getState(){
        return this.state;
    }

    public void setPlaced(){
        this.placed = true;
    }

    public long getTimer(){
        return this.timer;
    }

    public boolean isPlaced(){
        return this.placed;
    }

    public void tickAffectedBlock(){
        this.timer--;
    }

    public boolean canBePlaced(MinecraftServer server){
        return this.getState().canPlaceAt(this.getWorld(server), this.getPos());
    }

    String getAffectedBlockType(){
        return TYPE;
    }

    public SerializedAffectedBlock toSerialized(){
        return new SerializedAffectedBlock(this.getAffectedBlockType(), this.pos, this.state, this.worldRegistryKey, Optional.ofNullable(this.nbt), this.timer, this.placed);
    }

    public void tryHealing(MinecraftServer server, AbstractExplosionEvent currentExplosionEvent){

        BlockState state = this.getState();
        BlockPos pos = this.getPos();
        World world = this.getWorld(server);
        boolean stateReplaced = false;

        //Check if the block we are about to try placing is in the replace-map.
        //If it is, switch the state for the corresponding one in the replace-map.
        String blockIdentifier = Registries.BLOCK.getId(state.getBlock()).toString();
        if(ReplaceMapConfig.getReplaceMap().containsKey(blockIdentifier)){
            state = Registries.BLOCK.get(new Identifier(ReplaceMapConfig.getReplaceMap().get(blockIdentifier))).getStateWithProperties(state);
            stateReplaced = true;
        }
        if(!this.shouldHealBlock(world, this.pos)){
            return;
        }
        if(state.isSolidBlock(world, pos)) {
            ExplosionUtils.pushEntitiesUpwards(world, pos, false);
        }
        if(state.getBlock() instanceof FallingBlock){
            ExplosionUtils.FALLING_BLOCK_SCHEDULE_TICK.set(PreferencesConfig.MAKE_FALLING_BLOCKS_FALL.getEntry().getValue());
        }
        world.setBlockState(pos, state);
        if(this.nbt != null && !stateReplaced) {
            world.addBlockEntity(BlockEntity.createFromNbt(pos, state, this.nbt));
        }
        if(ExplosionUtils.shouldPlayBlockPlacementSound(world, state)) {
            world.playSound(null, pos, state.getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, state.getSoundGroup().getVolume(), state.getSoundGroup().getPitch());
        }

    }

    boolean shouldHealBlock(World world, BlockPos pos) {
        return world.getBlockState(pos).isReplaceable();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AffectedBlock that)) return false;
        return Objects.equals(getPos(), that.getPos()) && Objects.equals(getState(), that.getState()) && Objects.equals(getWorldRegistryKey(), that.getWorldRegistryKey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPos(), getState(), getWorldRegistryKey());
    }

}

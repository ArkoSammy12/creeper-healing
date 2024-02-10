package xd.arkosammy.creeperhealing.explosions;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import xd.arkosammy.creeperhealing.configuration.DelaysConfig;
import xd.arkosammy.creeperhealing.configuration.PreferencesConfig;
import xd.arkosammy.creeperhealing.configuration.ReplaceMapConfig;

import java.util.Objects;

public class AffectedBlock {

    public static final String TYPE = "single_affected_block";
    private final BlockPos pos;
    private final BlockState state;
    private final RegistryKey<World> worldRegistryKey;
    private long affectedBlockTimer;
    private boolean placed;

    AffectedBlock(BlockPos pos, BlockState state, RegistryKey<World> registryKey, long affectedBlockTimer, boolean placed){
        this.pos = pos;
        this.state = state;
        this.worldRegistryKey = registryKey;
        this.placed = placed;
        this.affectedBlockTimer = affectedBlockTimer;
    }

    public static AffectedBlock newAffectedBlock(BlockPos pos, World world){
        BlockState state = world.getBlockState(pos);
        return state.contains(Properties.DOUBLE_BLOCK_HALF) || state.contains(Properties.BED_PART) ? new DoubleAffectedBlock(pos, state, world.getRegistryKey(), DelaysConfig.getBlockPlacementDelayAsTicks(), false) : new AffectedBlock(pos, state, world.getRegistryKey(), DelaysConfig.getBlockPlacementDelayAsTicks(), false);
    }

    public void setAffectedBlockTimer(long delay){
        this.affectedBlockTimer = delay;
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

    public long getAffectedBlockTimer(){
        return this.affectedBlockTimer;
    }

    public boolean isAlreadyPlaced(){
        return this.placed;
    }

    public void tickAffectedBlock(){
        this.affectedBlockTimer--;
    }

    public boolean canBePlaced(MinecraftServer server){
        return this.getState().canPlaceAt(this.getWorld(server), this.getPos());
    }

    String getAffectedBlockType(){
        return TYPE;
    }

    SerializedAffectedBlock toSerialized(){
        return new SerializedAffectedBlock(this.getAffectedBlockType(), this.pos, this.state, this.worldRegistryKey, this.affectedBlockTimer, this.placed);
    }

    void tryHealing(MinecraftServer server, AbstractExplosionEvent currentExplosionEvent){

        BlockState state = this.getState();
        BlockPos pos = this.getPos();
        World world = this.getWorld(server);

        //Check if the block we are about to try placing is in the replace-map.
        //If it is, switch the state for the corresponding one in the replace-map.
        String blockIdentifier = Registries.BLOCK.getId(state.getBlock()).toString();
        if(ReplaceMapConfig.getReplaceMap().containsKey(blockIdentifier)){
            state = Registries.BLOCK.get(new Identifier(ReplaceMapConfig.getReplaceMap().get(blockIdentifier))).getStateWithProperties(state);
        }

        if(this.shouldHealBlock(server)) {
            if(state.isSolidBlock(world, pos)) {
                ExplosionUtils.pushEntitiesUpwards(world, pos, false);
            }
            world.setBlockState(pos, state);
            if(ExplosionUtils.shouldPlaySoundOnBlockHeal(world, state)) {
                world.playSound(null, pos, state.getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, state.getSoundGroup().getVolume(), state.getSoundGroup().getPitch());
            }
        }

    }

    boolean shouldHealBlock(MinecraftServer server) {
        BlockState blockState = this.getWorld(server).getBlockState(this.pos);
        FluidState fluidState = blockState.getFluidState();

        if (ExplosionUtils.isStateAirOrFire(blockState)) {
            return true;
        } else if ((fluidState.getFluid().equals(Fluids.FLOWING_WATER) && PreferencesConfig.HEAL_ON_FLOWING_WATER.getEntry().getValue()) ||
                (fluidState.getFluid().equals(Fluids.WATER) && PreferencesConfig.HEAL_ON_SOURCE_WATER.getEntry().getValue())) {
            return true;
        } else return (fluidState.getFluid().equals(Fluids.FLOWING_LAVA) && PreferencesConfig.HEAL_ON_FLOWING_LAVA.getEntry().getValue()) ||
                (fluidState.getFluid().equals(Fluids.LAVA) && PreferencesConfig.HEAL_ON_SOURCE_LAVA.getEntry().getValue());
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

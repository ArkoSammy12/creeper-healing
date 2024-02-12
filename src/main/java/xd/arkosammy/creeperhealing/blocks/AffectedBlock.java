package xd.arkosammy.creeperhealing.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import xd.arkosammy.creeperhealing.config.DelaysConfig;
import xd.arkosammy.creeperhealing.config.ReplaceMapConfig;
import xd.arkosammy.creeperhealing.explosions.AbstractExplosionEvent;
import xd.arkosammy.creeperhealing.util.ExplosionUtils;
import xd.arkosammy.creeperhealing.util.SerializedAffectedBlock;

import java.util.Objects;

public class AffectedBlock {

    public static final String TYPE = "single_affected_block";
    private final BlockPos pos;
    private final BlockState state;
    private final RegistryKey<World> worldRegistryKey;
    private long timer;
    private boolean placed;

    public AffectedBlock(BlockPos pos, BlockState state, RegistryKey<World> registryKey, long timer, boolean placed){
        this.pos = pos;
        this.state = state;
        this.worldRegistryKey = registryKey;
        this.placed = placed;
        this.timer = timer;
    }

    public static AffectedBlock newAffectedBlock(BlockPos pos, BlockState state, World world){
        return state.contains(Properties.DOUBLE_BLOCK_HALF) || state.contains(Properties.BED_PART) ? new DoubleAffectedBlock(pos, state, world.getRegistryKey(), DelaysConfig.getBlockPlacementDelayAsTicks(), false) : new AffectedBlock(pos, state, world.getRegistryKey(), DelaysConfig.getBlockPlacementDelayAsTicks(), false);
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
        return new SerializedAffectedBlock(this.getAffectedBlockType(), this.pos, this.state, this.worldRegistryKey, this.timer, this.placed);
    }

    public void tryHealing(MinecraftServer server, AbstractExplosionEvent currentExplosionEvent){

        BlockState state = this.getState();
        BlockPos pos = this.getPos();
        World world = this.getWorld(server);

        //Check if the block we are about to try placing is in the replace-map.
        //If it is, switch the state for the corresponding one in the replace-map.
        String blockIdentifier = Registries.BLOCK.getId(state.getBlock()).toString();
        if(ReplaceMapConfig.getReplaceMap().containsKey(blockIdentifier)){
            state = Registries.BLOCK.get(new Identifier(ReplaceMapConfig.getReplaceMap().get(blockIdentifier))).getStateWithProperties(state);
        }

        if(this.shouldHealBlock(world, this.pos)) {
            if(state.isSolidBlock(world, pos)) {
                ExplosionUtils.pushEntitiesUpwards(world, pos, false);
            }
            world.setBlockState(pos, state);
            if(ExplosionUtils.shouldPlayBlockPlacementSound(world, state)) {
                world.playSound(null, pos, state.getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, state.getSoundGroup().getVolume(), state.getSoundGroup().getPitch());
            }
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

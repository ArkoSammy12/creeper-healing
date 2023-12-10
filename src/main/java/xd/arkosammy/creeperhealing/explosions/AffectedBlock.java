package xd.arkosammy.creeperhealing.explosions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import xd.arkosammy.creeperhealing.CreeperHealing;
import xd.arkosammy.creeperhealing.configuration.DelaysConfig;
import xd.arkosammy.creeperhealing.configuration.PreferencesConfig;
import xd.arkosammy.creeperhealing.configuration.ReplaceMapConfig;
import xd.arkosammy.creeperhealing.handlers.DoubleBlockHandler;
import xd.arkosammy.creeperhealing.handlers.ExplosionListHandler;

public class AffectedBlock {

    private final BlockPos pos;
    private final BlockState state;
    private final RegistryKey<World> worldRegistryKey;
    private long affectedBlockTimer;
    private boolean placed;

    // Codec to serialize and deserialize AffectedBlock instances.
    private static final Codec<AffectedBlock> AFFECTED_BLOCK_CODEC = RecordCodecBuilder.create(affectedBlockInstance -> affectedBlockInstance.group(
            BlockPos.CODEC.fieldOf("Block_Position").forGetter(AffectedBlock::getPos),
            BlockState.CODEC.fieldOf("Block_State").forGetter(AffectedBlock::getState),
            World.CODEC.fieldOf("World").forGetter(AffectedBlock::getWorldRegistryKey),
            Codec.LONG.fieldOf("Block_Timer").forGetter(AffectedBlock::getAffectedBlockTimer),
            Codec.BOOL.fieldOf("Placed").forGetter(AffectedBlock::isAlreadyPlaced)
    ).apply(affectedBlockInstance, AffectedBlock::new));

    private AffectedBlock(BlockPos pos, BlockState state, RegistryKey<World> registryKey, long affectedBlockTimer, boolean placed){
        this.pos = pos;
        this.state = state;
        this.worldRegistryKey = registryKey;
        setPlaced(placed);
        setAffectedBlockTimer(affectedBlockTimer);
    }

    public static AffectedBlock newAffectedBlock(BlockPos pos, World world){
        return new AffectedBlock(pos, world.getBlockState(pos), world.getRegistryKey(), DelaysConfig.getBlockPlacementDelayAsTicks(), false);
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

    public void setPlaced(boolean placed){
        this.placed = placed;
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

    static Codec<AffectedBlock> getCodec(){
        return AFFECTED_BLOCK_CODEC;
    }

    public void tryHealing(MinecraftServer server, ExplosionEvent currentExplosionEvent){

        BlockState state = this.getState();
        BlockPos pos = this.getPos();
        World world = this.getWorld(server);

        //Check if the block we are about to try placing is in the replace-map.
        //If it is, switch the state for the corresponding one in the replace-map.
        String blockIdentifier = Registries.BLOCK.getId(state.getBlock()).toString();
        if(ReplaceMapConfig.getReplaceMap().containsKey(blockIdentifier)){
            state = Registries.BLOCK.get(new Identifier(ReplaceMapConfig.getReplaceMap().get(blockIdentifier))).getStateWithProperties(state);
        }

        //If the block we are about to place consists of two blocks, handle it separately
        if(DoubleBlockHandler.isDoubleBlock(state)){
            DoubleBlockHandler.handleDoubleBlock(world, state, pos, currentExplosionEvent);
            return;
        }

        if(this.shouldHealBlock(server)) {

            if(state.isSolidBlock(world, pos))
                ExplosionUtils.pushEntitiesUpwards(world, pos, false);

            world.setBlockState(pos, state);

            if(ExplosionUtils.shouldPlaySoundOnBlockHeal(world, state))
                world.playSound(null, pos, state.getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, state.getSoundGroup().getVolume(), state.getSoundGroup().getPitch());

        }

    }

    private boolean shouldHealBlock(MinecraftServer server) {
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

    //Called whenever the config is reloaded and when the server/world starts
    public static void updateAffectedBlocksTimers(){
        CreeperHealing.setHealerHandlerLock(false);
        for(ExplosionEvent explosionEvent : ExplosionListHandler.getExplosionEventList()){
            if(explosionEvent.getExplosionMode() == ExplosionHealingMode.DEFAULT_MODE) {
                for (int i = explosionEvent.getAffectedBlockCounter() + 1; i < explosionEvent.getAffectedBlocksList().size(); i++) {
                    explosionEvent.getAffectedBlocksList().get(i).setAffectedBlockTimer(DelaysConfig.getBlockPlacementDelayAsTicks());
                }
            }
        }
        CreeperHealing.setHealerHandlerLock(true);
    }

}

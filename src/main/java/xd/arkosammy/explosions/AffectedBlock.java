package xd.arkosammy.explosions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import xd.arkosammy.CreeperHealing;
import xd.arkosammy.handlers.DoubleBlockHandler;
import xd.arkosammy.handlers.ExplosionListHandler;
import static xd.arkosammy.CreeperHealing.CONFIG;


public class AffectedBlock {

    private final BlockPos pos;
    private final BlockState state;
    private final RegistryKey<World> worldRegistryKey;
    private long affectedBlockTimer;
    private boolean placed;

    // Codec to serialize and deserialize AffectedBlock instances.
    private static final Codec<AffectedBlock> AFFECTED_BLOCK_CODEC = RecordCodecBuilder.create(blockInfoInstance -> blockInfoInstance.group(
            BlockPos.CODEC.fieldOf("Block_Position").forGetter(AffectedBlock::getPos),
            BlockState.CODEC.fieldOf("Block_State").forGetter(AffectedBlock::getState),
            World.CODEC.fieldOf("World").forGetter(AffectedBlock::getWorldRegistryKey),
            Codec.LONG.fieldOf("Block_Timer").forGetter(AffectedBlock::getAffectedBlockTimer),
            Codec.BOOL.fieldOf("Placed").forGetter(AffectedBlock::isAlreadyPlaced)
    ).apply(blockInfoInstance, AffectedBlock::new));


    private AffectedBlock(BlockPos pos, BlockState state, RegistryKey<World> registryKey, long affectedBlockTimer, boolean placed){
        this.pos = pos;
        this.state = state;
        this.worldRegistryKey = registryKey;
        setPlaced(placed);
        setAffectedBlockTimer(affectedBlockTimer);
    }

    /**
     * Creates a new AffectedBlock instance based on the given position and world.
     *
     * @param pos   The position of the affected block.
     * @param world The world where the block exists.
     * @return A new AffectedBlock instance.
     */
    public static AffectedBlock newAffectedBlock(BlockPos pos, World world){
        return new AffectedBlock(pos, world.getBlockState(pos), world.getRegistryKey(), CONFIG.getBlockPlacementDelay(), false);
    }

    void setAffectedBlockTimer(long delay){
        this.affectedBlockTimer = delay;
    }

    public RegistryKey<World> getWorldRegistryKey(){
        return this.worldRegistryKey;
    }

    World getWorld(@NotNull MinecraftServer server){
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


    /**
     * Gets the Codec for serializing and deserializing AffectedBlock instances.
     *
     * @return The AffectedBlock Codec.
     */
    static Codec<AffectedBlock> getCodec(){
        return AFFECTED_BLOCK_CODEC;
    }


    /**
     * Attempts to place the affected block in the world.
     *
     * @param server             The Minecraft server instance.
     * @param currentExplosionEvent The current explosion event associated with the block placement.
     */
    public void tryPlacing(MinecraftServer server, ExplosionEvent currentExplosionEvent){

        BlockState state = this.getState();
        BlockPos pos = this.getPos();
        World world = this.getWorld(server);

        //Check if the block we are about to try placing is in the replace-list.
        //If it is, switch the state for the corresponding one in the replace-list.
        String blockIdentifier = Registries.BLOCK.getId(state.getBlock()).toString();

        if(CONFIG.getReplaceList().containsKey(blockIdentifier)){

            state = Registries.BLOCK.get(new Identifier(CONFIG.getReplaceList().get(blockIdentifier))).getStateWithProperties(state);

        }

        //If the block we are about to try placing is "special", handle it separately
        if(DoubleBlockHandler.isDoubleBlock(state)){

            DoubleBlockHandler.handleDoubleBlock(world, state, pos, currentExplosionEvent);

            return;

        }


        if(ExplosionUtils.shouldPlaceBlock(world, pos)) {

            if(state.isSolidBlock(world, pos))
                ExplosionUtils.pushPlayersUpwards(world, pos, false);

            world.setBlockState(pos, state);

            //The first argument being null tells the server to play the sound to all nearby players
            if(ExplosionUtils.shouldPlaySound(world, state))
                world.playSound(null, pos, state.getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, state.getSoundGroup().getVolume(), state.getSoundGroup().getPitch());


        }

    }


    /**
     * Updates the timers for affected blocks in explosion events.
     */
    public static void updateAffectedBlocksTimers(){
        CreeperHealing.setHealerHandlerLock(false);
        for(ExplosionEvent explosionEvent : ExplosionListHandler.getExplosionEventList()){
            if(!explosionEvent.isMarkedWithDayTimeHealingMode()) {
                for (int i = explosionEvent.getAffectedBlockCounter() + 1; i < explosionEvent.getAffectedBlocksList().size(); i++) {
                    explosionEvent.getAffectedBlocksList().get(i).setAffectedBlockTimer(CONFIG.getBlockPlacementDelay());
                }
            }
        }
        CreeperHealing.setHealerHandlerLock(true);
    }

}

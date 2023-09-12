package xd.arkosammy.events;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import xd.arkosammy.CreeperHealing;
import xd.arkosammy.handlers.DoubleBlockHandler;
import xd.arkosammy.handlers.ExplosionHealerHandler;

import static xd.arkosammy.CreeperHealing.CONFIG;
import static xd.arkosammy.handlers.ExplosionHealerHandler.shouldPlaceBlock;
import static xd.arkosammy.handlers.ExplosionHealerHandler.shouldPlaySound;

public class AffectedBlock {

    private final BlockPos pos;
    private final BlockState state;
    private final RegistryKey<World> worldRegistryKey;
    private long affectedBlockTimer;
    private boolean placed;

    //Create a CODEC for our AffectedBlock.
    //Each AffectedBlock CODEC will contain a field for BlockState, BlockPos,
    //and the World Registry Key, from which we will obtain the World instance.
    public static final Codec<AffectedBlock> CODEC = RecordCodecBuilder.create(blockInfoInstance -> blockInfoInstance.group(

            BlockPos.CODEC.fieldOf("Block_Position").forGetter(AffectedBlock::getPos),
            BlockState.CODEC.fieldOf("Block_State").forGetter(AffectedBlock::getState),
            World.CODEC.fieldOf("World").forGetter(AffectedBlock::getWorldRegistryKey),
            Codec.LONG.fieldOf("Block_Timer").forGetter(AffectedBlock::getAffectedBlockTimer),
            Codec.BOOL.fieldOf("Placed").forGetter(AffectedBlock::isAlreadyPlaced)

    ).apply(blockInfoInstance, AffectedBlock::new));

    public AffectedBlock(BlockPos pos, BlockState state, RegistryKey<World> registryKey, long affectedBlockTimer, boolean placed){

        this.pos = pos;
        this.state = state;
        this.worldRegistryKey = registryKey;
        setPlaced(placed);
        setAffectedBlockTimer(affectedBlockTimer);

    }

    public void setAffectedBlockTimer(long delay){
        this.affectedBlockTimer = delay;
    }

     RegistryKey<World> getWorldRegistryKey(){
        return this.worldRegistryKey;
    }

    //Get the World instance from the stored World Registry Key
     World getWorld(@NotNull MinecraftServer server){
        return server.getWorld(this.getWorldRegistryKey());
    }

     BlockPos getPos(){
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

    public static void updateAffectedBlocksTimers(){
        CreeperHealing.setHealerHandlerLock(false);
        for(CreeperExplosionEvent creeperExplosionEvent : ExplosionHealerHandler.getExplosionEventList()){
            if(!creeperExplosionEvent.isMarkedWithDayTimeHealingMode()) {
                for (int i = creeperExplosionEvent.getAffectedBlockCounter() + 1; i < creeperExplosionEvent.getAffectedBlocksList().size(); i++) {
                    creeperExplosionEvent.getAffectedBlocksList().get(i).setAffectedBlockTimer(CONFIG.getBlockPlacementDelay());
                }
            }
        }
        CreeperHealing.setHealerHandlerLock(true);
    }

    public void tryPlacing(MinecraftServer server, CreeperExplosionEvent currentCreeperExplosionEvent){

        BlockState state = this.getState();
        BlockPos pos = this.getPos();
        World world = this.getWorld(server);


        //Check if the block we are about to try placing is in the replace-list.
        //If it is, switch the state for the corresponding one in the replace-list.
        String blockString = Registries.BLOCK.getId(state.getBlock()).toString();

        if(CONFIG.getReplaceList().containsKey(blockString)){

            state = Registries.BLOCK.get(new Identifier(CONFIG.getReplaceList().get(blockString))).getStateWithProperties(state);

        }

        //If the block we are about to try placing is "special", handle it separately
        if(!DoubleBlockHandler.isDoubleBlock(world, state, pos, currentCreeperExplosionEvent)) {

            if(shouldPlaceBlock(world, pos)) {

                //TODO: Find a way to not move players exclusively upwards and linearly
                if(state.isSolidBlock(world, pos))handlePlayersOnBlockHeal(world, pos);

                world.setBlockState(pos, state);

                //if(state.isSolidBlock(world, pos))handlePlayersOnBlockHeal(world, pos);

                //The first argument being null tells the server to play the sound to all nearby players
                if(shouldPlaySound(world, state)) world.playSound(null, pos, state.getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, state.getSoundGroup().getVolume(), state.getSoundGroup().getPitch());


            }

        }

    }

    private static void handlePlayersOnBlockHeal(World world, BlockPos pos) {

        for(Entity entity : world.getEntitiesByClass(LivingEntity.class, new Box(pos), Entity::isAlive)){

            if(isAboveBlockFree(world, pos, entity)){

                //if(entity.isInsideWall())
                entity.refreshPositionAfterTeleport(entity.getPos().withAxis(Direction.Axis.Y, entity.getBlockY() + 1));

                CreeperHealing.LOGGER.info("Teleported entity");

            } else {

                CreeperHealing.LOGGER.info("Found an obstruction. Can't teleport");

            }

        }

    }

    private static boolean isAboveBlockFree(World world, BlockPos pos, Entity entity){

        CreeperHealing.LOGGER.info("Height of " + entity.getName().toString() + "is: " + (int) Math.ceil(entity.getStandingEyeHeight()));

        for(int i = pos.getY(); i < pos.offset(Direction.Axis.Y, (int) Math.ceil(entity.getStandingEyeHeight())).getY(); i++){

            BlockPos currentPos = pos.withY(i + 1);

            CreeperHealing.LOGGER.info("Checking coordinate: " + currentPos.toString());

            if(world.getBlockState(currentPos).isSolidBlock(world, currentPos)) return false;


        }

        return true;

    }

}

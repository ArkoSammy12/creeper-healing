package xd.arkosammy.creeperhealing.explosions;

import net.minecraft.block.BlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xd.arkosammy.creeperhealing.blocks.AffectedBlock;
import xd.arkosammy.creeperhealing.config.ConfigUtils;

import java.util.*;
import java.util.stream.Stream;

public abstract class AbstractExplosionEvent implements ExplosionEvent {

    private final List<AffectedBlock> affectedBlocks;
    private long healTimer;
    private int blockCounter;
    protected boolean finished;

    protected AbstractExplosionEvent(List<AffectedBlock> affectedBlocks, long healTimer, int blockCounter){
        this.affectedBlocks = affectedBlocks;
        this.healTimer = healTimer;
        this.blockCounter = blockCounter;
    }

    protected AbstractExplosionEvent(List<AffectedBlock> affectedBlocks) {
        this.affectedBlocks = affectedBlocks;
        this.healTimer = ConfigUtils.getExplosionHealDelay();
        this.blockCounter = 0;
    }

    public final Stream<AffectedBlock> getAffectedBlocks(){
        return this.affectedBlocks.stream();
    }

    @Override
    public World getWorld(MinecraftServer server) {
        return server.getWorld(this.affectedBlocks.getFirst().getWorldRegistryKey());
    }

    @Override
    public final long getHealTimer(){
        return this.healTimer;
    }

    @Override
    public final int getBlockCounter(){
        return this.blockCounter;
    }

    abstract ExplosionHealingMode getHealingMode();

    public final void setHealTimer(long healTimer){
        this.healTimer = healTimer;
    }

    protected final void incrementCounter(){
        this.blockCounter++;
    }

    @Override
    public final void tick(MinecraftServer server){
        if (this.finished) {
            return;
        }
        this.healTimer--;
        if (healTimer >= 0) {
            return;
        }
        final Optional<AffectedBlock> optionalAffectedBlock = this.getCurrentAffectedBlock();
        if(optionalAffectedBlock.isEmpty()){
            this.finished = true;
            return;
        }
        final AffectedBlock affectedBlock = optionalAffectedBlock.get();
        if(affectedBlock.isPlaced()){
            this.incrementCounter();
            return;
        }
        if(!affectedBlock.canBePlaced(server)){
            this.delayAffectedBlock(affectedBlock, server);
            return;
        }
        affectedBlock.tick(this, server);
        this.incrementCounter();
    }

    @Override
    public final SerializedExplosionEvent asSerialized(){
        return new SerializedExplosionEvent(this.getHealingMode().getName(), this.affectedBlocks.stream().map(AffectedBlock::asSerialized).toList(), this.healTimer, this.blockCounter);
    }

    @Override
    public final Optional<AffectedBlock> getCurrentAffectedBlock(){
        return this.blockCounter < this.affectedBlocks.size() ? Optional.of(this.affectedBlocks.get(this.blockCounter)) : Optional.empty();
    }

    @Override
    public boolean shouldKeepHealing(World world) {
        return !this.finished;
    }

    @Override
    public void setup(World world) {
    }

    public final void findAndMarkPlaced(BlockPos blockPos, BlockState blockState, World world){
        for(AffectedBlock affectedBlock : this.affectedBlocks) {
            if(affectedBlock.getBlockState().equals(blockState) && affectedBlock.getBlockPos().equals(blockPos) && affectedBlock.getWorldRegistryKey().equals(world.getRegistryKey())) {
                affectedBlock.setPlaced();
            }
        }
    }

     // If the current affected block cannot be placed at this moment, find the next block that is placeable in the list and swap them in the list.
     // This effectively gives the delayed block more chances to be placed until no more placeable blocks are found
     // Examples include wall torches, vines, lanterns, candles, etc.
     private void delayAffectedBlock(AffectedBlock affectedBlockToDelay, MinecraftServer server){
         final int indexOfDelayedBlock = this.affectedBlocks.indexOf(affectedBlockToDelay);
         if(indexOfDelayedBlock < 0){
             this.incrementCounter();
             affectedBlockToDelay.setPlaced();
             return;
         }
         final int indexOfNextPlaceable = this.findNextPlaceableBlockIndex(server);
         if(indexOfNextPlaceable >= 0){
             Collections.swap(this.affectedBlocks, indexOfDelayedBlock, indexOfNextPlaceable);
         } else {
             this.incrementCounter();
             affectedBlockToDelay.setPlaced();
         }
     }

     private int findNextPlaceableBlockIndex(MinecraftServer server){
        for(int i = this.blockCounter; i < this.affectedBlocks.size(); i++){
            if(this.affectedBlocks.get(i).canBePlaced(server)){
                return i;
            }
        }
         return -1;
     }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractExplosionEvent that)) return false;
        return Objects.equals(getAffectedBlocks(), that.getAffectedBlocks());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAffectedBlocks());
    }

}

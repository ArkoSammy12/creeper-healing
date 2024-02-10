package xd.arkosammy.creeperhealing.explosions;

import net.minecraft.block.BlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xd.arkosammy.creeperhealing.configuration.DelaysConfig;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public abstract class AbstractExplosionEvent {

    private final List<AffectedBlock> affectedBlocks;
    private long healTimer;
    private int blockCounter;

    AbstractExplosionEvent(List<AffectedBlock> affectedBlocks, long healTimer, int blockCounter){
        this.affectedBlocks = affectedBlocks;
        this.healTimer = healTimer;
        this.blockCounter = blockCounter;
    }

    AbstractExplosionEvent(List<AffectedBlock> affectedBlocks){
        this.affectedBlocks = affectedBlocks;
        this.healTimer = DelaysConfig.getExplosionHealDelayAsTicks();
        this.blockCounter = 0;
    }

    final public void setHealTimer(long healTimer){
        this.healTimer = healTimer;
    }

    final void incrementCounter(){
        this.blockCounter++;
    }

    final public List<AffectedBlock> getAffectedBlocks(){
        return this.affectedBlocks;
    }

    final long getHealTimer(){
        return this.healTimer;
    }

    final int getBlockCounter(){
        return this.blockCounter;
     }

     abstract ExplosionHealingMode getHealingMode();

     final public void tick(){
        this.healTimer--;
     }

    abstract void setupExplosion(World world);

     final SerializedExplosionEvent toSerialized(){
        return new SerializedExplosionEvent(this.getHealingMode().getName(), this.affectedBlocks.stream().map(AffectedBlock::toSerialized).toList(), this.healTimer, this.blockCounter);
     }

     final public Optional<AffectedBlock> getCurrentAffectedBlock(){
        return this.blockCounter < this.affectedBlocks.size() ? Optional.of(this.affectedBlocks.get(this.blockCounter)) : Optional.empty();
     }

     final void delayAffectedBlock(AffectedBlock affectedBlockToDelay, MinecraftServer server){
        int indexOfDelayedBlock = this.affectedBlocks.indexOf(affectedBlockToDelay);
        if(indexOfDelayedBlock != -1){
            Optional<Integer> indexOfNextPlaceableOptional = this.findNextPlaceableBlock(server);
            indexOfNextPlaceableOptional.ifPresentOrElse(indexOfNextPlaceable -> Collections.swap(this.affectedBlocks, indexOfDelayedBlock, indexOfNextPlaceable), () -> {
                this.incrementCounter();
                affectedBlockToDelay.setPlaced();
            });
        } else {
            this.incrementCounter();
            affectedBlockToDelay.setPlaced();
        }
     }

     private Optional<Integer> findNextPlaceableBlock(MinecraftServer server){
        for(int i = this.blockCounter; i < this.affectedBlocks.size(); i++){
            if(this.affectedBlocks.get(i).canBePlaced(server)){
                return Optional.of(i);
            }
        }
         return Optional.empty();
     }

     abstract boolean shouldKeepHealing(World world);

    final void markAffectedBlockAsPlaced(BlockState secondHalfState, BlockPos secondHalfPos, World world){
        for(AffectedBlock affectedBlock : this.getAffectedBlocks()) {
            if(affectedBlock.getState().equals(secondHalfState) && affectedBlock.getPos().equals(secondHalfPos) && affectedBlock.getWorldRegistryKey().equals(world.getRegistryKey())) {
                affectedBlock.setPlaced();
            }
        }
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

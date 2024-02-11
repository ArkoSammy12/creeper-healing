package xd.arkosammy.creeperhealing.explosions;

import net.minecraft.block.BlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xd.arkosammy.creeperhealing.blocks.AffectedBlock;
import xd.arkosammy.creeperhealing.config.DelaysConfig;
import xd.arkosammy.creeperhealing.config.ModeConfig;
import xd.arkosammy.creeperhealing.util.SerializedExplosionEvent;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public abstract class AbstractExplosionEvent {

    private final List<AffectedBlock> affectedBlocks;
    private long healTimer;
    private int blockCounter;

    protected AbstractExplosionEvent(List<AffectedBlock> affectedBlocks, long healTimer, int blockCounter){
        this.affectedBlocks = affectedBlocks;
        this.healTimer = healTimer;
        this.blockCounter = blockCounter;
    }

    protected AbstractExplosionEvent(List<AffectedBlock> affectedBlocks){
        this.affectedBlocks = affectedBlocks;
        this.healTimer = DelaysConfig.getExplosionHealDelayAsTicks();
        this.blockCounter = 0;
    }

    public static AbstractExplosionEvent newExplosionEvent(List<AffectedBlock> affectedBlocks, World world){
        ExplosionHealingMode explosionHealingMode = ExplosionHealingMode.getFromName(ModeConfig.MODE.getEntry().getValue());
        AbstractExplosionEvent explosionEvent = switch (explosionHealingMode) {
            case DAYTIME_HEALING_MODE -> new DaytimeExplosionEvent(affectedBlocks);
            case DIFFICULTY_BASED_HEALING_MODE -> new DifficultyBasedExplosionEvent(affectedBlocks);
            case BLAST_RESISTANCE_BASED_HEALING_MODE -> new BlastResistanceBasedExplosionEvent(affectedBlocks);
            default -> new DefaultExplosionEvent(affectedBlocks);
        };
        explosionEvent.setupExplosion(world);
        return explosionEvent;
    }

    public final void setHealTimer(long healTimer){
        this.healTimer = healTimer;
    }

    public final void incrementCounter(){
        this.blockCounter++;
    }

    public final List<AffectedBlock> getAffectedBlocks(){
        return this.affectedBlocks;
    }

    public final long getHealTimer(){
        return this.healTimer;
    }

    public final int getBlockCounter(){
        return this.blockCounter;
     }

    abstract ExplosionHealingMode getHealingMode();

    public final void tick(){
        this.healTimer--;
     }

    public abstract void setupExplosion(World world);

    public final SerializedExplosionEvent toSerialized(){
        return new SerializedExplosionEvent(this.getHealingMode().getName(), this.affectedBlocks.stream().map(AffectedBlock::toSerialized).toList(), this.healTimer, this.blockCounter);
    }

     public final Optional<AffectedBlock> getCurrentAffectedBlock(){
        return this.blockCounter < this.affectedBlocks.size() ? Optional.of(this.affectedBlocks.get(this.blockCounter)) : Optional.empty();
     }

     public final void delayAffectedBlock(AffectedBlock affectedBlockToDelay, MinecraftServer server){
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

     public abstract boolean shouldKeepHealing(World world);

    public final void markAffectedBlockAsPlaced(BlockState secondHalfState, BlockPos secondHalfPos, World world){
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

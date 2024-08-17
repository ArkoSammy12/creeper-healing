package xd.arkosammy.creeperhealing.explosions;

import net.minecraft.block.BlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xd.arkosammy.creeperhealing.blocks.AffectedBlock;
import xd.arkosammy.creeperhealing.config.ConfigUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public abstract class AbstractExplosionEvent implements ExplosionEvent {

    private final List<AffectedBlock> affectedBlocks;
    protected long healTimer;
    private int blockCounter;
    protected boolean finished;
    private final int radius;
    private final BlockPos center;

    public AbstractExplosionEvent(List<AffectedBlock> affectedBlocks, int radius, BlockPos center) {
        this(affectedBlocks, ConfigUtils.getExplosionHealDelay(), 0, radius, center);
    }

    public AbstractExplosionEvent(List<AffectedBlock> affectedBlocks, long healTimer, int blockCounter, int radius, BlockPos center) {
        this.affectedBlocks = affectedBlocks;
        this.healTimer = healTimer;
        this.blockCounter = blockCounter;
        this.center = center;
        this.radius = radius;
    }

    @Override
    public Stream<AffectedBlock> getAffectedBlocks() {
        return this.affectedBlocks.stream();
    }

    @Override
    public World getWorld(MinecraftServer server) {
        return server.getWorld(this.affectedBlocks.getFirst().getWorldRegistryKey());
    }

    @Override
    public long getHealTimer() {
        return this.healTimer;
    }

    public int getBlockCounter() {
        return this.blockCounter;
    }

    public BlockPos getCenter() {
        return this.center;
    }

    public int getRadius() {
        return this.radius;
    }

    @Override
    public boolean isFinished() {
        return this.finished;
    }

    protected final Optional<AffectedBlock> getCurrentAffectedBlock() {
        return this.blockCounter < this.affectedBlocks.size() ? Optional.of(this.affectedBlocks.get(this.blockCounter)) : Optional.empty();
    }

    protected final void incrementCounter() {
        this.blockCounter++;
    }

    public final void setHealTimer(long timer) {
        this.healTimer = timer;
    }

    protected void updateFinishedStatus(World world) {
    }

    abstract protected ExplosionHealingMode getHealingMode();

    @Override
    public final void tick(MinecraftServer server) {
        if (this.isFinished()) {
            return;
        }
        this.healTimer--;
        if (healTimer >= 0) {
            return;
        }
        Optional<AffectedBlock> optionalAffectedBlock = this.getCurrentAffectedBlock();
        if (optionalAffectedBlock.isEmpty()) {
            this.finished = true;
            return;
        }
        AffectedBlock currentAffectedBlock = optionalAffectedBlock.get();
        if (currentAffectedBlock.isPlaced()) {
            this.incrementCounter();
            return;
        }
        if (!currentAffectedBlock.canBePlaced(server)) {
            this.delayAffectedBlock(currentAffectedBlock, server);
            return;
        }
        this.updateFinishedStatus(this.getWorld(server));
        if (this.isFinished()) {
            return;
        }
        currentAffectedBlock.tick(this, server);
        if (currentAffectedBlock.getBlockTimer() < 0) {
            this.incrementCounter();
        }
    }

    // If the current affected block cannot be placed at this moment, find the next block that is placeable in the list and swap them in the list.
    // This effectively gives the delayed block more chances to be placed until no more placeable blocks are found
    // Examples include wall torches, vines, lanterns, candles, etc.
    private void delayAffectedBlock(AffectedBlock affectedBlockToDelay, MinecraftServer server) {
        int indexOfDelayedBlock = this.affectedBlocks.indexOf(affectedBlockToDelay);
        if (indexOfDelayedBlock < 0) {
            this.incrementCounter();
            affectedBlockToDelay.setPlaced();
            return;
        }
        int indexOfNextPlaceable = this.findNextPlaceableBlockIndex(server);
        if (indexOfNextPlaceable >= 0) {
            Collections.swap(this.affectedBlocks, indexOfDelayedBlock, indexOfNextPlaceable);
        } else {
            this.incrementCounter();
            affectedBlockToDelay.setPlaced();
        }
    }

    private int findNextPlaceableBlockIndex(MinecraftServer server) {
        for (int i = this.blockCounter; i < this.affectedBlocks.size(); i++) {
            if (this.affectedBlocks.get(i).canBePlaced(server)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public SerializedExplosionEvent asSerialized() {
        return new DefaultSerializedExplosion(this.getHealingMode().asString(), this.getAffectedBlocks().map(AffectedBlock::asSerialized).toList(), this.healTimer, this.blockCounter, this.radius, this.center);
    }

    public final void findAndMarkPlaced(BlockPos blockPos, BlockState blockState, World world) {
        for (AffectedBlock affectedBlock : this.affectedBlocks) {
            if (affectedBlock.getBlockState().equals(blockState) && affectedBlock.getBlockPos().equals(blockPos) && affectedBlock.getWorldRegistryKey().equals(world.getRegistryKey())) {
                affectedBlock.setPlaced();
            }
        }
    }

}

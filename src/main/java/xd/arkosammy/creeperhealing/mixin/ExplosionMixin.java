package xd.arkosammy.creeperhealing.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xd.arkosammy.creeperhealing.util.ExplosionUtils;
import xd.arkosammy.creeperhealing.explosions.ducks.ExplosionAccessor;
import xd.arkosammy.creeperhealing.util.callbacks.ExplosionCallbacks;

import java.util.*;

@Mixin(Explosion.class)
public abstract class ExplosionMixin implements ExplosionAccessor {

    @Shadow @Final private World world;

    @Shadow @Final private DamageSource damageSource;

    @Shadow public abstract @Nullable Entity getEntity();

    @Shadow public abstract List<BlockPos> getAffectedBlocks();

    @Unique
    private final Map<BlockPos, Pair<BlockState, BlockEntity>> affectedStatesAndBlockEntities = new HashMap<>();

    @Unique
    private final Set<BlockPos> indirectlyExplodedPositions = new HashSet<>();

    @Override
    public World creeperhealing$getWorld(){
        return this.world;
    }

    @Override
    public DamageSource creeperhealing$getDamageSource() {
        return this.damageSource;
    }

    @Override
    public boolean creeperhealing$willBeHealed(){
        return ExplosionUtils.getShouldHealPredicate().test(((Explosion) (Object) this));
    }

    @Override
    public Set<BlockPos> creeperhealing$getIndirectlyExplodedPositions() {
        return this.indirectlyExplodedPositions;
    }

    @Override
    public Map<BlockPos, Pair<BlockState, BlockEntity>> creeperhealing$getAffectedStatesAndBlockEntities() {
        return this.affectedStatesAndBlockEntities;
    }

    @Inject(method = "collectBlocksAndDamageEntities", at = @At("RETURN"))
    private void saveAffectedStates(CallbackInfo ci){
        this.checkForAffectedNeighborPositions();
        // Cache the block states and block entites before the explosion takes effect for use later
        this.getAffectedBlocks().forEach(pos -> this.affectedStatesAndBlockEntities.put(pos, new Pair<>(this.world.getBlockState(pos), this.world.getBlockEntity(pos))));
        this.indirectlyExplodedPositions.forEach(pos -> this.affectedStatesAndBlockEntities.put(pos, new Pair<>(this.world.getBlockState(pos), this.world.getBlockEntity(pos))));
        ExplosionCallbacks.BEFORE_EXPLOSION.invoker().beforeExplosion(((Explosion) (Object) this));
    }

    // Make sure the thread local is reset when entering and after exiting "affectWorld"
    @Inject(method = "affectWorld", at = @At(value = "HEAD"))
    private void setThreadLocals(boolean particles, CallbackInfo ci){
        ExplosionUtils.DROP_EXPLOSION_ITEMS.set(true);
        ExplosionUtils.DROP_BLOCK_INVENTORY_ITEMS.set(true);
    }

    @Inject(method = "affectWorld", at = @At(value = "RETURN"))
    private void resetThreadLocals(boolean particles, CallbackInfo ci){
        ExplosionUtils.DROP_EXPLOSION_ITEMS.set(true);
        ExplosionUtils.DROP_BLOCK_INVENTORY_ITEMS.set(true);
        // After the explosion has happened, filter out the calculated positions that correspond to block states
        // whose states did not change before and after the explosion (were not exploded).
        this.indirectlyExplodedPositions.removeIf(pos -> {
            BlockState oldState = this.creeperhealing$getAffectedStatesAndBlockEntities().get(pos).getLeft();
            BlockState newState = this.world.getBlockState(pos);
            return newState.equals(oldState);
        });
        ExplosionCallbacks.AFTER_EXPLOSION.invoker().afterExplosion(((Explosion) (Object) this));
    }


    // Recursive algorithm to find blocks connected to the explosion indirectly.
    // Start from the edge of the explosion radius and visit each non-air block until we either hit the max recursion depth,
    // or we encounter a block which has no non-visited neighbors.
    @Unique
    private void checkForAffectedNeighborPositions() {
        // Start by filtering out vanilla affected positions with no non-affected neighbor positions.
        // The goal is to start at the "edge" of the blast radius by considering blocks which might have adjacent blocks connected to them that will be indirectly destroyed
        // due to the support block being destroyed.
        List<BlockPos> filteredPositions = this.getAffectedBlocks().stream().filter(pos -> {
            // No blocks will be connected to this position is the state is air
            if (world.getBlockState(pos).isAir()) {
                return false;
            }
            for (Direction direction : Direction.values()) {
                BlockPos neighborPos = pos.offset(direction);
                BlockState neighborState = world.getBlockState(neighborPos);
                // No blocks will be connected to the neighbor position if the state is air
                if (neighborState.isAir()) {
                    continue;
                }
                if (!this.getAffectedBlocks().contains(neighborPos)) {
                    return true;
                }
            }
            return false;
        }).toList();
        Set<BlockPos> newPositions = new HashSet<>();
        for (BlockPos filteredPosition : filteredPositions) {
            checkNeighbors(150, filteredPosition, newPositions);
        }
        this.indirectlyExplodedPositions.addAll(newPositions);
    }

    @Unique
    private void checkNeighbors(int maxCheckDepth, BlockPos currentPosition, Set<BlockPos> newPositions) {
        if (maxCheckDepth <= 0) {
            return;
        }
        for (Direction neighborDirection : Direction.values()) {
            BlockPos neighborPos = currentPosition.offset(neighborDirection);
            BlockState neighborState = this.world.getBlockState(neighborPos);
            if (neighborState.isOf(Blocks.AIR) || newPositions.contains(neighborPos) || this.getAffectedBlocks().contains(neighborPos)) {
                continue;
            }
            newPositions.add(neighborPos);
            this.checkNeighbors(maxCheckDepth - 1, neighborPos, newPositions);
        }
    }

}

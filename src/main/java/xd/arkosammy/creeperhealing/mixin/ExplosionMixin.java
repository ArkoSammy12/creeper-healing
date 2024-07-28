package xd.arkosammy.creeperhealing.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
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
import xd.arkosammy.creeperhealing.ExplosionManagerRegistrar;
import xd.arkosammy.creeperhealing.util.ExplosionContext;
import xd.arkosammy.creeperhealing.util.ExplosionUtils;
import xd.arkosammy.creeperhealing.explosions.ducks.ExplosionAccessor;

import java.util.*;

@Mixin(Explosion.class)
public abstract class ExplosionMixin implements ExplosionAccessor {

    @Shadow @Final private World world;

    @Shadow @Final private DamageSource damageSource;

    @Shadow public abstract @Nullable Entity getEntity();

    @Shadow public abstract List<BlockPos> getAffectedBlocks();

    @Shadow @Nullable public abstract LivingEntity getCausingEntity();

    @Unique
    private final Map<BlockPos, Pair<BlockState, BlockEntity>> affectedStatesAndBlockEntities = new HashMap<>();

    @Unique
    private final Set<BlockPos> indirectlyAffectedPositions = new HashSet<>();

    @Override
    public DamageSource creeperhealing$getDamageSource() {
        return this.damageSource;
    }

    @Override
    public boolean creeperhealing$willBeHealed(){
        return ExplosionUtils.getShouldHealPredicate().test(((Explosion) (Object) this));
    }

    // Save the affected block states and block entities before the explosion takes effect
    @Inject(method = "collectBlocksAndDamageEntities", at = @At("RETURN"))
    private void collectAffectedStatesAndBlockEntities(CallbackInfo ci){
        this.checkForIndirectlyAffectedPositions();
        this.getAffectedBlocks().forEach(pos -> this.affectedStatesAndBlockEntities.put(pos, new Pair<>(this.world.getBlockState(pos), this.world.getBlockEntity(pos))));
        this.indirectlyAffectedPositions.forEach(pos -> this.affectedStatesAndBlockEntities.put(pos, new Pair<>(this.world.getBlockState(pos), this.world.getBlockEntity(pos))));
    }

    // Make sure the thread local is reset when entering and exiting Explosion#affectWorld
    @Inject(method = "affectWorld", at = @At(value = "HEAD"))
    private void setThreadLocals(boolean particles, CallbackInfo ci){
        ExplosionUtils.DROP_BLOCK_ITEMS.set(true);
        ExplosionUtils.DROP_CONTAINER_INVENTORY_ITEMS.set(true);
    }

    // Filter out indirectly affected positions whose corresponding state did not change before and after the explosion.
    // Filter out entries in the affected states and block entities map with block position keys not in the affected positions.
    // Emit an ExplosionContext object for ExplosionManagers to receive.
    @Inject(method = "affectWorld", at = @At(value = "RETURN"))
    private void onExplosion(boolean particles, CallbackInfo ci){
        ExplosionUtils.DROP_BLOCK_ITEMS.set(true);
        ExplosionUtils.DROP_CONTAINER_INVENTORY_ITEMS.set(true);
        this.indirectlyAffectedPositions.removeIf(pos -> {
            BlockState oldState = this.affectedStatesAndBlockEntities.get(pos).getLeft();
            BlockState newState = this.world.getBlockState(pos);
            return newState.equals(oldState);
        });
        Map<BlockPos, Pair<BlockState, BlockEntity>> filteredSavedStatesAndBlockEntities = new HashMap<>();
        for (Map.Entry<BlockPos, Pair<BlockState, BlockEntity>> entry : this.affectedStatesAndBlockEntities.entrySet()) {
            BlockPos entryPos = entry.getKey();
            if (this.getAffectedBlocks().contains(entryPos) || this.indirectlyAffectedPositions.contains(entryPos)) {
                filteredSavedStatesAndBlockEntities.put(entry.getKey(), entry.getValue());
            }
        }
        ExplosionContext explosionContext = new ExplosionContext(
                new ArrayList<>(this.getAffectedBlocks()),
                new ArrayList<>(this.indirectlyAffectedPositions),
                filteredSavedStatesAndBlockEntities,
                this.world,
                this.getEntity(),
                this.getCausingEntity(),
                this.damageSource
        );
        ExplosionManagerRegistrar.getInstance().emitExplosionContext(explosionContext);
        this.affectedStatesAndBlockEntities.clear();
        this.indirectlyAffectedPositions.clear();
    }

    // Recursively find indirectly affected positions connected to the main affected positions.
    // Start from the "edge" of the blast radius and visit each neighbor until a neighbor has no
    // non-visited positions, the neighbor is surrounded by air, or we hit the max recursion depth.
    @Unique
    private void checkForIndirectlyAffectedPositions() {
        List<BlockPos> edgeAffectedPositions = this.getAffectedBlocks().stream().filter(pos -> {
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

        // Look for a block position that is air and is surrounded by air. This position will be used to test
        // whether blocks need a supporting block. By checking whether a block can be placed at a position using
        // BlockState#canPlaceAt, and guaranteeing that the position is both air and is surrounded by air, we can tell
        // whether that block needs a supporting block. If it returns true, then we can safely omit the block as it will not
        // be directly affected by the explosion.
        BlockPos cachedNeutralPosition = null;
        BlockPos backupNeutralPosition = new BlockPos(0, this.world.getTopY() + 5, 0);

        Set<BlockPos> newPositions = new HashSet<>();
        for (BlockPos filteredPosition : edgeAffectedPositions) {

            if (cachedNeutralPosition == null) {
                BlockPos.Mutable mutablePos = new BlockPos.Mutable(filteredPosition.getX(), this.world.getTopY(), filteredPosition.getZ());
                mainLoop: while (cachedNeutralPosition == null && !this.world.isOutOfHeightLimit(mutablePos)) {
                    if (!this.world.getBlockState(mutablePos).isAir()) {
                        mutablePos.setY(mutablePos.getY() - 1);
                        continue;
                    }
                    for (Direction neighborDirection : Direction.values()) {
                        if (!this.world.getBlockState(mutablePos.offset(neighborDirection)).isAir()) {
                            mutablePos.setY(mutablePos.getY() - 1);
                            continue mainLoop;
                        }
                    }
                    cachedNeutralPosition = mutablePos.toImmutable();
                }

            }

            BlockPos neutralPos = cachedNeutralPosition == null ? backupNeutralPosition : cachedNeutralPosition;
            checkNeighbors(100, filteredPosition, newPositions, neutralPos);
        }
        this.indirectlyAffectedPositions.addAll(newPositions);
    }

    @Unique
    private void checkNeighbors(int maxCheckDepth, BlockPos currentPosition, Set<BlockPos> newPositions, BlockPos neutralPosition) {
        if (maxCheckDepth <= 0) {
            return;
        }
        for (Direction neighborDirection : Direction.values()) {
            BlockPos neighborPos = currentPosition.offset(neighborDirection);
            BlockState neighborState = this.world.getBlockState(neighborPos);

            if (neighborState.isAir() || neighborState.canPlaceAt(this.world, neutralPosition) || this.getAffectedBlocks().contains(neighborPos)) {
                continue;
            }
            if (newPositions.add(neighborPos)) {
                this.checkNeighbors(maxCheckDepth - 1, neighborPos, newPositions, neutralPosition);
            }
        }
    }

}

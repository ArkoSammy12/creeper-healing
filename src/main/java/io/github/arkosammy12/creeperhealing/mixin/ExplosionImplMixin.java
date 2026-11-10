package io.github.arkosammy12.creeperhealing.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.arkosammy12.creeperhealing.explosions.ducks.ServerWorldDuck;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import kotlin.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ServerExplosion;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import io.github.arkosammy12.creeperhealing.ExplosionManagerRegistrar;
import io.github.arkosammy12.creeperhealing.config.ConfigUtils;
import io.github.arkosammy12.creeperhealing.explosions.ducks.ExplosionImplDuck;
import io.github.arkosammy12.creeperhealing.managers.DefaultExplosionManager;
import io.github.arkosammy12.creeperhealing.util.EmptyWorld;
import io.github.arkosammy12.creeperhealing.util.ExcludedBlocks;
import io.github.arkosammy12.creeperhealing.util.ExplosionContext;
import io.github.arkosammy12.creeperhealing.util.ExplosionUtils;

import java.util.*;

@Mixin(ServerExplosion.class)
public abstract class ExplosionImplMixin implements Explosion, ExplosionImplDuck {

    @Shadow public abstract @Nullable Entity getDirectSourceEntity();

    @Shadow @Nullable public abstract LivingEntity getIndirectSourceEntity();

    @Shadow public abstract ServerLevel level();

    @Unique
    @Nullable
    private Level.ExplosionInteraction explosionSourceType = null;

    @Unique
    private final Map<BlockPos, Pair<BlockState, BlockEntity>> affectedStatesAndBlockEntities = new HashMap<>();

    @Unique
    private final Set<BlockPos> vanillaAffectedPositions = new HashSet<>();

    @Unique
    private final Set<BlockPos> indirectlyAffectedPositions = new HashSet<>();

    @Override
    public void creeperhealing$setExplosionInteraction(Level.ExplosionInteraction explosionSourceType) {
        this.explosionSourceType = explosionSourceType;
    }

    @Override
    public Level.ExplosionInteraction creeperhealing$getExplosionInteraction() {
        return this.explosionSourceType;
    }

    @Override
    public boolean creeperhealing$shouldHeal() {
        if (this.level().isClientSide()) {
            return false;
        }
        if (this.vanillaAffectedPositions.isEmpty()) {
            return false;
        }
        Level.ExplosionInteraction explosionSourceType = (this.explosionSourceType);
        boolean shouldHeal = switch (explosionSourceType) {
            case MOB -> {
                if (!ConfigUtils.getRawBooleanSetting(ConfigUtils.HEAL_MOB_EXPLOSIONS)) {
                    yield false;
                }
                LivingEntity causingEntity = this.getIndirectSourceEntity();
                if (causingEntity == null) {
                    yield true;
                }
                String entityId = BuiltInRegistries.ENTITY_TYPE.getKey(causingEntity.getType()).toString();
                List<? extends String> healMobExplosionsBlacklist = ConfigUtils.getRawStringListSetting(ConfigUtils.HEAL_MOB_EXPLOSIONS_BLACKLIST);
                yield !healMobExplosionsBlacklist.contains(entityId);
            }
            case BLOCK -> ConfigUtils.getRawBooleanSetting(ConfigUtils.HEAL_BLOCK_EXPLOSIONS);
            case TNT -> ConfigUtils.getRawBooleanSetting(ConfigUtils.HEAL_TNT_EXPLOSIONS);
            case TRIGGER -> ConfigUtils.getRawBooleanSetting(ConfigUtils.HEAL_TRIGGERED_EXPLOSIONS);
            case null, default -> ConfigUtils.getRawBooleanSetting(ConfigUtils.HEAL_OTHER_EXPLOSIONS);
        };
        return shouldHeal;
    }

    // Save the affected block states and block entities before the explosion takes effect
    @ModifyReturnValue(method = "calculateExplodedPositions", at = @At("RETURN"))
    private List<BlockPos> collectAffectedBlocks(List<BlockPos> original) {
        if (this.level().isClientSide()) {
            return original;
        }
        this.vanillaAffectedPositions.addAll(ExplosionUtils.filterPositionsToHeal(original, (pos) -> this.level().getBlockState(pos)));
        this.checkForIndirectlyAffectedPositions();
        for (BlockPos pos : this.vanillaAffectedPositions) {
            this.affectedStatesAndBlockEntities.put(pos, new Pair<>(this.level().getBlockState(pos), this.level().getBlockEntity(pos)));
        }
        for (BlockPos pos : this.indirectlyAffectedPositions) {
            this.affectedStatesAndBlockEntities.put(pos, new Pair<>(this.level().getBlockState(pos), this.level().getBlockEntity(pos)));
        }
        return original;
    }

    @WrapMethod(method = "interactWithBlocks")
    private void onDestroyBlocks(List<BlockPos> positions, Operation<Void> original) {

        // Make sure the thread local is reset when entering and exiting ServerExplosion#interactWithBlocks
        // If adding more logic here, remember to check logical server side with Level#isClient
        ExplosionUtils.DROP_BLOCK_ITEMS.set(true);
        ExplosionUtils.DROP_CONTAINER_INVENTORY_ITEMS.set(true);

        Level world = this.level();
        if ((!(world instanceof ServerLevel serverWorld)) || !this.creeperhealing$shouldHeal()) {
            this.vanillaAffectedPositions.clear();
            this.affectedStatesAndBlockEntities.clear();
            this.indirectlyAffectedPositions.clear();
            original.call(positions);
            ExplosionUtils.DROP_BLOCK_ITEMS.set(true);
            ExplosionUtils.DROP_CONTAINER_INVENTORY_ITEMS.set(true);
            return;
        }

        ((ServerWorldDuck) serverWorld).creeperhealing$addAffectedPositions(vanillaAffectedPositions);
        ((ServerWorldDuck) serverWorld).creeperhealing$addAffectedPositions(indirectlyAffectedPositions);

        original.call(positions);

        ((ServerWorldDuck) serverWorld).creeperhealing$clearAffectedPositions();

        // Filter out indirectly affected positions whose corresponding state did not change before and after the explosion.
        // Filter out entries in the affected states and block entities map with block position keys not in the affected positions.
        // Emit an ExplosionContext object for ExplosionManagers to receive.
        ExplosionUtils.DROP_BLOCK_ITEMS.set(true);
        ExplosionUtils.DROP_CONTAINER_INVENTORY_ITEMS.set(true);

        List<BlockPos> filteredIndirectlyAffectedPositions = new ArrayList<>();
        for (BlockPos pos : this.indirectlyAffectedPositions) {
            Pair<BlockState, BlockEntity> pair = this.affectedStatesAndBlockEntities.get(pos);
            if (pair == null) {
                continue;
            }
            BlockState oldState = pair.getFirst();
            // Hardcoded exception, place before all other logic
            if (ExcludedBlocks.isExcluded(oldState)) {
                continue;
            }
            BlockState newState = this.level().getBlockState(pos);
            if (!Objects.equals(oldState, newState)) {
                filteredIndirectlyAffectedPositions.add(pos);
            }
        }
        List<BlockPos> filteredAffectedPositions = new ArrayList<>();
        for (BlockPos pos : this.vanillaAffectedPositions) {
            Pair<BlockState, BlockEntity> pair = this.affectedStatesAndBlockEntities.get(pos);
            if (pair == null) {
                continue;
            }
            BlockState state = pair.getFirst();
            // Hardcoded exception, place before all other logic
            if (ExcludedBlocks.isExcluded(state)) {
                continue;
            }
            filteredAffectedPositions.add(pos);
        }
        Map<BlockPos, Pair<BlockState, BlockEntity>> filteredSavedStatesAndBlockEntities = new HashMap<>();
        for (Map.Entry<BlockPos, Pair<BlockState, BlockEntity>> entry : this.affectedStatesAndBlockEntities.entrySet()) {
            BlockPos entryPos = entry.getKey();
            if (filteredAffectedPositions.contains(entryPos) || filteredIndirectlyAffectedPositions.contains(entryPos)) {
                filteredSavedStatesAndBlockEntities.put(entryPos, entry.getValue());
            }
        }
        ExplosionContext explosionContext = new ExplosionContext(
                filteredAffectedPositions,
                filteredIndirectlyAffectedPositions,
                filteredSavedStatesAndBlockEntities,
                serverWorld,
                this.explosionSourceType
        );
        ExplosionManagerRegistrar.getInstance().emitExplosionContext(DefaultExplosionManager.ID, explosionContext);
        this.vanillaAffectedPositions.clear();
        this.affectedStatesAndBlockEntities.clear();
        this.indirectlyAffectedPositions.clear();

    }

    // Recursively find indirectly affected positions connected to the main affected positions.
    // Start from the "edge" of the blast radius and visit each neighbor until a neighbor has no
    // non-visited positions, the neighbor is surrounded by air, or we hit the max recursion depth.
    @Unique
    private void checkForIndirectlyAffectedPositions() {

        // Only consider block positions with adjacent non-affected positions
        List<BlockPos> edgeAffectedPositions = new ArrayList<>();
        for (BlockPos vanillaAffectedPosition : this.vanillaAffectedPositions) {
            if (this.level().getBlockState(vanillaAffectedPosition).isAir()) {
                continue;
            }
            for (Direction direction : Direction.values()) {
                BlockPos neighborPos = vanillaAffectedPosition.relative(direction);
                BlockState neighborState = this.level().getBlockState(neighborPos);
                // No blocks will be connected to the neighbor position if the state is air
                if (neighborState.isAir()) {
                    continue;
                }
                if (!this.vanillaAffectedPositions.contains(neighborPos)) {
                    edgeAffectedPositions.add(vanillaAffectedPosition);
                    break;
                }
            }
        }

        // Pass in a custom WorldView implementation that always returns an air BlockState when calling
        // WorldView#getBlockState on it. This guarantees that further checks with BlockState#canPlaceAt
        // are done in what will look like an empty world
        EmptyWorld emptyWorld = new EmptyWorld(this.level());
        Set<BlockPos> newPositions = new HashSet<>();
        for (BlockPos filteredPosition : edgeAffectedPositions) {
            checkNeighbors(512, filteredPosition, newPositions, emptyWorld);
        }
        this.indirectlyAffectedPositions.addAll(ExplosionUtils.filterPositionsToHeal(newPositions, (pos) -> this.level().getBlockState(pos)));
    }

    @Unique
    private void checkNeighbors(int maxCheckDepth, BlockPos currentPosition, Set<BlockPos> newPositions, EmptyWorld emptyWorld) {
        if (maxCheckDepth <= 0) {
            return;
        }
        for (Direction neighborDirection : Direction.values()) {
            BlockPos neighborPos = currentPosition.relative(neighborDirection);
            BlockState neighborState = this.level().getBlockState(neighborPos);

            // If the block cannot be placed at an empty position also surrounded by air, then we assume
            // the block needs a supporting block to be placed.
            if (neighborState.isAir() || neighborState.canSurvive(emptyWorld, neighborPos) || this.vanillaAffectedPositions.contains(neighborPos)) {
                continue;
            }
            if (newPositions.add(neighborPos)) {
                this.checkNeighbors(maxCheckDepth - 1, neighborPos, newPositions, emptyWorld);
            }
        }
    }

}

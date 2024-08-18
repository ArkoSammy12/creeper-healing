package xd.arkosammy.creeperhealing.mixin.compat.mythicmetals;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import nourl.mythicmetals.misc.EpicExplosion;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xd.arkosammy.creeperhealing.ExplosionManagerRegistrar;
import xd.arkosammy.creeperhealing.managers.DefaultExplosionManager;
import xd.arkosammy.creeperhealing.util.EmptyWorld;
import xd.arkosammy.creeperhealing.util.ExcludedBlocks;
import xd.arkosammy.creeperhealing.util.ExplosionContext;
import xd.arkosammy.creeperhealing.util.ExplosionUtils;

import java.util.*;
import java.util.function.Predicate;

@Mixin(EpicExplosion.class)
public abstract class EpicExplosionMixin {

    @WrapMethod(method = "explode")
    private static void onEpicExplosion(ServerWorld world,
                                        int x,
                                        int y,
                                        int z,
                                        int radius,
                                        Predicate<BlockState> statePredicate,
                                        @Nullable Entity exploder,
                                        @Nullable PlayerEntity cause,
                                        Operation<Void> original,
                                        @Share("affectedPositions") LocalRef<Map<BlockPos, Pair<BlockState, BlockEntity>>> affectedStatesAndBlockEntities,
                                        @Share("indirectlyAffectedPositions") LocalRef<Set<BlockPos>> indirectlyAffectedPositions,
                                        @Share("emptyWorld") LocalRef<EmptyWorld> emptyWorld) {
        affectedStatesAndBlockEntities.set(new HashMap<>());
        indirectlyAffectedPositions.set(new HashSet<>());
        emptyWorld.set(new EmptyWorld(world));
        ExplosionUtils.DROP_BLOCK_ITEMS.set(true);
        ExplosionUtils.DROP_CONTAINER_INVENTORY_ITEMS.set(true);

        original.call(world, x, y, z, radius, statePredicate, exploder, cause);

        ExplosionUtils.DROP_BLOCK_ITEMS.set(true);
        ExplosionUtils.DROP_CONTAINER_INVENTORY_ITEMS.set(true);

        List<BlockPos> filteredIndirectlyAffectedPositions = new ArrayList<>();
        for (BlockPos pos : indirectlyAffectedPositions.get()) {
            Pair<BlockState, BlockEntity> pair = affectedStatesAndBlockEntities.get().get(pos);
            if (pair == null) {
                continue;
            }
            BlockState oldState = pair.getLeft();
            // Hardcoded exception, place before all other logic
            if (ExcludedBlocks.isExcluded(oldState)) {
                continue;
            }
            BlockState newState = world.getBlockState(pos);
            if (!Objects.equals(oldState, newState)) {
                filteredIndirectlyAffectedPositions.add(pos);
            }
        }

        List<BlockPos> filteredAffectedPositions = new ArrayList<>();
        for (BlockPos pos : affectedStatesAndBlockEntities.get().keySet()) {
            Pair<BlockState, BlockEntity> pair = affectedStatesAndBlockEntities.get().get(pos);
            if (pair == null) {
                continue;
            }
            BlockState state = pair.getLeft();
            // Hardcoded exception, place before all other logic
            if (ExcludedBlocks.isExcluded(state)) {
                continue;
            }
            filteredAffectedPositions.add(pos);
        }
        Map<BlockPos, Pair<BlockState, BlockEntity>> filteredSavedStatesAndBlockEntities = new HashMap<>();
        for (Map.Entry<BlockPos, Pair<BlockState, BlockEntity>> entry : affectedStatesAndBlockEntities.get().entrySet()) {
            BlockPos entryPos = entry.getKey();
            if (filteredAffectedPositions.contains(entryPos) || filteredIndirectlyAffectedPositions.contains(entryPos)) {
                filteredSavedStatesAndBlockEntities.put(entryPos, entry.getValue());
            }
        }
        ExplosionContext explosionContext = new ExplosionContext(
                filteredAffectedPositions,
                filteredIndirectlyAffectedPositions,
                filteredSavedStatesAndBlockEntities,
                world,
                World.ExplosionSourceType.TNT
        );
        ExplosionManagerRegistrar.getInstance().emitExplosionContext(DefaultExplosionManager.ID, explosionContext);

    }

    @WrapOperation(method = "explode", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Z"))
    private static boolean onBlockSetAir(ServerWorld instance,
                                         BlockPos pos,
                                         BlockState blockState,
                                         Operation<Boolean> original,
                                         ServerWorld world,
                                         @Share("affectedPositions") LocalRef<Map<BlockPos, Pair<BlockState, BlockEntity>>> affectedStatesAndBlockEntities,
                                         @Share("indirectlyAffectedPositions") LocalRef<Set<BlockPos>> indirectlyAffectedPositions,
                                         @Share("emptyWorld") LocalRef<EmptyWorld> emptyWorld) {

        BlockState affectedState = instance.getBlockState(pos);
        BlockEntity affectedBlockEntity = instance.getBlockEntity(pos);
        affectedStatesAndBlockEntities.get().put(pos.toImmutable(), new Pair<>(affectedState, affectedBlockEntity));
        ExplosionUtils.checkNeighbors(512, pos, affectedStatesAndBlockEntities.get().keySet().stream().toList(), indirectlyAffectedPositions.get(), world, emptyWorld.get());
        return original.call(instance, pos, blockState);
    }

}

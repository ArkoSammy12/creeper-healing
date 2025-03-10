package io.github.arkosammy12.creeperhealing.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import io.github.arkosammy12.creeperhealing.util.ExcludedBlocks;
import io.github.arkosammy12.creeperhealing.util.ExplosionUtils;

@Mixin(World.class)
public abstract class WorldMixin {

    @Shadow
    public abstract BlockState getBlockState(BlockPos pos);

    // Use our thread local to pass an extra flag to World#setBlockState to make the explosion not drop the item of the current block.
    // Container blocks can still drop their inventories
    @Inject(method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;II)Z", at = @At("HEAD"))
    private void checkExcludedBlock(BlockPos pos, BlockState state, int flags, int maxUpdateDepth, CallbackInfoReturnable<Boolean> cir, @Share("isBlockAtPosExcluded") LocalBooleanRef isBlockAtPosExcluded) {
        isBlockAtPosExcluded.set(ExcludedBlocks.isExcluded(this.getBlockState(pos)));
    }

    @WrapOperation(method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;II)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;updateNeighbors(Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;II)V"))
    private void preventItemsFromDroppingOnExplosionsIfNeeded(BlockState instance, WorldAccess worldAccess, BlockPos blockPos, int flags, int maxUpdateDepth, Operation<Void> original, @Share("isBlockAtPosExcluded") LocalBooleanRef isBlockAtPosExcluded) {
        // Hardcoded exception. Place before all other logic
        if (isBlockAtPosExcluded.get()) {
            original.call(instance, worldAccess, blockPos, flags, maxUpdateDepth);
            return;
        }
        int newFlags = ExplosionUtils.DROP_BLOCK_ITEMS.get() ? flags : flags | Block.SKIP_DROPS;
        original.call(instance, worldAccess, blockPos, newFlags, maxUpdateDepth);
    }

}

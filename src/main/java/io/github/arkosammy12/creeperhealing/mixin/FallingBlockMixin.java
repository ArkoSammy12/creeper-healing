package io.github.arkosammy12.creeperhealing.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import io.github.arkosammy12.creeperhealing.util.ExcludedBlocks;
import io.github.arkosammy12.creeperhealing.util.ExplosionUtils;

@Mixin(FallingBlock.class)
public abstract class FallingBlockMixin {

    @ModifyExpressionValue(method = "scheduledTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/FallingBlock;canFallThrough(Lnet/minecraft/block/BlockState;)Z"))
    private boolean onBlockAttemptedFall(boolean original, BlockState blockState) {
        // Hardcoded Exception. Place before all other logic
        if (ExcludedBlocks.isExcluded(blockState)) {
            ExplosionUtils.FALLING_BLOCK_SCHEDULE_TICK.set(true);
            return original;
        }
        boolean canFall = original && ExplosionUtils.FALLING_BLOCK_SCHEDULE_TICK.get();
        ExplosionUtils.FALLING_BLOCK_SCHEDULE_TICK.set(true);
        return canFall;
    }

}

package xd.arkosammy.creeperhealing.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xd.arkosammy.creeperhealing.util.ExcludedBlocks;
import xd.arkosammy.creeperhealing.util.ExplosionUtils;

@Mixin(FallingBlock.class)
public class FallingBlockMixin {

    @ModifyExpressionValue(method = "scheduledTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/FallingBlock;canFallThrough(Lnet/minecraft/block/BlockState;)Z"))
    private boolean onBlockAttemptedFall(boolean original, @Local BlockState blockState){
        // Hardcoded Exception. Place before all other logic
        if(ExcludedBlocks.isExcluded(blockState)){
            return original;
        }
        boolean canFall = original && ExplosionUtils.FALLING_BLOCK_SCHEDULE_TICK.get();
        ExplosionUtils.FALLING_BLOCK_SCHEDULE_TICK.set(true);
        return canFall;
    }

}

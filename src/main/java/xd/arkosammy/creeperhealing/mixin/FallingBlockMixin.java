package xd.arkosammy.creeperhealing.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.Block;
import net.minecraft.block.FallingBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xd.arkosammy.creeperhealing.util.ExplosionUtils;

@Mixin(FallingBlock.class)
public class FallingBlockMixin {

    @WrapOperation(method = "onBlockAdded", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;scheduleBlockTick(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;I)V"))
    private void preventFallingBlockFromFallingWhenHealed(World instance, BlockPos pos, Block block, int i, Operation<Void> original){
        if(ExplosionUtils.FALLING_BLOCK_SCHEDULE_TICK.get() != null && ExplosionUtils.FALLING_BLOCK_SCHEDULE_TICK.get()){
            original.call(instance, pos, block, i);
        }
        ExplosionUtils.FALLING_BLOCK_SCHEDULE_TICK.set(true);
    }

}

package xd.arkosammy.creeperhealing.mixin;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import xd.arkosammy.creeperhealing.util.ExplosionUtils;

@Mixin(World.class)
public abstract class WorldMixin {
    @ModifyArg(method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;II)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;updateNeighbors(Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;II)V"), index = 2)
    private int preventItemsFromDroppingOnExplosionsIfNeeded(int flags){
        if(ExplosionUtils.SHOULD_DROP_ITEMS_THREAD_LOCAL.get() != null && !ExplosionUtils.SHOULD_DROP_ITEMS_THREAD_LOCAL.get()) {
            return flags | Block.SKIP_DROPS;
        } else {
            return flags;
        }
    }

}

package xd.arkosammy.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.Block;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xd.arkosammy.explosions.ExplosionUtils;

@Mixin(Block.class)
public abstract class ExplosionItemDropMixin {

    @ModifyReturnValue(method = "shouldDropItemsOnExplosion", at=@At("RETURN"))
    private boolean shouldDropItems(boolean dropItems, @Local Explosion explosion){
        return ExplosionUtils.shouldExplosionDropItems(dropItems, explosion);
    }

}

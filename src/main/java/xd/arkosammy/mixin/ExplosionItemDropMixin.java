package xd.arkosammy.mixin;

import net.minecraft.block.Block;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xd.arkosammy.CreeperHealing;
import xd.arkosammy.configuration.tables.PreferencesConfig;

@Mixin(Block.class)
public abstract class ExplosionItemDropMixin {

    @Inject(method = "shouldDropItemsOnExplosion", at=@At("HEAD"), cancellable = true)
    private void shouldDropItems(@NotNull Explosion explosion, CallbackInfoReturnable<Boolean> cir){

        if(explosion.getCausingEntity() instanceof CreeperEntity && !PreferencesConfig.getDropItemsOnExplosions()){

            cir.setReturnValue(false);

        }

    }

}

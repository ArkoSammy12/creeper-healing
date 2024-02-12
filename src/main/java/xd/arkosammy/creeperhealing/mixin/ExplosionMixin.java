package xd.arkosammy.creeperhealing.mixin;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xd.arkosammy.creeperhealing.util.ExplosionManager;
import xd.arkosammy.creeperhealing.util.ExplosionUtils;
import xd.arkosammy.creeperhealing.explosions.ducks.ExplosionAccessor;


@Mixin(Explosion.class)
public abstract class ExplosionMixin implements ExplosionAccessor {

    @Shadow @Final private World world;

    @Shadow @Final private DamageSource damageSource;

    @Override
    public World creeper_healing$getWorld(){
        return this.world;
    }

    @Override
    public DamageSource creeper_healing$getDamageSource() {
        return this.damageSource;
    }

    @Inject(method = "collectBlocksAndDamageEntities", at = @At("RETURN"))
    private void storeCurrentExplosionIfNeeded(CallbackInfo ci){
        ExplosionManager.getInstance().processExplosion((Explosion) (Object) this);
    }

    // Make sure the thread local is reset when entering and after exiting "affectWorld"
    @Inject(method = "affectWorld", at = @At(value = "HEAD"))
    private void setDropItemsThreadLocal(boolean particles, CallbackInfo ci){
        ExplosionUtils.SHOULD_DROP_ITEMS_THREAD_LOCAL.set(true);
    }

    @Inject(method = "affectWorld", at = @At(value = "RETURN"))
    private void clearDropItemsThreadLocal(boolean particles, CallbackInfo ci){
        ExplosionUtils.SHOULD_DROP_ITEMS_THREAD_LOCAL.set(true);
    }
}

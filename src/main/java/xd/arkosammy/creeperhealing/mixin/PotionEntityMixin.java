package xd.arkosammy.creeperhealing.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xd.arkosammy.creeperhealing.util.callbacks.SplashPotionCallbacks;

@Mixin(PotionEntity.class)
public abstract class PotionEntityMixin extends ThrownItemEntity {

    public PotionEntityMixin(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    // Make explosions start healing when you throw a potion of healing or regeneration on them
    @Inject(method = "onCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/thrown/PotionEntity;applySplashPotion(Lnet/minecraft/server/world/ServerWorld;Ljava/lang/Iterable;Lnet/minecraft/entity/Entity;)V"))
    private void onSplashPotionHit(HitResult hitResult, CallbackInfo ci, @Local PotionContentsComponent potionContentsComponent) {
        World world = this.getWorld();
        SplashPotionCallbacks.ON_COLLISION.invoker().onPotionCollide(((PotionEntity) (Object) this), potionContentsComponent, hitResult, world);
    }

}

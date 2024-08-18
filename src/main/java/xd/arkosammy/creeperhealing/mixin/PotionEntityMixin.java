package xd.arkosammy.creeperhealing.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xd.arkosammy.creeperhealing.util.callbacks.SplashPotionCallbacks;

import java.util.ArrayList;
import java.util.List;

@Mixin(PotionEntity.class)
public abstract class PotionEntityMixin extends ThrownItemEntity {

    public PotionEntityMixin(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    @WrapOperation(method = "onCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/thrown/PotionEntity;applySplashPotion(Ljava/lang/Iterable;Lnet/minecraft/entity/Entity;)V"))
    private void onSplashPotionHit(PotionEntity instance, Iterable<StatusEffectInstance> effects, Entity entity, Operation<Void> original, HitResult hitResult) {
        World world = this.getWorld();
        List<StatusEffectInstance> statusEffects = new ArrayList<>();
        for (StatusEffectInstance statusEffect : effects) {
            statusEffects.add(statusEffect);
        }
        SplashPotionCallbacks.ON_COLLISION.invoker().onPotionCollide(((PotionEntity) (Object) this), statusEffects, hitResult, world);
    }

}

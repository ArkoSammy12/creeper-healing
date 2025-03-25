package io.github.arkosammy12.creeperhealing.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.component.ComponentType;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.entity.projectile.thrown.SplashPotionEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import io.github.arkosammy12.creeperhealing.util.callbacks.SplashPotionCallbacks;

@Mixin(PotionEntity.class)
public abstract class PotionEntityMixin extends ThrownItemEntity {

    public PotionEntityMixin(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    // Make explosions start healing when you throw a potion of healing or regeneration on them
    @WrapOperation(method = "onBlockHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getOrDefault(Lnet/minecraft/component/ComponentType;Ljava/lang/Object;)Ljava/lang/Object;"))
    private Object onSplashPotionHit(ItemStack instance, ComponentType type, Object fallback, Operation<Object> original, @Local(argsOnly = true) BlockHitResult blockHitResult) {
        Object result = original.call(instance, type, fallback);
        if (!(((PotionEntity) (Object) this) instanceof SplashPotionEntity)) {
            return result;
        }
        if (result instanceof PotionContentsComponent potionContentsComponent) {
            World world = this.getWorld();
            SplashPotionCallbacks.ON_COLLISION.invoker().onPotionCollide(((PotionEntity) (Object) this), potionContentsComponent, blockHitResult, world);
        }
        return result;
    }

}

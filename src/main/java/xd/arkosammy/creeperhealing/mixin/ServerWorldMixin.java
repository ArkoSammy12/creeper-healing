package xd.arkosammy.creeperhealing.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import net.minecraft.world.explosion.ExplosionImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xd.arkosammy.creeperhealing.explosions.ducks.ExplosionImplDuck;
import xd.arkosammy.creeperhealing.util.callbacks.DaylightCycleEvents;

import java.util.function.BooleanSupplier;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;setTimeOfDay(J)V", shift = At.Shift.AFTER, ordinal = 0))
    private void fastForwardDaytimeHealingModeExplosionsOnNightSkipped(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        DaylightCycleEvents.ON_NIGHT_SKIPPED.invoker().onNightSkipped(((ServerWorld) (Object) this), shouldKeepTicking);
    }

    @WrapOperation(method = "createExplosion", at = @At(value = "NEW", target = "(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/damage/DamageSource;Lnet/minecraft/world/explosion/ExplosionBehavior;Lnet/minecraft/util/math/Vec3d;FZLnet/minecraft/world/explosion/Explosion$DestructionType;)Lnet/minecraft/world/explosion/ExplosionImpl;"))
    private ExplosionImpl attachExplosionSourceTypeToExplosion(ServerWorld world, Entity entity, DamageSource damageSource, ExplosionBehavior behavior, Vec3d pos, float power, boolean createFire, Explosion.DestructionType destructionType, Operation<ExplosionImpl> original, @Local(argsOnly = true) World.ExplosionSourceType explosionSourceType) {
        ExplosionImpl explosion = original.call(world, entity, damageSource, behavior, pos, power, createFire, destructionType);
        ((ExplosionImplDuck) explosion).creeperhealing$setExplosionSourceType(explosionSourceType);
        return explosion;
    }

}


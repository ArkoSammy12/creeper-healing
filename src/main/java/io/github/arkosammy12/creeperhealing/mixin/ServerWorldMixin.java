package io.github.arkosammy12.creeperhealing.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.arkosammy12.creeperhealing.explosions.ducks.ServerWorldDuck;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.clock.ClockTimeMarker;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.ServerExplosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import io.github.arkosammy12.creeperhealing.explosions.ducks.ExplosionImplDuck;
import io.github.arkosammy12.creeperhealing.util.callbacks.DaylightCycleEvents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.BooleanSupplier;

@Mixin(ServerLevel.class)
public abstract class ServerWorldMixin implements ServerWorldDuck {

    @Unique
    private final Collection<BlockPos> affectedBlockPositions = new ArrayList<>();

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/clock/ServerClockManager;moveToTimeMarker(Lnet/minecraft/core/Holder;Lnet/minecraft/resources/ResourceKey;)Z", ordinal = 0))
    private boolean fastForwardDaytimeHealingModeExplosionsOnNightSkipped(ServerClockManager instance, Holder<WorldClock> clock, ResourceKey<ClockTimeMarker> timeMarker, Operation<Boolean> original, @Local(argsOnly = true) BooleanSupplier shouldKeepTicking) {
        boolean moved = original.call(instance, clock, timeMarker);
        DaylightCycleEvents.ON_NIGHT_SKIPPED.invoker().onNightSkipped(((ServerLevel) (Object) this), shouldKeepTicking);
        return moved;
    }

    @WrapOperation(method = "explode", at = @At(value = "NEW", target = "(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/world/level/ExplosionDamageCalculator;Lnet/minecraft/world/phys/Vec3;FZLnet/minecraft/world/level/Explosion$BlockInteraction;)Lnet/minecraft/world/level/ServerExplosion;"))
    private ServerExplosion attachExplosionInteractionToExplosion(ServerLevel world, Entity entity, DamageSource damageSource, ExplosionDamageCalculator behavior, Vec3 pos, float power, boolean createFire, Explosion.BlockInteraction destructionType, Operation<ServerExplosion> original, @Local(argsOnly = true) Level.ExplosionInteraction explosionSourceType) {
        ServerExplosion explosion = original.call(world, entity, damageSource, behavior, pos, power, createFire, destructionType);
        ((ExplosionImplDuck) explosion).creeperhealing$setExplosionInteraction(explosionSourceType);
        return explosion;
    }

    @Override
    public void creeperhealing$addAffectedPositions(Collection<BlockPos> affectedPositions) {
        this.affectedBlockPositions.addAll(affectedPositions);
    }

    @Override
    public void creeperhealing$clearAffectedPositions() {
        this.affectedBlockPositions.clear();
    }

    @Override
    public boolean creeperhealing$isAffectedPosition(BlockPos pos) {
        return this.affectedBlockPositions.contains(pos);
    }

}


package xd.arkosammy.creeperhealing.mixin;

import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xd.arkosammy.creeperhealing.util.callbacks.DaylightCycleEvents;

import java.util.function.BooleanSupplier;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {

    @Inject(method = "tick", at=@At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;setTimeOfDay(J)V", shift = At.Shift.AFTER, ordinal = 0))
    private void fastForwardDaytimeHealingModeExplosionsOnNightSkipped(BooleanSupplier shouldKeepTicking, CallbackInfo ci){
        DaylightCycleEvents.ON_NIGHT_SKIPPED.invoker().onNightSkipped(((ServerWorld) (Object) this), shouldKeepTicking);
    }

}


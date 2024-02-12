package xd.arkosammy.creeperhealing.mixin;

import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xd.arkosammy.creeperhealing.explosions.*;
import xd.arkosammy.creeperhealing.util.ExplosionManager;

import java.util.function.BooleanSupplier;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {

    // Start healing DaytimeExplosionEvents when the night is skipped
    @Inject(method = "tick", at=@At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;setTimeOfDay(J)V", shift = At.Shift.AFTER, ordinal = 0))
    private void fastForwardDaytimeHealingModeExplosionsOnNightSkipped(BooleanSupplier shouldKeepTicking, CallbackInfo ci){
        for(AbstractExplosionEvent explosionEvent : ExplosionManager.getInstance().getExplosionEvents()){
            if(explosionEvent instanceof DaytimeExplosionEvent){
                explosionEvent.setHealTimer(1);
            }
        }
    }

}


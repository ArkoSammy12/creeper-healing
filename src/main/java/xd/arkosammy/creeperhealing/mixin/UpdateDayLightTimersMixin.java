package xd.arkosammy.creeperhealing.mixin;

import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xd.arkosammy.creeperhealing.explosions.ExplosionEvent;
import xd.arkosammy.creeperhealing.explosions.ExplosionHealingMode;
import xd.arkosammy.creeperhealing.handlers.ExplosionListHandler;
import java.util.function.BooleanSupplier;

@Mixin(ServerWorld.class)
public abstract class UpdateDayLightTimersMixin {

    @Inject(method = "tick", at=@At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;setTimeOfDay(J)V", shift = At.Shift.AFTER, ordinal = 0))
    private void fastForwardDaytimeHealingModeExplosionsOnNightSkipped(BooleanSupplier shouldKeepTicking, CallbackInfo ci){
        for(ExplosionEvent explosionEvent : ExplosionListHandler.getExplosionEventList()){
            if(explosionEvent.getExplosionMode() == ExplosionHealingMode.DAYTIME_HEALING_MODE){
                explosionEvent.setExplosionTimer(-1);
            }
        }
    }

}


package xd.arkosammy.mixin;


import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xd.arkosammy.events.CreeperExplosionEvent;
import xd.arkosammy.handlers.ExplosionHealerHandler;

import java.util.function.BooleanSupplier;

@Mixin(ServerWorld.class)
public abstract class UpdateDayLightTimersMixin {

    @Inject(method = "tick", at=@At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;setTimeOfDay(J)V", shift = At.Shift.AFTER, ordinal = 0))
    private void onNightSkipped(BooleanSupplier shouldKeepTicking, CallbackInfo ci){

        for(CreeperExplosionEvent creeperExplosionEvent : ExplosionHealerHandler.getExplosionEventList()){

            if(creeperExplosionEvent.isMarkedWithDayTimeHealingMode()){

                creeperExplosionEvent.setCreeperExplosionTimer(-1);

            }

        }

    }

}

package xd.arkosammy.creeperhealing.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.server.command.TimeCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xd.arkosammy.creeperhealing.explosions.AbstractExplosionEvent;
import xd.arkosammy.creeperhealing.explosions.DaytimeExplosionEvent;
import xd.arkosammy.creeperhealing.util.ExplosionManager;

@Mixin(TimeCommand.class)
public abstract class TimeCommandMixin {

    // Recalculate DaytimeExplosionEvents' timers when ticks are added or set
    @ModifyReturnValue(method = "executeAdd", at = @At("RETURN"))
    private static int onTimeAdd(int original){
        for(AbstractExplosionEvent explosionEvent : ExplosionManager.getInstance().getExplosionEvents()){
            if(explosionEvent instanceof DaytimeExplosionEvent && explosionEvent.getHealTimer() > 0){
                explosionEvent.setHealTimer(24000 - original);
            }
        }
        return original;
    }

    @ModifyReturnValue(method = "executeSet", at = @At("RETURN"))
    private static int onTimeSet(int original){
        for(AbstractExplosionEvent explosionEvent : ExplosionManager.getInstance().getExplosionEvents()){
            if(explosionEvent instanceof DaytimeExplosionEvent && explosionEvent.getHealTimer() > 0){
                explosionEvent.setHealTimer(24000 - original);
            }
        }
        return original;
    }

}

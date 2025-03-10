package io.github.arkosammy12.creeperhealing.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.TimeCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import io.github.arkosammy12.creeperhealing.util.callbacks.TimeCommandCallbacks;

@Mixin(TimeCommand.class)
public abstract class TimeCommandMixin {

    @ModifyReturnValue(method = "executeAdd", at = @At("RETURN"))
    private static int onTimeAdd(int original, ServerCommandSource serverCommandSource, int time) {
        TimeCommandCallbacks.ON_TIME_EXECUTE_ADD.invoker().onTimeExecuteAdd(serverCommandSource, time, original);
        return original;
    }

    @ModifyReturnValue(method = "executeSet", at = @At("RETURN"))
    private static int onTimeSet(int original, ServerCommandSource serverCommandSource, int time) {
        TimeCommandCallbacks.ON_TIME_EXECUTE_SET.invoker().onTimeExecuteSet(serverCommandSource, time, original);
        return original;
    }

}

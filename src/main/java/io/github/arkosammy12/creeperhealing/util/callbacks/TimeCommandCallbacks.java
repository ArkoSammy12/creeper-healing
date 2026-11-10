package io.github.arkosammy12.creeperhealing.util.callbacks;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.commands.CommandSourceStack;

public interface TimeCommandCallbacks {

    Event<OnTimeExecuteAdd> ON_TIME_EXECUTE_ADD = EventFactory.createArrayBacked(OnTimeExecuteAdd.class,
            (listeners) -> ((serverCommandSource, time, newTime) -> {
                for (OnTimeExecuteAdd listener : listeners) {
                    listener.onTimeExecuteAdd(serverCommandSource, time, newTime);
                }
            }));

    Event<OnTimeExecuteSet> ON_TIME_EXECUTE_SET = EventFactory.createArrayBacked(OnTimeExecuteSet.class,
            (listeners) -> ((serverCommandSource, time, newTime) -> {
                for (OnTimeExecuteSet listener : listeners) {
                    listener.onTimeExecuteSet(serverCommandSource, time, newTime);
                }
            }));

    interface OnTimeExecuteAdd {
        void onTimeExecuteAdd(CommandSourceStack serverCommandSource, int time, int newTime);
    }

    interface OnTimeExecuteSet {
        void onTimeExecuteSet(CommandSourceStack serverCommandSource, int time, int newTime);
    }

}

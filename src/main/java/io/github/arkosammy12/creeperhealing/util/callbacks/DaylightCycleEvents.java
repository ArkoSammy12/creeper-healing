package io.github.arkosammy12.creeperhealing.util.callbacks;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerLevel;

import java.util.function.BooleanSupplier;

public interface DaylightCycleEvents {

    Event<NightSkipped> ON_NIGHT_SKIPPED = EventFactory.createArrayBacked(NightSkipped.class,
            (listeners) -> (world, shouldKeepTicking) -> {
                for (NightSkipped listener : listeners) {
                    listener.onNightSkipped(world, shouldKeepTicking);
                }
            });

    interface NightSkipped {
        void onNightSkipped(ServerLevel world, BooleanSupplier shouldKeepTicking);
    }

}

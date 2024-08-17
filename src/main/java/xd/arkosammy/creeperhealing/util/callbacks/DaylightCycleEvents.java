package xd.arkosammy.creeperhealing.util.callbacks;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.world.ServerWorld;

import java.util.function.BooleanSupplier;

public interface DaylightCycleEvents {

    Event<NightSkipped> ON_NIGHT_SKIPPED = EventFactory.createArrayBacked(NightSkipped.class,
            (listeners) -> (world, shouldKeepTicking) -> {
                for (NightSkipped listener : listeners) {
                    listener.onNightSkipped(world, shouldKeepTicking);
                }
            });

    interface NightSkipped {
        void onNightSkipped(ServerWorld world, BooleanSupplier shouldKeepTicking);
    }

}

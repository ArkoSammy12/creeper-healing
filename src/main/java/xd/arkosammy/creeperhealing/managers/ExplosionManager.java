package xd.arkosammy.creeperhealing.managers;

import net.minecraft.server.MinecraftServer;
import xd.arkosammy.creeperhealing.explosions.ExplosionEvent;
import xd.arkosammy.creeperhealing.explosions.factories.ExplosionEventFactory;

import java.util.stream.Stream;

public interface ExplosionManager {

    void tick(MinecraftServer server);

    Stream<ExplosionEvent> getExplosionEvents();

    <T extends ExplosionEvent> void addExplosionEvent(ExplosionEventFactory<T> explosionEventFactory);

    default void onServerStarting(MinecraftServer server) {
        this.readExplosionEvents(server);
    }

    default void onServerStopping(MinecraftServer server) {
        this.storeExplosionEvents(server);
    }

    void storeExplosionEvents(MinecraftServer server);

    void readExplosionEvents(MinecraftServer server);

}

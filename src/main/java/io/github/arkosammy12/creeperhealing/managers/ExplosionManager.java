package io.github.arkosammy12.creeperhealing.managers;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import io.github.arkosammy12.creeperhealing.explosions.ExplosionEvent;
import io.github.arkosammy12.creeperhealing.explosions.factories.ExplosionEventFactory;
import io.github.arkosammy12.creeperhealing.util.ExplosionContext;

import java.util.function.Function;
import java.util.stream.Stream;

public interface ExplosionManager {

    Identifier getId();

    Function<ExplosionContext, ExplosionEventFactory<?>> getExplosionContextToEventFactoryFunction();

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

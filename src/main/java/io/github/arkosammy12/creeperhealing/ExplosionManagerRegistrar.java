package io.github.arkosammy12.creeperhealing;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import io.github.arkosammy12.creeperhealing.explosions.factories.ExplosionEventFactory;
import io.github.arkosammy12.creeperhealing.managers.ExplosionManager;
import io.github.arkosammy12.creeperhealing.util.ExplosionContext;

import java.util.*;

public final class ExplosionManagerRegistrar {

    private static ExplosionManagerRegistrar instance;

    private final List<ExplosionManager> explosionManagers = new ArrayList<>();

    public static ExplosionManagerRegistrar getInstance() {
        if (instance == null) {
            instance = new ExplosionManagerRegistrar();
        }
        return instance;
    }

    /**
     * Registers a new {@link ExplosionManager}. Registered {@link ExplosionManager}s will receive emitted instances of {@link ExplosionContext} for Explosion Managers to process based on the provided id.
     * This registrar will also invoke {@link ExplosionManager#onServerStarting} and {@link ExplosionManager#onServerStopping} on the registered Explosion Managers automatically.
     *
     * @param explosionManager The {@link ExplosionManager} to register.
     * @throws IllegalArgumentException if an {@link ExplosionManager} with an ID matching that of the passed in Explosion Manager is already registered.
     */
    public void registerExplosionManager(ExplosionManager explosionManager) {
        synchronized (this.explosionManagers) {
            Identifier explosionManagerId = explosionManager.getId();
            for (ExplosionManager manager : this.explosionManagers) {
                if (manager.getId().equals(explosionManagerId)) {
                    throw new IllegalArgumentException("Cannot register ExplosionManager with ID \"%s\" as an ExplosionManager with that ID has already been registered!".formatted(explosionManagerId));
                }
            }
            this.explosionManagers.add(explosionManager);
        }
    }

    public void emitExplosionContext(Identifier explosionManagerId, ExplosionContext explosionContext) {
        synchronized (this.explosionManagers) {
            for (ExplosionManager explosionManager : this.explosionManagers) {
                if (!explosionManager.getId().equals(explosionManagerId)) {
                    continue;
                }
                ExplosionEventFactory<?> explosionEventFactory = explosionManager.getExplosionContextToEventFactoryFunction().apply(explosionContext);
                explosionManager.addExplosionEvent(explosionEventFactory);
                return;
            }
            CreeperHealing.LOGGER.warn("Found no ExplosionManager with ID {}. An explosion will not be healed", explosionManagerId);
        }
    }

    void invokeOnServerStarting(MinecraftServer server) {
        synchronized (this.explosionManagers) {
            for (ExplosionManager explosionManager : this.explosionManagers) {
                explosionManager.onServerStarting(server);
            }
        }
    }

    void invokeOnServerStopping(MinecraftServer server) {
        synchronized (this.explosionManagers) {
            for (ExplosionManager explosionManager : this.explosionManagers) {
                explosionManager.onServerStopping(server);
            }
        }
    }

}

package xd.arkosammy.creeperhealing;
import net.minecraft.server.MinecraftServer;
import xd.arkosammy.creeperhealing.explosions.factories.ExplosionEventFactory;
import xd.arkosammy.creeperhealing.managers.ExplosionManager;
import xd.arkosammy.creeperhealing.util.ExplosionContext;

import java.util.*;
import java.util.stream.Collectors;

public final class ExplosionManagerRegistrar {

    private static ExplosionManagerRegistrar instance;

    private final List<ExplosionManager> explosionManagers = new ArrayList<>();

    public static ExplosionManagerRegistrar getInstance() {
        if (instance == null) {
            instance = new ExplosionManagerRegistrar();
        }
        return instance;
    }


    public void registerExplosionManager(ExplosionManager explosionManager) {
        synchronized (this.explosionManagers) {
            this.explosionManagers.add(explosionManager);
        }
    }

    public void emitExplosionContext(ExplosionContext explosionContext) {
        synchronized (this.explosionManagers) {
            List<ExplosionManager> idleManagers = this.explosionManagers.stream().filter(explosionManager -> explosionManager.getExplosionEvents().findAny().isEmpty()).collect(Collectors.toList());
            if (!idleManagers.isEmpty()) {
                Collections.shuffle(idleManagers);
                for (ExplosionManager idleManager : idleManagers) {
                    ExplosionEventFactory<?> explosionEventFactory = idleManager.getExplosionContextToEventFactoryFunction().apply(explosionContext);
                    if (!explosionEventFactory.shouldHealExplosion()) {
                        continue;
                    }
                    idleManager.addExplosionEvent(explosionEventFactory);
                    return;
                }
            }
            Collections.shuffle(this.explosionManagers);
            for (ExplosionManager explosionManager : this.explosionManagers) {
                ExplosionEventFactory<?> explosionEventFactory = explosionManager.getExplosionContextToEventFactoryFunction().apply(explosionContext);
                if (!explosionEventFactory.shouldHealExplosion()) {
                    continue;
                }
                explosionManager.addExplosionEvent(explosionEventFactory);
                return;
            }

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

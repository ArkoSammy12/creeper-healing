package io.github.arkosammy12.creeperhealing;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.arkosammy12.creeperhealing.config.ConfigSettings;
import io.github.arkosammy12.creeperhealing.config.SettingGroups;
import io.github.arkosammy12.creeperhealing.explosions.DefaultSerializedExplosion;
import io.github.arkosammy12.creeperhealing.managers.DefaultExplosionManager;
import io.github.arkosammy12.creeperhealing.util.Events;
import xd.arkosammy.monkeyconfig.managers.ConfigManager;
import xd.arkosammy.monkeyconfig.managers.TomlConfigManager;
import xd.arkosammy.monkeyconfig.registrars.DefaultConfigRegistrar;

public class CreeperHealing implements ModInitializer {

    public static final String MOD_ID = "creeperhealing";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final DefaultExplosionManager EXPLOSION_MANAGER = new DefaultExplosionManager(DefaultSerializedExplosion.CODEC);
    public static final ConfigManager CONFIG_MANAGER = new TomlConfigManager("creeper-healing", SettingGroups.getSettingGroups(), ConfigSettings.getSettingBuilders());

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(CreeperHealing::onServerStarting);
        ServerLifecycleEvents.SERVER_STOPPING.register(CreeperHealing::onServerStopping);
        DefaultConfigRegistrar.INSTANCE.registerConfigManager(CONFIG_MANAGER);
        Events.registerEvents();
        LOGGER.info("I will try my best to heal your explosions :)");
    }

    private static void onServerStarting(MinecraftServer server) {
        ExplosionManagerRegistrar.getInstance().invokeOnServerStarting(server);
    }

    private static void onServerStopping(MinecraftServer server) {
        ExplosionManagerRegistrar.getInstance().invokeOnServerStopping(server);
    }

}
package xd.arkosammy.creeperhealing;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xd.arkosammy.creeperhealing.config.ConfigSettings;
import xd.arkosammy.creeperhealing.config.SettingGroups;
import xd.arkosammy.creeperhealing.util.ExplosionManager;
import xd.arkosammy.monkeyconfig.managers.ConfigManager;
import xd.arkosammy.monkeyconfig.managers.TomlConfigManager;
import xd.arkosammy.monkeyconfig.registrars.DefaultConfigRegistrar;

public class CreeperHealing implements ModInitializer {

	public static final String MOD_ID = "creeper-healing";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final ConfigManager CONFIG_MANAGER = new TomlConfigManager(MOD_ID, SettingGroups.getSettingGroups(), ConfigSettings.getSettingBuilders());

	@Override
	public void onInitialize() {
		DefaultConfigRegistrar.INSTANCE.registerConfigManager(CONFIG_MANAGER);
		ServerLifecycleEvents.SERVER_STARTING.register(CreeperHealing::onServerStarting);
		ServerLifecycleEvents.SERVER_STOPPING.register(CreeperHealing::onServerStopping);
		ServerTickEvents.END_SERVER_TICK.register(server -> ExplosionManager.getInstance().tick(server));
		LOGGER.info("I will try my best to heal your explosions :)");

	}

	private static void onServerStarting(MinecraftServer server) {
		ExplosionManager.getInstance().readExplosionEvents(server);
		ExplosionManager.getInstance().updateAffectedBlocksTimers();
	}

	private static void onServerStopping(MinecraftServer server) {
		ExplosionManager.getInstance().storeExplosions(server);
		ExplosionManager.getInstance().getExplosionEvents().clear();
	}

}
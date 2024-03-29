package xd.arkosammy.creeperhealing;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xd.arkosammy.creeperhealing.config.ConfigManager;
import xd.arkosammy.creeperhealing.util.ExplosionManager;
import xd.arkosammy.creeperhealing.commands.CommandManager;

public class CreeperHealing implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("Creeper-Healing");

	// TODO: Remake entire config system somehow. Make it more generic?
	@Override
	public void onInitialize() {
		ConfigManager.init();
		ServerLifecycleEvents.SERVER_STARTING.register(CreeperHealing::onServerStarting);
		ServerLifecycleEvents.SERVER_STOPPING.register(CreeperHealing::onServerStopping);
		ServerTickEvents.END_SERVER_TICK.register(server -> ExplosionManager.getInstance().tick(server));
		CommandRegistrationCallback.EVENT.register(CommandManager::registerCommands);
		LOGGER.info("I will try my best to heal your explosions :)");
	}

	private static void onServerStarting(MinecraftServer server) {
		ExplosionManager.getInstance().readExplosionEvents(server);
		ExplosionManager.getInstance().updateAffectedBlocksTimers();
	}

	private static void onServerStopping(MinecraftServer server) {
		ExplosionManager.getInstance().storeExplosions(server);
		ExplosionManager.getInstance().getExplosionEvents().clear();
		ConfigManager.updateConfigFile();
	}

}
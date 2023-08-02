package xd.arkosammy;

import net.fabricmc.api.DedicatedServerModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xd.arkosammy.handlers.ExplosionHealerHandler;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static xd.arkosammy.handlers.ExplosionHealerHandler.explosionExecutorService;

public class CreeperHealing implements DedicatedServerModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("creeper-healing");

	public static final Config CONFIG = new Config();

	@Override
	public void onInitializeServer() {

		LOGGER.info("I will try my best to heal your creeper explosions :)");

		ExplosionHealerHandler explosionHealerHandler = new ExplosionHealerHandler();

		//Initialize config
		try {

			tryInitConfig();

		} catch (IOException e) {

			throw new RuntimeException(e);

		}

		//LOGGER.info(String.valueOf(CONFIG.blockPlacementDelay));
		//LOGGER.info(String.valueOf(CONFIG.explosionHealDelay));

		//Register our TickHandler server initialization
		explosionHealerHandler.registerTickEventHandler();

		//Make sure to stop this ScheduleTickHandler upon server shutdown
		ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStopping);

	}

	private void tryInitConfig() throws IOException {

		File file = new File(FabricLoader.getInstance().getConfigDir() + "/creeper-healing.json");

		CONFIG.replaceList = new HashMap<>();

		if(!file.exists()){

			CONFIG.replaceList.put("minecraft:diamond_block", "minecraft_stone");
			CONFIG.writeConfig(file);

			LOGGER.error("Config not found. Generating a new one at " + file.getAbsolutePath());
			LOGGER.error("Edit the default config and restart the serve to apply changes");

		} else {

			CONFIG.readConfig(file);
			CONFIG.readCustomBlockReplacements(file);
			ExplosionHealerHandler.setExplosionDelay(CONFIG.explosionHealDelay);
			ExplosionHealerHandler.setBlockPlacementDelayTicks(CONFIG.blockPlacementDelay);

			LOGGER.info("Applied custom configs");

		}

	}

	private void onServerStopping(MinecraftServer server){

		explosionExecutorService.shutdown(); // Initiate a graceful shutdown

		try {

			// Wait for the executorService to terminate or timeout after 10 seconds

			if (!explosionExecutorService.awaitTermination(10, TimeUnit.SECONDS)) {

				explosionExecutorService.shutdownNow(); // If it doesn't terminate in 10 seconds, force shutdown

			}

		} catch (InterruptedException e) {

			explosionExecutorService.shutdownNow(); // Thread interrupted, force shutdown

		}
	}

}
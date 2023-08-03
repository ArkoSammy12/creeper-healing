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
import java.util.concurrent.TimeUnit;

import static xd.arkosammy.handlers.ExplosionHealerHandler.explosionExecutorService;

public class CreeperHealing implements DedicatedServerModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("creeper-healing");

	public static final Config CONFIG = new Config();

	@Override
	public void onInitializeServer() {

		LOGGER.info("I will try my best to heal your creeper explosions :)\nThanks to @sulpherstaer for the idea, and thanks to @_jacg for the help with the config setup\n");

		ExplosionHealerHandler explosionHealerHandler = new ExplosionHealerHandler();

		//Initialize config
		try {

			tryInitConfig();

		} catch (IOException e) {

			throw new RuntimeException(e);

		}

		//Register our ServerTickEvent on server initialization
		explosionHealerHandler.registerTickEventHandler();

		//Make sure to stop this ServerTickEvent upon server shutdown
		ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStopping);

	}

	private void tryInitConfig() throws IOException {

		File file = new File(FabricLoader.getInstance().getConfigDir() + "/creeper-healing.json");


		//writeConfig() will return false if the config doesn't already exist, in which case we can read the data from the already present one
		if(!CONFIG.writeConfig(CONFIG)){

			CONFIG.readConfig(file);
			ExplosionHealerHandler.setExplosionDelay(CONFIG.explosionHealDelay);
			ExplosionHealerHandler.setBlockPlacementDelayTicks(CONFIG.blockPlacementDelay);
			ExplosionHealerHandler.setCustomReplaceList(CONFIG.replaceMap);

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
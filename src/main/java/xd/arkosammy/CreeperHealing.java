package xd.arkosammy;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xd.arkosammy.events.CreeperExplosionEvent;
import xd.arkosammy.handlers.ExplosionHealerHandler;
import xd.arkosammy.util.ScheduledCreeperExplosions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import static xd.arkosammy.handlers.ExplosionHealerHandler.explosionExecutorService;

public class CreeperHealing implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("creeper-healing");
	public static final Config CONFIG = new Config();
	private static final ExplosionHealerHandler explosionHealerHandler = new ExplosionHealerHandler();

	public static ScheduledCreeperExplosions SCHEDULED_CREEPER_EXPLOSIONS;

	@Override
	public void onInitialize() {

		//Initialize config
		try {

			tryInitConfig();

		} catch (IOException e) {

			throw new RuntimeException(e);

		}

		//Register a new ServerTickEvent upon server/world starting
		ServerLifecycleEvents.SERVER_STARTING.register(server -> {

			try {
				onServerStarting(server);

			} catch (IOException e) {

				throw new RuntimeException(e);

			}

		});

		//Read the list of CreeperExplosionEvents stored in our file upon server fully started.
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {

			try {

				onServerStarted(server);

			} catch (IOException e) {

				throw new RuntimeException(e);

			}

		});

		//Stop our TickEventHandler and store any to-be healed or currently ongoing CreeperExplosionEvents to a file upon server shutdown
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {

			try {
				onServerStopping(server);

			} catch (IOException e) {

				throw new RuntimeException(e);

			}

		});

		LOGGER.info("I will try my best to heal your creeper explosions :).Thanks to @sulpherstaer for the idea and inspiration, and thanks to @_jacg for the help with the config setup");

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

	private void onServerStarting(MinecraftServer server) throws IOException {

		List<CreeperExplosionEvent> list = new ArrayList<>();

		SCHEDULED_CREEPER_EXPLOSIONS = new ScheduledCreeperExplosions(list);

		explosionHealerHandler.registerTickEventHandler(server);

	}

	private void onServerStarted(MinecraftServer server) throws IOException {

		//ScheduledCreeperExplosions.reScheduleCreeperExplosionEvents(server);

	}

	private void onServerStopping(MinecraftServer server) throws IOException {

		//SCHEDULED_CREEPER_EXPLOSIONS.storeBlockPlacements(server);


	}

}
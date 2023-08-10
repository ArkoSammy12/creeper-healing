package xd.arkosammy;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xd.arkosammy.events.CreeperExplosionEvent;
import xd.arkosammy.handlers.ExplosionHealerHandler;
import xd.arkosammy.util.ScheduledCreeperExplosions;
import java.io.File;
import java.io.IOException;

public class CreeperHealing implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("creeper-healing");
	public static final Config CONFIG = new Config();

	@Override
	public void onInitialize() {

		//Initialize config
		try {

			tryInitConfig();

		} catch (IOException e) {

			throw new RuntimeException(e);

		}

		//Read the list of CreeperExplosionEvents stored in our file upon server fully started.
		ServerLifecycleEvents.SERVER_STARTING.register(server -> {

			try {

				onServerStarting(server);

			} catch (IOException e) {

				throw new RuntimeException(e);

			}

		});

		//Register a new ServerTickEvent upon server/world starting
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {

			try {

				onServerStarted();

			} catch (IOException e) {

				throw new RuntimeException(e);

			}

		});

		//Grab our current list of CreeperExplosionEvents and store it
		ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStopping);

		LOGGER.info("I will try my best to heal your creeper explosions :)");
		LOGGER.info("Thanks to @sulpherstaer for the idea and inspiration, @_jacg for the help with the config setup, and @dale8689 for the help with improving the mod");
	}

	private void tryInitConfig() throws IOException {

		File file = new File(FabricLoader.getInstance().getConfigDir() + "/creeper-healing.json");

		//writeConfig() will return false if the config file already exists,
		// in which case we can read the data from the already present one
		if(!CONFIG.writeConfig(CONFIG)){

			CONFIG.readConfig(file);
			ExplosionHealerHandler.setExplosionDelay(CONFIG.explosionHealDelay);
			ExplosionHealerHandler.setBlockPlacementDelayTicks(CONFIG.blockPlacementDelay);
			ExplosionHealerHandler.setCustomReplaceList(CONFIG.replaceMap);

			LOGGER.info("Applied custom configs");

		}

	}

	private void onServerStarting(MinecraftServer server) throws IOException {

		//Read the contents of our scheduled-explosions.json file and them to the queue, before starting the tick event
		ScheduledCreeperExplosions.reScheduleCreeperExplosionEvents(server);

	}

	private void onServerStarted() throws IOException {

		//Start listening for queued CreeperExplosionEvents upon server fully started
		ServerTickEvents.END_SERVER_TICK.register(ExplosionHealerHandler::handleExplosionQueue);

	}

	private void onServerStopping(MinecraftServer server) {

		//Make a new ScheduledCreeperExplosions object and pass the current list to the constructor
		ScheduledCreeperExplosions SCHEDULED_CREEPER_EXPLOSIONS = new ScheduledCreeperExplosions(CreeperExplosionEvent.getExplosionEventsForUsage());

		//Encode and store this same object
		SCHEDULED_CREEPER_EXPLOSIONS.storeBlockPlacements(server);


	}

}
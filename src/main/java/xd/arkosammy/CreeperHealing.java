package xd.arkosammy;

import com.electronwill.nightconfig.core.file.FileConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xd.arkosammy.handlers.ExplosionHealerHandler;
import xd.arkosammy.util.Commands;
import xd.arkosammy.util.Config;
import xd.arkosammy.util.ExplosionEventsSerializer;

import java.io.File;
import java.io.IOException;

public class CreeperHealing implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("Creeper-Healing");
	private static boolean healerHandlerLock;
	private static MinecraftServer serverInstance;

	public static FileConfig CONFIG = null;
	public static final File CONFIG_FILE_PATH = new File(FabricLoader.getInstance().getConfigDir() + "/default-creeper-healing-config.toml");


	@Override
	public void onInitialize() {

		//Initialize config
		/*
		try {
			initConfig();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		 */


		//Read the list of CreeperExplosionEvents stored in our file once the server has fully started
		ServerLifecycleEvents.SERVER_STARTING.register(server -> {

			try {
				onServerStarting(server);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

		});

		//Grab our current list of CreeperExplosionEvents and store it.
		//Update the config file with new values changed via commands
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {

			try {
				onServerStopping(server);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

		});

		//Start listening for CreeperExplosionEvents in our list once we have read the config
		ServerTickEvents.END_SERVER_TICK.register(ExplosionHealerHandler::handleExplosionEventList);


		//Register our commands
		CommandRegistrationCallback.EVENT.register(Commands::registerCommands);

		LOGGER.info("I will try my best to heal your creeper explosions :)");

	}

	private void initConfig() {

		if(CONFIG_FILE_PATH.exists()){

			Config.readConfig();


		} else {

			CreeperHealing.LOGGER.info("Config file not found. Creating new one");
			Config.writeFreshConfig();

		}

		CONFIG.save();

	}

	private void onServerStarting(MinecraftServer server) throws IOException {

		CONFIG = FileConfig.builder(new File(FabricLoader.getInstance().getConfigDir() + "/creeper-healing.toml"), TomlFormat.instance())
				.autosave()
				.preserveInsertionOrder()
				.concurrent()
				.build();

		CONFIG.load();

		initConfig();

		//Capture the server instance
		serverInstance = server;

		//Read the contents of our scheduled-explosions.json file and add them to the list
		ExplosionEventsSerializer.reScheduleCreeperExplosionEvents(server);

		//We can now start listening for explosions in the list
		setHealerHandlerLock(true);

		//ExplosionHealerHandler.updateExplosionTimers();
		ExplosionHealerHandler.updateAffectedBlocksTimers();

	}

	private void onServerStopping(MinecraftServer server) throws IOException {

		//Reset the flag
		setHealerHandlerLock(false);

		//Make a new ExplosionEventsSerializer object and pass the current list to the constructor, then store it
		ExplosionEventsSerializer explosionEventsSerializer = new ExplosionEventsSerializer(ExplosionHealerHandler.getExplosionEventList());
		explosionEventsSerializer.storeBlockPlacements(server);

		//Once we have stored the list, clear the current list from memory
		ExplosionHealerHandler.getExplosionEventList().clear();

		//Update the config by overriding the current values with new ones obtained via commands
		Config.updateConfig();

		CONFIG.close();

	}

	//Return whether we are allowed to handle explosion events.
	// Security lock for avoiding any potential issues with concurrency
	public static boolean isExplosionHandlingUnlocked(){
		return healerHandlerLock;
	}

	public static void setHealerHandlerLock(boolean locked){
		healerHandlerLock = locked;
	}

	public static MinecraftServer getServerInstance(){
		return serverInstance;
	}

}
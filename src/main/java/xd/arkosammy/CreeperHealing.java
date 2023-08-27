package xd.arkosammy;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerChunkManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xd.arkosammy.events.CreeperExplosionEvent;
import xd.arkosammy.handlers.ExplosionHealerHandler;
import xd.arkosammy.util.Commands;
import xd.arkosammy.util.Config;
import xd.arkosammy.util.ScheduledCreeperExplosions;
import java.io.File;
import java.io.IOException;

public class CreeperHealing implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("Creeper-Healing");
	public static final Config CONFIG = new Config();
	private static boolean hasReadConfig;

	@Override
	public void onInitialize() {

		//Initialize config
		try {
			initConfig();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

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

		LOGGER.info("I will try my best to heal your creeper explosions :)");
		LOGGER.info("Thanks to @sulpherstaer for the idea and inspiration, @_jacg for the help with the config setup, and @dale8689 for the help with improving the mod");

		//Start listening for CreeperExplosionEvents in our list once we have read the config
		ServerTickEvents.END_SERVER_TICK.register(ExplosionHealerHandler::handleExplosionQueue);

		//Register our commands
		Commands.registerCommands();

	}

	private void initConfig() throws IOException {

		File file = new File(FabricLoader.getInstance().getConfigDir() + "/creeper-healing.json");

		//writeConfig() will return false if the config file already exists,
		//in which case we can read the data from the already present one
		if(!CONFIG.writeConfig()){

			CONFIG.readConfig(file);
			LOGGER.info("Applied custom configs");

		}

	}

	private void onServerStarting(MinecraftServer server) throws IOException {

		//Read the contents of our scheduled-explosions.json file and add them to the list
		ScheduledCreeperExplosions.reScheduleCreeperExplosionEvents(server);

		//We can now start listening for explosions in the list
		setHasReadConfig(true);

	}

	private void onServerStopping(MinecraftServer server) throws IOException {

		//Reset the flag
		setHasReadConfig(false);

		//Make a new ScheduledCreeperExplosions object and pass the current list to the constructor
		ScheduledCreeperExplosions scheduledCreeperExplosions = new ScheduledCreeperExplosions(CreeperExplosionEvent.getExplosionEventsForUsage());

		//Store this same object
		scheduledCreeperExplosions.storeBlockPlacements(server);

		//Once we have stored the list, clear the current list from memory
		CreeperExplosionEvent.getExplosionEventsForUsage().clear();

		//Update the config by overriding the current values with new ones obtained via commands
		File file = new File(FabricLoader.getInstance().getConfigDir() + "/creeper-healing.json");

		CONFIG.updateConfig(file);

	}

	//Return whether we have read the config already, to allow for ExplosionHealerHandler.handleExplosionQueue() to start iterating through
	//the CreeperExplosionList. Just extra security to avoid any concurrency issues
	public static boolean hasReadConfig(){

		return hasReadConfig;

	}

	public static void setHasReadConfig(boolean val){

		hasReadConfig = val;

	}

}
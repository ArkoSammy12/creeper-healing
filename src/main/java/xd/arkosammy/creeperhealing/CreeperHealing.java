package xd.arkosammy.creeperhealing;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xd.arkosammy.creeperhealing.configuration.CreeperHealingConfig;
import xd.arkosammy.creeperhealing.explosions.ExplosionListCodec;
import xd.arkosammy.creeperhealing.explosions.AffectedBlock;
import xd.arkosammy.creeperhealing.handlers.ExplosionListHandler;
import xd.arkosammy.creeperhealing.commands.HealingCommandManager;

import java.io.IOException;

public class CreeperHealing implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("Creeper-Healing");
	private static boolean healerHandlerLock;

	@Override
	public void onInitialize() {

		CreeperHealingConfig.initializeConfig();

		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			try {
				onServerStarting(server);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});

		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			try {
				onServerStopping(server);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});

		ServerTickEvents.END_SERVER_TICK.register(ExplosionListHandler::handleExplosionList);
		CommandRegistrationCallback.EVENT.register(HealingCommandManager::registerCommands);
		LOGGER.info("I will try my best to heal your explosions :)");
	}

	private static void onServerStarting(MinecraftServer server) throws IOException {
		ExplosionListCodec.rescheduleExplosionEvents(server);
		setHealerHandlerLock(true);
		AffectedBlock.updateAffectedBlocksTimers();
	}

	private static void onServerStopping(MinecraftServer server) throws IOException {
		setHealerHandlerLock(false);
		ExplosionListCodec explosionListCodec = new ExplosionListCodec(ExplosionListHandler.getExplosionEventList());
		explosionListCodec.serializeExplosionEvents(server);
		ExplosionListHandler.getExplosionEventList().clear();
		CreeperHealingConfig.updateConfigFile();
	}

	public static boolean isExplosionHandlingUnlocked(){
		return healerHandlerLock;
	}

	public static void setHealerHandlerLock(boolean locked){
		healerHandlerLock = locked;
	}

}
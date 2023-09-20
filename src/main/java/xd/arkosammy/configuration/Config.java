package xd.arkosammy.configuration;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.GenericBuilder;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import xd.arkosammy.CreeperHealing;
import xd.arkosammy.configuration.tables.*;
import xd.arkosammy.explosions.AffectedBlock;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class Config {

    private Config(){}
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("creeper-healing.toml");
    @Nullable
    private static final GenericBuilder<CommentedConfig, CommentedFileConfig> CONFIG_BUILDER;

    static {

        System.setProperty("nightconfig.preserveInsertionOrder", "true");

        GenericBuilder<CommentedConfig, CommentedFileConfig> builder;

        try{

            builder = CommentedFileConfig.builder(CONFIG_PATH, TomlFormat.instance())
                    .preserveInsertionOrder()
                    .concurrent()
                    .sync();

        } catch (Throwable throwable){

            CreeperHealing.LOGGER.info("Unable to initialize config: {}", throwable.getMessage());
            CreeperHealing.LOGGER.info("The config will be unable to be used.");
            builder = null;

        }

        CONFIG_BUILDER = builder;

    }

    public static void initializeConfig(){

        if(CONFIG_BUILDER != null) {

            try(CommentedFileConfig fileConfig = CONFIG_BUILDER.build()){

                if (!Files.exists(CONFIG_PATH)) {

                    CreeperHealing.LOGGER.warn("Found no preexisting config to load settings from. Creating a new config with default values in " + CONFIG_PATH);
                    CreeperHealing.LOGGER.warn("Change the settings in the config file, then reload the config by using /creeper-healing settings reload, or restart the server.");
                    saveDefaultConfigSettingsToFile(fileConfig);
                    fileConfig.save();

                } else {

                    fileConfig.load();
                    loadConfigSettingsToMemory(fileConfig);
                    updateConfigFile();

                    //Warn the user if these delays were set to 0 or fewer seconds
                    if(Math.round(Math.max(DelaysConfig.getExplosionHealDelayRaw(), 0) * 20L) == 0) CreeperHealing.LOGGER.warn("Explosion heal delay set to a very low value in the config file. A value of 1 second will be used instead. Please set a valid value in the config file");
                    if(Math.round(Math.max(DelaysConfig.getBlockPlacementDelayRaw(), 0) * 20L) == 0) CreeperHealing.LOGGER.warn("Block placement delay set to a very low value in the config file. A value of 1 second will be used instead. Please set a valid value in the config file");
                    CreeperHealing.LOGGER.info("Applied custom config settings");


                }

            }

        }

    }

    public static void updateConfigFile(){

        if(CONFIG_BUILDER != null){

            try(CommentedFileConfig fileConfig = CONFIG_BUILDER.build()){

                if(Files.exists(CONFIG_PATH)){

                    fileConfig.load();
                    ReplaceMapConfig.loadReplaceMapToMemory(fileConfig);
                    saveConfigSettingsToFile(fileConfig);
                    fileConfig.save();

                } else {

                    CreeperHealing.LOGGER.warn("Found no preexisting config to load settings from. Creating a new config with default values in " + CONFIG_PATH);
                    CreeperHealing.LOGGER.warn("Change the settings in the config file, then reload the config by using /creeper-healing settings reload, or restart the server.");

                    saveDefaultConfigSettingsToFile(fileConfig);
                    fileConfig.save();


                }

            }

        }

    }

    public static boolean reloadConfigSettingsInMemory(CommandContext<ServerCommandSource> ctx){

        if(CONFIG_BUILDER != null){

            try(CommentedFileConfig fileConfig = CONFIG_BUILDER.build()){

                if(Files.exists(CONFIG_PATH)){

                    CreeperHealing.setHealerHandlerLock(false);

                    fileConfig.load();
                    loadConfigSettingsToMemory(fileConfig);

                    CreeperHealing.setHealerHandlerLock(true);

                    AffectedBlock.updateAffectedBlocksTimers();

                    //Warn the user if these delays were set to 0 or fewer seconds
                    if(Math.round(Math.max(DelaysConfig.getExplosionHealDelayRaw(), 0) * 20L) == 0) ctx.getSource().sendMessage(Text.literal("Explosion heal delay set to a very low value in the config file. A value of 1 second will be used instead. Please set a valid value in the config file").formatted(Formatting.YELLOW));
                    if(Math.round(Math.max(DelaysConfig.getBlockPlacementDelayRaw(), 0) * 20L) == 0) ctx.getSource().sendMessage(Text.literal("Block placement delay set to a very low value in the config file. A value of 1 second will be used instead. Please set a valid value in the config file").formatted(Formatting.YELLOW));

                    return true;

                } else {

                    return false;

                }

            }

        }

        return false;

    }

    private static void saveDefaultConfigSettingsToFile(CommentedFileConfig fileConfig){

        ModeConfig.saveDefaultSettingsToFile(fileConfig);
        ExplosionSourceConfig.saveDefaultSettingsToFile(fileConfig);
        DelaysConfig.saveDefaultSettingsToFile(fileConfig);
        PreferencesConfig.saveDefaultSettingsToFile(fileConfig);
        ReplaceMapConfig.saveToFileWithDefaultValues(fileConfig);

    }

    private static void saveConfigSettingsToFile(CommentedFileConfig fileConfig){

        ModeConfig.saveSettingsToFile(fileConfig);
        ExplosionSourceConfig.saveSettingsToFile(fileConfig);
        DelaysConfig.saveSettingsToFile(fileConfig);
        PreferencesConfig.saveSettingsToFile(fileConfig);
        ReplaceMapConfig.saveReplaceMapToFile(fileConfig);

    }

    private static void loadConfigSettingsToMemory(CommentedFileConfig fileConfig){

        ModeConfig.loadSettingsToMemory(fileConfig);
        ExplosionSourceConfig.loadSettingsToMemory(fileConfig);
        DelaysConfig.loadSettingsToMemory(fileConfig);
        PreferencesConfig.loadSettingsToMemory(fileConfig);
        ReplaceMapConfig.loadReplaceMapToMemory(fileConfig);

    }


}

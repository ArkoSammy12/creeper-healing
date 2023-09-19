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
import xd.arkosammy.configuration.tables.DelaysConfig;
import xd.arkosammy.configuration.tables.ModeConfig;
import xd.arkosammy.configuration.tables.PreferencesConfig;
import xd.arkosammy.configuration.tables.ReplaceMapConfig;
import xd.arkosammy.explosions.AffectedBlock;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {

    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("creeper-healing.toml");

    private static final CommentedFileConfig TOML_CONFIG = CommentedFileConfig.builder(CONFIG_PATH, TomlFormat.instance())
            .concurrent()
            .preserveInsertionOrder()
            .sync()
            .build();
    
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

                    saveWithDefaults(fileConfig);
                    fileConfig.save();


                } else {

                    fileConfig.load();
                    loadEntryValues(fileConfig);

                }

            }

        }

    }

    public static void updateConfigFile(){

        if(CONFIG_BUILDER != null){

            try(CommentedFileConfig fileConfig = CONFIG_BUILDER.build()){

                if(Files.exists(CONFIG_PATH)){

                    saveConfig(fileConfig);
                    fileConfig.save();

                } else {

                    saveWithDefaults(fileConfig);
                    fileConfig.save();

                }

            }

        }

    }

    public static boolean reloadConfigEntries(CommandContext<ServerCommandSource> ctx){

        if(CONFIG_BUILDER != null){

            try(CommentedFileConfig fileConfig = CONFIG_BUILDER.build()){

                if(Files.exists(CONFIG_PATH)){

                    CreeperHealing.setHealerHandlerLock(false);

                    fileConfig.load();
                    loadEntryValues(fileConfig);

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

    public static void saveWithDefaults(CommentedFileConfig fileConfig){

        ModeConfig.saveDefaultEntries(fileConfig);
        DelaysConfig.saveDefaultEntries(fileConfig);
        PreferencesConfig.saveDefaultEntries(fileConfig);
        ReplaceMapConfig.saveDefaultEntries(fileConfig);

    }

    public static void saveConfig(CommentedFileConfig fileConfig){

        ModeConfig.saveEntries(fileConfig);
        DelaysConfig.saveEntries(fileConfig);
        PreferencesConfig.saveEntries(fileConfig);
        ReplaceMapConfig.saveEntries(fileConfig);

    }

    public static void loadEntryValues(CommentedFileConfig fileConfig){

        ModeConfig.loadEntries(fileConfig);
        DelaysConfig.loadEntries(fileConfig);
        PreferencesConfig.loadEntries(fileConfig);
        ReplaceMapConfig.loadEntries(fileConfig);

    }


}

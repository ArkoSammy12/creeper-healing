package xd.arkosammy.creeperhealing.config;

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
import xd.arkosammy.creeperhealing.CreeperHealing;
import xd.arkosammy.creeperhealing.util.ExplosionManager;

import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigManager {

    private ConfigManager(){}
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

    public static void init(){
        if(CONFIG_BUILDER == null){
            return;
        }
        try (CommentedFileConfig fileConfig = CONFIG_BUILDER.build()) {
            if (Files.exists(CONFIG_PATH)) {
                fileConfig.load();
                getValuesFromConfig(fileConfig);
                updateConfigFile();

                //Warn the user if these delays were set to 0 or fewer seconds
                if (Math.round(Math.max(DelaysConfig.EXPLOSION_HEAL_DELAY.getEntry().getValue(), 0) * 20L) == 0) {
                    CreeperHealing.LOGGER.warn("Explosion heal delay set to a very low value in the config file. A value of 1 second will be used instead. Please set a valid value in the config file");
                }
                if (Math.round(Math.max(DelaysConfig.BLOCK_PLACEMENT_DELAY.getEntry().getValue(), 0) * 20L) == 0) {
                    CreeperHealing.LOGGER.warn("Block placement delay set to a very low value in the config file. A value of 1 second will be used instead. Please set a valid value in the config file");
                }
                CreeperHealing.LOGGER.info("Applied custom config settings");
            } else {
                CreeperHealing.LOGGER.warn("Found no preexisting config to load settings from. Creating a new config with default values in " + CONFIG_PATH);
                CreeperHealing.LOGGER.warn("Change the settings in the config file, then reload the config by using /creeper-healing reload_config, or restart the server.");
                setDefaultValuesToConfig(fileConfig);
                fileConfig.save();
            }
        }
    }

    public static void updateConfigFile(){
        if(CONFIG_BUILDER == null){
            return;
        }
        try (CommentedFileConfig fileConfig = CONFIG_BUILDER.build()) {
            if (Files.exists(CONFIG_PATH)) {
                fileConfig.load();
                ReplaceMapConfig.getValues(fileConfig);
                WhitelistConfig.getValues(fileConfig);
                setValuesToConfig(fileConfig);
                fileConfig.save();
            } else {
                CreeperHealing.LOGGER.warn("Found no preexisting config to load settings from. Creating a new config with default values in " + CONFIG_PATH);
                CreeperHealing.LOGGER.warn("Change the settings in the config file, then reload the config by using /creeper-healing reload_config, or restart the server.");
                setDefaultValuesToConfig(fileConfig);
                fileConfig.save();
            }
        }
    }

    public static boolean reloadValuesFromConfig(CommandContext<ServerCommandSource> ctx){
        if(CONFIG_BUILDER == null){
            return false;
        }
        try (CommentedFileConfig fileConfig = CONFIG_BUILDER.build()) {
            if (Files.exists(CONFIG_PATH)) {
                fileConfig.load();
                getValuesFromConfig(fileConfig);
                ExplosionManager.getInstance().updateAffectedBlocksTimers();

                //Warn the user if these delays were set to 0 or fewer seconds
                if (Math.round(Math.max(DelaysConfig.EXPLOSION_HEAL_DELAY.getEntry().getValue(), 0) * 20L) == 0) {
                    ctx.getSource().sendMessage(Text.literal("Explosion heal delay set to a very low value in the config file. A value of 1 second will be used instead. Please set a valid value in the config file").formatted(Formatting.YELLOW));
                }
                if (Math.round(Math.max(DelaysConfig.BLOCK_PLACEMENT_DELAY.getEntry().getValue(), 0) * 20L) == 0) {
                    ctx.getSource().sendMessage(Text.literal("Block placement delay set to a very low value in the config file. A value of 1 second will be used instead. Please set a valid value in the config file").formatted(Formatting.YELLOW));
                }
                return true;
            } else {
                return false;
            }
        }
    }

    private static void setDefaultValuesToConfig(CommentedFileConfig fileConfig){
        ModeConfig.setDefaultValues(fileConfig);
        ExplosionSourceConfig.setDefaultValues(fileConfig);
        ExplosionItemDropConfig.setDefaultValues(fileConfig);
        DelaysConfig.setDefaultValues(fileConfig);
        PreferencesConfig.setDefaultValues(fileConfig);
        WhitelistConfig.setDefaultValues(fileConfig);
        ReplaceMapConfig.setDefaultValues(fileConfig);
    }

    private static void setValuesToConfig(CommentedFileConfig fileConfig){
        ModeConfig.setValues(fileConfig);
        ExplosionSourceConfig.setValues(fileConfig);
        ExplosionItemDropConfig.setValues(fileConfig);
        DelaysConfig.setValues(fileConfig);
        PreferencesConfig.setValues(fileConfig);
        WhitelistConfig.setValues(fileConfig);
        ReplaceMapConfig.setValues(fileConfig);
    }

    private static void getValuesFromConfig(CommentedFileConfig fileConfig){
        ModeConfig.getValues(fileConfig);
        ExplosionSourceConfig.getValues(fileConfig);
        ExplosionItemDropConfig.getValues(fileConfig);
        DelaysConfig.getValues(fileConfig);
        PreferencesConfig.getValues(fileConfig);
        WhitelistConfig.getValues(fileConfig);
        ReplaceMapConfig.getValues(fileConfig);
    }

}

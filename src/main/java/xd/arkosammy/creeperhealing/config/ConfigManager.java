package xd.arkosammy.creeperhealing.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.GenericBuilder;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.google.common.collect.ImmutableList;
import net.fabricmc.loader.api.FabricLoader;
import xd.arkosammy.creeperhealing.CreeperHealing;
import xd.arkosammy.creeperhealing.config.settings.*;
import xd.arkosammy.creeperhealing.config.tables.ConfigTable;
import xd.arkosammy.creeperhealing.config.tables.ConfigTables;
import xd.arkosammy.creeperhealing.util.ExplosionManager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class ConfigManager {

    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("creeper-healing.toml");
    private static ConfigManager INSTANCE;
    private final GenericBuilder<CommentedConfig, CommentedFileConfig> CONFIG_BUILDER;
    private final List<ConfigTable> configTables;

    private ConfigManager() {
        System.setProperty("nightconfig.preserveInsertionOrder", "true");
        GenericBuilder<CommentedConfig, CommentedFileConfig> builder;
        try {
            builder = CommentedFileConfig.builder(CONFIG_PATH, TomlFormat.instance())
                    .preserveInsertionOrder()
                    .concurrent()
                    .sync();
        } catch (Throwable throwable) {
            builder = null;
            CreeperHealing.LOGGER.error("Unable to initialize config: {}. Config will be unavailable.", throwable.getMessage());
        }
        CONFIG_BUILDER = builder;
        List<ConfigTable> configTableList = ConfigTables.getConfigTables();
        this.checkForSettingNameUniqueness(configTableList);
        this.configTables = ImmutableList.copyOf(configTableList);
    }

    public static ConfigManager getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new ConfigManager();
        }
        return INSTANCE;
    }

    private boolean ifConfigPresent(Function<CommentedFileConfig, Boolean> fileConfigFunction) {
        if(CONFIG_BUILDER == null) {
            return false;
        }
        try(CommentedFileConfig fileConfig = CONFIG_BUILDER.build()) {
            if(fileConfig != null) {
                return fileConfigFunction.apply(fileConfig);
            }
        }
        return false;
    }

    public void init() {
        this.ifConfigPresent(fileConfig -> {
            if(!Files.exists(CONFIG_PATH)) {
                CreeperHealing.LOGGER.warn("Found no preexisting configuration file. Creating a new configuration file with default values in {}", CONFIG_PATH);
                this.createNewConfigFile(fileConfig);
            } else {
                fileConfig.load();
                this.configTables.forEach(table -> table.loadValues(fileConfig));
                this.saveToFile();
                CreeperHealing.LOGGER.info("Found existing configuration file. Loaded values from {}", CONFIG_PATH);
            }
            return true;
        });
    }

    public boolean reloadFromFile() {

        return this.ifConfigPresent(fileConfig -> {
            if(!Files.exists(CONFIG_PATH)) {
                return false;
            }
            fileConfig.load();
            this.configTables.forEach(table -> table.loadValues(fileConfig));
            ExplosionManager.getInstance().updateAffectedBlocksTimers();
            return true;
        });
    }

    public void saveToFile() {
        this.ifConfigPresent(fileConfig -> {
            if(!Files.exists(CONFIG_PATH)) {
                CreeperHealing.LOGGER.warn("Found no preexisting configuration file. Creating a new configuration file with default values in {}", CONFIG_PATH);
                this.createNewConfigFile(fileConfig);
            } else {
                fileConfig.load();
                this.getConfigTable(ConfigTables.REPLACE_MAP_TABLE.getName()).loadValues(fileConfig);
                this.getConfigTable(ConfigTables.WHITELIST_TABLE.getName()).loadValues(fileConfig);
                this.configTables.forEach(table -> table.setValues(fileConfig));
                fileConfig.save();
            }
            return true;
        });
    }

    public ConfigTable getConfigTable(String tableName) {
        for(ConfigTable configTable : this.configTables) {
            if(configTable.getName().equals(tableName)) {
                return configTable;
            }
        }
        throw new IllegalArgumentException("Config table not found: " + tableName);
    }

    public ConfigSetting<?> getSetting(String settingName) {
        for(ConfigTable configTable : this.configTables) {
            for(ConfigSetting<?> setting : configTable.getConfigSettings()) {
                if(setting.getName().equals(settingName)) {
                    return setting;
                }
            }
        }
        throw new IllegalArgumentException("Setting not found in config tables: " + settingName);
    }

    public DoubleSetting getAsDoubleSetting(String settingName) {
        ConfigSetting<?> configSetting = this.getSetting(settingName);
        if(!(configSetting instanceof DoubleSetting doubleSetting)) {
            throw new IllegalArgumentException("Setting is not a double setting: " + settingName);
        }
        return doubleSetting;
    }

    public BooleanSetting getAsBooleanSetting(String settingName) {
        ConfigSetting<?> configSetting = this.getSetting(settingName);
        if(!(configSetting instanceof BooleanSetting booleanSetting)) {
            throw new IllegalArgumentException("Setting is not a boolean setting: " + settingName);
        }
        return booleanSetting;
    }

    public StringSetting getAsStringSetting(String settingName) {
        ConfigSetting<?> configSetting = this.getSetting(settingName);
        if(!(configSetting instanceof StringSetting stringSetting)) {
            throw new IllegalArgumentException("Setting is not a string setting: " + settingName);
        }
        return stringSetting;
    }

    public StringListSetting getAsStringListSetting(String settingName) {
        ConfigSetting<?> configSetting = this.getSetting(settingName);
        if(!(configSetting instanceof StringListSetting stringListSetting)) {
            throw new IllegalArgumentException("Setting is not a string list setting: " + settingName);
        }
        return stringListSetting;
    }

    private void createNewConfigFile(CommentedFileConfig fileConfig) {
        this.configTables.forEach(table -> table.setDefaultValues(fileConfig));
        fileConfig.save();
    }

    private void checkForSettingNameUniqueness(List<ConfigTable> configTables) {
        Set<String> settingNames = new HashSet<>();
        for (ConfigTable table : configTables) {
            for (ConfigSetting<?> setting : table.getConfigSettings()) {
                if (!settingNames.add(setting.getName())) {
                    throw new IllegalArgumentException("Duplicate config setting name found: " + setting.getName());
                }
            }
        }
    }

}

package xd.arkosammy.creeperhealing.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.GenericBuilder;
import com.electronwill.nightconfig.toml.TomlFormat;
import net.fabricmc.loader.api.FabricLoader;
import xd.arkosammy.creeperhealing.CreeperHealing;
import xd.arkosammy.creeperhealing.config.settings.*;
import xd.arkosammy.creeperhealing.config.util.SettingIdentifier;
import xd.arkosammy.creeperhealing.util.ExplosionManager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class ConfigManager {

    private static ConfigManager INSTANCE;
    private final Path configPath;
    private final GenericBuilder<CommentedConfig, CommentedFileConfig> CONFIG_BUILDER;
    private final List<ConfigTable> configTables = new ArrayList<>();
    private boolean isInitialized = false;

    private ConfigManager(String configName) {
        this.configPath = FabricLoader.getInstance().getConfigDir().resolve(configName + ".toml");
        System.setProperty("nightconfig.preserveInsertionOrder", "true");
        GenericBuilder<CommentedConfig, CommentedFileConfig> builder;
        try {
            builder = CommentedFileConfig.builder(this.configPath, TomlFormat.instance())
                    .preserveInsertionOrder()
                    .concurrent()
                    .sync();
        } catch (Throwable throwable) {
            builder = null;
            CreeperHealing.LOGGER.error("Unable to initialize CommentedConfigBuilder: {}. Config will be unavailable.", throwable.getMessage());
        }
        CONFIG_BUILDER = builder;
    }

    public static ConfigManager getInstance() {
        if(INSTANCE == null) {
            throw new IllegalStateException("ConfigManager has not been initialized");
        }
        return INSTANCE;
    }

    public static void init(List<ConfigTable> configTables, List<ConfigSetting.Builder<?, ?>> settingBuilders, String configName) {
        INSTANCE = new ConfigManager(configName);
        getInstance().registerConfigSettings(configTables, settingBuilders);
        getInstance().checkForSettingNameUniqueness();
        getInstance().ifConfigPresent(fileConfig -> {
            if(!Files.exists(getInstance().configPath)) {
                CreeperHealing.LOGGER.warn("Found no preexisting configuration file. Creating a new configuration file with default values in {}", getInstance().configPath);
                getInstance().createNewConfigFile(fileConfig);
            } else {
                fileConfig.load();
                getInstance().configTables.forEach(table -> table.loadValues(fileConfig));
                getInstance().saveToFile();
                CreeperHealing.LOGGER.info("Found existing configuration file. Loaded values from {}", getInstance().configPath);
            }
            return true;
        });
        getInstance().isInitialized = true;
    }

    private void registerConfigSettings(List<ConfigTable> configTables, List<ConfigSetting.Builder<?, ?>> settingBuilders) {
        if(isInitialized) {
            throw new IllegalStateException("ConfigManager has already been initialized");
        }
        this.configTables.addAll(configTables);
        for(ConfigSetting.Builder<?, ?> settingBuilder : settingBuilders) {
            String tableName = settingBuilder.getTableName();
            for(ConfigTable configTable : this.configTables) {
                if(configTable.getName().equals(tableName)) {
                    ConfigSetting<?> setting = settingBuilder.build();
                    configTable.addConfigSetting(setting);
                    break;
                }
            }
        }
        this.configTables.forEach(ConfigTable::setAsRegistered);
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

    public boolean reloadFromFile() {
        return this.ifConfigPresent(fileConfig -> {
            if(!Files.exists(configPath)) {
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
            if(!Files.exists(configPath)) {
                CreeperHealing.LOGGER.warn("Found no preexisting configuration file to save settings to. Creating a new configuration file with default values in {}", configPath);
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

    public ConfigSetting<?> getSetting(SettingIdentifier settingId) {
        for(ConfigTable configTable : this.configTables) {
            if(configTable.getName().equals(settingId.tableName())) {
                for(ConfigSetting<?> setting : configTable.getConfigSettings()) {
                    if(setting.getName().equals(settingId.settingName())) {
                        return setting;
                    }
                }
            }
        }
        throw new IllegalArgumentException("Setting " + settingId.settingName() + " not found in table " + settingId.tableName());
    }

    public DoubleSetting getAsDoubleSetting(SettingIdentifier settingId) {
        ConfigSetting<?> configSetting = this.getSetting(settingId);
        if(!(configSetting instanceof DoubleSetting doubleSetting)) {
            throw new IllegalArgumentException("Setting " + settingId.settingName() + " is not a double setting");
        }
        return doubleSetting;
    }

    public BooleanSetting getAsBooleanSetting(SettingIdentifier settingId) {
        ConfigSetting<?> configSetting = this.getSetting(settingId);
        if(!(configSetting instanceof BooleanSetting booleanSetting)) {
            throw new IllegalArgumentException("Setting " + settingId.settingName() + " is not a boolean setting");
        }
        return booleanSetting;
    }

    public StringSetting getAsStringSetting(SettingIdentifier settingId) {
        ConfigSetting<?> configSetting = this.getSetting(settingId);
        if(!(configSetting instanceof StringSetting stringSetting)) {
            throw new IllegalArgumentException("Setting " + settingId.settingName() + " is not a string setting");
        }
        return stringSetting;
    }

    public StringListSetting getAsStringListSetting(SettingIdentifier settingId) {
        ConfigSetting<?> configSetting = this.getSetting(settingId);
        if(!(configSetting instanceof StringListSetting stringListSetting)) {
            throw new IllegalArgumentException("Setting " + settingId.settingName() + " is not a string list setting");
        }
        return stringListSetting;
    }

    private void createNewConfigFile(CommentedFileConfig fileConfig) {
        this.configTables.forEach(table -> table.setDefaultValues(fileConfig));
        fileConfig.save();
    }

    private void checkForSettingNameUniqueness() {
        Set<String> settingNames = new HashSet<>();
        for (ConfigTable table : this.configTables) {
            for (ConfigSetting<?> setting : table.getConfigSettings()) {
                if (!settingNames.add(setting.getName())) {
                    throw new IllegalArgumentException("Duplicate config setting name found: " + setting.getName());
                }
            }
        }
    }

}

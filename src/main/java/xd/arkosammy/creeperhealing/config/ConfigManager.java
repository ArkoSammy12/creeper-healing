package xd.arkosammy.creeperhealing.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.GenericBuilder;
import com.electronwill.nightconfig.toml.TomlFormat;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.Nullable;
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

    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("creeper-healing.toml");
    private static ConfigManager INSTANCE;
    private final GenericBuilder<CommentedConfig, CommentedFileConfig> CONFIG_BUILDER;
    private final List<ConfigTable> configTables = new ArrayList<>();
    @Nullable
    private List<ConfigSetting.Builder<?, ?>> temporarySettingBuilders = new ArrayList<>();

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
    }

    public static ConfigManager getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new ConfigManager();
        }
        return INSTANCE;
    }

    public void addConfigSetting(ConfigSetting.Builder<?, ?> builder) {
        if(this.temporarySettingBuilders == null) {
            return;
        }
        this.temporarySettingBuilders.add(builder);
    }

    private void registerConfigTables(List<ConfigTable> configTables) {
        if(this.temporarySettingBuilders == null) {
            return;
        }
        this.configTables.addAll(configTables);
        for(ConfigSetting.Builder<?, ?> settingBuilder : this.temporarySettingBuilders) {
            String tableName = settingBuilder.getTableName();
            for(ConfigTable configTable : this.configTables) {
                if(configTable.getName().equals(tableName)) {
                    ConfigSetting<?> setting = settingBuilder.build();
                    configTable.addConfigSetting(setting);
                    break;
                }
            }
        }
        this.temporarySettingBuilders.clear();
        this.temporarySettingBuilders = null;
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

    public void init(List<ConfigTable> configTables) {
        this.registerConfigTables(configTables);
        this.checkForSettingNameUniqueness();
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

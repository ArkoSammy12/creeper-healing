package xd.arkosammy.creeperhealing.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import xd.arkosammy.creeperhealing.CreeperHealing;
import xd.arkosammy.creeperhealing.config.settings.ConfigSetting;
import xd.arkosammy.creeperhealing.config.settings.StringSetting;
import xd.arkosammy.creeperhealing.config.util.SettingIdentifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReplaceMapTable implements ConfigTable {

    private final String name = "replace_map";
    private final String comment = """
            dd your own replace entries to configure which blocks should be used to heal other blocks. The block on the right will be used to heal the block on the left.
            Specify the block's namespace along with the block's name identifier, separated by a colon and enclosed in double quotes.
            Example entry:
            "minecraft:gold_block" = "minecraft:stone"
            Warning, the same key cannot appear more than once in the replace map! For example, the following will cause an error:
            "minecraft:diamond_block" = "minecraft:stone"
            "minecraft:diamond_block" = "minecraft:air"\s""";

    private final List<ConfigSetting<?>> configSettings = new ArrayList<>();

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Optional<String> getComment() {
        return Optional.of(this.comment);
    }

    @Override
    public List<ConfigSetting<?>> getConfigSettings() {
        return this.configSettings;
    }

    @Override
    public void setAsRegistered() {
        // Not needed
    }

    @Override
    public boolean isRegistered() {
        // Since this is a dynamic table, we want to allow it to receive config settings multiple times during run-time
        return false;
    }

    public static Optional<String> getFromKey(String key) {
        ConfigTable table = ConfigManager.getInstance().getConfigTable(ConfigTables.REPLACE_MAP_TABLE.getName());
        if(!(table instanceof ReplaceMapTable replaceMapTable)) {
            CreeperHealing.LOGGER.error("Failed to get Replace Map Table.");
            return Optional.empty();
        }
        for(ConfigSetting<?> setting : replaceMapTable.configSettings) {
            if(setting.getName().equals(key)) {
                Object value = setting.getValue();
                if (value instanceof String stringValue) {
                    return Optional.of(stringValue);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public void setDefaultValues(CommentedFileConfig fileConfig) {
        this.configSettings.clear();
        StringSetting defaultSetting = new StringSetting.Builder(new SettingIdentifier(ConfigTables.REPLACE_MAP_TABLE.getName(), "minecraft:diamond_block"), "minecraft:stone").build();
        this.configSettings.add(defaultSetting);
        this.setValues(fileConfig);
    }

    @Override
    public void setValues(CommentedFileConfig fileConfig) {
        if(!this.configSettings.isEmpty()) {
            for(ConfigSetting<?> setting : this.configSettings) {
                fileConfig.set(this.getName() + "." + setting.getName(), setting.getValue());
            }
        } else {
            fileConfig.set(this.getName() + "." + "minecraft:placeholder_key", "minecraft:placeholder_value");
        }
        fileConfig.setComment(this.getName(), this.comment);
    }

    @Override
    public void loadValues(CommentedFileConfig fileConfig) {
        CommentedConfig replaceMapConfig = fileConfig.get(this.getName());
        List<StringSetting> tempReplaceMap = new ArrayList<>();
        for(CommentedConfig.Entry entry : replaceMapConfig.entrySet()) {
            if(entry.getValue() instanceof String && entry.getKey() != null) {
                StringSetting stringSetting = new StringSetting.Builder(new SettingIdentifier(ConfigTables.REPLACE_MAP_TABLE.getName(), entry.getKey()), entry.getValue()).build();
                tempReplaceMap.add(stringSetting);
            } else if (!(entry.getValue() instanceof String)) {
                CreeperHealing.LOGGER.error("Failed to read Replace Map: Invalid value in replace map for key: {}", entry.getKey());
            } else if (entry.getKey() == null) {
                CreeperHealing.LOGGER.error("Failed to read Replace Map: Invalid key found in replace map.");
            }
        }
        this.configSettings.clear();
        this.configSettings.addAll(tempReplaceMap);
    }

}

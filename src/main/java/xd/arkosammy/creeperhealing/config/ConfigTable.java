package xd.arkosammy.creeperhealing.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import xd.arkosammy.creeperhealing.CreeperHealing;
import xd.arkosammy.creeperhealing.config.settings.ConfigSetting;
import java.util.List;
import java.util.Optional;

public interface ConfigTable {

    String getName();

    Optional<String> getComment();

    List<ConfigSetting<?>> getConfigSettings();

    void setAsRegistered();

    boolean isRegistered();

    default void addConfigSetting(ConfigSetting<?> setting) {
        if(this.isRegistered()){
            return;
        }
        this.getConfigSettings().add(setting);
    }

    default void setDefaultValues(CommentedFileConfig fileConfig) {
        for(ConfigSetting<?> setting : getConfigSettings()) {
            setting.resetValue();
        }
        this.setValues(fileConfig);
    }

    default void setValues(CommentedFileConfig fileConfig) {
        for(ConfigSetting<?> setting : getConfigSettings()) {
            String settingAddress = this.getName() + "." + setting.getName();
            fileConfig.set(settingAddress, setting.getValue());
            setting.getComment().ifPresent(comment -> fileConfig.setComment(settingAddress, comment));
        }
        this.getComment().ifPresent(comment -> fileConfig.setComment(this.getName(), comment));

        CommentedConfig tableConfig = fileConfig.get(this.getName());
        if(tableConfig != null){
            tableConfig.entrySet().removeIf(entry -> !this.containsSettingName(entry.getKey()));
        }
    }

    default void loadValues(CommentedFileConfig fileConfig) {
        for(ConfigSetting<?> setting : getConfigSettings()) {
            String settingAddress = this.getName() + "." + setting.getName();
            Object value = fileConfig.getOrElse(settingAddress, setting.getDefaultValue());
            setValueSafely(setting, value);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void setValueSafely(ConfigSetting<T> setting, Object value) {
        if(setting.getDefaultValue().getClass().equals(Integer.class) && value instanceof Number number) {
            value = number.intValue();
        } else if(setting.getDefaultValue().getClass().equals(Double.class) && value instanceof Number number) {
            value = number.doubleValue();
        }

        if (!setting.getDefaultValue().getClass().isInstance(value)) {
            CreeperHealing.LOGGER.error("Failed to load value for setting {} in table {}. Expected {} but got {}. Using default value instead.", setting.getName(), this.getName(), setting.getDefaultValue().getClass().getName(), value.getClass().getName());
            return;
        }
        setting.setValue((T) value);
    }

    default boolean containsSettingName(String settingName) {
        for(ConfigSetting<?> setting : getConfigSettings()) {
            if (setting.getName().equals(settingName)) {
                return true;
            }
        }
        return false;
    }

}

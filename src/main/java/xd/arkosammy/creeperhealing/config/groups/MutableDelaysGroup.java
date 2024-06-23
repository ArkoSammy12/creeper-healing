package xd.arkosammy.creeperhealing.config.groups;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xd.arkosammy.monkeyconfig.groups.MutableSettingGroup;
import xd.arkosammy.monkeyconfig.settings.ConfigSetting;
import xd.arkosammy.monkeyconfig.types.SerializableType;
import xd.arkosammy.monkeyconfig.util.SettingLocation;

import java.util.ArrayList;
import java.util.List;

public class MutableDelaysGroup extends DelaysGroup implements MutableSettingGroup {

    public MutableDelaysGroup(@NotNull String name, @Nullable String comment) {
        super(name, comment, new ArrayList<>());
    }

    @Override
    public <T, S extends SerializableType<?>> void addConfigSetting(@NotNull ConfigSetting<T, S> configSetting) {
        this.getConfigSettings().add(configSetting);
    }

    @Override
    public boolean removeConfigSetting(@NotNull SettingLocation settingLocation) {
        return false;
    }

    @Override
    public @NotNull DelaysGroup toImmutable(List<? extends ConfigSetting<?, ?>> configSettings) {
        return new DelaysGroup(this.getName(), this.getComment(), configSettings != null ? new ArrayList<>(configSettings) : new ArrayList<>(this.getConfigSettings()) ,this.getLoadBeforeSave(), this.getRegisterSettingsAsCommands());
    }
}

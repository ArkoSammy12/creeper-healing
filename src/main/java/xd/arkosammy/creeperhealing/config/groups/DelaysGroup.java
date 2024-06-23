package xd.arkosammy.creeperhealing.config.groups;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xd.arkosammy.creeperhealing.util.ExplosionManager;
import xd.arkosammy.monkeyconfig.groups.DefaultSettingGroup;
import xd.arkosammy.monkeyconfig.settings.ConfigSetting;

import java.util.List;

public class DelaysGroup extends DefaultSettingGroup {

    public DelaysGroup(@NotNull String name, @Nullable String comment, @NotNull List<? extends ConfigSetting<?, ?>> configSettings, boolean loadBeforeSave, boolean registerSettingsAsCommands) {
        super(name, comment, configSettings, loadBeforeSave, registerSettingsAsCommands);
    }

    public DelaysGroup(@NotNull String name, @Nullable String comment, @NotNull List<? extends ConfigSetting<?, ?>> configSettings) {
        super(name, comment, configSettings);
    }

    @Override
    public void onLoaded() {
        ExplosionManager.getInstance().updateAffectedBlocksTimers();
    }

}

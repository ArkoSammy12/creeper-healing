package xd.arkosammy.creeperhealing.config.tables;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.Nullable;
import xd.arkosammy.creeperhealing.config.settings.ConfigSetting;

import java.util.List;
import java.util.Optional;

public class SimpleConfigTable implements ConfigTable {

    private final String name;
    @Nullable
    private final String comment;
    private final List<ConfigSetting<?>> configSettings;

    public SimpleConfigTable(String name, @Nullable String comment, List<ConfigSetting<?>> configSettings) {
        this.name = name;
        this.comment = comment;
        this.configSettings = ImmutableList.copyOf(configSettings);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Optional<String> getComment() {
        return Optional.ofNullable(this.comment);
    }

    @Override
    public List<ConfigSetting<?>> getConfigSettings() {
        return this.configSettings;
    }
}

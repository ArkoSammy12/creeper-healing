package xd.arkosammy.creeperhealing.config;

import org.jetbrains.annotations.Nullable;
import xd.arkosammy.creeperhealing.config.settings.ConfigSetting;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SimpleConfigTable implements ConfigTable {

    private final String name;
    @Nullable
    private final String comment;
    private final List<ConfigSetting<?>> configSettings = new ArrayList<>();
    private boolean registered = false;

    public SimpleConfigTable(String name, @Nullable String comment) {
        this.name = name;
        this.comment = comment;
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

    @Override
    public void setAsRegistered() {
        this.registered = true;
    }

    @Override
    public boolean isRegistered() {
        return this.registered;
    }
}

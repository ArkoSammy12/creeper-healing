package xd.arkosammy.creeperhealing.config.settings;

import org.jetbrains.annotations.Nullable;
import xd.arkosammy.creeperhealing.config.util.SettingIdentifier;

import java.util.Optional;

public abstract class ConfigSetting<T>  {

    private T value;
    private final T defaultValue;
    private final String name;
    @Nullable
    private final String comment;

    ConfigSetting(String name, T defaultValue) {
        this(name, defaultValue, null);
    }

    ConfigSetting(String name, T defaultValue, @Nullable String comment) {
        this(name, defaultValue, defaultValue, comment);
    }

    ConfigSetting(String name , T value, T defaultValue, @Nullable String comment) {
        this.name = name;
        this.comment = comment;
        this.value = value;
        this.defaultValue = defaultValue;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public T getValue() {
        return this.value;
    }

    public T getDefaultValue() {
        return this.defaultValue;
    }

    public String getName() {
        return this.name;
    }

    public Optional<String> getComment() {
        return Optional.ofNullable(this.comment);
    }

    public void resetValue() {
        this.value = this.defaultValue;
    }

    public static abstract class Builder<V, S extends ConfigSetting<V>> {

        final SettingIdentifier id;
        final V defaultValue;
        @Nullable
        String comment;

        public Builder(SettingIdentifier id, V defaultValue) {
            this.id = id;
            this.defaultValue = defaultValue;
        }

        public Builder<V, S> withComment(String comment) {
            this.comment = comment;
            return this;
        }

        public String getTableName() {
            return this.id.tableName();
        }

        public abstract S build();

    }

}

package xd.arkosammy.creeperhealing.config.settings;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public abstract class ConfigSetting<T>  {

    private T value;
    private final T defaultValue;
    private final String name;
    @Nullable
    private final String comment;

    protected ConfigSetting(String name, T defaultValue) {
        this(name, defaultValue, null);
    }

    protected ConfigSetting(String name, T defaultValue, @Nullable String comment) {
        this(name, defaultValue, defaultValue, comment);
    }

    protected ConfigSetting(String name , T value, T defaultValue, @Nullable String comment) {
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

}

package xd.arkosammy.creeperhealing.config.util;

import java.util.Objects;

public record SettingIdentifier(String tableName, String settingName) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SettingIdentifier that)) return false;
        return Objects.equals(tableName(), that.tableName()) && Objects.equals(settingName(), that.settingName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(tableName(), settingName());
    }

    @Override
    public String toString() {
        return this.tableName + "." + this.settingName;
    }

}

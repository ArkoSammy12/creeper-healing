package xd.arkosammy.creeperhealing.config.settings;

import xd.arkosammy.creeperhealing.config.util.SettingIdentifier;

public class StringSetting extends ConfigSetting<String> {

    public StringSetting(String name, String value, String comment) {
        super(name, value, comment);
    }

    public static class Builder extends ConfigSetting.Builder<String, StringSetting> {
        public Builder(SettingIdentifier name, String defaultValue) {
            super(name, defaultValue);
        }

        @Override
        public StringSetting build() {
            return new StringSetting(this.id.settingName(), this.defaultValue, this.comment);
        }

    }

}

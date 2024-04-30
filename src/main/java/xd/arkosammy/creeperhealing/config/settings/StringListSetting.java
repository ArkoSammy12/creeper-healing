package xd.arkosammy.creeperhealing.config.settings;

import xd.arkosammy.creeperhealing.config.util.SettingIdentifier;

import java.util.List;

public class StringListSetting extends ConfigSetting<List<String>> {

    StringListSetting(String name, List<String> value) {
        super(name, value);
    }

    public static class Builder extends ConfigSetting.Builder<List<String>, StringListSetting> {
        public Builder(SettingIdentifier name, List<String> defaultValue) {
            super(name, defaultValue);
        }

        @Override
        public StringListSetting build() {
            return new StringListSetting(this.id.settingName(), this.defaultValue);
        }

    }

}

package xd.arkosammy.creeperhealing.config.settings;

import xd.arkosammy.creeperhealing.config.util.SettingIdentifier;

public class BooleanSetting extends ConfigSetting<Boolean> {

    BooleanSetting(String name, boolean value, String comment) {
        super(name, value, comment);
    }

    public static class Builder extends ConfigSetting.Builder<Boolean, BooleanSetting> {

        public Builder(SettingIdentifier name, boolean defaultValue) {
            super(name, defaultValue);
        }

        @Override
        public BooleanSetting build() {
            return new BooleanSetting(this.id.settingName(), this.defaultValue, this.comment);
        }

    }

}

package xd.arkosammy.creeperhealing.config.settings;

import org.jetbrains.annotations.Nullable;
import xd.arkosammy.creeperhealing.config.ConfigManager;
import xd.arkosammy.creeperhealing.config.util.SettingIdentifier;

public class BlockPlacementDelaySetting extends DoubleSetting {

    BlockPlacementDelaySetting(String name, double value, @Nullable Double lowerBound, @Nullable Double upperBound, String comment) {
        super(name, value, lowerBound, upperBound, comment);
    }

    public static long getAsTicks() {
        DoubleSetting setting = ConfigManager.getInstance().getAsDoubleSetting(ConfigSettings.BLOCK_PLACEMENT_DELAY.getId());
        if (!(setting instanceof BlockPlacementDelaySetting blockPlacementDelaySetting)) {
            throw new IllegalStateException("Retrieved setting is not an instance of BlockPlacementDelaySetting");
        }
        return blockPlacementDelaySetting.asTicks();
    }

    public long asTicks() {
        long rounded = Math.round(Math.max(0, this.getValue()) * 20);
        return rounded == 0 ? 20 : rounded;
    }

    public static class Builder extends DoubleSetting.Builder {

        public Builder(SettingIdentifier id, double defaultValue) {
            super(id, defaultValue);
        }

        @Override
        public DoubleSetting build() {
            return new BlockPlacementDelaySetting(this.id.settingName(), this.defaultValue, this.lowerBound, this.upperBound, this.comment);
        }
    }

}

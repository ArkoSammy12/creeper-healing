package xd.arkosammy.creeperhealing.config.settings;

import org.jetbrains.annotations.Nullable;
import xd.arkosammy.creeperhealing.config.ConfigManager;
import xd.arkosammy.creeperhealing.config.enums.DelaysSettings;

public class HealDelaySetting extends DoubleSetting {

    public HealDelaySetting(String name, double value, @Nullable Double lowerBound, @Nullable Double upperBound, String comment) {
        super(name, value, lowerBound, upperBound, comment);
    }

    public static long getAsTicks() {
        DoubleSetting setting = ConfigManager.getInstance().getAsDoubleSetting(DelaysSettings.EXPLOSION_HEAL_DELAY.getName());
        if (!(setting instanceof HealDelaySetting healDelaySetting)) {
            throw new IllegalStateException("Retrieved setting is not an instance of BlockPlacementDelaySetting");
        }
        return healDelaySetting.asTicks();
    }

    public long asTicks() {
        long rounded = Math.round(Math.max(0, this.getValue()) * 20);
        return rounded == 0 ? 20 : rounded;
    }

}

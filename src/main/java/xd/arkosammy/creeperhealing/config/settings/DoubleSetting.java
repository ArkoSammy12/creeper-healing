package xd.arkosammy.creeperhealing.config.settings;

import org.jetbrains.annotations.Nullable;
import xd.arkosammy.creeperhealing.CreeperHealing;

public class DoubleSetting extends ConfigSetting<Double> {

    @Nullable
    private final Double lowerBound;
    @Nullable
    private final Double upperBound;

    public DoubleSetting(String name, double value, @Nullable Double lowerBound, @Nullable Double upperBound, String comment) {
        super(name, value, comment);
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    @Override
    public void setValue(Double value) {
        if (this.lowerBound != null && value < this.lowerBound) {
            CreeperHealing.LOGGER.error("Value {} for setting {} is below the lower bound!", value, this.getName());
            return;
        }
        if (this.upperBound != null && value > this.upperBound) {
            CreeperHealing.LOGGER.error("Value {} for setting {} is above the upper bound!", value, this.getName());
            return;
        }
        super.setValue(value);

    }

}

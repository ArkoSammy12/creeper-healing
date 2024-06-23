package xd.arkosammy.creeperhealing.config.settings;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import kotlin.jvm.functions.Function2;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xd.arkosammy.creeperhealing.util.ExplosionManager;
import xd.arkosammy.monkeyconfig.managers.ConfigManager;
import xd.arkosammy.monkeyconfig.settings.NumberSetting;
import xd.arkosammy.monkeyconfig.util.SettingLocation;

public class BlockPlacementDelaySetting extends NumberSetting<Double> {

    public BlockPlacementDelaySetting(@NotNull SettingLocation settingLocation, @Nullable String comment, @NotNull Double defaultValue, @NotNull Double value, @Nullable Double lowerBound, @Nullable Double upperBound) {
        super(settingLocation, comment, defaultValue, value, lowerBound, upperBound);
    }

    @Override
    @NotNull
    public Function2<CommandContext<ServerCommandSource>, ConfigManager, Integer> getOnValueSetCallback() {
        return (ctx, manager) -> {
            super.getOnValueSetCallback().invoke(ctx, manager);
            ExplosionManager.getInstance().updateAffectedBlocksTimers();
            return Command.SINGLE_SUCCESS;
        };
    }

    public static class Builder extends NumberSetting.Builder<Double> {

        public Builder(@NotNull SettingLocation settingLocation, @Nullable String comment, @NotNull Double defaultValue) {
            super(settingLocation, comment, defaultValue);
        }

        @NotNull
        @Override
        public BlockPlacementDelaySetting build() {
            return new BlockPlacementDelaySetting(this.getSettingLocation(), this.getComment(), this.getDefaultValue(), this.getDefaultValue(), this.getLowerBound(), this.getUpperBound());
        }
    }

}

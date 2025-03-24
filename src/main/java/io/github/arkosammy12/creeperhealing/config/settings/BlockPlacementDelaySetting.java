package io.github.arkosammy12.creeperhealing.config.settings;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.github.arkosammy12.creeperhealing.CreeperHealing;
import io.github.arkosammy12.monkeyconfig.base.ConfigManager;
import io.github.arkosammy12.monkeyconfig.builders.NumberSettingBuilder;
import io.github.arkosammy12.monkeyutils.settings.CommandNumberSetting;
import kotlin.jvm.functions.Function2;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

public class BlockPlacementDelaySetting extends CommandNumberSetting<Double> {

    public BlockPlacementDelaySetting(@NotNull NumberSettingBuilder<Double> settingBuilder) {
        super(settingBuilder);
    }

    @Override
    public @NotNull Function2<CommandContext<? extends ServerCommandSource>, ConfigManager, Integer> getOnValueSetCallback() {
        return (ctx, manager) -> {
            super.getOnValueSetCallback().invoke(ctx, manager);
            CreeperHealing.EXPLOSION_MANAGER.updateAffectedBlocksTimers();
            return Command.SINGLE_SUCCESS;
        };
    }
}

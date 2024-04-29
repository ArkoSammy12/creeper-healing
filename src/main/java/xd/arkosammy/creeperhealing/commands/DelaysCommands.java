package xd.arkosammy.creeperhealing.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import xd.arkosammy.creeperhealing.config.ConfigManager;
import xd.arkosammy.creeperhealing.config.settings.BlockPlacementDelaySetting;
import xd.arkosammy.creeperhealing.config.settings.ConfigSettings;
import xd.arkosammy.creeperhealing.config.settings.DoubleSetting;
import xd.arkosammy.creeperhealing.config.settings.HealDelaySetting;
import xd.arkosammy.creeperhealing.util.ExplosionManager;

public final class DelaysCommands {

    private DelaysCommands(){}

    static void register(LiteralCommandNode<ServerCommandSource> creeperHealingNode){

        LiteralCommandNode<ServerCommandSource> delaysNode = CommandManager
                .literal("delays")
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Explosion Heal Delay node
        LiteralCommandNode<ServerCommandSource> explosionHealDelayNode = CommandManager
                .literal("explosion_heal_delay")
                .executes(DelaysCommands::getExplosionHealDelayCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Block Placement Delay node
        LiteralCommandNode<ServerCommandSource> blockPlacementDelayNode = CommandManager
                .literal("block_placement_delay")
                .executes(DelaysCommands::getBlockPlacementDelayCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Explosion heal delay argument node
        ArgumentCommandNode<ServerCommandSource, Double> explosionHealDelayArgumentNode = CommandManager
                .argument("seconds", DoubleArgumentType.doubleArg())
                .executes(DelaysCommands::setExplosionHealDelayCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Block placement delay argument node
        ArgumentCommandNode<ServerCommandSource, Double> blockPlacementDelayArgumentNode = CommandManager
                .argument("seconds", DoubleArgumentType.doubleArg())
                .executes(DelaysCommands::setBlockPlacementDelayCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Root node connection
        creeperHealingNode.addChild(delaysNode);

        //Delays command nodes
        delaysNode.addChild(explosionHealDelayNode);
        delaysNode.addChild(blockPlacementDelayNode);

        //Argument nodes
        explosionHealDelayNode.addChild(explosionHealDelayArgumentNode);
        blockPlacementDelayNode.addChild(blockPlacementDelayArgumentNode);

    }

    private static int setExplosionHealDelayCommand(CommandContext<ServerCommandSource> ctx) {
        double value = DoubleArgumentType.getDouble(ctx, "seconds");
        if(Math.round(Math.max(value, 0) * 20L) != 0) {
            ConfigManager.getInstance().getAsDoubleSetting(ConfigSettings.EXPLOSION_HEAL_DELAY.getId()).setValue(value);
            ctx.getSource().sendMessage(Text.literal("Explosion heal delay has been set to: " + DoubleArgumentType.getDouble(ctx, "seconds") + " second(s)"));
        } else {
            ctx.getSource().sendMessage(Text.literal("Cannot set explosion heal delay to a very low value").formatted(Formatting.RED));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int setBlockPlacementDelayCommand(CommandContext<ServerCommandSource> ctx) {
        double value = DoubleArgumentType.getDouble(ctx, "seconds");
        if (Math.round(Math.max(value, 0) * 20L) != 0) {
            ConfigManager.getInstance().getAsDoubleSetting(ConfigSettings.BLOCK_PLACEMENT_DELAY.getId()).setValue(value);
            ExplosionManager.getInstance().updateAffectedBlocksTimers();
            ctx.getSource().sendMessage(Text.literal("Block placement delay has been set to: " + DoubleArgumentType.getDouble(ctx, "seconds") + " second(s)"));
        } else {
            ctx.getSource().sendMessage(Text.literal("Cannot set block placement delay to a very low value").formatted(Formatting.RED));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int getExplosionHealDelayCommand(CommandContext<ServerCommandSource> ctx){

        DoubleSetting setting = ConfigManager.getInstance().getAsDoubleSetting(ConfigSettings.EXPLOSION_HEAL_DELAY.getId());
        if(setting instanceof HealDelaySetting doubleSetting){
            ctx.getSource().sendMessage(Text.literal("Explosion heal delay currently set to: " + doubleSetting.getValue() + " second(s)"));
        } else {
            ctx.getSource().sendMessage(Text.literal("Error getting explosion heal delay setting").formatted(Formatting.RED));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int getBlockPlacementDelayCommand(CommandContext<ServerCommandSource> ctx){

        DoubleSetting setting = ConfigManager.getInstance().getAsDoubleSetting(ConfigSettings.BLOCK_PLACEMENT_DELAY.getId());
        if(setting instanceof BlockPlacementDelaySetting doubleSetting){
            ctx.getSource().sendMessage(Text.literal("Block placement delay currently set to: " + doubleSetting.getValue() + " second(s)"));
        } else {
            ctx.getSource().sendMessage(Text.literal("Error getting block placement delay setting").formatted(Formatting.RED));
        }
        return Command.SINGLE_SUCCESS;
    }

}

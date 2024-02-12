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
import xd.arkosammy.creeperhealing.config.DelaysConfig;
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
        if(Math.round(Math.max(DoubleArgumentType.getDouble(ctx, "seconds"), 0) * 20L) != 0) {
            DelaysConfig.EXPLOSION_HEAL_DELAY.getEntry().setValue(DoubleArgumentType.getDouble(ctx, "seconds"));
            ctx.getSource().sendMessage(Text.literal("Explosion heal delay has been set to: " + DoubleArgumentType.getDouble(ctx, "seconds") + " second(s)"));
        } else {
            ctx.getSource().sendMessage(Text.literal("Cannot set explosion heal delay to a very low value").formatted(Formatting.RED));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int setBlockPlacementDelayCommand(CommandContext<ServerCommandSource> ctx) {
        if (Math.round(Math.max(DoubleArgumentType.getDouble(ctx, "seconds"), 0) * 20L) != 0) {
            DelaysConfig.BLOCK_PLACEMENT_DELAY.getEntry().setValue(DoubleArgumentType.getDouble(ctx, "seconds"));
            ExplosionManager.getInstance().updateAffectedBlocksTimers();
            ctx.getSource().sendMessage(Text.literal("Block placement delay has been set to: " + DoubleArgumentType.getDouble(ctx, "seconds") + " second(s)"));
        } else {
            ctx.getSource().sendMessage(Text.literal("Cannot set block placement delay to a very low value").formatted(Formatting.RED));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int getExplosionHealDelayCommand(CommandContext<ServerCommandSource> ctx){
        ctx.getSource().sendMessage(Text.literal("Explosion heal delay currently set to: " + ((double)DelaysConfig.getExplosionHealDelayAsTicks() / 20) + " second(s)"));
        return Command.SINGLE_SUCCESS;
    }

    private static int getBlockPlacementDelayCommand(CommandContext<ServerCommandSource> ctx){
        ctx.getSource().sendMessage(Text.literal("Block placement delay currently set to: " + ((double) DelaysConfig.getBlockPlacementDelayAsTicks() / 20) + " second(s)"));
        return Command.SINGLE_SUCCESS;
    }

}

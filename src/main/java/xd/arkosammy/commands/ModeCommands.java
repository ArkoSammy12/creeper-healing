package xd.arkosammy.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import xd.arkosammy.configuration.tables.ModeConfig;

final class ModeCommands {

    private ModeCommands(){}

    static void register(LiteralCommandNode<ServerCommandSource> creeperHealingNode){

        //Mode node
        LiteralCommandNode<ServerCommandSource> modeMode = CommandManager
                .literal("mode")
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Daytime healing node
        LiteralCommandNode<ServerCommandSource> doDayTimeHealingNode = CommandManager
                .literal("daytime_healing_mode")
                .executes(ModeCommands::getDoDayLightHealingCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Daytime healing mode argument node
        ArgumentCommandNode<ServerCommandSource, Boolean> doDayLightHealingArgumentNode = CommandManager
                .argument("value", BoolArgumentType.bool())
                .executes(ModeCommands::setDoDayTimeHealingCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Root node connections
        creeperHealingNode.addChild(modeMode);

        //Modes command nodes
        modeMode.addChild(doDayTimeHealingNode);

        //Argument nodes
        doDayTimeHealingNode.addChild(doDayLightHealingArgumentNode);

    }

    private static int setDoDayTimeHealingCommand(CommandContext<ServerCommandSource> ctx){
        ModeConfig.setDaytimeHealingMode(BoolArgumentType.getBool(ctx, "value"));
        ctx.getSource().sendMessage(Text.literal("Daytime healing mode has been set to: " + BoolArgumentType.getBool(ctx, "value")));
        return Command.SINGLE_SUCCESS;
    }

    private static int getDoDayLightHealingCommand(CommandContext<ServerCommandSource> ctx){
        ctx.getSource().sendMessage(Text.literal("Daytime healing mode currently set to: " + ModeConfig.getDayTimeHealingMode()));
        return Command.SINGLE_SUCCESS;
    }

}

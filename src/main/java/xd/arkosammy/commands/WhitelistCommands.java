package xd.arkosammy.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import xd.arkosammy.configuration.tables.WhitelistConfig;

public final class WhitelistCommands {

    private WhitelistCommands(){}

    static void register(LiteralCommandNode<ServerCommandSource> creeperHealingNode){

        //Whitelist node
        LiteralCommandNode<ServerCommandSource> whitelistNode = CommandManager
                .literal("whitelist")
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Enable whitelist node
        LiteralCommandNode<ServerCommandSource> enableWhitelistNode = CommandManager
                .literal("enable_whitelist")
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .executes(WhitelistCommands::getEnableWhitelist)
                .build();

        //Enable whitelist argument node
        ArgumentCommandNode<ServerCommandSource, Boolean> enableWhitelistArgumentNode = CommandManager
                .argument("value", BoolArgumentType.bool())
                .executes(WhitelistCommands::setEnableWhitelist)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        creeperHealingNode.addChild(whitelistNode);

        whitelistNode.addChild(enableWhitelistNode);

        enableWhitelistNode.addChild(enableWhitelistArgumentNode);

    }

    private static int setEnableWhitelist(CommandContext<ServerCommandSource> ctx){
        WhitelistConfig.setEnableWhitelist(BoolArgumentType.getBool(ctx, "value"));
        ctx.getSource().sendMessage(Text.literal("The whitelist has been " + (BoolArgumentType.getBool(ctx, "value") == true ? "enabled" : "disabled")));
        return Command.SINGLE_SUCCESS;
    }

    private static int getEnableWhitelist(CommandContext<ServerCommandSource> ctx){
        ctx.getSource().sendMessage(Text.literal("The whitelist is currently " + (WhitelistConfig.getEnableWhitelist() == true ? "enabled" : "disabled")));
        return Command.SINGLE_SUCCESS;
    }

}

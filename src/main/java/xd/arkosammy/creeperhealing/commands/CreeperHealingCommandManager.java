package xd.arkosammy.creeperhealing.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import xd.arkosammy.creeperhealing.config.CreeperHealingConfigManager;

import java.io.IOException;

public final class CreeperHealingCommandManager {

    private CreeperHealingCommandManager(){}
    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment){
        //Root node
        LiteralCommandNode<ServerCommandSource> creeperHealingNode = CommandManager
                .literal("creeper-healing")
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Reload CreeperHealingConfigManager node
        LiteralCommandNode<ServerCommandSource> reloadNode = CommandManager
                .literal("reload_config")
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .executes(context -> {
                    try {
                        CreeperHealingCommandManager.reload(context);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return Command.SINGLE_SUCCESS;
                })
                .build();

        //Root connection
        dispatcher.getRoot().addChild(creeperHealingNode);
        creeperHealingNode.addChild(reloadNode);
        ExplosionSourcesCommands.register(creeperHealingNode);
        ModeCommands.register(creeperHealingNode);
        PreferencesCommands.register(creeperHealingNode);
        DelaysCommands.register(creeperHealingNode);
        ExplosionItemDropCommands.register(creeperHealingNode);
    }

    private static void reload(CommandContext<ServerCommandSource> ctx) throws IOException {
        //If this returns true, then the config file exists, and we can update our values from it
        if(CreeperHealingConfigManager.reloadValuesFromConfig(ctx)) ctx.getSource().sendMessage(Text.literal("CreeperHealingConfigManager successfully reloaded"));
        else ctx.getSource().sendMessage(Text.literal("Found no existing config file to reload values from").formatted(Formatting.RED));
    }

}

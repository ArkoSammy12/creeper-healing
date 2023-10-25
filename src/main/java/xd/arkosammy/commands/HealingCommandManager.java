package xd.arkosammy.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public final class HealingCommandManager {

    private HealingCommandManager(){}
    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment){
        //Root node
        LiteralCommandNode<ServerCommandSource> creeperHealingNode = CommandManager
                .literal("creeper-healing")
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();
        //Root connection
        dispatcher.getRoot().addChild(creeperHealingNode);
        ExplosionSourcesCommands.register(creeperHealingNode);
        ModeCommands.register(creeperHealingNode);
        SettingsCommands.register(creeperHealingNode);
        WhitelistCommands.register(creeperHealingNode);
    }

}

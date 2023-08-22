package xd.arkosammy.util;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import xd.arkosammy.CreeperHealing;

import java.io.IOException;

public class Commands {

    public static void registerCommands(){

        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> {

            LiteralCommandNode<ServerCommandSource> creeperHealingNode = CommandManager
                    .literal("creeper-healing")
                    .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                    .build();

            LiteralCommandNode<ServerCommandSource> explosionHealDelayNode = CommandManager
                    .literal("set_explosion_heal_delay")
                    .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                    .build();

            LiteralCommandNode<ServerCommandSource> blockPlacementDelayNode = CommandManager
                    .literal("set_block_placement_delay")
                    .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                    .build();

            LiteralCommandNode<ServerCommandSource> shouldHealOnFlowingWaterNode = CommandManager
                    .literal("set_heal_on_flowing_water")
                    .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                    .build();

            LiteralCommandNode<ServerCommandSource> shouldHealOnFlowingLavaNode = CommandManager
                    .literal("set_heal_on_flowing_lava")
                    .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                    .build();

            LiteralCommandNode<ServerCommandSource> reloadNode = CommandManager
                    .literal("reload_config")
                    .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                    .executes(context -> {

                        try {
                            Commands.reload(context);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        return 1;

                    })
                    .build();

            ArgumentCommandNode<ServerCommandSource, Integer> healDelayArgumentNode = CommandManager
                    .argument("seconds", IntegerArgumentType.integer())
                    .executes(Commands::setExplosionHealDelayCommand)
                    .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                    .build();

            ArgumentCommandNode<ServerCommandSource, Integer> blockPlacementDelayArgumentNode = CommandManager
                    .argument("seconds", IntegerArgumentType.integer())
                    .executes(Commands::setBlockPlacementDelayCommand)
                    .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                    .build();

            ArgumentCommandNode<ServerCommandSource, Boolean> healOnFlowingWaterArgumentNode = CommandManager
                    .argument("value", BoolArgumentType.bool())
                    .executes(Commands::setShouldHealOnFlowingWaterCommand)
                    .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                    .build();

            ArgumentCommandNode<ServerCommandSource, Boolean> healOnFlowingLavaArgumentNode = CommandManager
                    .argument("value", BoolArgumentType.bool())
                    .executes(Commands::setShouldHealOnFlowingLavaCommand)
                    .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                    .build();

            dispatcher.getRoot().addChild(creeperHealingNode);
            creeperHealingNode.addChild(explosionHealDelayNode);
            creeperHealingNode.addChild(blockPlacementDelayNode);
            creeperHealingNode.addChild(shouldHealOnFlowingWaterNode);
            creeperHealingNode.addChild(shouldHealOnFlowingLavaNode);
            creeperHealingNode.addChild(reloadNode);

            explosionHealDelayNode.addChild(healDelayArgumentNode);
            blockPlacementDelayNode.addChild(blockPlacementDelayArgumentNode);
            shouldHealOnFlowingWaterNode.addChild(healOnFlowingWaterArgumentNode);
            shouldHealOnFlowingLavaNode.addChild(healOnFlowingLavaArgumentNode);

        }));

    }


    private static int setExplosionHealDelayCommand(CommandContext<ServerCommandSource> serverCommandSourceCommandContext) {

        CreeperHealing.CONFIG.setExplosionHealDelay(IntegerArgumentType.getInteger(serverCommandSourceCommandContext, "seconds"));

        serverCommandSourceCommandContext.getSource().sendMessage(Text.literal("Explosion heal delay has been set to: " + IntegerArgumentType.getInteger(serverCommandSourceCommandContext, "seconds") + " second(s)"));

        return 1;

    }

    private static int setBlockPlacementDelayCommand(CommandContext<ServerCommandSource> serverCommandSourceCommandContext) {

        CreeperHealing.CONFIG.setBlockPlacementDelay(IntegerArgumentType.getInteger(serverCommandSourceCommandContext, "seconds"));

        serverCommandSourceCommandContext.getSource().sendMessage(Text.literal("Block placement delay has been set to to: " + IntegerArgumentType.getInteger(serverCommandSourceCommandContext, "seconds") + " second(s)"));

        return 1;

    }

    private static int setShouldHealOnFlowingWaterCommand(CommandContext<ServerCommandSource> serverCommandSourceCommandContext) {

        CreeperHealing.CONFIG.setShouldHealOnFlowingWater(BoolArgumentType.getBool(serverCommandSourceCommandContext, "value"));

        serverCommandSourceCommandContext.getSource().sendMessage(Text.literal("Heal on flowing water has been set to: " + BoolArgumentType.getBool(serverCommandSourceCommandContext, "value")));

        return 1;

    }

    private static int setShouldHealOnFlowingLavaCommand(CommandContext<ServerCommandSource> serverCommandSourceCommandContext) {

        CreeperHealing.CONFIG.setShouldHealOnFlowingLava(BoolArgumentType.getBool(serverCommandSourceCommandContext, "value"));

        serverCommandSourceCommandContext.getSource().sendMessage(Text.literal("Heal on flowing lava has been set to: " + BoolArgumentType.getBool(serverCommandSourceCommandContext, "value")));

        return 1;

    }

    private static void reload(CommandContext<ServerCommandSource> serverCommandSourceCommandContext) throws IOException {

        //If this returns true, then the config file exists, and we can update our values from it
        if(CreeperHealing.CONFIG.reloadConfig()) serverCommandSourceCommandContext.getSource().sendMessage(Text.literal("Config successfully reloaded"));
        else serverCommandSourceCommandContext.getSource().sendMessage(Text.literal("Found no existing config file to reload values from"));

    }

}

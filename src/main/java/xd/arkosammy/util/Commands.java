package xd.arkosammy.util;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import xd.arkosammy.CreeperHealing;

public class Commands {

    public static void registerCommands(){

        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> {

            LiteralCommandNode<ServerCommandSource> creeperHealingNode = CommandManager
                    .literal("creeper-healing")
                    .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                    .build();

            LiteralCommandNode<ServerCommandSource> explosionHealDelayNode = CommandManager
                    .literal("explosion_heal_delay")
                    .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                    .build();

            LiteralCommandNode<ServerCommandSource> blockPlacementDelayNode = CommandManager
                    .literal("block_placement_delay")
                    .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                    .build();

            LiteralCommandNode<ServerCommandSource> shouldHealOnFlowingWaterNode = CommandManager
                    .literal("heal_on_flowing_water")
                    .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                    .build();

            LiteralCommandNode<ServerCommandSource> shouldHealOnFlowingLavaNode = CommandManager
                    .literal("heal_on_flowing_lava")
                    .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
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

            explosionHealDelayNode.addChild(healDelayArgumentNode);
            blockPlacementDelayNode.addChild(blockPlacementDelayArgumentNode);
            shouldHealOnFlowingWaterNode.addChild(healOnFlowingWaterArgumentNode);
            shouldHealOnFlowingLavaNode.addChild(healOnFlowingLavaArgumentNode);

        }));

    }


    private static int setExplosionHealDelayCommand(CommandContext<ServerCommandSource> serverCommandSourceCommandContext) throws CommandSyntaxException {

        CreeperHealing.CONFIG.setExplosionHealDelay(IntegerArgumentType.getInteger(serverCommandSourceCommandContext, "seconds"));

        serverCommandSourceCommandContext.getSource().sendMessage(Text.literal("Explosion heal delay has been set to: " + IntegerArgumentType.getInteger(serverCommandSourceCommandContext, "seconds")));

        return 1;

    }

    private static int setBlockPlacementDelayCommand(CommandContext<ServerCommandSource> serverCommandSourceCommandContext) throws CommandSyntaxException {

        CreeperHealing.CONFIG.setBlockPlacementDelay(IntegerArgumentType.getInteger(serverCommandSourceCommandContext, "seconds"));

        serverCommandSourceCommandContext.getSource().sendMessage(Text.literal("Block placement delay has been set to: " + IntegerArgumentType.getInteger(serverCommandSourceCommandContext, "seconds")));

        return 1;

    }

    private static int setShouldHealOnFlowingWaterCommand(CommandContext<ServerCommandSource> serverCommandSourceCommandContext) throws CommandSyntaxException {

        CreeperHealing.CONFIG.setShouldHealOnFlowingWater(BoolArgumentType.getBool(serverCommandSourceCommandContext, "value"));

        serverCommandSourceCommandContext.getSource().sendMessage(Text.literal("Heal on flowing water has been set to: " + BoolArgumentType.getBool(serverCommandSourceCommandContext, "value")));

        return 1;

    }

    private static int setShouldHealOnFlowingLavaCommand(CommandContext<ServerCommandSource> serverCommandSourceCommandContext) throws CommandSyntaxException {

        CreeperHealing.CONFIG.setShouldHealOnFlowingLava(BoolArgumentType.getBool(serverCommandSourceCommandContext, "value"));

        serverCommandSourceCommandContext.getSource().sendMessage(Text.literal("Heal on flowing lava has been set to: " + BoolArgumentType.getBool(serverCommandSourceCommandContext, "value")));

        return 1;

    }

}

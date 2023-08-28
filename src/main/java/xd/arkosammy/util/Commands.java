package xd.arkosammy.util;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import xd.arkosammy.CreeperHealing;

import java.io.IOException;

public class Commands {

    public static void registerCommands(){

        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> {

            //Root node
            LiteralCommandNode<ServerCommandSource> creeperHealingNode = CommandManager
                    .literal("creeper-healing")
                    .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                    .build();

            //Explosion Heal Delay node
            LiteralCommandNode<ServerCommandSource> explosionHealDelayNode = CommandManager
                    .literal("explosion_heal_delay")
                    .executes(Commands::getExplosionHealDelayCommand)
                    .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                    .build();

            //Block Placement Delay node
            LiteralCommandNode<ServerCommandSource> blockPlacementDelayNode = CommandManager
                    .literal("block_placement_delay")
                    .executes(Commands::getBlockPlacementDelayCommand)
                    .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                    .build();

            //Heal on Flowing Water node
            LiteralCommandNode<ServerCommandSource> shouldHealOnFlowingWaterNode = CommandManager
                    .literal("heal_on_flowing_water")
                    .executes(Commands::getShouldHealOnFlowingWaterCommand)
                    .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                    .build();

            //Heal on Flowing Lava node
            LiteralCommandNode<ServerCommandSource> shouldHealOnFlowingLavaNode = CommandManager
                    .literal("heal_on_flowing_lava")
                    .executes(Commands::getShouldHealOnFlowingLavaCommand)
                    .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                    .build();

            //Play sound on block placement node
            LiteralCommandNode<ServerCommandSource> shouldPlaySoundOnBlockPlacementNode = CommandManager
                    .literal("block_placement_sound_effect")
                    .executes(Commands::getShouldPlaySoundOnBlockPlacement)
                    .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                    .build();

            //Reload Config node
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

            ArgumentCommandNode<ServerCommandSource, Double> explosionHealDelayArgumentNode = CommandManager
                    .argument("seconds", DoubleArgumentType.doubleArg())
                    .executes(Commands::setExplosionHealDelayCommand)
                    .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                    .build();

            ArgumentCommandNode<ServerCommandSource, Double> blockPlacementDelayArgumentNode = CommandManager
                    .argument("seconds", DoubleArgumentType.doubleArg())
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

            ArgumentCommandNode<ServerCommandSource, Boolean> playSoundOnBlockPlacementArgumentNode = CommandManager
                    .argument("value", BoolArgumentType.bool())
                    .executes(Commands::setShouldPlaySoundOnBlockPlacement)
                    .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                    .build();

            //Root connections
            dispatcher.getRoot().addChild(creeperHealingNode);

            //Parent command connections
            creeperHealingNode.addChild(explosionHealDelayNode);
            creeperHealingNode.addChild(blockPlacementDelayNode);
            creeperHealingNode.addChild(shouldHealOnFlowingWaterNode);
            creeperHealingNode.addChild(shouldHealOnFlowingLavaNode);
            creeperHealingNode.addChild(shouldPlaySoundOnBlockPlacementNode);
            creeperHealingNode.addChild(reloadNode);

            //Argument node connections
            explosionHealDelayNode.addChild(explosionHealDelayArgumentNode);
            blockPlacementDelayNode.addChild(blockPlacementDelayArgumentNode);
            shouldHealOnFlowingWaterNode.addChild(healOnFlowingWaterArgumentNode);
            shouldHealOnFlowingLavaNode.addChild(healOnFlowingLavaArgumentNode);
            shouldPlaySoundOnBlockPlacementNode.addChild(playSoundOnBlockPlacementArgumentNode);


        }));

    }


    private static int setExplosionHealDelayCommand(CommandContext<ServerCommandSource> serverCommandSourceCommandContext) {

        if(Math.round(Math.max(DoubleArgumentType.getDouble(serverCommandSourceCommandContext, "seconds"), 0) * 20L) != 0) {

            CreeperHealing.CONFIG.setExplosionHealDelay(DoubleArgumentType.getDouble(serverCommandSourceCommandContext, "seconds"));

            serverCommandSourceCommandContext.getSource().sendMessage(Text.literal("Explosion heal delay has been set to: " + DoubleArgumentType.getDouble(serverCommandSourceCommandContext, "seconds") + " second(s)"));

        } else {

            serverCommandSourceCommandContext.getSource().sendMessage(Text.literal("Cannot set explosion heal delay to a very low value").formatted(Formatting.RED));

        }

        return 1;

    }

    private static int getExplosionHealDelayCommand(CommandContext<ServerCommandSource> serverCommandSourceCommandContext){

        serverCommandSourceCommandContext.getSource().sendMessage(Text.literal("Explosion heal delay currently set to: " + ((double)CreeperHealing.CONFIG.getExplosionDelay() / 20) + " second(s)"));

        return 1;

    }

    private static int setBlockPlacementDelayCommand(CommandContext<ServerCommandSource> serverCommandSourceCommandContext) {

        if (Math.round(Math.max(DoubleArgumentType.getDouble(serverCommandSourceCommandContext, "seconds"), 0) * 20L) != 0) {

            CreeperHealing.CONFIG.setBlockPlacementDelay(DoubleArgumentType.getDouble(serverCommandSourceCommandContext, "seconds"));

            serverCommandSourceCommandContext.getSource().sendMessage(Text.literal("Block placement delay has been set to to: " + DoubleArgumentType.getDouble(serverCommandSourceCommandContext, "seconds") + " second(s)"));

        } else {

            serverCommandSourceCommandContext.getSource().sendMessage(Text.literal("Cannot set block placement delay to a very low value").formatted(Formatting.RED));

        }

        return 1;

    }

    private static int getBlockPlacementDelayCommand(CommandContext<ServerCommandSource> serverCommandSourceCommandContext){

        serverCommandSourceCommandContext.getSource().sendMessage(Text.literal("Block placement delay currently set to: " + ((double)CreeperHealing.CONFIG.getBlockPlacementDelay() / 20) + " second(s)"));

        return 1;

    }

    private static int setShouldHealOnFlowingWaterCommand(CommandContext<ServerCommandSource> serverCommandSourceCommandContext) {

        CreeperHealing.CONFIG.setShouldHealOnFlowingWater(BoolArgumentType.getBool(serverCommandSourceCommandContext, "value"));

        serverCommandSourceCommandContext.getSource().sendMessage(Text.literal("Heal on flowing water has been set to: " + BoolArgumentType.getBool(serverCommandSourceCommandContext, "value")));

        return 1;

    }

    private static int getShouldHealOnFlowingWaterCommand(CommandContext<ServerCommandSource> serverCommandSourceCommandContext){

        serverCommandSourceCommandContext.getSource().sendMessage(Text.literal("Heal on flowing water currently set to: " + CreeperHealing.CONFIG.shouldHealOnFlowingWater()));

        return 1;

    }

    private static int setShouldHealOnFlowingLavaCommand(CommandContext<ServerCommandSource> serverCommandSourceCommandContext) {

        CreeperHealing.CONFIG.setShouldHealOnFlowingLava(BoolArgumentType.getBool(serverCommandSourceCommandContext, "value"));

        serverCommandSourceCommandContext.getSource().sendMessage(Text.literal("Heal on flowing lava has been set to: " + BoolArgumentType.getBool(serverCommandSourceCommandContext, "value")));

        return 1;

    }

    private static int getShouldHealOnFlowingLavaCommand(CommandContext<ServerCommandSource> serverCommandSourceCommandContext){

        serverCommandSourceCommandContext.getSource().sendMessage(Text.literal("Heal on flowing lava currently set to: " + CreeperHealing.CONFIG.shouldHealOnFlowingLava()));

        return 1;

    }

    private static int setShouldPlaySoundOnBlockPlacement(CommandContext<ServerCommandSource> serverCommandSourceCommandContext) {

        CreeperHealing.CONFIG.setShouldPlaySoundOnBlockPlacement(BoolArgumentType.getBool(serverCommandSourceCommandContext, "value"));

        serverCommandSourceCommandContext.getSource().sendMessage(Text.literal("Play sound on block placement has been set to: " + BoolArgumentType.getBool(serverCommandSourceCommandContext, "value")));

        return 1;

    }

    private static int getShouldPlaySoundOnBlockPlacement(CommandContext<ServerCommandSource> serverCommandSourceCommandContext) {

        serverCommandSourceCommandContext.getSource().sendMessage(Text.literal("Play sound on block placement currently set to: " + CreeperHealing.CONFIG.shouldPlaySoundOnBlockPlacement()));

        return 1;

    }

    private static void reload(CommandContext<ServerCommandSource> serverCommandSourceCommandContext) throws IOException {

        //If this returns true, then the config file exists, and we can update our values from it
        if(CreeperHealing.CONFIG.reloadConfig(serverCommandSourceCommandContext)) serverCommandSourceCommandContext.getSource().sendMessage(Text.literal("Config successfully reloaded"));
        else serverCommandSourceCommandContext.getSource().sendMessage(Text.literal("Found no existing config file to reload values from").formatted(Formatting.RED));

    }

}

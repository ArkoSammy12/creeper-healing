package xd.arkosammy.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import xd.arkosammy.configuration.Config;
import xd.arkosammy.configuration.tables.DelaysConfig;
import xd.arkosammy.configuration.tables.ExplosionSourceConfig;
import xd.arkosammy.configuration.tables.ModeConfig;
import xd.arkosammy.configuration.tables.PreferencesConfig;
import xd.arkosammy.explosions.AffectedBlock;
import java.io.IOException;

public final class Commands {

    private Commands(){}
    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment){

        //Root node
        LiteralCommandNode<ServerCommandSource> creeperHealingNode = CommandManager
                .literal("creeper-healing")
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Mode node
        LiteralCommandNode<ServerCommandSource> modeMode = CommandManager
                .literal("mode")
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Explosion source node
        LiteralCommandNode<ServerCommandSource> explosionSourceMode = CommandManager
                .literal("explosion_source")
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Daytime healing node
        LiteralCommandNode<ServerCommandSource> doDayTimeHealingNode = CommandManager
                .literal("daytime_healing_mode")
                .executes(Commands::getDoDayLightHealingCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Settings node
        LiteralCommandNode<ServerCommandSource> settingsNode = CommandManager
                .literal("settings")
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

        //Drop items on creeper explosions node
        LiteralCommandNode<ServerCommandSource> dropItemsOnCreeperExplosionsNode = CommandManager
                .literal("drop_items_on_creeper_explosions")
                .executes(Commands::getDropItemsOnExplosionCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Requires light node
        LiteralCommandNode<ServerCommandSource> requiresLightNode = CommandManager
                .literal("requires_light")
                .executes(Commands::getRequiresLightCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Reload Config node
        LiteralCommandNode<ServerCommandSource> reloadNode = CommandManager
                .literal("reload")
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .executes(context -> {

                    try {
                        Commands.reload(context);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    return Command.SINGLE_SUCCESS;

                })
                .build();

        //Heal creeper explosions node
        LiteralCommandNode<ServerCommandSource> healCreeperExplosionsNode = CommandManager
                .literal("heal_creeper_explosions")
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .executes(Commands::getHealCreeperExplosionsCommand)
                .build();

        //Heal ghast explosions node
        LiteralCommandNode<ServerCommandSource> healGhastExplosionsNode = CommandManager
                .literal("heal_ghast_explosions")
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .executes(Commands::getHealGhastExplosionsCommand)
                .build();

        //Heal wither explosions node
        LiteralCommandNode<ServerCommandSource> healWitherExplosionsNode = CommandManager
                .literal("heal_wither_explosions")
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .executes(Commands::getHealWitherExplosionsCommand)
                .build();

        //Heal tnt explosions node
        LiteralCommandNode<ServerCommandSource> healTNTExplosionsNode = CommandManager
                .literal("heal_tnt_explosions")
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .executes(Commands::getHealTNTExplosionsCommand)
                .build();

        //Heal tnt minecart explosions node
        LiteralCommandNode<ServerCommandSource> healTNTMinecartExplosionsNode = CommandManager
                .literal("heal_tnt_minecart_explosions")
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .executes(Commands::getHealTNTMinecartExplosionCommand)
                .build();

        //Daytime healing mode argument node
        ArgumentCommandNode<ServerCommandSource, Boolean> doDayLightHealingArgumentNode = CommandManager
                .argument("value", BoolArgumentType.bool())
                .executes(Commands::setDoDayTimeHealingCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Explosion heal delay argument node
        ArgumentCommandNode<ServerCommandSource, Double> explosionHealDelayArgumentNode = CommandManager
                .argument("seconds", DoubleArgumentType.doubleArg())
                .executes(Commands::setExplosionHealDelayCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Block placement delay argument node
        ArgumentCommandNode<ServerCommandSource, Double> blockPlacementDelayArgumentNode = CommandManager
                .argument("seconds", DoubleArgumentType.doubleArg())
                .executes(Commands::setBlockPlacementDelayCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Heal on flowing water argument node
        ArgumentCommandNode<ServerCommandSource, Boolean> healOnFlowingWaterArgumentNode = CommandManager
                .argument("value", BoolArgumentType.bool())
                .executes(Commands::setHealOnFlowingWaterCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Heal on flowing lava argument node
        ArgumentCommandNode<ServerCommandSource, Boolean> healOnFlowingLavaArgumentNode = CommandManager
                .argument("value", BoolArgumentType.bool())
                .executes(Commands::setHealOnFlowingLavaCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Play sound on block placement argument node
        ArgumentCommandNode<ServerCommandSource, Boolean> playSoundOnBlockPlacementArgumentNode = CommandManager
                .argument("value", BoolArgumentType.bool())
                .executes(Commands::setPlaySoundOnBlockPlacement)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Drop items on creeper argument node
        ArgumentCommandNode<ServerCommandSource, Boolean> dropItemsOnCreeperExplosionsArgumentNode = CommandManager
                .argument("value", BoolArgumentType.bool())
                .executes(Commands::setDropItemsOnExplosionCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();


        //Requires light argument node
        ArgumentCommandNode<ServerCommandSource, Boolean> requiresLightArgumentNode = CommandManager
                .argument("value", BoolArgumentType.bool())
                .executes(Commands::setRequiresLightCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Heal creeper explosions argument node
        ArgumentCommandNode<ServerCommandSource, Boolean> healCreeperExplosionsArgumentNode = CommandManager
                .argument("value", BoolArgumentType.bool())
                .executes(Commands::setHealCreeperExplosionsCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Heal ghast explosions argument node
        ArgumentCommandNode<ServerCommandSource, Boolean> healGhastExplosionsArgumentNode = CommandManager
                .argument("value", BoolArgumentType.bool())
                .executes(Commands::setHealGhastExplosionsCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Heal wither explosions argument node
        ArgumentCommandNode<ServerCommandSource, Boolean> healWitherExplosionsArgumentNode = CommandManager
                .argument("value", BoolArgumentType.bool())
                .executes(Commands::setHealWitherExplosionsCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Heal tnt explosions argument node
        ArgumentCommandNode<ServerCommandSource, Boolean> healTNTExplosionsArgumentNode = CommandManager
                .argument("value", BoolArgumentType.bool())
                .executes(Commands::setHealTNTExplosionsCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Heal tnt minecart explosions argument node
        ArgumentCommandNode<ServerCommandSource, Boolean> healTNTMinecartExplosionsArgumentNode = CommandManager
                .argument("value", BoolArgumentType.bool())
                .executes(Commands::setHealTNTMinecartExplosionsCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();


        //Root connections
        dispatcher.getRoot().addChild(creeperHealingNode);

        //Parent command connections
        creeperHealingNode.addChild(settingsNode);
        creeperHealingNode.addChild(modeMode);
        creeperHealingNode.addChild(explosionSourceMode);

        //Settings command connections
        settingsNode.addChild(explosionHealDelayNode);
        settingsNode.addChild(blockPlacementDelayNode);
        settingsNode.addChild(dropItemsOnCreeperExplosionsNode);
        settingsNode.addChild(shouldHealOnFlowingWaterNode);
        settingsNode.addChild(shouldHealOnFlowingLavaNode);
        settingsNode.addChild(shouldPlaySoundOnBlockPlacementNode);
        settingsNode.addChild(requiresLightNode);
        settingsNode.addChild(reloadNode);

        //Mode command connections
        modeMode.addChild(doDayTimeHealingNode);

        //Explosion source command connections
        explosionSourceMode.addChild(healCreeperExplosionsNode);
        explosionSourceMode.addChild(healGhastExplosionsNode);
        explosionSourceMode.addChild(healWitherExplosionsNode);
        explosionSourceMode.addChild(healTNTExplosionsNode);
        explosionSourceMode.addChild(healTNTMinecartExplosionsNode);

        //Argument node connections
        doDayTimeHealingNode.addChild(doDayLightHealingArgumentNode);
        explosionHealDelayNode.addChild(explosionHealDelayArgumentNode);
        blockPlacementDelayNode.addChild(blockPlacementDelayArgumentNode);
        shouldHealOnFlowingWaterNode.addChild(healOnFlowingWaterArgumentNode);
        shouldHealOnFlowingLavaNode.addChild(healOnFlowingLavaArgumentNode);
        shouldPlaySoundOnBlockPlacementNode.addChild(playSoundOnBlockPlacementArgumentNode);
        dropItemsOnCreeperExplosionsNode.addChild(dropItemsOnCreeperExplosionsArgumentNode);
        requiresLightNode.addChild(requiresLightArgumentNode);

        healCreeperExplosionsNode.addChild(healCreeperExplosionsArgumentNode);
        healGhastExplosionsNode.addChild(healGhastExplosionsArgumentNode);
        healWitherExplosionsNode.addChild(healWitherExplosionsArgumentNode);
        healTNTExplosionsNode.addChild(healTNTExplosionsArgumentNode);
        healTNTMinecartExplosionsNode.addChild(healTNTMinecartExplosionsArgumentNode);

    }

    /*
    ======================================= COMMAND SETTERS =======================================
     */

    private static int setDoDayTimeHealingCommand(CommandContext<ServerCommandSource> ctx){

        ModeConfig.setDaytimeHealingMode(BoolArgumentType.getBool(ctx, "value"));

        ctx.getSource().sendMessage(Text.literal("Daytime healing mode has been set to: " + BoolArgumentType.getBool(ctx, "value")));

        return Command.SINGLE_SUCCESS;

    }

    private static int setExplosionHealDelayCommand(CommandContext<ServerCommandSource> ctx) {

        if(Math.round(Math.max(DoubleArgumentType.getDouble(ctx, "seconds"), 0) * 20L) != 0) {

            DelaysConfig.setExplosionHealDelay(DoubleArgumentType.getDouble(ctx, "seconds"));

            ctx.getSource().sendMessage(Text.literal("Explosion heal delay has been set to: " + DoubleArgumentType.getDouble(ctx, "seconds") + " second(s)"));

        } else {

            ctx.getSource().sendMessage(Text.literal("Cannot set explosion heal delay to a very low value").formatted(Formatting.RED));

        }

        return Command.SINGLE_SUCCESS;

    }

    private static int setBlockPlacementDelayCommand(CommandContext<ServerCommandSource> ctx) {

        if (Math.round(Math.max(DoubleArgumentType.getDouble(ctx, "seconds"), 0) * 20L) != 0) {

            DelaysConfig.setBlockPlacementDelay(DoubleArgumentType.getDouble(ctx, "seconds"));

            AffectedBlock.updateAffectedBlocksTimers();

            ctx.getSource().sendMessage(Text.literal("Block placement delay has been set to to: " + DoubleArgumentType.getDouble(ctx, "seconds") + " second(s)"));

        } else {

            ctx.getSource().sendMessage(Text.literal("Cannot set block placement delay to a very low value").formatted(Formatting.RED));

        }

        return Command.SINGLE_SUCCESS;

    }

    private static int setHealOnFlowingWaterCommand(CommandContext<ServerCommandSource> ctx) {

        PreferencesConfig.setHealOnFlowingWater(BoolArgumentType.getBool(ctx, "value"));

        ctx.getSource().sendMessage(Text.literal("Heal on flowing water has been set to: " + BoolArgumentType.getBool(ctx, "value")));

        return Command.SINGLE_SUCCESS;

    }

    private static int setHealOnFlowingLavaCommand(CommandContext<ServerCommandSource> ctx) {

        PreferencesConfig.setHealOnFlowingLava(BoolArgumentType.getBool(ctx, "value"));

        ctx.getSource().sendMessage(Text.literal("Heal on flowing lava has been set to: " + BoolArgumentType.getBool(ctx, "value")));

        return Command.SINGLE_SUCCESS;

    }

    private static int setPlaySoundOnBlockPlacement(CommandContext<ServerCommandSource> ctx) {

        PreferencesConfig.setBlockPlacementSoundEffect(BoolArgumentType.getBool(ctx, "value"));

        ctx.getSource().sendMessage(Text.literal("Play sound on block placement has been set to: " + BoolArgumentType.getBool(ctx, "value")));

        return Command.SINGLE_SUCCESS;

    }

    private static int setDropItemsOnExplosionCommand(CommandContext<ServerCommandSource> ctx){

        PreferencesConfig.setDropItemsOnExplosions(BoolArgumentType.getBool(ctx, "value"));

        ctx.getSource().sendMessage(Text.literal("Drop items on explosions has been set to: " + BoolArgumentType.getBool(ctx, "value")));

        return Command.SINGLE_SUCCESS;

    }

    private static int setRequiresLightCommand(CommandContext<ServerCommandSource> ctx){

        PreferencesConfig.setRequiresLight(BoolArgumentType.getBool(ctx, "value"));

        ctx.getSource().sendMessage(Text.literal("Requires light has been set to: " + BoolArgumentType.getBool(ctx, "value")));

        return Command.SINGLE_SUCCESS;

    }

    private static int setHealCreeperExplosionsCommand(CommandContext<ServerCommandSource> ctx){

        ExplosionSourceConfig.setHealCreeperExplosions(BoolArgumentType.getBool(ctx, "value"));

        ctx.getSource().sendMessage(Text.literal("Heal creeper explosions has been set to: " + BoolArgumentType.getBool(ctx, "value")));

        return Command.SINGLE_SUCCESS;

    }

    private static int setHealGhastExplosionsCommand(CommandContext<ServerCommandSource> ctx){

        ExplosionSourceConfig.setHealGhastExplosions(BoolArgumentType.getBool(ctx, "value"));

        ctx.getSource().sendMessage(Text.literal("Heal ghast explosions has been set to: " + BoolArgumentType.getBool(ctx, "value")));

        return Command.SINGLE_SUCCESS;

    }

    private static int setHealWitherExplosionsCommand(CommandContext<ServerCommandSource> ctx){

        ExplosionSourceConfig.setHealWitherExplosions(BoolArgumentType.getBool(ctx, "value"));

        ctx.getSource().sendMessage(Text.literal("Heal wither explosions has been set to: " + BoolArgumentType.getBool(ctx, "value")));

        return Command.SINGLE_SUCCESS;

    }

    private static int setHealTNTExplosionsCommand(CommandContext<ServerCommandSource> ctx){

        ExplosionSourceConfig.setHealTNTExplosions(BoolArgumentType.getBool(ctx, "value"));

        ctx.getSource().sendMessage(Text.literal("Heal tnt explosions has been set to: " + BoolArgumentType.getBool(ctx, "value")));

        return Command.SINGLE_SUCCESS;

    }

    private static int setHealTNTMinecartExplosionsCommand(CommandContext<ServerCommandSource> ctx){

        ExplosionSourceConfig.setHealTNTMinecartExplosions(BoolArgumentType.getBool(ctx, "value"));

        ctx.getSource().sendMessage(Text.literal("Heal tnt minecart explosions has been set to: " + BoolArgumentType.getBool(ctx, "value")));

        return Command.SINGLE_SUCCESS;

    }

        /*
    ======================================= COMMAND GETTERS =======================================
     */

    private static int getDoDayLightHealingCommand(CommandContext<ServerCommandSource> ctx){

        ctx.getSource().sendMessage(Text.literal("Daytime healing mode currently set to: " + ModeConfig.getDayTimeHealingMode()));

        return Command.SINGLE_SUCCESS;

    }

    private static int getExplosionHealDelayCommand(CommandContext<ServerCommandSource> ctx){

        ctx.getSource().sendMessage(Text.literal("Explosion heal delay currently set to: " + ((double)DelaysConfig.getExplosionHealDelay() / 20) + " second(s)"));

        return Command.SINGLE_SUCCESS;

    }

    private static int getBlockPlacementDelayCommand(CommandContext<ServerCommandSource> ctx){

        ctx.getSource().sendMessage(Text.literal("Block placement delay currently set to: " + ((double) DelaysConfig.getBlockPlacementDelay() / 20) + " second(s)"));

        return Command.SINGLE_SUCCESS;

    }

    private static int getShouldHealOnFlowingWaterCommand(CommandContext<ServerCommandSource> ctx){

        ctx.getSource().sendMessage(Text.literal("Heal on flowing water currently set to: " + PreferencesConfig.getHealOnFlowingWater()));

        return Command.SINGLE_SUCCESS;

    }

    private static int getShouldHealOnFlowingLavaCommand(CommandContext<ServerCommandSource> ctx){

        ctx.getSource().sendMessage(Text.literal("Heal on flowing lava currently set to: " + PreferencesConfig.getHealOnFlowingLava()));

        return Command.SINGLE_SUCCESS;

    }

    private static int getShouldPlaySoundOnBlockPlacement(CommandContext<ServerCommandSource> ctx) {

        ctx.getSource().sendMessage(Text.literal("Play sound on block placement currently set to: " + PreferencesConfig.getBlockPlacementSoundEffect()));

        return Command.SINGLE_SUCCESS;

    }

    private static int getDropItemsOnExplosionCommand(CommandContext<ServerCommandSource> ctx){

        ctx.getSource().sendMessage(Text.literal("Drop items on explosions currently set to: " + PreferencesConfig.getDropItemsOnExplosions()));

        return Command.SINGLE_SUCCESS;

    }

    private static int getRequiresLightCommand(CommandContext<ServerCommandSource> ctx){

        ctx.getSource().sendMessage(Text.literal("Requires light currently set to: " + PreferencesConfig.getRequiresLight()));

        return Command.SINGLE_SUCCESS;

    }

    private static int getHealCreeperExplosionsCommand(CommandContext<ServerCommandSource> ctx){

        ctx.getSource().sendMessage(Text.literal("Heal creeper explosions currently set to: " + ExplosionSourceConfig.getHealCreeperExplosions()));

        return Command.SINGLE_SUCCESS;

    }

    private static int getHealGhastExplosionsCommand(CommandContext<ServerCommandSource> ctx){

        ctx.getSource().sendMessage(Text.literal("Heal ghast explosions currently set to: " + ExplosionSourceConfig.getHealGhastExplosions()));

        return Command.SINGLE_SUCCESS;

    }

    private static int getHealWitherExplosionsCommand(CommandContext<ServerCommandSource> ctx){

        ctx.getSource().sendMessage(Text.literal("Heal wither explosions currently set to: " + ExplosionSourceConfig.getHealWitherExplosions()));

        return Command.SINGLE_SUCCESS;

    }

    private static int getHealTNTExplosionsCommand(CommandContext<ServerCommandSource> ctx){

        ctx.getSource().sendMessage(Text.literal("Heal tnt explosions currently set to: " + ExplosionSourceConfig.getHealTNTExplosions()));

        return Command.SINGLE_SUCCESS;

    }

    private static int getHealTNTMinecartExplosionCommand(CommandContext<ServerCommandSource> ctx){

        ctx.getSource().sendMessage(Text.literal("Heal tnt minecart explosions currently set to: " + ExplosionSourceConfig.getHealTNTMinecartExplosions()));

        return Command.SINGLE_SUCCESS;

    }

            /*
    ======================================= RELOAD COMMAND =======================================
     */

    private static void reload(CommandContext<ServerCommandSource> ctx) throws IOException {

        //If this returns true, then the config file exists, and we can update our values from it
        if(Config.reloadConfigSettingsInMemory(ctx)) ctx.getSource().sendMessage(Text.literal("Config successfully reloaded"));
        else ctx.getSource().sendMessage(Text.literal("Found no existing config file to reload values from").formatted(Formatting.RED));

    }

}

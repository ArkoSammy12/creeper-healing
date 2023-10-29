package xd.arkosammy.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import xd.arkosammy.configuration.tables.DelaysConfig;
import xd.arkosammy.configuration.tables.PreferencesConfig;
import xd.arkosammy.explosions.AffectedBlock;


final class PreferencesCommands {

    private PreferencesCommands(){}

    static void register(LiteralCommandNode<ServerCommandSource> creeperHealingNode){

        //Settings node
        LiteralCommandNode<ServerCommandSource> settingsNode = CommandManager
                .literal("preferences")
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Explosion Heal Delay node
        LiteralCommandNode<ServerCommandSource> explosionHealDelayNode = CommandManager
                .literal("explosion_heal_delay")
                .executes(PreferencesCommands::getExplosionHealDelayCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Block Placement Delay node
        LiteralCommandNode<ServerCommandSource> blockPlacementDelayNode = CommandManager
                .literal("block_placement_delay")
                .executes(PreferencesCommands::getBlockPlacementDelayCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Heal on Flowing Water node
        LiteralCommandNode<ServerCommandSource> shouldHealOnFlowingWaterNode = CommandManager
                .literal("heal_on_flowing_water")
                .executes(PreferencesCommands::getShouldHealOnFlowingWaterCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Heal on Flowing Lava node
        LiteralCommandNode<ServerCommandSource> shouldHealOnFlowingLavaNode = CommandManager
                .literal("heal_on_flowing_lava")
                .executes(PreferencesCommands::getShouldHealOnFlowingLavaCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Play sound on block placement node
        LiteralCommandNode<ServerCommandSource> shouldPlaySoundOnBlockPlacementNode = CommandManager
                .literal("block_placement_sound_effect")
                .executes(PreferencesCommands::getShouldPlaySoundOnBlockPlacement)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Drop items on creeper explosions node
        LiteralCommandNode<ServerCommandSource> dropItemsOnCreeperExplosionsNode = CommandManager
                .literal("drop_items_on_explosions")
                .executes(PreferencesCommands::getDropItemsOnExplosionCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Heal on healing potion splash node
        LiteralCommandNode<ServerCommandSource> healOnHealingPotionSplashNode = CommandManager
                .literal("heal_on_healing_potion_splash")
                .executes(PreferencesCommands::getHealOnHealingPotionSplashCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Heal on regeneration potion splash node
        LiteralCommandNode<ServerCommandSource> healOnRegenerationPotionSplash = CommandManager
                .literal("heal_on_regeneration_potion_splash")
                .executes(PreferencesCommands::getHealOnRegenerationPotionSplashCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Enable whitelist node
        LiteralCommandNode<ServerCommandSource> enableWhitelistNode = CommandManager
                .literal("enable_whitelist")
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .executes(PreferencesCommands::getEnableWhitelist)
                .build();

        //Explosion heal delay argument node
        ArgumentCommandNode<ServerCommandSource, Double> explosionHealDelayArgumentNode = CommandManager
                .argument("seconds", DoubleArgumentType.doubleArg())
                .executes(PreferencesCommands::setExplosionHealDelayCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Block placement delay argument node
        ArgumentCommandNode<ServerCommandSource, Double> blockPlacementDelayArgumentNode = CommandManager
                .argument("seconds", DoubleArgumentType.doubleArg())
                .executes(PreferencesCommands::setBlockPlacementDelayCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Heal on flowing water argument node
        ArgumentCommandNode<ServerCommandSource, Boolean> healOnFlowingWaterArgumentNode = CommandManager
                .argument("value", BoolArgumentType.bool())
                .executes(PreferencesCommands::setHealOnFlowingWaterCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Heal on flowing lava argument node
        ArgumentCommandNode<ServerCommandSource, Boolean> healOnFlowingLavaArgumentNode = CommandManager
                .argument("value", BoolArgumentType.bool())
                .executes(PreferencesCommands::setHealOnFlowingLavaCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Play sound on block placement argument node
        ArgumentCommandNode<ServerCommandSource, Boolean> playSoundOnBlockPlacementArgumentNode = CommandManager
                .argument("value", BoolArgumentType.bool())
                .executes(PreferencesCommands::setPlaySoundOnBlockPlacement)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Drop items on creeper argument node
        ArgumentCommandNode<ServerCommandSource, Boolean> dropItemsOnCreeperExplosionsArgumentNode = CommandManager
                .argument("value", BoolArgumentType.bool())
                .executes(PreferencesCommands::setDropItemsOnExplosionCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Heal on Healing potion splash argument node
        ArgumentCommandNode<ServerCommandSource, Boolean> healOnHealingPotionSplashArgumentNode = CommandManager
                .argument("value", BoolArgumentType.bool())
                .executes(PreferencesCommands::setHealOnHealingPotionSplashCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Heal on Regeneration potion splash argument node
        ArgumentCommandNode<ServerCommandSource, Boolean> healOnRegenerationPotionSplashArgumentNode = CommandManager
                .argument("value", BoolArgumentType.bool())
                .executes(PreferencesCommands::setHealOnRegenerationPotionSplashCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Enable whitelist argument node
        ArgumentCommandNode<ServerCommandSource, Boolean> enableWhitelistArgumentNode = CommandManager
                .argument("value", BoolArgumentType.bool())
                .executes(PreferencesCommands::setEnableWhitelist)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Root node connection
        creeperHealingNode.addChild(settingsNode);

        //Settings commands nodes
        settingsNode.addChild(explosionHealDelayNode);
        settingsNode.addChild(blockPlacementDelayNode);
        settingsNode.addChild(dropItemsOnCreeperExplosionsNode);
        settingsNode.addChild(shouldHealOnFlowingWaterNode);
        settingsNode.addChild(shouldHealOnFlowingLavaNode);
        settingsNode.addChild(shouldPlaySoundOnBlockPlacementNode);
        settingsNode.addChild(healOnHealingPotionSplashNode);
        settingsNode.addChild(healOnRegenerationPotionSplash);
        settingsNode.addChild(enableWhitelistNode);

        //Argument nodes
        explosionHealDelayNode.addChild(explosionHealDelayArgumentNode);
        blockPlacementDelayNode.addChild(blockPlacementDelayArgumentNode);
        shouldHealOnFlowingWaterNode.addChild(healOnFlowingWaterArgumentNode);
        shouldHealOnFlowingLavaNode.addChild(healOnFlowingLavaArgumentNode);
        shouldPlaySoundOnBlockPlacementNode.addChild(playSoundOnBlockPlacementArgumentNode);
        dropItemsOnCreeperExplosionsNode.addChild(dropItemsOnCreeperExplosionsArgumentNode);
        healOnHealingPotionSplashNode.addChild(healOnHealingPotionSplashArgumentNode);
        healOnRegenerationPotionSplash.addChild(healOnRegenerationPotionSplashArgumentNode);
        enableWhitelistNode.addChild(enableWhitelistArgumentNode);

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
            ctx.getSource().sendMessage(Text.literal("Block placement delay has been set to: " + DoubleArgumentType.getDouble(ctx, "seconds") + " second(s)"));
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

    private static int setHealOnHealingPotionSplashCommand(CommandContext<ServerCommandSource> ctx){
        PreferencesConfig.setHealOnHealingPotionSplash(BoolArgumentType.getBool(ctx, "value"));
        ctx.getSource().sendMessage(Text.literal("Heal on Healing potion splash set to: " + BoolArgumentType.getBool(ctx, "value")));
        return Command.SINGLE_SUCCESS;
    }

    private static int setHealOnRegenerationPotionSplashCommand(CommandContext<ServerCommandSource> ctx){
        PreferencesConfig.setHealOnRegenerationPotionSplash(BoolArgumentType.getBool(ctx, "value"));
        ctx.getSource().sendMessage(Text.literal("Heal on Regeneration potion splash set to: " + BoolArgumentType.getBool(ctx, "value")));
        return Command.SINGLE_SUCCESS;
    }

    private static int setEnableWhitelist(CommandContext<ServerCommandSource> ctx){
        PreferencesConfig.setEnableWhitelist(BoolArgumentType.getBool(ctx, "value"));
        ctx.getSource().sendMessage(Text.literal("The whitelist has been " + (BoolArgumentType.getBool(ctx, "value") ? "enabled" : "disabled")));
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

    private static int getHealOnHealingPotionSplashCommand(CommandContext<ServerCommandSource> ctx){
        ctx.getSource().sendMessage(Text.literal("Heal on Healing potion splash set to: " + PreferencesConfig.getHealOnHealingPotionSplash()));
        return Command.SINGLE_SUCCESS;
    }

    private static int getHealOnRegenerationPotionSplashCommand(CommandContext<ServerCommandSource> ctx){
        ctx.getSource().sendMessage(Text.literal("Heal on Regeneration potion splash set to: " + PreferencesConfig.getHealOnRegenerationPotionSplash()));
        return Command.SINGLE_SUCCESS;
    }

    private static int getEnableWhitelist(CommandContext<ServerCommandSource> ctx){
        ctx.getSource().sendMessage(Text.literal("The whitelist is currently " + (PreferencesConfig.getEnableWhitelist() ? "enabled" : "disabled")));
        return Command.SINGLE_SUCCESS;
    }


}

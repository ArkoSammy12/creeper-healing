package xd.arkosammy.commands.categories;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import xd.arkosammy.configuration.tables.PreferencesConfig;


public final class PreferencesCommands {

    private PreferencesCommands(){}

    public static void register(LiteralCommandNode<ServerCommandSource> creeperHealingNode){

        //Preferences node
        LiteralCommandNode<ServerCommandSource> settingsNode = CommandManager
                .literal("preferences")
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Heal on Flowing Water node
        LiteralCommandNode<ServerCommandSource> shouldHealOnFlowingWaterNode = CommandManager
                .literal("heal_on_flowing_water")
                .executes(PreferencesCommands::getShouldHealOnFlowingWaterCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Heal on Source Water node
        LiteralCommandNode<ServerCommandSource> shouldHealOnSourceWaterNode = CommandManager
                .literal("heal_on_source_water")
                .executes(PreferencesCommands::getShouldHealOnSourceWaterCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Heal on Flowing Lava node
        LiteralCommandNode<ServerCommandSource> shouldHealOnFlowingLavaNode = CommandManager
                .literal("heal_on_flowing_lava")
                .executes(PreferencesCommands::getShouldHealOnFlowingLavaCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Heal on Source Lava node
        LiteralCommandNode<ServerCommandSource> shouldHealOnSourceLavaNode = CommandManager
                .literal("heal_on_source_lava")
                .executes(PreferencesCommands::getShouldHealOnSourceLavaCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Play sound on block placement node
        LiteralCommandNode<ServerCommandSource> shouldPlaySoundOnBlockPlacementNode = CommandManager
                .literal("block_placement_sound_effect")
                .executes(PreferencesCommands::getShouldPlaySoundOnBlockPlacement)
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

        //Heal on flowing water argument node
        ArgumentCommandNode<ServerCommandSource, Boolean> healOnFlowingWaterArgumentNode = CommandManager
                .argument("value", BoolArgumentType.bool())
                .executes(PreferencesCommands::setHealOnFlowingWaterCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Heal on source water argument node
        ArgumentCommandNode<ServerCommandSource, Boolean> healOnSourceWaterArgumentNode = CommandManager
                .argument("value", BoolArgumentType.bool())
                .executes(PreferencesCommands::setHealOnSourceWaterCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Heal on flowing lava argument node
        ArgumentCommandNode<ServerCommandSource, Boolean> healOnFlowingLavaArgumentNode = CommandManager
                .argument("value", BoolArgumentType.bool())
                .executes(PreferencesCommands::setHealOnFlowingLavaCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Heal on source lava argument node
        ArgumentCommandNode<ServerCommandSource, Boolean> healOnSourceLavaArgumentNode = CommandManager
                .argument("value", BoolArgumentType.bool())
                .executes(PreferencesCommands::setHealOnSourceLavaCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Play sound on block placement argument node
        ArgumentCommandNode<ServerCommandSource, Boolean> playSoundOnBlockPlacementArgumentNode = CommandManager
                .argument("value", BoolArgumentType.bool())
                .executes(PreferencesCommands::setPlaySoundOnBlockPlacement)
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

        //Preferences commands nodes
        settingsNode.addChild(shouldHealOnFlowingWaterNode);
        settingsNode.addChild(shouldHealOnSourceWaterNode);
        settingsNode.addChild(shouldHealOnFlowingLavaNode);
        settingsNode.addChild(shouldHealOnSourceLavaNode);
        settingsNode.addChild(shouldPlaySoundOnBlockPlacementNode);
        settingsNode.addChild(healOnHealingPotionSplashNode);
        settingsNode.addChild(healOnRegenerationPotionSplash);
        settingsNode.addChild(enableWhitelistNode);

        //Argument nodes
        shouldHealOnFlowingWaterNode.addChild(healOnFlowingWaterArgumentNode);
        shouldHealOnSourceWaterNode.addChild(healOnSourceWaterArgumentNode);
        shouldHealOnFlowingLavaNode.addChild(healOnFlowingLavaArgumentNode);
        shouldHealOnSourceLavaNode.addChild(healOnSourceLavaArgumentNode);
        shouldPlaySoundOnBlockPlacementNode.addChild(playSoundOnBlockPlacementArgumentNode);
        healOnHealingPotionSplashNode.addChild(healOnHealingPotionSplashArgumentNode);
        healOnRegenerationPotionSplash.addChild(healOnRegenerationPotionSplashArgumentNode);
        enableWhitelistNode.addChild(enableWhitelistArgumentNode);

    }

    private static int setHealOnFlowingWaterCommand(CommandContext<ServerCommandSource> ctx) {
        PreferencesConfig.HEAL_ON_FLOWING_WATER.getEntry().setValue(BoolArgumentType.getBool(ctx, "value"));
        ctx.getSource().sendMessage(Text.literal("Heal on flowing water has been set to: " + BoolArgumentType.getBool(ctx, "value")));
        return Command.SINGLE_SUCCESS;
    }

    private static int setHealOnSourceWaterCommand(CommandContext<ServerCommandSource> ctx){
        PreferencesConfig.HEAL_ON_SOURCE_WATER.getEntry().setValue(BoolArgumentType.getBool(ctx, "value"));
        ctx.getSource().sendMessage(Text.literal("Heal on source water has been set to: " + BoolArgumentType.getBool(ctx, "value")));
        return Command.SINGLE_SUCCESS;
    }

    private static int setHealOnFlowingLavaCommand(CommandContext<ServerCommandSource> ctx) {
        PreferencesConfig.HEAL_ON_FLOWING_LAVA.getEntry().setValue(BoolArgumentType.getBool(ctx, "value"));
        ctx.getSource().sendMessage(Text.literal("Heal on flowing lava has been set to: " + BoolArgumentType.getBool(ctx, "value")));
        return Command.SINGLE_SUCCESS;
    }

    private static int setHealOnSourceLavaCommand(CommandContext<ServerCommandSource> ctx){
        PreferencesConfig.HEAL_ON_SOURCE_LAVA.getEntry().setValue(BoolArgumentType.getBool(ctx, "value"));
        ctx.getSource().sendMessage(Text.literal("Heal on source lava has been set to: " + BoolArgumentType.getBool(ctx, "value")));
        return Command.SINGLE_SUCCESS;
    }

    private static int setPlaySoundOnBlockPlacement(CommandContext<ServerCommandSource> ctx) {
        PreferencesConfig.BLOCK_PLACEMENT_SOUND_EFFECT.getEntry().setValue(BoolArgumentType.getBool(ctx, "value"));
        ctx.getSource().sendMessage(Text.literal("Play sound on block placement has been set to: " + BoolArgumentType.getBool(ctx, "value")));
        return Command.SINGLE_SUCCESS;
    }

    private static int setHealOnHealingPotionSplashCommand(CommandContext<ServerCommandSource> ctx){
        PreferencesConfig.HEAL_ON_HEALING_POTION_SPLASH.getEntry().setValue(BoolArgumentType.getBool(ctx, "value"));
        ctx.getSource().sendMessage(Text.literal("Heal on Healing potion splash set to: " + BoolArgumentType.getBool(ctx, "value")));
        return Command.SINGLE_SUCCESS;
    }

    private static int setHealOnRegenerationPotionSplashCommand(CommandContext<ServerCommandSource> ctx){
        PreferencesConfig.HEAL_ON_REGENERATION_POTION_SPLASH.getEntry().setValue(BoolArgumentType.getBool(ctx, "value"));
        ctx.getSource().sendMessage(Text.literal("Heal on Regeneration potion splash set to: " + BoolArgumentType.getBool(ctx, "value")));
        return Command.SINGLE_SUCCESS;
    }

    private static int setEnableWhitelist(CommandContext<ServerCommandSource> ctx){
        PreferencesConfig.ENABLE_WHITELIST.getEntry().setValue(BoolArgumentType.getBool(ctx, "value"));
        ctx.getSource().sendMessage(Text.literal("The whitelist has been " + (BoolArgumentType.getBool(ctx, "value") ? "enabled" : "disabled")));
        return Command.SINGLE_SUCCESS;
    }

    private static int getShouldHealOnFlowingWaterCommand(CommandContext<ServerCommandSource> ctx){
        ctx.getSource().sendMessage(Text.literal("Heal on flowing water currently set to: " + PreferencesConfig.HEAL_ON_FLOWING_WATER.getEntry().getValue()));
        return Command.SINGLE_SUCCESS;
    }

    private static int getShouldHealOnSourceWaterCommand(CommandContext<ServerCommandSource> ctx){
        ctx.getSource().sendMessage(Text.literal("Heal on source water currently set to: " + PreferencesConfig.HEAL_ON_SOURCE_WATER.getEntry().getValue()));
        return Command.SINGLE_SUCCESS;
    }

    private static int getShouldHealOnFlowingLavaCommand(CommandContext<ServerCommandSource> ctx){
        ctx.getSource().sendMessage(Text.literal("Heal on flowing lava currently set to: " + PreferencesConfig.HEAL_ON_FLOWING_LAVA.getEntry().getValue()));
        return Command.SINGLE_SUCCESS;
    }

    private static int getShouldHealOnSourceLavaCommand(CommandContext<ServerCommandSource> ctx){
        ctx.getSource().sendMessage(Text.literal("Heal on source lava currently set to: " + PreferencesConfig.HEAL_ON_SOURCE_LAVA.getEntry().getValue()));
        return Command.SINGLE_SUCCESS;
    }

    private static int getShouldPlaySoundOnBlockPlacement(CommandContext<ServerCommandSource> ctx) {
        ctx.getSource().sendMessage(Text.literal("Play sound on block placement currently set to: " + PreferencesConfig.BLOCK_PLACEMENT_SOUND_EFFECT.getEntry().getValue()));
        return Command.SINGLE_SUCCESS;
    }

    private static int getHealOnHealingPotionSplashCommand(CommandContext<ServerCommandSource> ctx){
        ctx.getSource().sendMessage(Text.literal("Heal on Healing potion splash set to: " + PreferencesConfig.HEAL_ON_HEALING_POTION_SPLASH.getEntry().getValue()));
        return Command.SINGLE_SUCCESS;
    }

    private static int getHealOnRegenerationPotionSplashCommand(CommandContext<ServerCommandSource> ctx){
        ctx.getSource().sendMessage(Text.literal("Heal on Regeneration potion splash set to: " + PreferencesConfig.HEAL_ON_REGENERATION_POTION_SPLASH.getEntry().getValue()));
        return Command.SINGLE_SUCCESS;
    }

    private static int getEnableWhitelist(CommandContext<ServerCommandSource> ctx){
        ctx.getSource().sendMessage(Text.literal("The whitelist is currently " + (PreferencesConfig.ENABLE_WHITELIST.getEntry().getValue() ? "enabled" : "disabled")));
        return Command.SINGLE_SUCCESS;
    }

}

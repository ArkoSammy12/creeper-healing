package xd.arkosammy.creeperhealing.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import xd.arkosammy.creeperhealing.config.ConfigManager;
import xd.arkosammy.creeperhealing.config.settings.ConfigSettings;


public final class PreferencesCommands {

    private PreferencesCommands(){}

    static void register(LiteralCommandNode<ServerCommandSource> creeperHealingNode){

        //Preferences node
        LiteralCommandNode<ServerCommandSource> settingsNode = CommandManager
                .literal("preferences")
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        // Restore block nbt node
        LiteralCommandNode<ServerCommandSource> restoreBlockNbtNode = CommandManager
                .literal("restore_block_nbt")
                .executes(PreferencesCommands::getRestoreBlockNbtCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        // Force blocks with nbt to always heal node
        LiteralCommandNode<ServerCommandSource> forceBlocksToHealNode = CommandManager
                .literal("force_blocks_to_heal")
                .executes(PreferencesCommands::getForceBlocksToHealCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Make falling blocks fall node
        LiteralCommandNode<ServerCommandSource> makeFallingBlocksFallNode = CommandManager
                .literal("make_falling_blocks_fall")
                .executes(PreferencesCommands::getMakeFallingBlocksFallCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Play sound on block placement node
        LiteralCommandNode<ServerCommandSource> shouldPlaySoundOnBlockPlacementNode = CommandManager
                .literal("block_placement_sound_effect")
                .executes(PreferencesCommands::getShouldPlaySoundOnBlockPlacementCommand)
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
                .executes(PreferencesCommands::getEnableWhitelistCommand)
                .build();

        // Restore block nbt argument node
        ArgumentCommandNode<ServerCommandSource, Boolean> restoreBlockNbtArgumentNode = CommandManager
                .argument("value", BoolArgumentType.bool())
                .executes(PreferencesCommands::setRestoreBlockNbtCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        // Force blocks with nbt to always heal argument node
        ArgumentCommandNode<ServerCommandSource, Boolean> forceBlocksToHealArgumentNode = CommandManager
                .argument("value", BoolArgumentType.bool())
                .executes(PreferencesCommands::setForceBlocksToHealCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        // Make falling blocks fall argument node
        ArgumentCommandNode<ServerCommandSource, Boolean> makeFallingBlocksFallArgumentNode = CommandManager
                .argument("value", BoolArgumentType.bool())
                .executes(PreferencesCommands::setMakeFallingBlocksFallCommands)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Play sound on block placement argument node
        ArgumentCommandNode<ServerCommandSource, Boolean> playSoundOnBlockPlacementArgumentNode = CommandManager
                .argument("value", BoolArgumentType.bool())
                .executes(PreferencesCommands::setPlaySoundOnBlockPlacementCommand)
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
                .executes(PreferencesCommands::setEnableWhitelistCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Root node connection
        creeperHealingNode.addChild(settingsNode);

        //Preferences commands nodes
        settingsNode.addChild(restoreBlockNbtNode);
        settingsNode.addChild(forceBlocksToHealNode);
        settingsNode.addChild(makeFallingBlocksFallNode);
        settingsNode.addChild(shouldPlaySoundOnBlockPlacementNode);
        settingsNode.addChild(healOnHealingPotionSplashNode);
        settingsNode.addChild(healOnRegenerationPotionSplash);
        settingsNode.addChild(enableWhitelistNode);

        //Argument nodes
        restoreBlockNbtNode.addChild(restoreBlockNbtArgumentNode);
        forceBlocksToHealNode.addChild(forceBlocksToHealArgumentNode);
        makeFallingBlocksFallNode.addChild(makeFallingBlocksFallArgumentNode);
        shouldPlaySoundOnBlockPlacementNode.addChild(playSoundOnBlockPlacementArgumentNode);
        healOnHealingPotionSplashNode.addChild(healOnHealingPotionSplashArgumentNode);
        healOnRegenerationPotionSplash.addChild(healOnRegenerationPotionSplashArgumentNode);
        enableWhitelistNode.addChild(enableWhitelistArgumentNode);

    }

    private static int setRestoreBlockNbtCommand(CommandContext<ServerCommandSource> ctx){
        boolean value = BoolArgumentType.getBool(ctx, "value");
        ConfigManager.getInstance().getAsBooleanSetting(ConfigSettings.RESTORE_BLOCK_NBT.getId()).setValue(value);
        ctx.getSource().sendMessage(Text.literal("Restore block nbt data has been set to: " + value));
        return Command.SINGLE_SUCCESS;
    }

    private static int setForceBlocksToHealCommand(CommandContext<ServerCommandSource> ctx){
        boolean value = BoolArgumentType.getBool(ctx, "value");
        ConfigManager.getInstance().getAsBooleanSetting(ConfigSettings.FORCE_BLOCKS_WITH_NBT_TO_ALWAYS_HEAL.getId()).setValue(value);
        ctx.getSource().sendMessage(Text.literal("Force blocks with nbt to always heal has been set to: " + value));
        return Command.SINGLE_SUCCESS;
    }

    private static int setMakeFallingBlocksFallCommands(CommandContext<ServerCommandSource> ctx){
        boolean value = BoolArgumentType.getBool(ctx, "value");
        ConfigManager.getInstance().getAsBooleanSetting(ConfigSettings.MAKE_FALLING_BLOCKS_FALL.getId()).setValue(value);
        ctx.getSource().sendMessage(Text.literal("Make falling blocks fall has been set to: " + value));
        return Command.SINGLE_SUCCESS;
    }

    private static int setPlaySoundOnBlockPlacementCommand(CommandContext<ServerCommandSource> ctx) {
        boolean value = BoolArgumentType.getBool(ctx, "value");
        ConfigManager.getInstance().getAsBooleanSetting(ConfigSettings.BLOCK_PLACEMENT_SOUND_EFFECT.getId()).setValue(value);
        ctx.getSource().sendMessage(Text.literal("Play sound on block placement has been set to: " + value));
        return Command.SINGLE_SUCCESS;
    }

    private static int setHealOnHealingPotionSplashCommand(CommandContext<ServerCommandSource> ctx){
        boolean value = BoolArgumentType.getBool(ctx, "value");
        ConfigManager.getInstance().getAsBooleanSetting(ConfigSettings.HEAL_ON_HEALING_POTION_SPLASH.getId()).setValue(value);
        ctx.getSource().sendMessage(Text.literal("Heal on Healing potion splash set to: " + value));
        return Command.SINGLE_SUCCESS;
    }

    private static int setHealOnRegenerationPotionSplashCommand(CommandContext<ServerCommandSource> ctx){
        boolean value = BoolArgumentType.getBool(ctx, "value");
        ConfigManager.getInstance().getAsBooleanSetting(ConfigSettings.HEAL_ON_REGENERATION_POTION_SPLASH.getId()).setValue(value);
        ctx.getSource().sendMessage(Text.literal("Heal on Regeneration potion splash set to: " + value));
        return Command.SINGLE_SUCCESS;
    }

    private static int setEnableWhitelistCommand(CommandContext<ServerCommandSource> ctx){
        boolean value = BoolArgumentType.getBool(ctx, "value");
        ConfigManager.getInstance().getAsBooleanSetting(ConfigSettings.ENABLE_WHITELIST.getId()).setValue(value);
        ctx.getSource().sendMessage(Text.literal("The whitelist has been " + value));
        return Command.SINGLE_SUCCESS;
    }

    private static int getRestoreBlockNbtCommand(CommandContext<ServerCommandSource> ctx){
        ctx.getSource().sendMessage(Text.literal("Restore block nbt data currently set to: " + ConfigManager.getInstance().getAsBooleanSetting(ConfigSettings.RESTORE_BLOCK_NBT.getId()).getValue()));
        return Command.SINGLE_SUCCESS;
    }

    private static int getForceBlocksToHealCommand(CommandContext<ServerCommandSource> ctx){
        ctx.getSource().sendMessage(Text.literal("Force blocks with nbt to always heal currently set to: " + ConfigManager.getInstance().getAsBooleanSetting(ConfigSettings.FORCE_BLOCKS_WITH_NBT_TO_ALWAYS_HEAL.getId()).getValue()));
        return Command.SINGLE_SUCCESS;
    }

    private static int getMakeFallingBlocksFallCommand(CommandContext<ServerCommandSource> ctx){
        ctx.getSource().sendMessage(Text.literal("Make falling blocks fall currently set to: " + ConfigManager.getInstance().getAsBooleanSetting(ConfigSettings.MAKE_FALLING_BLOCKS_FALL.getId()).getValue()));
        return Command.SINGLE_SUCCESS;
    }

    private static int getShouldPlaySoundOnBlockPlacementCommand(CommandContext<ServerCommandSource> ctx) {
        ctx.getSource().sendMessage(Text.literal("Play sound on block placement currently set to: " + ConfigManager.getInstance().getAsBooleanSetting(ConfigSettings.BLOCK_PLACEMENT_SOUND_EFFECT.getId()).getValue()));
        return Command.SINGLE_SUCCESS;
    }

    private static int getHealOnHealingPotionSplashCommand(CommandContext<ServerCommandSource> ctx){
        ctx.getSource().sendMessage(Text.literal("Heal on Healing potion splash set to: " + ConfigManager.getInstance().getAsBooleanSetting(ConfigSettings.HEAL_ON_HEALING_POTION_SPLASH.getId()).getValue()));
        return Command.SINGLE_SUCCESS;
    }

    private static int getHealOnRegenerationPotionSplashCommand(CommandContext<ServerCommandSource> ctx){
        ctx.getSource().sendMessage(Text.literal("Heal on Regeneration potion splash set to: " + ConfigManager.getInstance().getAsBooleanSetting(ConfigSettings.HEAL_ON_REGENERATION_POTION_SPLASH.getId()).getValue()));
        return Command.SINGLE_SUCCESS;
    }

    private static int getEnableWhitelistCommand(CommandContext<ServerCommandSource> ctx){
        ctx.getSource().sendMessage(Text.literal("The whitelist is currently " + (ConfigManager.getInstance().getAsBooleanSetting(ConfigSettings.ENABLE_WHITELIST.getId()).getValue() ? "enabled" : "disabled")));
        return Command.SINGLE_SUCCESS;
    }

}

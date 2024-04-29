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

public final class ExplosionSourcesCommands {

    private ExplosionSourcesCommands(){}

    static void register(LiteralCommandNode<ServerCommandSource> creeperHealingNode){

        //Explosion source node
        LiteralCommandNode<ServerCommandSource> explosionSourceMode = CommandManager
                .literal("explosion_source")
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Heal creeper explosions node
        LiteralCommandNode<ServerCommandSource> healCreeperExplosionsNode = CommandManager
                .literal("heal_creeper_explosions")
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .executes(ExplosionSourcesCommands::getHealCreeperExplosionsCommand)
                .build();

        //Heal ghast explosions node
        LiteralCommandNode<ServerCommandSource> healGhastExplosionsNode = CommandManager
                .literal("heal_ghast_explosions")
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .executes(ExplosionSourcesCommands::getHealGhastExplosionsCommand)
                .build();

        //Heal wither explosions node
        LiteralCommandNode<ServerCommandSource> healWitherExplosionsNode = CommandManager
                .literal("heal_wither_explosions")
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .executes(ExplosionSourcesCommands::getHealWitherExplosionsCommand)
                .build();

        //Heal tnt explosions node
        LiteralCommandNode<ServerCommandSource> healTNTExplosionsNode = CommandManager
                .literal("heal_tnt_explosions")
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .executes(ExplosionSourcesCommands::getHealTNTExplosionsCommand)
                .build();

        //Heal tnt minecart explosions node
        LiteralCommandNode<ServerCommandSource> healTNTMinecartExplosionsNode = CommandManager
                .literal("heal_tnt_minecart_explosions")
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .executes(ExplosionSourcesCommands::getHealTNTMinecartExplosionCommand)
                .build();

        //Heal bed and respawn anchor explosions
        LiteralCommandNode<ServerCommandSource> healBedAndRespawnAnchorExplosionsNode = CommandManager
                .literal("heal_bed_and_respawn_anchor_explosions")
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .executes(ExplosionSourcesCommands::getHealBedAndRespawnAnchorExplosionsCommand)
                .build();

        //Heal end crystal explosions
        LiteralCommandNode<ServerCommandSource> healEndCrystalExplosionsNode = CommandManager
                .literal("heal_end_crystal_explosions")
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .executes(ExplosionSourcesCommands::getHealEndCrystalExplosionsCommand)
                .build();

        //Heal creeper explosions argument node
        ArgumentCommandNode<ServerCommandSource, Boolean> healCreeperExplosionsArgumentNode = CommandManager
                .argument("value", BoolArgumentType.bool())
                .executes(ExplosionSourcesCommands::setHealCreeperExplosionsCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Heal ghast explosions argument node
        ArgumentCommandNode<ServerCommandSource, Boolean> healGhastExplosionsArgumentNode = CommandManager
                .argument("value", BoolArgumentType.bool())
                .executes(ExplosionSourcesCommands::setHealGhastExplosionsCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Heal wither explosions argument node
        ArgumentCommandNode<ServerCommandSource, Boolean> healWitherExplosionsArgumentNode = CommandManager
                .argument("value", BoolArgumentType.bool())
                .executes(ExplosionSourcesCommands::setHealWitherExplosionsCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Heal tnt explosions argument node
        ArgumentCommandNode<ServerCommandSource, Boolean> healTNTExplosionsArgumentNode = CommandManager
                .argument("value", BoolArgumentType.bool())
                .executes(ExplosionSourcesCommands::setHealTNTExplosionsCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Heal tnt minecart explosions argument node
        ArgumentCommandNode<ServerCommandSource, Boolean> healTNTMinecartExplosionsArgumentNode = CommandManager
                .argument("value", BoolArgumentType.bool())
                .executes(ExplosionSourcesCommands::setHealTNTMinecartExplosionsCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Heal bed and respawn anchor explosions argument node
        ArgumentCommandNode<ServerCommandSource, Boolean> healBedAndRespawnAnchorExplosionsArgumentNode = CommandManager
                .argument("value", BoolArgumentType.bool())
                .executes(ExplosionSourcesCommands::setHealBedAndRespawnAnchorExplosionsCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Heal end crystal explosions argument node
        ArgumentCommandNode<ServerCommandSource, Boolean> healEndCrystalExplosionsArgumentNode = CommandManager
                .argument("value", BoolArgumentType.bool())
                .executes(ExplosionSourcesCommands::setHealEndCrystalExplosionsCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Root node connection
        creeperHealingNode.addChild(explosionSourceMode);

        //Explosion sources commands nodes
        explosionSourceMode.addChild(healCreeperExplosionsNode);
        explosionSourceMode.addChild(healGhastExplosionsNode);
        explosionSourceMode.addChild(healWitherExplosionsNode);
        explosionSourceMode.addChild(healTNTExplosionsNode);
        explosionSourceMode.addChild(healTNTMinecartExplosionsNode);
        explosionSourceMode.addChild(healBedAndRespawnAnchorExplosionsNode);
        explosionSourceMode.addChild(healEndCrystalExplosionsNode);

        //Argument nodes
        healCreeperExplosionsNode.addChild(healCreeperExplosionsArgumentNode);
        healGhastExplosionsNode.addChild(healGhastExplosionsArgumentNode);
        healWitherExplosionsNode.addChild(healWitherExplosionsArgumentNode);
        healTNTExplosionsNode.addChild(healTNTExplosionsArgumentNode);
        healTNTMinecartExplosionsNode.addChild(healTNTMinecartExplosionsArgumentNode);
        healBedAndRespawnAnchorExplosionsNode.addChild(healBedAndRespawnAnchorExplosionsArgumentNode);
        healEndCrystalExplosionsNode.addChild(healEndCrystalExplosionsArgumentNode);

    }

    private static int setHealCreeperExplosionsCommand(CommandContext<ServerCommandSource> ctx){
        boolean value = BoolArgumentType.getBool(ctx, "value");
        ConfigManager.getInstance().getAsBooleanSetting(ConfigSettings.HEAL_CREEPER_EXPLOSIONS.getId()).setValue(value);
        ctx.getSource().sendMessage(Text.literal("Heal Creeper explosions has been set to: " + value));
        return Command.SINGLE_SUCCESS;
    }

    private static int setHealGhastExplosionsCommand(CommandContext<ServerCommandSource> ctx){
        boolean value = BoolArgumentType.getBool(ctx, "value");
        ConfigManager.getInstance().getAsBooleanSetting(ConfigSettings.HEAL_GHAST_EXPLOSIONS.getId()).setValue(value);
        ctx.getSource().sendMessage(Text.literal("Heal Ghast explosions has been set to: " + value));
        return Command.SINGLE_SUCCESS;
    }

    private static int setHealWitherExplosionsCommand(CommandContext<ServerCommandSource> ctx){
        boolean value = BoolArgumentType.getBool(ctx, "value");
        ConfigManager.getInstance().getAsBooleanSetting(ConfigSettings.HEAL_WITHER_EXPLOSIONS.getId()).setValue(value);
        ctx.getSource().sendMessage(Text.literal("Heal Wither explosions has been set to: " + value));
        return Command.SINGLE_SUCCESS;
    }

    private static int setHealTNTExplosionsCommand(CommandContext<ServerCommandSource> ctx){
        boolean value = BoolArgumentType.getBool(ctx, "value");
        ConfigManager.getInstance().getAsBooleanSetting(ConfigSettings.HEAL_TNT_EXPLOSIONS.getId()).setValue(value);
        ctx.getSource().sendMessage(Text.literal("Heal TNT explosions has been set to: " + value));
        return Command.SINGLE_SUCCESS;
    }

    private static int setHealTNTMinecartExplosionsCommand(CommandContext<ServerCommandSource> ctx){
        boolean value = BoolArgumentType.getBool(ctx, "value");
        ConfigManager.getInstance().getAsBooleanSetting(ConfigSettings.HEAL_TNT_MINECART_EXPLOSIONS.getId()).setValue(value);
        ctx.getSource().sendMessage(Text.literal("Heal TNT Minecart explosions has been set to: " + value));
        return Command.SINGLE_SUCCESS;
    }

    private static int setHealBedAndRespawnAnchorExplosionsCommand(CommandContext<ServerCommandSource> ctx){
        boolean value = BoolArgumentType.getBool(ctx, "value");
        ConfigManager.getInstance().getAsBooleanSetting(ConfigSettings.HEAL_BED_AND_RESPAWN_ANCHOR_EXPLOSIONS.getId()).setValue(value);
        ctx.getSource().sendMessage(Text.literal("Heal bed and respawn anchor explosions has been set to: " + value));
        return Command.SINGLE_SUCCESS;
    }

    private static int setHealEndCrystalExplosionsCommand(CommandContext<ServerCommandSource> ctx){
        boolean value = BoolArgumentType.getBool(ctx, "value");
        ConfigManager.getInstance().getAsBooleanSetting(ConfigSettings.HEAL_END_CRYSTAL_EXPLOSIONS.getId()).setValue(value);
        ctx.getSource().sendMessage(Text.literal("Heal end crystal explosions has been set to: " + value));
        return Command.SINGLE_SUCCESS;
    }

    private static int getHealCreeperExplosionsCommand(CommandContext<ServerCommandSource> ctx){
        ctx.getSource().sendMessage(Text.literal("Heal Creeper explosions currently set to: " + ConfigManager.getInstance().getAsBooleanSetting(ConfigSettings.HEAL_CREEPER_EXPLOSIONS.getId()).getValue()));
        return Command.SINGLE_SUCCESS;
    }

    private static int getHealGhastExplosionsCommand(CommandContext<ServerCommandSource> ctx){
        ctx.getSource().sendMessage(Text.literal("Heal Ghast explosions currently set to: " + ConfigManager.getInstance().getAsBooleanSetting(ConfigSettings.HEAL_GHAST_EXPLOSIONS.getId()).getValue()));
        return Command.SINGLE_SUCCESS;
    }

    private static int getHealWitherExplosionsCommand(CommandContext<ServerCommandSource> ctx){
        ctx.getSource().sendMessage(Text.literal("Heal Wither explosions currently set to: " + ConfigManager.getInstance().getAsBooleanSetting(ConfigSettings.HEAL_WITHER_EXPLOSIONS.getId()).getValue()));
        return Command.SINGLE_SUCCESS;
    }

    private static int getHealTNTExplosionsCommand(CommandContext<ServerCommandSource> ctx){
        ctx.getSource().sendMessage(Text.literal("Heal TNT explosions currently set to: " + ConfigManager.getInstance().getAsBooleanSetting(ConfigSettings.HEAL_TNT_MINECART_EXPLOSIONS.getId()).getValue()));
        return Command.SINGLE_SUCCESS;
    }

    private static int getHealTNTMinecartExplosionCommand(CommandContext<ServerCommandSource> ctx){
        ctx.getSource().sendMessage(Text.literal("Heal TNT minecart explosions currently set to: " + ConfigManager.getInstance().getAsBooleanSetting(ConfigSettings.HEAL_TNT_MINECART_EXPLOSIONS.getId()).getValue()));
        return Command.SINGLE_SUCCESS;
    }

    private static int getHealBedAndRespawnAnchorExplosionsCommand(CommandContext<ServerCommandSource> ctx){
        ctx.getSource().sendMessage(Text.literal("Heal bed and respawn anchor explosions currently set to: " + ConfigManager.getInstance().getAsBooleanSetting(ConfigSettings.HEAL_BED_AND_RESPAWN_ANCHOR_EXPLOSIONS.getId()).getValue()));
        return Command.SINGLE_SUCCESS;
    }

    private static int getHealEndCrystalExplosionsCommand(CommandContext<ServerCommandSource> ctx){
        ctx.getSource().sendMessage(Text.literal("Heal end crystal explosions currently set to: " + ConfigManager.getInstance().getAsBooleanSetting(ConfigSettings.HEAL_END_CRYSTAL_EXPLOSIONS.getId()).getValue()));
        return Command.SINGLE_SUCCESS;
    }

}

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

public final class ExplosionItemDropCommands {

    private ExplosionItemDropCommands(){}

    static void register(LiteralCommandNode<ServerCommandSource> creeperHealingNode){

        //Explosion item drop node
        LiteralCommandNode<ServerCommandSource> explosionItemDropNode = CommandManager
                .literal("explosion_item_drops")
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Drop items on creeper explosions
        LiteralCommandNode<ServerCommandSource> dropItemsOnCreeperExplosionsNode = CommandManager
                .literal("drop_items_on_creeper_explosions")
                .executes(ExplosionItemDropCommands::getDropItemsOnCreeperExplosionsCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Drop items on ghast explosions
        LiteralCommandNode<ServerCommandSource> dropItemsOnGhastExplosionsNode = CommandManager
                .literal("drop_items_on_ghast_explosions")
                .executes(ExplosionItemDropCommands::getDropItemsOnGhastExplosionsCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Drop items on wither explosions
        LiteralCommandNode<ServerCommandSource> dropItemsOnWitherExplosionsNode = CommandManager
                .literal("drop_items_on_wither_explosions")
                .executes(ExplosionItemDropCommands::getDropItemsOnWitherExplosionsCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Drop items on tnt explosions
        LiteralCommandNode<ServerCommandSource> dropItemsOnTNTExplosionsNode = CommandManager
                .literal("drop_items_on_tnt_explosions")
                .executes(ExplosionItemDropCommands::getDropItemsOnTNTExplosionsCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Drop items on tnt minecart explosions
        LiteralCommandNode<ServerCommandSource> dropItemsOnTNTMinecartExplosionsNode = CommandManager
                .literal("drop_items_on_tnt_minecart_explosions")
                .executes(ExplosionItemDropCommands::getDropItemsOnTNTMinecartExplosionsCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Drop items on bed and respawn anchors explosions
        LiteralCommandNode<ServerCommandSource> dropItemsOnBedAndRspawnAnchorExplosions = CommandManager
                .literal("drop_items_on_bed_and_respawn_anchor_explosions")
                .executes(ExplosionItemDropCommands::getDropItemsOnBedAndRespawnAnchorExplosionsCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Drop items on end crystal explosions
        LiteralCommandNode<ServerCommandSource> dropItemsOnEndCrystalExplosions = CommandManager
                .literal("drop_items_on_end_crystal_explosions")
                .executes(ExplosionItemDropCommands::getDropItemsOnEndCrystalExplosionsCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Drop items on creeper explosions argument node
        ArgumentCommandNode<ServerCommandSource, Boolean> dropItemsOnCreeperExplosionsArgumentNode = CommandManager
                .argument("value", BoolArgumentType.bool())
                .executes(ExplosionItemDropCommands::setDropItemsOnCreeperExplosionsCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Drop items on ghast explosions argument node
        ArgumentCommandNode<ServerCommandSource, Boolean> dropItemsOnGhastExplosionsArgumentNode = CommandManager
                .argument("value", BoolArgumentType.bool())
                .executes(ExplosionItemDropCommands::setDropItemsOnGhastExplosionsCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Drop items on wither explosions argument node
        ArgumentCommandNode<ServerCommandSource, Boolean> dropItemsOnWitherExplosionsArgumentNode = CommandManager
                .argument("value", BoolArgumentType.bool())
                .executes(ExplosionItemDropCommands::setDropItemsOnWitherExplosionsCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Drop items on tnt explosions argument node
        ArgumentCommandNode<ServerCommandSource, Boolean> dropItemsOnTNTExplosionsArgumentNode = CommandManager
                .argument("value", BoolArgumentType.bool())
                .executes(ExplosionItemDropCommands::setDropItemsOnTNTExplosionsCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Drop items on tnt minecart explosions argument node
        ArgumentCommandNode<ServerCommandSource, Boolean> dropItemsOnTNTMinecartExplosionsArgumentNode = CommandManager
                .argument("value", BoolArgumentType.bool())
                .executes(ExplosionItemDropCommands::setDropItemsOnTNTMinecartExplosionsCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Drop items on bed and respawn anchor explosions argument node
        ArgumentCommandNode<ServerCommandSource, Boolean> dropItemsOnBedAndRespawnAnchorExplosionsArgumentNode = CommandManager
                .argument("value", BoolArgumentType.bool())
                .executes(ExplosionItemDropCommands::setDropItemsOnBedAndRespawnAnchorExplosionsCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Drop items on end crystal explosions argument node
        ArgumentCommandNode<ServerCommandSource, Boolean> dropItemsOnEndCrystalExplosionsArgumentNode = CommandManager
                .argument("value", BoolArgumentType.bool())
                .executes(ExplosionItemDropCommands::setDropItemsOnEndCrystalExplosionsCommand)
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .build();

        //Root node connection
        creeperHealingNode.addChild(explosionItemDropNode);

        //Drop items on explosions command nodes
        explosionItemDropNode.addChild(dropItemsOnCreeperExplosionsNode);
        explosionItemDropNode.addChild(dropItemsOnGhastExplosionsNode);
        explosionItemDropNode.addChild(dropItemsOnWitherExplosionsNode);
        explosionItemDropNode.addChild(dropItemsOnTNTExplosionsNode);
        explosionItemDropNode.addChild(dropItemsOnTNTMinecartExplosionsNode);
        explosionItemDropNode.addChild(dropItemsOnBedAndRspawnAnchorExplosions);
        explosionItemDropNode.addChild(dropItemsOnEndCrystalExplosions);

        //Argument nodes
        dropItemsOnCreeperExplosionsNode.addChild(dropItemsOnCreeperExplosionsArgumentNode);
        dropItemsOnGhastExplosionsNode.addChild(dropItemsOnGhastExplosionsArgumentNode);
        dropItemsOnWitherExplosionsNode.addChild(dropItemsOnWitherExplosionsArgumentNode);
        dropItemsOnTNTExplosionsNode.addChild(dropItemsOnTNTExplosionsArgumentNode);
        dropItemsOnTNTMinecartExplosionsNode.addChild(dropItemsOnTNTMinecartExplosionsArgumentNode);
        dropItemsOnBedAndRspawnAnchorExplosions.addChild(dropItemsOnBedAndRespawnAnchorExplosionsArgumentNode);
        dropItemsOnEndCrystalExplosions.addChild(dropItemsOnEndCrystalExplosionsArgumentNode);

    }

    private static int setDropItemsOnCreeperExplosionsCommand(CommandContext<ServerCommandSource> ctx){
        boolean value = BoolArgumentType.getBool(ctx, "value");
        ConfigManager.getInstance().getAsBooleanSetting(ConfigSettings.DROP_ITEMS_ON_CREEPER_EXPLOSIONS.getId()).setValue(value);
        ctx.getSource().sendMessage(Text.literal("Drop items on Creeper explosions has been set to: " + value));
        return Command.SINGLE_SUCCESS;
    }

    private static int setDropItemsOnGhastExplosionsCommand(CommandContext<ServerCommandSource> ctx){
        boolean value = BoolArgumentType.getBool(ctx, "value");
        ConfigManager.getInstance().getAsBooleanSetting(ConfigSettings.DROP_ITEMS_ON_GHAST_EXPLOSIONS.getId()).setValue(value);
        ctx.getSource().sendMessage(Text.literal("Drop items on Ghast explosions has been set to: " + value));
        return Command.SINGLE_SUCCESS;
    }

    private static int setDropItemsOnWitherExplosionsCommand(CommandContext<ServerCommandSource> ctx){
        boolean value = BoolArgumentType.getBool(ctx, "value");
        ConfigManager.getInstance().getAsBooleanSetting(ConfigSettings.DROP_ITEMS_ON_WITHER_EXPLOSIONS.getId()).setValue(value);
        ctx.getSource().sendMessage(Text.literal("Drop items on Wither explosions has been set to: " + value));
        return Command.SINGLE_SUCCESS;
    }

    private static int setDropItemsOnTNTExplosionsCommand(CommandContext<ServerCommandSource> ctx){
        boolean value = BoolArgumentType.getBool(ctx, "value");
        ConfigManager.getInstance().getAsBooleanSetting(ConfigSettings.DROP_ITEMS_ON_TNT_EXPLOSIONS.getId()).setValue(value);
        ctx.getSource().sendMessage(Text.literal("Drop items on TNT explosions has been set to: " + value));
        return Command.SINGLE_SUCCESS;
    }

    private static int setDropItemsOnTNTMinecartExplosionsCommand(CommandContext<ServerCommandSource> ctx){
        boolean value = BoolArgumentType.getBool(ctx, "value");
        ConfigManager.getInstance().getAsBooleanSetting(ConfigSettings.DROP_ITEMS_ON_TNT_MINECART_EXPLOSIONS.getId()).setValue(value);
        ctx.getSource().sendMessage(Text.literal("Drop items on TNT minecart explosions has been set to: " + value));
        return Command.SINGLE_SUCCESS;
    }

    private static int setDropItemsOnBedAndRespawnAnchorExplosionsCommand(CommandContext<ServerCommandSource> ctx){
        boolean value = BoolArgumentType.getBool(ctx, "value");
        ConfigManager.getInstance().getAsBooleanSetting(ConfigSettings.DROP_ITEMS_ON_BED_AND_RESPAWN_ANCHOR_EXPLOSIONS.getId()).setValue(value);
        ctx.getSource().sendMessage(Text.literal("Drop items on bed and respawn anchor explosions has been set to: " + value));
        return Command.SINGLE_SUCCESS;
    }

    private static int setDropItemsOnEndCrystalExplosionsCommand(CommandContext<ServerCommandSource> ctx){
        boolean value = BoolArgumentType.getBool(ctx, "value");
        ConfigManager.getInstance().getAsBooleanSetting(ConfigSettings.DROP_ITEMS_ON_END_CRYSTAL_EXPLOSIONS.getId()).setValue(value);
        ctx.getSource().sendMessage(Text.literal("Drop items on end crystal explosions has been set to: " + value));
        return Command.SINGLE_SUCCESS;
    }

    private static int getDropItemsOnCreeperExplosionsCommand(CommandContext<ServerCommandSource> ctx){
        ctx.getSource().sendMessage(Text.literal("Drop items on Creeper explosions currently set to: " + ConfigManager.getInstance().getAsBooleanSetting(ConfigSettings.DROP_ITEMS_ON_CREEPER_EXPLOSIONS.getId()).getValue()));
        return Command.SINGLE_SUCCESS;
    }

    private static int getDropItemsOnGhastExplosionsCommand(CommandContext<ServerCommandSource> ctx){
        ctx.getSource().sendMessage(Text.literal("Drop items on Ghast explosions currently set to: " + ConfigManager.getInstance().getAsBooleanSetting(ConfigSettings.DROP_ITEMS_ON_GHAST_EXPLOSIONS.getId()).getValue()));
        return Command.SINGLE_SUCCESS;
    }

    private static int getDropItemsOnWitherExplosionsCommand(CommandContext<ServerCommandSource> ctx){
        ctx.getSource().sendMessage(Text.literal("Drop items on Wither explosions currently set to: " + ConfigManager.getInstance().getAsBooleanSetting(ConfigSettings.DROP_ITEMS_ON_WITHER_EXPLOSIONS.getId()).getValue()));
        return Command.SINGLE_SUCCESS;
    }

    private static int getDropItemsOnTNTExplosionsCommand(CommandContext<ServerCommandSource> ctx){
        ctx.getSource().sendMessage(Text.literal("Drop items on TNT explosions currently set to: " + ConfigManager.getInstance().getAsBooleanSetting(ConfigSettings.DROP_ITEMS_ON_TNT_EXPLOSIONS.getId()).getValue()));
        return Command.SINGLE_SUCCESS;
    }

    private static int getDropItemsOnTNTMinecartExplosionsCommand(CommandContext<ServerCommandSource> ctx){
        ctx.getSource().sendMessage(Text.literal("Drop items on TNT minecart explosions currently set to: " + ConfigManager.getInstance().getAsBooleanSetting(ConfigSettings.DROP_ITEMS_ON_TNT_MINECART_EXPLOSIONS.getId()).getValue()));
        return Command.SINGLE_SUCCESS;
    }

    private static int getDropItemsOnBedAndRespawnAnchorExplosionsCommand(CommandContext<ServerCommandSource> ctx){
        ctx.getSource().sendMessage(Text.literal("Drop items on bed and respawn anchor explosions currently set to: " + ConfigManager.getInstance().getAsBooleanSetting(ConfigSettings.DROP_ITEMS_ON_BED_AND_RESPAWN_ANCHOR_EXPLOSIONS.getId()).getValue()));
        return Command.SINGLE_SUCCESS;
    }

    private static int getDropItemsOnEndCrystalExplosionsCommand(CommandContext<ServerCommandSource> ctx){
        ctx.getSource().sendMessage(Text.literal("Drop items on end crystal explosions currently set to: " + ConfigManager.getInstance().getAsBooleanSetting(ConfigSettings.DROP_ITEMS_ON_END_CRYSTAL_EXPLOSIONS.getId()).getValue()));
        return Command.SINGLE_SUCCESS;
    }

}

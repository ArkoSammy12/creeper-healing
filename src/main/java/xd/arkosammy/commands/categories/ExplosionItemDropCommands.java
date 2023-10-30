package xd.arkosammy.commands.categories;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import xd.arkosammy.configuration.tables.ExplosionItemDropConfig;

public final class ExplosionItemDropCommands {

    private ExplosionItemDropCommands(){}

    public static void register(LiteralCommandNode<ServerCommandSource> creeperHealingNode){

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

        //Root node connection
        creeperHealingNode.addChild(explosionItemDropNode);

        //Drop items on explosions command nodes
        explosionItemDropNode.addChild(dropItemsOnCreeperExplosionsNode);
        explosionItemDropNode.addChild(dropItemsOnGhastExplosionsNode);
        explosionItemDropNode.addChild(dropItemsOnWitherExplosionsNode);
        explosionItemDropNode.addChild(dropItemsOnTNTExplosionsNode);
        explosionItemDropNode.addChild(dropItemsOnTNTMinecartExplosionsNode);

        //Argument nodes
        dropItemsOnCreeperExplosionsNode.addChild(dropItemsOnCreeperExplosionsArgumentNode);
        dropItemsOnGhastExplosionsNode.addChild(dropItemsOnGhastExplosionsArgumentNode);
        dropItemsOnWitherExplosionsNode.addChild(dropItemsOnWitherExplosionsArgumentNode);
        dropItemsOnTNTExplosionsNode.addChild(dropItemsOnTNTExplosionsArgumentNode);
        dropItemsOnTNTMinecartExplosionsNode.addChild(dropItemsOnTNTMinecartExplosionsArgumentNode);

    }

    private static int setDropItemsOnCreeperExplosionsCommand(CommandContext<ServerCommandSource> ctx){
        ExplosionItemDropConfig.setDropItemsOnCreeperExplosions(BoolArgumentType.getBool(ctx, "value"));
        ctx.getSource().sendMessage(Text.literal("Drop items on Creeper explosions has been set to: " + BoolArgumentType.getBool(ctx, "value")));
        return Command.SINGLE_SUCCESS;
    }

    private static int setDropItemsOnGhastExplosionsCommand(CommandContext<ServerCommandSource> ctx){
        ExplosionItemDropConfig.setDropItemsOnGhastExplosions(BoolArgumentType.getBool(ctx, "value"));
        ctx.getSource().sendMessage(Text.literal("Drop items on Ghast explosions has been set to: " + BoolArgumentType.getBool(ctx, "value")));
        return Command.SINGLE_SUCCESS;
    }

    private static int setDropItemsOnWitherExplosionsCommand(CommandContext<ServerCommandSource> ctx){
        ExplosionItemDropConfig.setDropItemsOnWitherExplosions(BoolArgumentType.getBool(ctx, "value"));
        ctx.getSource().sendMessage(Text.literal("Drop items on Wither explosions has been set to: " + BoolArgumentType.getBool(ctx, "value")));
        return Command.SINGLE_SUCCESS;
    }

    private static int setDropItemsOnTNTExplosionsCommand(CommandContext<ServerCommandSource> ctx){
        ExplosionItemDropConfig.setDropItemsOnTNTExplosions(BoolArgumentType.getBool(ctx, "value"));
        ctx.getSource().sendMessage(Text.literal("Drop items on TNT explosions has been set to: " + BoolArgumentType.getBool(ctx, "value")));
        return Command.SINGLE_SUCCESS;
    }

    private static int setDropItemsOnTNTMinecartExplosionsCommand(CommandContext<ServerCommandSource> ctx){
        ExplosionItemDropConfig.setDropItemsOnTNTMinecartExplosions(BoolArgumentType.getBool(ctx, "value"));
        ctx.getSource().sendMessage(Text.literal("Drop items on TNT minecart explosions has been set to: " + BoolArgumentType.getBool(ctx, "value")));
        return Command.SINGLE_SUCCESS;
    }

    private static int getDropItemsOnCreeperExplosionsCommand(CommandContext<ServerCommandSource> ctx){
        ctx.getSource().sendMessage(Text.literal("Drop items on Creeper explosions currently set to: " + ExplosionItemDropConfig.getDropItemsOnCreeperExplosions()));
        return Command.SINGLE_SUCCESS;
    }

    private static int getDropItemsOnGhastExplosionsCommand(CommandContext<ServerCommandSource> ctx){
        ctx.getSource().sendMessage(Text.literal("Drop items on Ghast explosions currently set to: " + ExplosionItemDropConfig.getDropItemsOnGhastExplosions()));
        return Command.SINGLE_SUCCESS;
    }

    private static int getDropItemsOnWitherExplosionsCommand(CommandContext<ServerCommandSource> ctx){
        ctx.getSource().sendMessage(Text.literal("Drop items on Wither explosions currently set to: " + ExplosionItemDropConfig.getDropItemsOnWitherExplosions()));
        return Command.SINGLE_SUCCESS;
    }

    private static int getDropItemsOnTNTExplosionsCommand(CommandContext<ServerCommandSource> ctx){
        ctx.getSource().sendMessage(Text.literal("Drop items on TNT explosions currently set to: " + ExplosionItemDropConfig.getDropItemsOnTNTExplosions()));
        return Command.SINGLE_SUCCESS;
    }

    private static int getDropItemsOnTNTMinecartExplosionsCommand(CommandContext<ServerCommandSource> ctx){
        ctx.getSource().sendMessage(Text.literal("Drop items on TNT minecart explosions currently set to: " + ExplosionItemDropConfig.getDropItemsOnTNTMinecartExplosions()));
        return Command.SINGLE_SUCCESS;
    }

}

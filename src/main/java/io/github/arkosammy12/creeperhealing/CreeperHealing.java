package io.github.arkosammy12.creeperhealing;

import io.github.arkosammy12.creeperhealing.config.ConfigUtils;
import io.github.arkosammy12.creeperhealing.config.settings.BlockPlacementDelaySetting;
import io.github.arkosammy12.creeperhealing.explosions.ExplosionHealingMode;
import io.github.arkosammy12.monkeyconfig.base.ConfigManager;
import io.github.arkosammy12.monkeyconfig.builders.ConfigManagerBuilderKt;
import io.github.arkosammy12.monkeyutils.registrars.DefaultConfigRegistrar;
import io.github.arkosammy12.monkeyutils.settings.CommandBooleanSetting;
import io.github.arkosammy12.monkeyutils.settings.CommandEnumSetting;
import io.github.arkosammy12.monkeyutils.settings.CommandNumberSetting;
import kotlin.Pair;
import kotlin.Unit;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.arkosammy12.creeperhealing.explosions.DefaultSerializedExplosion;
import io.github.arkosammy12.creeperhealing.managers.DefaultExplosionManager;
import io.github.arkosammy12.creeperhealing.util.Events;
import static io.github.arkosammy12.creeperhealing.config.ConfigUtils.*;

import java.util.ArrayList;
import java.util.List;

public class CreeperHealing implements ModInitializer {

    public static final String MOD_ID = "creeperhealing";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final DefaultExplosionManager EXPLOSION_MANAGER = new DefaultExplosionManager(DefaultSerializedExplosion.CODEC);
    public static final ConfigManager CONFIG_MANAGER = ConfigManagerBuilderKt.tomlConfigManager("creeper-healing", FabricLoader.getInstance().getConfigDir().resolve("creeper-healing.toml"), (manager) -> {
        manager.setLogger(LOGGER);
        manager.section("delays", (delays) -> {
            delays.setComment("Configure the delays related to the healing of explosions.");
            delays.setOnUpdated((section) -> {
                CreeperHealing.EXPLOSION_MANAGER.updateAffectedBlocksTimers();
                return Unit.INSTANCE;
            });
            EXPLOSION_HEAL_DELAY = delays.numberSetting("explosion_heal_delay", ConfigUtils.DEFAULT_EXPLOSION_HEAL_DELAY, (explosionHealDelay) -> {
                explosionHealDelay.setComment("(Default = %.1f) How much time in seconds should an explosion wait for to begin healing.".formatted(ConfigUtils.DEFAULT_EXPLOSION_HEAL_DELAY));
                explosionHealDelay.setMinValue(0.05d);
                explosionHealDelay.setImplementation(CommandNumberSetting::new);
                return Unit.INSTANCE;
            });
            BLOCK_PLACEMENT_DELAY = delays.numberSetting("block_placement_delay", ConfigUtils.DEFAULT_BLOCK_PLACEMENT_DELAY, (blockPlacementDelay) -> {
                blockPlacementDelay.setComment("(Default = %.1f) The time in seconds that a block takes to heal.".formatted(ConfigUtils.DEFAULT_BLOCK_PLACEMENT_DELAY));
                blockPlacementDelay.setMinValue(0.05d);
                blockPlacementDelay.setImplementation(BlockPlacementDelaySetting::new);
                return Unit.INSTANCE;
            });
            return Unit.INSTANCE;
        });
        manager.section("explosion_item_drops", (explosionItemDrops) -> {
            explosionItemDrops.setComment("Toggle whether certain explosion should drop items. Does not include items stored in container blocks.");
            DROP_ITEMS_ON_MOB_EXPLOSIONS = explosionItemDrops.booleanSetting("drop_items_on_mob_explosions", false, (dropItemsOnMobExplosions) -> {
                dropItemsOnMobExplosions.setComment("(Default = false) Whether to drop items on explosions caused by mobs such as Creepers.");
                dropItemsOnMobExplosions.setImplementation(CommandBooleanSetting::new);
                return Unit.INSTANCE;
            });
            DROP_ITEMS_ON_BLOCK_EXPLOSIONS = explosionItemDrops.booleanSetting("drop_items_on_block_explosions", true, (dropItemsOnBlockExplosions) -> {
                dropItemsOnBlockExplosions.setComment("(Default = true) Whether to drop items on explosions caused by blocks such as beds or end crystal blocks.");
                dropItemsOnBlockExplosions.setImplementation(CommandBooleanSetting::new);
                return Unit.INSTANCE;
            });
            DROP_ITEMS_ON_TNT_EXPLOSIONS = explosionItemDrops.booleanSetting("drop_items_on_tnt_explosions", true, (dropItemsOnTntExplosions) -> {
                dropItemsOnTntExplosions.setComment("(Default = true) Whether to drop items on explosions caused by TNT blocks and TNT minecarts.");
                dropItemsOnTntExplosions.setImplementation(CommandBooleanSetting::new);
                return Unit.INSTANCE;
            });
            DROP_ITEMS_ON_TRIGGERED_EXPLOSIONS = explosionItemDrops.booleanSetting("drop_items_on_triggered_explosions", true, (dropItemsOnTriggeredExplosions) -> {
                dropItemsOnTriggeredExplosions.setComment("(Default = true) Whether to drop items on explosions such as those caused by wind bursts.");
                dropItemsOnTriggeredExplosions.setImplementation(CommandBooleanSetting::new);
                return Unit.INSTANCE;
            });
            DROP_ITEMS_ON_OTHER_EXPLOSIONS = explosionItemDrops.booleanSetting("drop_items_on_other_explosions", true, (dropItemsOnOtherExplosions) -> {
                dropItemsOnOtherExplosions.setComment("(Default = true) Whether to drop items on explosions whose source is not any of the ones provided in this setting category.");
                dropItemsOnOtherExplosions.setImplementation(CommandBooleanSetting::new);
                return Unit.INSTANCE;
            });
            DROP_ITEMS_ON_MOB_EXPLOSIONS_BLACKLIST = explosionItemDrops.stringListSetting("drop_items_on_mob_explosions_blacklist", new ArrayList<>(List.of("minecraft:placeholder")), (dropItemsOnMobExplosionsBlacklist) -> {
                dropItemsOnMobExplosionsBlacklist.setComment("Add mob identifiers to this blacklist to prevent explosions caused by the added mobs from dropping items if drop_items_on_mob_explosions is enabled.");
                return Unit.INSTANCE;
            });
            return Unit.INSTANCE;
        });
        manager.section("explosion_sources", (explosionSources) -> {
            explosionSources.setComment("Configure which explosions are allowed to heal.");
            HEAL_MOB_EXPLOSIONS = explosionSources.booleanSetting("heal_mob_explosions", true, (healMobExplosions) -> {
                healMobExplosions.setComment("(Default = true) Heal explosions caused by mobs such as Creepers.");
                healMobExplosions.setImplementation(CommandBooleanSetting::new);
                return Unit.INSTANCE;
            });
            HEAL_BLOCK_EXPLOSIONS = explosionSources.booleanSetting("heal_block_explosions", false, (healBlockExplosions) -> {
                healBlockExplosions.setComment("(Default = false) Heal explosions caused by blocks such as beds or end crystal blocks.");
                healBlockExplosions.setImplementation(CommandBooleanSetting::new);
                return Unit.INSTANCE;
            });
            HEAL_TNT_EXPLOSIONS = explosionSources.booleanSetting("heal_tnt_explosions", false, (healTntExplosions) -> {
                healTntExplosions.setComment("(Default = false) Heal explosions caused by TNT blocks and TNT minecarts.");
                healTntExplosions.setImplementation(CommandBooleanSetting::new);
                return Unit.INSTANCE;
            });
            HEAL_TRIGGERED_EXPLOSIONS = explosionSources.booleanSetting("heal_triggered_explosions", false, (healTriggeredExplosions) -> {
                healTriggeredExplosions.setComment("(Default = false) Heal explosions such as those caused by wind bursts.");
                healTriggeredExplosions.setImplementation(CommandBooleanSetting::new);
                return Unit.INSTANCE;
            });
            HEAL_OTHER_EXPLOSIONS = explosionSources.booleanSetting("heal_other_explosions", false, (healOtherExplosions) -> {
                healOtherExplosions.setComment("(Default = false) Heal explosions caused by sources which aren't any of the ones provided in this setting category.");
                healOtherExplosions.setImplementation(CommandBooleanSetting::new);
                return Unit.INSTANCE;
            });
            HEAL_MOB_EXPLOSIONS_BLACKLIST = explosionSources.stringListSetting("heal_mob_explosions_blacklist", new ArrayList<>(List.of("minecraft:placeholder")), (healMobExplosionsBlacklist) -> {
                healMobExplosionsBlacklist.setComment("Add mob identifiers to this blacklist to prevent explosions caused by the added mobs from healing if heal_mob_explosions is enabled.");
                return Unit.INSTANCE;
            });
            return Unit.INSTANCE;
        });
        manager.section("explosion_healing_mode", (explosionHealingMode) -> {
            explosionHealingMode.setComment("Choose between different special modes for explosion healing. Note that certain healing modes will not follow the explosion delay and block delay settings.");
            MODE = explosionHealingMode.enumSetting("mode", ExplosionHealingMode.DEFAULT_MODE, (mode) -> {
                mode.setComment("(Default = \"default_mode\") Choose any of the following healing modes by copying one of the strings and pasting it into the value of the \"mode\" setting below:\n\"%s\", \"%s\", \"%s\", \"%s\"".formatted(ExplosionHealingMode.DEFAULT_MODE.getName(), ExplosionHealingMode.DAYTIME_HEALING_MODE.getName(), ExplosionHealingMode.DIFFICULTY_BASED_HEALING_MODE.getName(), ExplosionHealingMode.BLAST_RESISTANCE_BASED_HEALING_MODE.getName()));
                mode.setImplementation(CommandEnumSetting::new);
                return Unit.INSTANCE;
            });
            return Unit.INSTANCE;
        });

        manager.section("preferences", (preferences) -> {
            preferences.setComment("Toggleable settings for extra features.");
            RESTORE_BLOCK_NBT = preferences.booleanSetting("restore_block_nbt", false, (restoreBlockNbt) -> {
                restoreBlockNbt.setComment("(Default = false) Whether to restore block nbt data upon healing. This option prevents container blocks like chests from dropping their inventories. Does not apply when the healed block is different from the destroyed block due to a replace map entry.");
                restoreBlockNbt.setImplementation(CommandBooleanSetting::new);
                return Unit.INSTANCE;
            });
            FORCE_BLOCKS_WITH_NBT_TO_ALWAYS_HEAL = preferences.booleanSetting("force_blocks_with_nbt_to_always_heal", false, (forceBlocksWithNbtToAlwaysHeal) -> {
                forceBlocksWithNbtToAlwaysHeal.setComment("(Default = false) Whether to force blocks with nbt data to always heal, even if the replace map specifies a replacement for that block, and regardless of the block that may be occupying that position at the moment of healing.");
                forceBlocksWithNbtToAlwaysHeal.setImplementation(CommandBooleanSetting::new);
                return Unit.INSTANCE;
            });
            MAKE_FALLING_BLOCKS_FALL = preferences.booleanSetting("make_falling_blocks_fall", true, (makeFallingBlocksFall) -> {
                makeFallingBlocksFall.setComment("(Default = true) Allows for a falling block, like sand or gravel, to fall when healed. Disabling this option makes the falling block have to receive a neighbor update before falling.");
                makeFallingBlocksFall.setImplementation(CommandBooleanSetting::new);
                return Unit.INSTANCE;
            });
            BLOCK_PLACEMENT_SOUND_EFFECT = preferences.booleanSetting("block_placement_sound_effect", true, (blockPlacementSoundEffect) -> {
                blockPlacementSoundEffect.setComment("(Default = true) Whether a block placement sound effect should be played when a block is healed.");
                blockPlacementSoundEffect.setImplementation(CommandBooleanSetting::new);
                return Unit.INSTANCE;
            });
            BLOCK_PLACEMENT_PARTICLES = preferences.booleanSetting("block_placement_particles", true, (blockPlacementParticles) -> {
                blockPlacementParticles.setComment("(Default = true) Whether a block placement sound effect should produce some cloud particles.");
                blockPlacementParticles.setImplementation(CommandBooleanSetting::new);
                return Unit.INSTANCE;
            });
            HEAL_ON_HEALING_POTION_SPLASH = preferences.booleanSetting("heal_on_healing_potion_splash", true, (healOnHealingPotionSplash) -> {
                healOnHealingPotionSplash.setComment("(Default = true) Makes explosion heal immediately when a potion of Healing is thrown on them.");
                healOnHealingPotionSplash.setImplementation(CommandBooleanSetting::new);
                return Unit.INSTANCE;
            });
            HEAL_ON_REGENERATION_POTION_SPLASH = preferences.booleanSetting("heal_on_regeneration_potion_splash", true, (healOnRegenerationPotionSplash) -> {
                healOnRegenerationPotionSplash.setComment("(Default = true) Makes explosions begin their healing process when a potion of Regeneration is thrown on them.");
                healOnRegenerationPotionSplash.setImplementation(CommandBooleanSetting::new);
                return Unit.INSTANCE;
            });
            ENABLE_WHITELIST = preferences.booleanSetting("enable_whitelist", false, (enableWhitelist) -> {
                enableWhitelist.setComment("(Default = false) Toggle the usage of the whitelist.");
                enableWhitelist.setImplementation(CommandBooleanSetting::new);
                return Unit.INSTANCE;
            });
            return Unit.INSTANCE;
        });

        manager.section("whitelist", (whitelist) -> {
            whitelist.setComment("""
                    Use an optional whitelist to customize which blocks are allowed to heal. To add an entry, specify the block's namespace
                    along with its identifier, separated by a colon and enclosed in double quotes, and add it in-between the square brackets below. Separate each entry with a comma.
                    Example entries:
                    whitelist_entries = ["minecraft:grass",  "minecraft:stone", "minecraft:sand"]""");
            whitelist.setLoadBeforeSave(true);
            WHITELIST = whitelist.stringListSetting("whitelist", new ArrayList<>(List.of("minecraft:placeholder")), (whitelistSetting) -> Unit.INSTANCE);
            return Unit.INSTANCE;
        });

        REPLACE_MAP = manager.stringMapSection("replace_map", (replaceMap) -> {
            replaceMap.setComment("""
                    Add your own replace entries to configure which blocks should be used to heal other blocks. The block on the right will be used to heal the block on the left.
                    Specify the block's namespace along with the block's name identifier, separated by a colon and enclosed in double quotes.
                    Example entry:
                    "minecraft:gold_block" = "minecraft:stone"
                    Warning, the same key cannot appear more than once in the replace map! For example, the following will cause an error:
                    "minecraft:diamond_block" = "minecraft:stone"
                    "minecraft:diamond_block" = "minecraft:air"\s""");
            replaceMap.addDefaultEntry(new Pair<>("minecraft:diamond_block", "minecraft:stone"));
            return Unit.INSTANCE;
        });

        return Unit.INSTANCE;
    });

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(CreeperHealing::onServerStarting);
        ServerLifecycleEvents.SERVER_STOPPING.register(CreeperHealing::onServerStopping);
        DefaultConfigRegistrar.INSTANCE.registerConfigManager(CONFIG_MANAGER);
        Events.registerEvents();
        LOGGER.info("I will try my best to heal your explosions :)");
    }

    private static void onServerStarting(MinecraftServer server) {
        ExplosionManagerRegistrar.getInstance().invokeOnServerStarting(server);
    }

    private static void onServerStopping(MinecraftServer server) {
        ExplosionManagerRegistrar.getInstance().invokeOnServerStopping(server);
    }

}
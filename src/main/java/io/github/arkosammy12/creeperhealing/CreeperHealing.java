package io.github.arkosammy12.creeperhealing;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
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
    public static final ConfigManager CONFIG_MANAGER = ConfigManagerBuilderKt.tomlConfigManager(
            "creeper-healing",
            FabricLoader.getInstance().getConfigDir().resolve("creeper-healing.toml"),
            manager -> {
                manager.setLogger(LOGGER);
                manager.section("delays", delays -> {
                    delays.setComment("Configure the delays related to explosion healing.");
                    delays.setOnUpdated(section -> {
                        EXPLOSION_MANAGER.updateAffectedBlocksTimers();
                        return Unit.INSTANCE;
                    });
                    EXPLOSION_HEAL_DELAY = delays.numberSetting("explosion_heal_delay", DEFAULT_EXPLOSION_HEAL_DELAY, setting -> {
                        setting.setComment("(Default = %.1f) Delay in seconds before explosion healing begins.".formatted(DEFAULT_EXPLOSION_HEAL_DELAY));
                        setting.setMinValue(0.05d);
                        setting.setImplementation(CommandNumberSetting::new);
                        return Unit.INSTANCE;
                    });
                    BLOCK_PLACEMENT_DELAY = delays.numberSetting("block_placement_delay", DEFAULT_BLOCK_PLACEMENT_DELAY, setting -> {
                        setting.setComment("(Default = %.1f) Time in seconds for each block to heal.".formatted(DEFAULT_BLOCK_PLACEMENT_DELAY));
                        setting.setMinValue(0.05d);
                        setting.setImplementation(BlockPlacementDelaySetting::new);
                        return Unit.INSTANCE;
                    });
                    return Unit.INSTANCE;
                });
                manager.section("explosion_item_drops", section -> {
                    section.setComment("Toggle whether certain explosion types drop items.");
                    DROP_ITEMS_ON_MOB_EXPLOSIONS = section.booleanSetting("drop_items_on_mob_explosions", false, setting -> {
                        setting.setComment("Whether mob explosions drop items.");
                        setting.setImplementation(CommandBooleanSetting::new);
                        return Unit.INSTANCE;
                    });
                    DROP_ITEMS_ON_BLOCK_EXPLOSIONS = section.booleanSetting("drop_items_on_block_explosions", true, setting -> {
                        setting.setComment("Whether block explosions drop items.");
                        setting.setImplementation(CommandBooleanSetting::new);
                        return Unit.INSTANCE;
                    });
                    DROP_ITEMS_ON_TNT_EXPLOSIONS = section.booleanSetting("drop_items_on_tnt_explosions", true, setting -> {
                        setting.setComment("Whether TNT explosions drop items.");
                        setting.setImplementation(CommandBooleanSetting::new);
                        return Unit.INSTANCE;
                    });
                    DROP_ITEMS_ON_TRIGGERED_EXPLOSIONS = section.booleanSetting("drop_items_on_triggered_explosions", true, setting -> {
                        setting.setComment("Whether triggered explosions drop items.");
                        setting.setImplementation(CommandBooleanSetting::new);
                        return Unit.INSTANCE;
                    });
                    DROP_ITEMS_ON_OTHER_EXPLOSIONS = section.booleanSetting("drop_items_on_other_explosions", true, setting -> {
                        setting.setComment("Whether other explosions drop items.");
                        setting.setImplementation(CommandBooleanSetting::new);
                        return Unit.INSTANCE;
                    });
                    DROP_ITEMS_ON_MOB_EXPLOSIONS_BLACKLIST = section.stringListSetting("drop_items_on_mob_explosions_blacklist", new ArrayList<>(List.of("minecraft:placeholder")), setting -> {
                        setting.setComment("Mob identifiers excluded from item drops.");
                        return Unit.INSTANCE;
                    });
                    return Unit.INSTANCE;
                });
                manager.section("explosion_sources", section -> {
                    section.setComment("Configure which explosions are allowed to heal.");
                    HEAL_MOB_EXPLOSIONS = section.booleanSetting("heal_mob_explosions", true, setting -> {
                        setting.setComment("Heal explosions caused by mobs.");
                        setting.setImplementation(CommandBooleanSetting::new);
                        return Unit.INSTANCE;
                    });
                    HEAL_BLOCK_EXPLOSIONS = section.booleanSetting("heal_block_explosions", false, setting -> {
                        setting.setComment("Heal explosions caused by blocks.");
                        setting.setImplementation(CommandBooleanSetting::new);
                        return Unit.INSTANCE;
                    });
                    HEAL_TNT_EXPLOSIONS = section.booleanSetting("heal_tnt_explosions", false, setting -> {
                        setting.setComment("Heal TNT explosions.");
                        setting.setImplementation(CommandBooleanSetting::new);
                        return Unit.INSTANCE;
                    });
                    HEAL_TRIGGERED_EXPLOSIONS = section.booleanSetting("heal_triggered_explosions", false, setting -> {
                        setting.setComment("Heal triggered explosions.");
                        setting.setImplementation(CommandBooleanSetting::new);
                        return Unit.INSTANCE;
                    });
                    HEAL_OTHER_EXPLOSIONS = section.booleanSetting("heal_other_explosions", false, setting -> {
                        setting.setComment("Heal explosions from other sources.");
                        setting.setImplementation(CommandBooleanSetting::new);
                        return Unit.INSTANCE;
                    });
                    HEAL_MOB_EXPLOSIONS_BLACKLIST = section.stringListSetting("heal_mob_explosions_blacklist", new ArrayList<>(List.of("minecraft:placeholder")), setting -> {
                        setting.setComment("Mob identifiers excluded from explosion healing.");
                        return Unit.INSTANCE;
                    });
                    return Unit.INSTANCE;
                });
                manager.section("explosion_healing_mode", section -> {
                    section.setComment("Choose a special explosion healing mode.");
                    MODE = section.enumSetting("mode", ExplosionHealingMode.DEFAULT_MODE, setting -> {
                        setting.setComment("Healing mode used for explosions.");
                        setting.setImplementation(CommandEnumSetting::new);
                        return Unit.INSTANCE;
                    });
                    return Unit.INSTANCE;
                });
                manager.section("preferences", section -> {
                    section.setComment("Toggleable settings for extra features.");
                    RESTORE_BLOCK_NBT = section.booleanSetting("restore_block_nbt", false, setting -> {
                        setting.setComment("Restore block entity data when healing blocks.");
                        setting.setImplementation(CommandBooleanSetting::new);
                        return Unit.INSTANCE;
                    });
                    FORCE_BLOCKS_WITH_NBT_TO_ALWAYS_HEAL = section.booleanSetting("force_blocks_with_nbt_to_always_heal", false, setting -> {
                        setting.setComment("Always heal blocks with block entity data.");
                        setting.setImplementation(CommandBooleanSetting::new);
                        return Unit.INSTANCE;
                    });
                    MAKE_FALLING_BLOCKS_FALL = section.booleanSetting("make_falling_blocks_fall", true, setting -> {
                        setting.setComment("Allow healed falling blocks to fall.");
                        setting.setImplementation(CommandBooleanSetting::new);
                        return Unit.INSTANCE;
                    });
                    BLOCK_PLACEMENT_SOUND_EFFECT = section.booleanSetting("block_placement_sound_effect", true, setting -> {
                        setting.setComment("Play a placement sound when a block heals.");
                        setting.setImplementation(CommandBooleanSetting::new);
                        return Unit.INSTANCE;
                    });
                    BLOCK_PLACEMENT_PARTICLES = section.booleanSetting("block_placement_particles", true, setting -> {
                        setting.setComment("Show particles when a block heals.");
                        setting.setImplementation(CommandBooleanSetting::new);
                        return Unit.INSTANCE;
                    });
                    HEAL_ON_HEALING_POTION_SPLASH = section.booleanSetting("heal_on_healing_potion_splash", true, setting -> {
                        setting.setComment("Heal explosions when hit by a healing potion.");
                        setting.setImplementation(CommandBooleanSetting::new);
                        return Unit.INSTANCE;
                    });
                    HEAL_ON_REGENERATION_POTION_SPLASH = section.booleanSetting("heal_on_regeneration_potion_splash", true, setting -> {
                        setting.setComment("Begin healing when hit by a regeneration potion.");
                        setting.setImplementation(CommandBooleanSetting::new);
                        return Unit.INSTANCE;
                    });
                    ENABLE_WHITELIST = section.booleanSetting("enable_whitelist", false, setting -> {
                        setting.setComment("Enable the healing whitelist.");
                        setting.setImplementation(CommandBooleanSetting::new);
                        return Unit.INSTANCE;
                    });
                    return Unit.INSTANCE;
                });
                manager.section("whitelist", section -> {
                    section.setComment("Blocks allowed to heal when the whitelist is enabled.");
                    section.setLoadBeforeSave(true);
                    WHITELIST = section.stringListSetting("whitelist", new ArrayList<>(List.of("minecraft:placeholder")), setting -> Unit.INSTANCE);
                    return Unit.INSTANCE;
                });
                REPLACE_MAP = manager.stringMapSection("replace_map", section -> {
                    section.setComment("Map block identifiers to the block used to heal them.");
                    section.addDefaultEntry(new Pair<>("minecraft:diamond_block", "minecraft:stone"));
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

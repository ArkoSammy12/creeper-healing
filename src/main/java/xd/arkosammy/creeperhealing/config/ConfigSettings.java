package xd.arkosammy.creeperhealing.config;

import xd.arkosammy.creeperhealing.config.settings.BlockPlacementDelaySetting;
import xd.arkosammy.creeperhealing.explosions.ExplosionHealingMode;
import xd.arkosammy.monkeyconfig.settings.*;
import xd.arkosammy.monkeyconfig.settings.list.StringListSetting;
import xd.arkosammy.monkeyconfig.util.SettingLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum ConfigSettings {
    EXPLOSION_HEAL_DELAY(new NumberSetting.Builder<>(new SettingLocation(SettingGroups.DELAYS.getName(), "explosion_heal_delay"), "(Default = %.1f) How much time in seconds should an explosion wait for to begin healing.".formatted(ConfigUtils.DEFAULT_EXPLOSION_HEAL_DELAY), ConfigUtils.DEFAULT_EXPLOSION_HEAL_DELAY).withLowerBound(0.05d)),
    BLOCK_PLACEMENT_DELAY(new BlockPlacementDelaySetting.Builder(new SettingLocation(SettingGroups.DELAYS.getName(), "block_placement_delay"),"(Default = %.1f) The time in seconds that a block takes to heal.".formatted(ConfigUtils.DEFAULT_BLOCK_PLACEMENT_DELAY), ConfigUtils.DEFAULT_BLOCK_PLACEMENT_DELAY).withLowerBound(0.05d)),

    DROP_ITEMS_ON_CREEPER_EXPLOSIONS(new BooleanSetting.Builder(new SettingLocation(SettingGroups.EXPLOSION_ITEM_DROPS.getName(), "drop_items_on_creeper_explosions"), "(Default = true) Whether to drop items when a creeper explodes.",  true)),
    DROP_ITEMS_ON_GHAST_EXPLOSIONS(new BooleanSetting.Builder(new SettingLocation(SettingGroups.EXPLOSION_ITEM_DROPS.getName(), "drop_items_on_ghast_explosions"), "(Default = true) Whether to drop items when a ghast explodes.",  true)),
    DROP_ITEMS_ON_WITHER_EXPLOSIONS(new BooleanSetting.Builder(new SettingLocation(SettingGroups.EXPLOSION_ITEM_DROPS.getName(), "drop_items_on_wither_explosions"), "(Default = true) Whether to drop items when a wither explodes.", true)),
    DROP_ITEMS_ON_TNT_EXPLOSIONS(new BooleanSetting.Builder(new SettingLocation(SettingGroups.EXPLOSION_ITEM_DROPS.getName(), "drop_items_on_tnt_explosions"), "(Default = true) Whether to drop items when a tnt explodes.", true)),
    DROP_ITEMS_ON_TNT_MINECART_EXPLOSIONS(new BooleanSetting.Builder(new SettingLocation(SettingGroups.EXPLOSION_ITEM_DROPS.getName(), "drop_items_on_tnt_minecart_explosions"), "(Default = true) Whether to drop items when a tnt minecart explodes.", true)),
    DROP_ITEMS_ON_BED_AND_RESPAWN_ANCHOR_EXPLOSIONS(new BooleanSetting.Builder(new SettingLocation(SettingGroups.EXPLOSION_ITEM_DROPS.getName(), "drop_items_on_bed_and_respawn_anchor_explosions"), "(Default = true) Whether to drop items when a bed or respawn anchor explodes.",  true)),
    DROP_ITEMS_ON_END_CRYSTAL_EXPLOSIONS(new BooleanSetting.Builder(new SettingLocation(SettingGroups.EXPLOSION_ITEM_DROPS.getName(), "drop_items_on_end_crystal_explosions"), "(Default = true) Whether to drop items when an end crystal explodes.",  true)),

    HEAL_CREEPER_EXPLOSIONS(new BooleanSetting.Builder(new SettingLocation(SettingGroups.EXPLOSION_SOURCES.getName(), "heal_creeper_explosions"), "(Default = true) Heal explosions caused by Creepers.", true)),
    HEAL_GHAST_EXPLOSIONS(new BooleanSetting.Builder(new SettingLocation(SettingGroups.EXPLOSION_SOURCES.getName(), "heal_ghast_explosions"), "(Default = false) Heal explosions caused by Ghasts.", false)),
    HEAL_WITHER_EXPLOSIONS(new BooleanSetting.Builder(new SettingLocation(SettingGroups.EXPLOSION_SOURCES.getName(), "heal_wither_explosions"), "(Default = false) Heal explosions caused by Withers.", false)),
    HEAL_TNT_EXPLOSIONS(new BooleanSetting.Builder(new SettingLocation(SettingGroups.EXPLOSION_SOURCES.getName(), "heal_tnt_explosions"), "(Default = false) Heal explosions caused by TNT blocks.", false)),
    HEAL_TNT_MINECART_EXPLOSIONS(new BooleanSetting.Builder(new SettingLocation(SettingGroups.EXPLOSION_SOURCES.getName(), "heal_tnt_minecart_explosions"), "(Default = false) Heal explosions caused by TNT minecarts.", false)),
    HEAL_BED_AND_RESPAWN_ANCHOR_EXPLOSIONS(new BooleanSetting.Builder(new SettingLocation(SettingGroups.EXPLOSION_SOURCES.getName(), "heal_bed_and_respawn_anchor_explosions"), "(Default = false) Heal explosions caused by beds and respawn anchors.", false)),
    HEAL_END_CRYSTAL_EXPLOSIONS(new BooleanSetting.Builder(new SettingLocation(SettingGroups.EXPLOSION_SOURCES.getName(), "heal_end_crystal_explosions"), "(Default = false) Heal explosions caused by End Crystals.", false)),

    MODE(new CommandControllableEnumSetting.Builder<>(new SettingLocation(SettingGroups.HEALING_MODE.getName(), "mode"), "(Default = \"default_mode\") Choose any of the following healing modes by copying one of the strings and pasting it into the value of the \"mode\" setting below:\n" +
            "\"%s\", \"%s\", \"%s\", \"%s\"".formatted(ExplosionHealingMode.DEFAULT_MODE.getName(), ExplosionHealingMode.DAYTIME_HEALING_MODE.getName(),
                    ExplosionHealingMode.DIFFICULTY_BASED_HEALING_MODE.getName(), ExplosionHealingMode.BLAST_RESISTANCE_BASED_HEALING_MODE.getName()), ExplosionHealingMode.DEFAULT_MODE)),

    RESTORE_BLOCK_NBT(new BooleanSetting.Builder(new SettingLocation(SettingGroups.PREFERENCES.getName(), "restore_block_nbt"), "(Default = false) Whether to restore block nbt data upon healing. This option prevents container blocks like chests from dropping their inventories. Does not apply when the healed block is different from the destroyed block due to a replace map entry.", false)),
    FORCE_BLOCKS_WITH_NBT_TO_ALWAYS_HEAL(new BooleanSetting.Builder(new SettingLocation(SettingGroups.PREFERENCES.getName(), "force_blocks_with_nbt_to_always_heal"), "(Default = false) Whether to force blocks with nbt data to always heal, even if the replace map specifies a replacement for that block, and regardless of the block that may be occupying that position at the moment of healing.", false)),
    MAKE_FALLING_BLOCKS_FALL(new BooleanSetting.Builder(new SettingLocation(SettingGroups.PREFERENCES.getName(), "make_falling_blocks_fall"), "(Default = true) Allows for a falling block, like sand or gravel, to fall when healed. Disabling this option makes the falling block have to receive a neighbor update before falling.", true)),
    BLOCK_PLACEMENT_SOUND_EFFECT(new BooleanSetting.Builder(new SettingLocation(SettingGroups.PREFERENCES.getName(), "block_placement_sound_effect"), "(Default = true) Whether a block placement sound effect should be played when a block is healed.", true)),
    HEAL_ON_HEALING_POTION_SPLASH(new BooleanSetting.Builder(new SettingLocation(SettingGroups.PREFERENCES.getName(), "heal_on_healing_potion_splash"), "(Default = true) Makes explosion heal immediately when a potion of Healing is thrown on them.", true)),
    HEAL_ON_REGENERATION_POTION_SPLASH(new BooleanSetting.Builder(new SettingLocation(SettingGroups.PREFERENCES.getName(), "heal_on_regeneration_potion_splash"), "(Default = true) Makes explosions begin their healing process when a potion of Regeneration is thrown on them.",  true)),
    ENABLE_WHITELIST(new BooleanSetting.Builder(new SettingLocation(SettingGroups.PREFERENCES.getName(), "enable_whitelist"), "(Default = false) Toggle the usage of the whitelist.", false)),

    WHITELIST(new StringListSetting.Builder(new SettingLocation(SettingGroups.WHITELIST.getName(), "whitelist"), new ArrayList<>(List.of("minecraft:placeholder"))));

    private final ConfigSetting.Builder<?, ?, ?> builder;
    private final SettingLocation settingLocation;

    ConfigSettings(ConfigSetting.Builder<?, ?, ?> builder) {
        this.builder = builder;
        this.settingLocation = builder.getSettingLocation();
    }

    public SettingLocation getSettingLocation() {
        return settingLocation;
    }

    public static List<ConfigSetting.Builder<?, ?, ?>> getSettingBuilders() {
        return Arrays.stream(ConfigSettings.values()).map(e -> e.builder).collect(Collectors.toList());
    }

}

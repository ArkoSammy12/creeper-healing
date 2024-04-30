package xd.arkosammy.creeperhealing.config.settings;

import xd.arkosammy.creeperhealing.config.ConfigTables;
import xd.arkosammy.creeperhealing.config.util.SettingIdentifier;
import xd.arkosammy.creeperhealing.explosions.ExplosionHealingMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum ConfigSettings {
    EXPLOSION_HEAL_DELAY(new HealDelaySetting.Builder(new SettingIdentifier(ConfigTables.DELAYS_CONFIG_TABLE.getName(), "explosion_heal_delay"), 3.0)
            .withComment("(Default = 3) How much time in seconds should an explosion wait for to begin healing.")),
    BLOCK_PLACEMENT_DELAY(new BlockPlacementDelaySetting.Builder(new SettingIdentifier(ConfigTables.DELAYS_CONFIG_TABLE.getName(), "block_placement_delay"), 1.0)
            .withComment("(Default = 1) The time in seconds that a block takes to heal.")),

    DROP_ITEMS_ON_CREEPER_EXPLOSIONS(new BooleanSetting.Builder(new SettingIdentifier(ConfigTables.EXPLOSION_ITEM_DROPS_TABLE.getName(), "drop_items_on_creeper_explosions"), true)
            .withComment("(Default = true) Whether to drop items when a creeper explodes.")),
    DROP_ITEMS_ON_GHAST_EXPLOSIONS(new BooleanSetting.Builder(new SettingIdentifier(ConfigTables.EXPLOSION_ITEM_DROPS_TABLE.getName(), "drop_items_on_ghast_explosions"), true)
            .withComment("(Default = true) Whether to drop items when a ghast explodes.")),
    DROP_ITEMS_ON_WITHER_EXPLOSIONS(new BooleanSetting.Builder(new SettingIdentifier(ConfigTables.EXPLOSION_ITEM_DROPS_TABLE.getName(), "drop_items_on_wither_explosions"), true)
            .withComment("(Default = true) Whether to drop items when a wither explodes.")),
    DROP_ITEMS_ON_TNT_EXPLOSIONS(new BooleanSetting.Builder(new SettingIdentifier(ConfigTables.EXPLOSION_ITEM_DROPS_TABLE.getName(), "drop_items_on_tnt_explosions"), true)
            .withComment("(Default = true) Whether to drop items when a tnt explodes.")),
    DROP_ITEMS_ON_TNT_MINECART_EXPLOSIONS(new BooleanSetting.Builder(new SettingIdentifier(ConfigTables.EXPLOSION_ITEM_DROPS_TABLE.getName(), "drop_items_on_tnt_minecart_explosions"), true)
            .withComment("(Default = true) Whether to drop items when a tnt minecart explodes.")),
    DROP_ITEMS_ON_BED_AND_RESPAWN_ANCHOR_EXPLOSIONS(new BooleanSetting.Builder(new SettingIdentifier(ConfigTables.EXPLOSION_ITEM_DROPS_TABLE.getName(), "drop_items_on_bed_and_respawn_anchor_explosions"), true)
            .withComment("(Default = true) Whether to drop items when a bed or respawn anchor explodes.")),
    DROP_ITEMS_ON_END_CRYSTAL_EXPLOSIONS(new BooleanSetting.Builder(new SettingIdentifier(ConfigTables.EXPLOSION_ITEM_DROPS_TABLE.getName(), "drop_items_on_end_crystal_explosions"), true)
            .withComment("(Default = true) Whether to drop items when an end crystal explodes.")),

    HEAL_CREEPER_EXPLOSIONS(new BooleanSetting.Builder(new SettingIdentifier(ConfigTables.EXPLOSION_SOURCE_TABLE.getName(), "heal_creeper_explosions"), true)
            .withComment("(Default = true) Heal explosions caused by Creepers.")),
    HEAL_GHAST_EXPLOSIONS(new BooleanSetting.Builder(new SettingIdentifier(ConfigTables.EXPLOSION_SOURCE_TABLE.getName(), "heal_ghast_explosions"), false)
            .withComment("(Default = false) Heal explosions caused by Ghasts.")),
    HEAL_WITHER_EXPLOSIONS(new BooleanSetting.Builder(new SettingIdentifier(ConfigTables.EXPLOSION_SOURCE_TABLE.getName(), "heal_wither_explosions"), false)
            .withComment("(Default = false) Heal explosions caused by Withers.")),
    HEAL_TNT_EXPLOSIONS(new BooleanSetting.Builder(new SettingIdentifier(ConfigTables.EXPLOSION_SOURCE_TABLE.getName(), "heal_tnt_explosions"), false)
            .withComment("(Default = false) Heal explosions caused by TNT blocks.")),
    HEAL_TNT_MINECART_EXPLOSIONS(new BooleanSetting.Builder(new SettingIdentifier(ConfigTables.EXPLOSION_SOURCE_TABLE.getName(), "heal_tnt_minecart_explosions"), false)
            .withComment("(Default = false) Heal explosions caused by TNT minecarts.")),
    HEAL_BED_AND_RESPAWN_ANCHOR_EXPLOSIONS(new BooleanSetting.Builder(new SettingIdentifier(ConfigTables.EXPLOSION_SOURCE_TABLE.getName(), "heal_bed_and_respawn_anchor_explosions"), false)
            .withComment("(Default = false) Heal explosions caused by beds and respawn anchors.")),
    HEAL_END_CRYSTAL_EXPLOSIONS(new BooleanSetting.Builder(new SettingIdentifier(ConfigTables.EXPLOSION_SOURCE_TABLE.getName(), "heal_end_crystal_explosions"), false)
            .withComment("(Default = false) Heal explosions caused by End Crystals.")),

    MODE(new StringSetting.Builder(new SettingIdentifier(ConfigTables.HEALING_MODE_TABLE.getName(), "mode"), ExplosionHealingMode.DEFAULT_MODE.getName())
            .withComment("(Default = \"default_mode\") Choose any of the following healing modes by copying one of the strings and pasting it into the value of the \"mode\" setting below:\n" +
                    "- \"%s\", \"%s\", \"%s\", \"%s\"".formatted(ExplosionHealingMode.DEFAULT_MODE.getName(), ExplosionHealingMode.DAYTIME_HEALING_MODE.getName(),
                            ExplosionHealingMode.DIFFICULTY_BASED_HEALING_MODE.getName(), ExplosionHealingMode.BLAST_RESISTANCE_BASED_HEALING_MODE.getName()))),

    RESTORE_BLOCK_NBT(new BooleanSetting.Builder(new SettingIdentifier(ConfigTables.PREFERENCES_TABLE.getName(), "restore_block_nbt"), false)
            .withComment("(Default = false) Whether to restore block nbt data upon healing. This option prevents container blocks like chests from dropping their inventories. Does not apply when the healed block is different from the destroyed block due to a replace map entry.")),
    FORCE_BLOCKS_WITH_NBT_TO_ALWAYS_HEAL(new BooleanSetting.Builder(new SettingIdentifier(ConfigTables.PREFERENCES_TABLE.getName(), "force_blocks_with_nbt_to_always_heal"), false)
            .withComment("(Default = false) Whether to force blocks with nbt data to always heal, even if the replace map specifies a replacement for that block, and regardless of the block that may be occupying that position at the moment of healing.")),
    MAKE_FALLING_BLOCKS_FALL(new BooleanSetting.Builder(new SettingIdentifier(ConfigTables.PREFERENCES_TABLE.getName(), "make_falling_blocks_fall"), true)
            .withComment("(Default = true) Allows for a falling block, like sand or gravel, to fall when healed. Disabling this option makes the falling block have to receive a neighbor update before falling.")),
    BLOCK_PLACEMENT_SOUND_EFFECT(new BooleanSetting.Builder(new SettingIdentifier(ConfigTables.PREFERENCES_TABLE.getName(), "block_placement_sound_effect"), true)
            .withComment("(Default = true) Whether a block placement sound effect should be played when a block is healed.")),
    HEAL_ON_HEALING_POTION_SPLASH(new BooleanSetting.Builder(new SettingIdentifier(ConfigTables.PREFERENCES_TABLE.getName(), "heal_on_healing_potion_splash"), true)
            .withComment("(Default = true) Makes explosion heal immediately when a potion of Healing is thrown on them.")),
    HEAL_ON_REGENERATION_POTION_SPLASH(new BooleanSetting.Builder(new SettingIdentifier(ConfigTables.PREFERENCES_TABLE.getName(), "heal_on_regeneration_potion_splash"), true)
            .withComment("(Default = true) Makes explosions begin their healing process when a potion of Regeneration is thrown on them.")),
    ENABLE_WHITELIST(new BooleanSetting.Builder(new SettingIdentifier(ConfigTables.PREFERENCES_TABLE.getName(), "enable_whitelist"), false)
            .withComment("(Default = false) Toggle the usage of the whitelist.")),

    WHITELIST(new StringListSetting.Builder(new SettingIdentifier(ConfigTables.WHITELIST_TABLE.getName(), "whitelist"), new ArrayList<>(Arrays.asList("minecraft:placeholder"))));

    private final ConfigSetting.Builder<?, ?> builder;
    private final SettingIdentifier id;

    ConfigSettings(ConfigSetting.Builder<?, ?> builder) {
        this.builder = builder;
        this.id = builder.id;
    }

    public SettingIdentifier getId() {
        return this.id;
    }

    public static List<ConfigSetting.Builder<?, ?>> getSettingBuilders() {
        List<ConfigSetting.Builder<?, ?>> settingBuilders = new ArrayList<>();
        for (ConfigSettings setting : ConfigSettings.values()) {
            settingBuilders.add(setting.builder);
        }
        return settingBuilders;
    }

}


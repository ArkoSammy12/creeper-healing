package io.github.arkosammy12.creeperhealing.config;

import io.github.arkosammy12.monkeyconfig.base.Setting;
import io.github.arkosammy12.monkeyconfig.managers.ConfigManagerUtils;
import io.github.arkosammy12.monkeyconfig.sections.maps.StringMapSection;
import io.github.arkosammy12.monkeyconfig.util.ElementPath;

import java.util.List;

import static io.github.arkosammy12.creeperhealing.CreeperHealing.CONFIG_MANAGER;

public final class ConfigUtils {

    public static ElementPath EXPLOSION_HEAL_DELAY;
    public static ElementPath BLOCK_PLACEMENT_DELAY;

    public static ElementPath DROP_ITEMS_ON_MOB_EXPLOSIONS;
    public static ElementPath DROP_ITEMS_ON_BLOCK_EXPLOSIONS;
    public static ElementPath DROP_ITEMS_ON_TNT_EXPLOSIONS;
    public static ElementPath DROP_ITEMS_ON_TRIGGERED_EXPLOSIONS;
    public static ElementPath DROP_ITEMS_ON_OTHER_EXPLOSIONS;
    public static ElementPath DROP_ITEMS_ON_MOB_EXPLOSIONS_BLACKLIST;

    public static ElementPath HEAL_MOB_EXPLOSIONS;
    public static ElementPath HEAL_BLOCK_EXPLOSIONS;
    public static ElementPath HEAL_TNT_EXPLOSIONS;
    public static ElementPath HEAL_TRIGGERED_EXPLOSIONS;
    public static ElementPath HEAL_OTHER_EXPLOSIONS;
    public static ElementPath HEAL_MOB_EXPLOSIONS_BLACKLIST;

    public static ElementPath MODE;

    public static ElementPath RESTORE_BLOCK_NBT;
    public static ElementPath FORCE_BLOCKS_WITH_NBT_TO_ALWAYS_HEAL;
    public static ElementPath MAKE_FALLING_BLOCKS_FALL;
    public static ElementPath BLOCK_PLACEMENT_SOUND_EFFECT;
    public static ElementPath BLOCK_PLACEMENT_PARTICLES;
    public static ElementPath HEAL_ON_HEALING_POTION_SPLASH;
    public static ElementPath HEAL_ON_REGENERATION_POTION_SPLASH;
    public static ElementPath ENABLE_WHITELIST;

    public static ElementPath WHITELIST;

    public static ElementPath REPLACE_MAP;

    private ConfigUtils() {
    }

    public static double DEFAULT_EXPLOSION_HEAL_DELAY = 3;
    public static double DEFAULT_BLOCK_PLACEMENT_DELAY = 1;

    public static boolean getRawBooleanSetting(ElementPath path) {
        Setting<Boolean, ?> setting = ConfigManagerUtils.getBooleanSetting(CONFIG_MANAGER, path);
        if (setting == null) {
            throw new IllegalArgumentException("No boolean setting found with path: " + path);
        }
        return setting.getValue().getRaw();
    }

    public static List<? extends String> getRawStringListSetting(ElementPath path) {
        Setting<List<? extends String>, ?> setting = ConfigManagerUtils.getStringListSetting(CONFIG_MANAGER, path);
        if (setting == null) {
            throw new IllegalArgumentException("No string list setting found with path: " + path);
        }
        return setting.getValue().getRaw();
    }

    public static <E extends Enum<E>> E getRawEnumSetting(ElementPath path) {
        Setting<E, ?> setting = ConfigManagerUtils.getEnumSetting(CONFIG_MANAGER, path);
        if (setting == null) {
            throw new IllegalArgumentException("No enum setting found with path: " + path);
        }
        return setting.getValue().getRaw();
    }

    public static StringMapSection getRawStringMapSection(ElementPath path) {
        StringMapSection section = ConfigManagerUtils.getStringMapSection(CONFIG_MANAGER, path);
        if (section == null) {
            throw new IllegalArgumentException("No string map section found with path: " + path);
        }
        return section;
    }

    public static long getExplosionHealDelay() {
        Setting<Double, ?> explosionHealDelaySetting = ConfigManagerUtils.getNumberSetting(CONFIG_MANAGER, EXPLOSION_HEAL_DELAY);
        double healDelay = explosionHealDelaySetting == null ? DEFAULT_EXPLOSION_HEAL_DELAY : explosionHealDelaySetting.getValue().getRaw();
        long rounded = Math.round(Math.max(0, healDelay) * 20);
        return rounded == 0 ? 20 : rounded;
    }

    public static long getBlockPlacementDelay() {
        Setting<Double, ?> blockPlacementDelaySetting = ConfigManagerUtils.getNumberSetting(CONFIG_MANAGER, BLOCK_PLACEMENT_DELAY);
        double blockPlacementDelay = blockPlacementDelaySetting == null ? DEFAULT_BLOCK_PLACEMENT_DELAY : blockPlacementDelaySetting.getValue().getRaw();
        long rounded = Math.round(Math.max(0, blockPlacementDelay) * 20);
        return rounded == 0 ? 20 : rounded;
    }

}

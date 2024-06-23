package xd.arkosammy.creeperhealing.config;

import xd.arkosammy.monkeyconfig.groups.SettingGroup;
import xd.arkosammy.monkeyconfig.managers.ConfigManagerKt;
import xd.arkosammy.monkeyconfig.settings.ConfigSetting;
import xd.arkosammy.monkeyconfig.settings.EnumSetting;
import xd.arkosammy.monkeyconfig.settings.NumberSetting;
import xd.arkosammy.monkeyconfig.util.SettingLocation;

import static xd.arkosammy.creeperhealing.CreeperHealing.CONFIG_MANAGER;

public final class ConfigUtils {

    private ConfigUtils() {}

    public static double DEFAULT_EXPLOSION_HEAL_DELAY = 3;
    public static double DEFAULT_BLOCK_PLACEMENT_DELAY = 1;

    public static SettingGroup getSettingGroup(String groupName) {
        SettingGroup settingGroup = CONFIG_MANAGER.getSettingGroup(groupName);
        if (settingGroup == null) {
            throw new IllegalArgumentException("No setting group with name " + groupName + " was found on Config Manager for file" + CONFIG_MANAGER.getConfigName());
        }
        return settingGroup;
    }

    public static <V, T extends ConfigSetting<V, ?>> V getSettingValue(SettingLocation settingLocation, Class<T> clazz) {
        T setting = CONFIG_MANAGER.getTypedSetting(settingLocation, clazz);
        if (setting == null) {
            throw new IllegalArgumentException("No setting with location " + settingLocation + " with type + " + clazz.getSimpleName() + " was found on Config Manager for file " + CONFIG_MANAGER.getConfigName());
        }
        return setting.getValue();
    }

    public static <E extends Enum<E>> E getEnumSettingValue(SettingLocation settingLocation) {
        EnumSetting<E> enumSetting = ConfigManagerKt.getAsEnumSetting(CONFIG_MANAGER, settingLocation);
        if (enumSetting == null) {
            throw new IllegalArgumentException("No enum setting with location " + settingLocation + " was found on Config Manager for file " + CONFIG_MANAGER.getConfigName());
        }
        return enumSetting.getValue();
    }

    public static long getExplosionHealDelay() {
        NumberSetting<Double> explosionHealDelaySetting = ConfigManagerKt.getAsDoubleSetting(CONFIG_MANAGER, ConfigSettings.EXPLOSION_HEAL_DELAY.getSettingLocation());
        double healDelay = explosionHealDelaySetting == null ? DEFAULT_EXPLOSION_HEAL_DELAY : explosionHealDelaySetting.getValue();
        long rounded = Math.round(Math.max(0, healDelay) * 20);
        return rounded == 0 ? 20 : rounded;
    }

    public static long getBlockPlacementDelay() {
        NumberSetting<Double> blockPlacementDelaySetting = ConfigManagerKt.getAsDoubleSetting(CONFIG_MANAGER, ConfigSettings.BLOCK_PLACEMENT_DELAY.getSettingLocation());
        double blockPlacementDelay = blockPlacementDelaySetting == null ? DEFAULT_BLOCK_PLACEMENT_DELAY : blockPlacementDelaySetting.getValue();
        long rounded = Math.round(Math.max(0, blockPlacementDelay) * 20);
        return rounded == 0 ? 20 : rounded;
    }

}

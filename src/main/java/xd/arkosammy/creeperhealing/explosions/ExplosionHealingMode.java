package xd.arkosammy.creeperhealing.explosions;

import net.minecraft.util.StringIdentifiable;

public enum ExplosionHealingMode implements StringIdentifiable {

    DEFAULT_MODE("default_mode"),
    DAYTIME_HEALING_MODE("daytime_healing_mode"),
    DIFFICULTY_BASED_HEALING_MODE("difficulty_based_healing_mode"),
    BLAST_RESISTANCE_BASED_HEALING_MODE("blast_resistance_based_healing_mode");

    private final String name;

    ExplosionHealingMode(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public static ExplosionHealingMode getFromName(String name) {
        for (ExplosionHealingMode explosionMode : ExplosionHealingMode.values()) {
            if (explosionMode.getName().equals(name)) {
                return explosionMode;
            }
        }
        return ExplosionHealingMode.DEFAULT_MODE;
    }

    @Override
    public String asString() {
        return this.name;
    }

}

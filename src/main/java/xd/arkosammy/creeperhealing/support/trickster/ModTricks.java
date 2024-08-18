package xd.arkosammy.creeperhealing.support.trickster;

import dev.enjarai.trickster.spell.trick.Tricks;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import xd.arkosammy.creeperhealing.CreeperHealing;
import xd.arkosammy.creeperhealing.support.trickster.tricks.HealExplosionTrick;

public final class ModTricks {

    private ModTricks() {}

    public static HealExplosionTrick HEAL_EXPLOSION_TRICK = new HealExplosionTrick();

    public static void registerTricks() {

        Registry.register(Tricks.REGISTRY, Identifier.of(CreeperHealing.MOD_ID, "heal_explosion"), HEAL_EXPLOSION_TRICK);

    }

}

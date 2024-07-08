package xd.arkosammy.creeperhealing.explosions;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import xd.arkosammy.creeperhealing.blocks.AffectedBlock;
import xd.arkosammy.creeperhealing.config.ConfigSettings;
import xd.arkosammy.creeperhealing.config.ConfigUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface ExplosionEvent {

    Stream<AffectedBlock> getAffectedBlocks();

    Optional<AffectedBlock> getCurrentAffectedBlock();

    World getWorld(MinecraftServer server);

    long getHealTimer();

    int getBlockCounter();

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean shouldKeepHealing(World world);

    void setup(World world);

    SerializedExplosionEvent asSerialized();

    void tick(MinecraftServer server);

    static ExplosionEvent newInstance(List<AffectedBlock> affectedBlocks, World world) {
        final ExplosionHealingMode explosionHealingMode = ConfigUtils.getEnumSettingValue(ConfigSettings.MODE.getSettingLocation());
        final AbstractExplosionEvent explosionEvent = switch (explosionHealingMode) {
            case DAYTIME_HEALING_MODE -> new DaytimeExplosionEvent(affectedBlocks);
            case DIFFICULTY_BASED_HEALING_MODE -> new DifficultyBasedExplosionEvent(affectedBlocks);
            case BLAST_RESISTANCE_BASED_HEALING_MODE -> new BlastResistanceBasedExplosionEvent(affectedBlocks);
            default -> new DefaultExplosionEvent(affectedBlocks);
        };
        explosionEvent.setup(world);
        return explosionEvent;
    }

}

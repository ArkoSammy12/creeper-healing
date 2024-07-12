package xd.arkosammy.creeperhealing.explosions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;
import xd.arkosammy.creeperhealing.blocks.AffectedBlock;
import xd.arkosammy.creeperhealing.blocks.DefaultSerializedAffectedBlock;
import xd.arkosammy.creeperhealing.blocks.SerializedAffectedBlock;

import java.util.List;
import java.util.stream.Collectors;

public record DefaultSerializedExplosion(
        String healingMode,
        List<SerializedAffectedBlock> serializedAffectedBlocks,
        long healTimer,
        int blockCounter,
        int radius,
        BlockPos center
) implements SerializedExplosionEvent {

    public static final Codec<SerializedExplosionEvent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("healing_mode").forGetter(SerializedExplosionEvent::getExplosionTypeName),
            Codec.list(DefaultSerializedAffectedBlock.CODEC).fieldOf("affected_blocks").forGetter(SerializedExplosionEvent::getSerializedAffectedBlocks),
            Codec.LONG.fieldOf("heal_timer").forGetter(SerializedExplosionEvent::getHealTimer),
            Codec.INT.fieldOf("block_counter").forGetter(serializedExplosionEvent -> serializedExplosionEvent.getCustomData("blockCounter", Integer.class)),
            Codec.INT.fieldOf("radius").forGetter(serializedExplosionEvent -> serializedExplosionEvent.getCustomData("radius", Integer.class)),
            BlockPos.CODEC.fieldOf("center").forGetter(serializedExplosionEvent -> serializedExplosionEvent.getCustomData("center", BlockPos.class))
    ).apply(instance, DefaultSerializedExplosion::new));

    @Override
    public String getExplosionTypeName() {
        return healingMode;
    }

    @Override
    public List<SerializedAffectedBlock> getSerializedAffectedBlocks() {
        return this.serializedAffectedBlocks;
    }

    @Override
    public long getHealTimer() {
        return this.healTimer;
    }

    // Unchecked cast done under the assumption that T is always the proper type of the custom data that we wish to access
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCustomData(String name, Class<T> clazz) {
        Object data = switch (name) {
            case "blockCounter" -> this.blockCounter;
            case "radius" -> this.radius;
            case "center" -> this.center;
            default -> throw new IllegalArgumentException("Unknown serialized explosion event property of name: " + name + "!");
        };
        if (clazz.isInstance(data)) {
            return (T) data;
        } else {
            throw new IllegalArgumentException("Property of serialized explosion event with name " + name + " cannot be cased to " + clazz.getSimpleName());
        }
    }

    @Override
    public ExplosionEvent asDeserialized() {
        ExplosionHealingMode healingMode = ExplosionHealingMode.getFromName(this.healingMode);
        List<AffectedBlock> affectedBlocks = this.serializedAffectedBlocks.stream().map(SerializedAffectedBlock::asDeserialized).collect(Collectors.toList());
        return switch (healingMode) {
            case DEFAULT_MODE -> new DefaultExplosionEvent(affectedBlocks, this.healTimer, this.blockCounter, this.radius, this.center);
            case DAYTIME_HEALING_MODE -> new DaytimeExplosionEvent(affectedBlocks, this.healTimer, this.blockCounter, this.radius, this.center);
            case DIFFICULTY_BASED_HEALING_MODE -> new DifficultyBasedExplosionEvent(affectedBlocks, this.healTimer, this.blockCounter, this.radius, this.center);
            case BLAST_RESISTANCE_BASED_HEALING_MODE -> new BlastResistanceBasedExplosionEvent(affectedBlocks, this.healTimer, this.blockCounter, this.radius, this.center);
        };
    }

}

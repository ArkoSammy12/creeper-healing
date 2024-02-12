package xd.arkosammy.creeperhealing.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import xd.arkosammy.creeperhealing.blocks.AffectedBlock;
import xd.arkosammy.creeperhealing.explosions.*;

import java.util.List;
import java.util.stream.Collectors;

public record SerializedExplosionEvent(String healingMode, List<SerializedAffectedBlock> serializedAffectedBlocks, long healTimer, int blockCounter) {

    static final Codec<SerializedExplosionEvent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("healing_mode").forGetter(SerializedExplosionEvent::healingMode),
            Codec.list(SerializedAffectedBlock.CODEC).fieldOf("affected_blocks").forGetter(SerializedExplosionEvent::serializedAffectedBlocks),
            Codec.LONG.fieldOf("heal_timer").forGetter(SerializedExplosionEvent::healTimer),
            Codec.INT.fieldOf("block_counter").forGetter(SerializedExplosionEvent::blockCounter)
    ).apply(instance, SerializedExplosionEvent::new));

    AbstractExplosionEvent toDeserialized(){
        List<AffectedBlock> affectedBlocks = this.serializedAffectedBlocks.stream().map(SerializedAffectedBlock::toDeserialized).collect(Collectors.toList());
        ExplosionHealingMode explosionHealingMode = ExplosionHealingMode.getFromName(this.healingMode);
        return switch (explosionHealingMode){
            case DAYTIME_HEALING_MODE -> new DaytimeExplosionEvent(affectedBlocks, this.healTimer, this.blockCounter);
            case DIFFICULTY_BASED_HEALING_MODE -> new DifficultyBasedExplosionEvent(affectedBlocks, this.healTimer, this.blockCounter);
            case BLAST_RESISTANCE_BASED_HEALING_MODE -> new BlastResistanceBasedExplosionEvent(affectedBlocks, this.healTimer, this.blockCounter);
            default -> new DefaultExplosionEvent(affectedBlocks, this.healTimer, this.blockCounter);
        };
    }

}

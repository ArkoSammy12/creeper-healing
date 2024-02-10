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
            Codec.LONG.fieldOf(" heal_timer").forGetter(SerializedExplosionEvent::healTimer),
            Codec.INT.fieldOf("block_counter").forGetter(SerializedExplosionEvent::blockCounter)
    ).apply(instance, SerializedExplosionEvent::new));

    AbstractExplosionEvent toDeserialized(){
        List<AffectedBlock> affectedBlocks = this.serializedAffectedBlocks.stream().map(SerializedAffectedBlock::toDeserialized).collect(Collectors.toList());
        if(this.healingMode.equals(ExplosionHealingMode.DAYTIME_HEALING_MODE.getName())){
            return new DaytimeExplosionEvent(affectedBlocks, this.healTimer, this.blockCounter);
        } else if (this.healingMode.equals(ExplosionHealingMode.DIFFICULTY_BASED_HEALING_MODE.getName())){
            return new DifficultyBasedExplosionEvent(affectedBlocks, this.healTimer, this.blockCounter);
        } else if (this.healingMode.equals(ExplosionHealingMode.BLAST_RESISTANCE_BASED_HEALING_MODE.getName())){
            return new BlastResistanceBasedExplosionEvent(affectedBlocks, this.healTimer, this.blockCounter);
        } else {
            return new DefaultExplosionEvent(affectedBlocks, this.healTimer, this.blockCounter);
        }

    }

}

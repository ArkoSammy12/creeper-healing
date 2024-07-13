package xd.arkosammy.creeperhealing.explosions;

import xd.arkosammy.creeperhealing.blocks.SerializedAffectedBlock;

import java.util.List;
import java.util.Optional;

public interface SerializedExplosionEvent {

    String getExplosionTypeName();

    List<SerializedAffectedBlock> getSerializedAffectedBlocks();

    long getHealTimer();

    <T> Optional<T> getCustomData(String name, Class<T> clazz);

    ExplosionEvent asDeserialized();

}

package xd.arkosammy.creeperhealing.explosions;

import xd.arkosammy.creeperhealing.blocks.SerializedAffectedBlock;

import java.util.List;

public interface SerializedExplosionEvent {

    String getExplosionTypeName();

    List<SerializedAffectedBlock> getSerializedAffectedBlocks();

    long getHealTimer();

    <T> T getCustomData(String name, Class<T> clazz);

    ExplosionEvent asDeserialized();

}

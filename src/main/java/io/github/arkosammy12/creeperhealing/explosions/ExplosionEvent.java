package io.github.arkosammy12.creeperhealing.explosions;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import io.github.arkosammy12.creeperhealing.blocks.AffectedBlock;

import java.util.stream.Stream;

public interface ExplosionEvent {

    void setup(ServerLevel world);

    Stream<AffectedBlock> getAffectedBlocks();

    ServerLevel getWorld(MinecraftServer server);

    long getHealTimer();

    boolean isFinished();

    void tick(MinecraftServer server);

    SerializedExplosionEvent asSerialized();

}

package xd.arkosammy.creeperhealing.explosions;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import xd.arkosammy.creeperhealing.blocks.AffectedBlock;

import java.util.stream.Stream;

public interface ExplosionEvent {

    void setup(ServerWorld world);

    Stream<AffectedBlock> getAffectedBlocks();

    ServerWorld getWorld(MinecraftServer server);

    long getHealTimer();

    boolean isFinished();

    void tick(MinecraftServer server);

    SerializedExplosionEvent asSerialized();

}

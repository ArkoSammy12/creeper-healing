package xd.arkosammy.creeperhealing.explosions;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import xd.arkosammy.creeperhealing.blocks.AffectedBlock;

import java.util.stream.Stream;

public interface ExplosionEvent {

    void setup(World world);

    Stream<AffectedBlock> getAffectedBlocks();

    World getWorld(MinecraftServer server);

    long getHealTimer();

    boolean isFinished();

    void tick(MinecraftServer server);

    SerializedExplosionEvent asSerialized();

}

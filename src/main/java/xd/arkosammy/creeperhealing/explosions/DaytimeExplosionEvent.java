package xd.arkosammy.creeperhealing.explosions;

import net.minecraft.SharedConstants;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import xd.arkosammy.creeperhealing.blocks.AffectedBlock;
import xd.arkosammy.creeperhealing.blocks.SingleAffectedBlock;

import java.util.List;

public class DaytimeExplosionEvent extends AbstractExplosionEvent {


    public DaytimeExplosionEvent(List<AffectedBlock> affectedBlocks, int radius, BlockPos center) {
        super(affectedBlocks, radius, center);
    }

    public DaytimeExplosionEvent(List<AffectedBlock> affectedBlocks, long healTimer, int blockCounter, int radius, BlockPos center) {
        super(affectedBlocks, healTimer, blockCounter, radius, center);
    }

    @Override
    protected ExplosionHealingMode getHealingMode() {
        return ExplosionHealingMode.DAYTIME_HEALING_MODE;
    }

    @Override
    public void setup(World world) {
        this.healTimer = SharedConstants.TICKS_PER_IN_GAME_DAY - (world.getTimeOfDay() % SharedConstants.TICKS_PER_IN_GAME_DAY);
        int daylightBasedBlockPlacementDelay = (int) (13000 / Math.max(this.getAffectedBlocks().count(), 1));
        for (AffectedBlock affectedBlock : this.getAffectedBlocks().toList()) {
            if (!(affectedBlock instanceof SingleAffectedBlock singleAffectedBlock)) {
                continue;
            }
            singleAffectedBlock.setTimer(daylightBasedBlockPlacementDelay);
        }
    }

    @Override
    public void updateFinishedStatus(World world) {
        if (this.getBlockCounter() > 0) {
            return;
        }
        MinecraftServer server = world.getServer();
        boolean sufficientLight = this.getAffectedBlocks().anyMatch(affectedBlock -> {
            BlockPos pos = affectedBlock.getBlockPos();
            World blockWorld = affectedBlock.getWorld(server);
            return blockWorld.getLightLevel(LightType.BLOCK, pos) > 0 || blockWorld.getLightLevel(LightType.SKY, pos) > 0;
        });
        if (!sufficientLight) {
            this.finished = true;
        }
    }

}

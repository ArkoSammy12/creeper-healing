package io.github.arkosammy12.creeperhealing.explosions;

import net.minecraft.SharedConstants;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.Level;
import io.github.arkosammy12.creeperhealing.blocks.AffectedBlock;
import io.github.arkosammy12.creeperhealing.blocks.SingleAffectedBlock;
import net.minecraft.world.level.gamerules.GameRules;

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
    public void setup(ServerLevel world) {
        if (!world.getGameRules().get(GameRules.ADVANCE_TIME)) {
            return;
        }
        this.healTimer = SharedConstants.TICKS_PER_GAME_DAY - (world.getDefaultClockTime() % SharedConstants.TICKS_PER_GAME_DAY);
        int daylightBasedBlockPlacementDelay = (int) (13000 / Math.max(this.getAffectedBlocks().count(), 1));
        for (AffectedBlock affectedBlock : this.getAffectedBlocks().toList()) {
            if (!(affectedBlock instanceof SingleAffectedBlock singleAffectedBlock)) {
                continue;
            }
            singleAffectedBlock.setTimer(daylightBasedBlockPlacementDelay);
        }
    }

    @Override
    public void updateFinishedStatus(Level world) {
        if (this.getBlockCounter() > 0) {
            return;
        }
        MinecraftServer server = world.getServer();
        boolean sufficientLight = this.getAffectedBlocks().anyMatch(affectedBlock -> {
            BlockPos pos = affectedBlock.getBlockPos();
            Level blockWorld = affectedBlock.getWorld(server);
            return blockWorld.getBrightness(LightLayer.BLOCK, pos) > 0 || blockWorld.getBrightness(LightLayer.SKY, pos) > 0;
        });
        if (!sufficientLight) {
            this.finished = true;
        }
    }

}

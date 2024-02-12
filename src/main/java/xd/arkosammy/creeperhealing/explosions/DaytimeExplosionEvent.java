package xd.arkosammy.creeperhealing.explosions;

import net.minecraft.world.LightType;
import net.minecraft.world.World;
import xd.arkosammy.creeperhealing.blocks.AffectedBlock;

import java.util.List;

public class DaytimeExplosionEvent extends AbstractExplosionEvent {

    public DaytimeExplosionEvent(List<AffectedBlock> affectedBlocks, long healTimer, int blockCounter) {
        super(affectedBlocks, healTimer, blockCounter);
    }

    DaytimeExplosionEvent(List<AffectedBlock> affectedBlocks){
        super(affectedBlocks);
    }

    @Override
    ExplosionHealingMode getHealingMode() {
        return ExplosionHealingMode.DAYTIME_HEALING_MODE;
    }

    // Set the timer of this explosion equal to the time left between now and the next sunrise (getTimeOfDay % 24000 == 0)
    // Spread the placements of this explosion's affected blocks evenly throughout the day
    @Override
    public void setupExplosion(World world){
        this.setHealTimer(24000 - (world.getTimeOfDay() % 24000));
        int daylightBasedBlockPlacementDelay = 13000 / Math.max(this.getAffectedBlocks().size(), 1);
        this.getAffectedBlocks().forEach(affectedBlock -> affectedBlock.setTimer(daylightBasedBlockPlacementDelay));
    }

    // Check for sufficient light level at the explosion's location
    @Override
    public boolean shouldKeepHealing(World world) {
        //We return true if the current block counter is greater than 0,
        //since we want to allow explosions to heal completely if the light conditions were only met initially
        if (this.getBlockCounter() > 0){
            return true;
        }
        return this.getAffectedBlocks().stream().anyMatch(affectedBlock -> affectedBlock.getWorld(world.getServer()).getLightLevel(LightType.BLOCK, affectedBlock.getPos()) > 0 || affectedBlock.getWorld(world.getServer()).getLightLevel(LightType.SKY, affectedBlock.getPos()) > 0);
    }

}

package xd.arkosammy.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xd.arkosammy.events.AffectedBlock;
import xd.arkosammy.events.CreeperExplosionEvent;
import xd.arkosammy.handlers.ExplosionHealerHandler;
import java.util.ArrayList;
import java.util.List;
import static xd.arkosammy.CreeperHealing.CONFIG;

@Mixin(Explosion.class)
public abstract class CreeperExplosionMixin {

    @Shadow @Final private World world;
    @Shadow @Nullable public abstract LivingEntity getCausingEntity();
    @Shadow public abstract List<BlockPos> getAffectedBlocks();

    @Inject(method = "collectBlocksAndDamageEntities", at = @At("RETURN"))
    private void getExplodedBlocks(CallbackInfo ci){

        //Only check for explosions caused by creepers
        if(this.getCausingEntity() instanceof CreeperEntity) {

            //Get our list of affected block positions after they have been created by the target method
            List<BlockPos> affectedBlocksPos = this.getAffectedBlocks();

            //Don't store empty explosions
            if(!affectedBlocksPos.isEmpty()) {

                ArrayList<AffectedBlock> affectedBlocks = new ArrayList<>();

                for (BlockPos pos : affectedBlocksPos) {

                    //Let's not store a bunch of unnecessary air blocks
                    if (!world.getBlockState(pos).isAir()) {

                        affectedBlocks.add(new AffectedBlock(pos, world.getBlockState(pos), world.getRegistryKey(), CONFIG.getBlockPlacementDelay(), false));

                    }

                }

                CreeperExplosionEvent creeperExplosionEvent = new CreeperExplosionEvent(affectedBlocks, CONFIG.getExplosionDelay(), 0, false);

                if (CONFIG.isDaytimeHealingEnabled()) {

                    creeperExplosionEvent.setupDayTimeHealing(world);

                }

                //Add a new CreeperExplosionEvent to the list, passing in our list of affected blocks
                ExplosionHealerHandler.getExplosionEventList().add(creeperExplosionEvent);

            }

        }

    }

}

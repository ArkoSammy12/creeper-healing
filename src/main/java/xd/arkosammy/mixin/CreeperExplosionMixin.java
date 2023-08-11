package xd.arkosammy.mixin;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
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
import xd.arkosammy.handlers.ExplosionHealerHandler;
import xd.arkosammy.util.BlockInfo;
import xd.arkosammy.events.CreeperExplosionEvent;

import java.util.ArrayList;

@Mixin(Explosion.class)
public abstract class CreeperExplosionMixin {
    @Shadow @Final private ObjectArrayList<BlockPos> affectedBlocks;
    @Shadow @Nullable public abstract Entity getEntity();
    @Shadow @Final private World world;

    @Inject(method = "collectBlocksAndDamageEntities", at = @At("RETURN"))
    public void getExplodedBlocks(CallbackInfo ci){

        //Only check for explosions caused by creepers
        if(this.getEntity() instanceof CreeperEntity) {

            ArrayList<BlockInfo> blockInfoList = new ArrayList<>();

            //Get our list of affectedBlocks straight from the Explosion class
            ObjectArrayList<BlockPos> affectedBlocksPos = (ObjectArrayList<BlockPos>) ((Explosion) (Object) this).getAffectedBlocks();

            for(BlockPos pos: affectedBlocksPos){

                //Let's not store a bunch of unnecessary air blocks
                if(!world.getBlockState(pos).getBlock().getName().equals(Blocks.AIR.getName())) {

                    blockInfoList.add(new BlockInfo(pos, world.getBlockState(pos), world.getRegistryKey(), ExplosionHealerHandler.getBlockPlacementDelay()));

                }

            }

            //Create a new CreeperExplosionEvent object from the blockInfoList
            //we just obtained and add it to the queue to be processed.
            //Also sort the list of BlockInfo from lowest to highest Y position to heal bottom to top.
            CreeperExplosionEvent.getExplosionEventsForUsage().add(new CreeperExplosionEvent(BlockInfo.getAsYSorted(blockInfoList), ExplosionHealerHandler.getExplosionDelay(), 0));

        }

    }

}

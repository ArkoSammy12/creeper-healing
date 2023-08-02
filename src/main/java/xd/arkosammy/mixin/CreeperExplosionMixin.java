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

        if(this.getEntity() instanceof CreeperEntity) { //Is the explosion from a Creeper entity?

            ArrayList<BlockInfo> blockInfoList = new ArrayList<>();

            ObjectArrayList<BlockPos> affectedBlocksPos = (ObjectArrayList<BlockPos>) ((Explosion) (Object) this).getAffectedBlocks();

            for(BlockPos pos: affectedBlocksPos){

                //Let's not store a bunch of unnecessary air blocks

                if(!world.getBlockState(pos).getBlock().getName().equals(Blocks.AIR.getName())) {

                    blockInfoList.add(new BlockInfo(pos, world.getBlockState(pos)));

                }

            }

            //Offer a new CreeperExplosionEvent to the queue

            CreeperExplosionEvent.getExplosionEvents().offer(new CreeperExplosionEvent(blockInfoList));

        }

    }

}
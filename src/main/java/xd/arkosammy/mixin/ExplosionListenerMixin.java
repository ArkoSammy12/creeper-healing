package xd.arkosammy.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xd.arkosammy.configuration.tables.ExplosionSourceConfig;
import xd.arkosammy.explosions.AffectedBlock;
import xd.arkosammy.explosions.ExplosionEvent;
import xd.arkosammy.handlers.ExplosionListHandler;
import java.util.ArrayList;
import java.util.List;


@Mixin(Explosion.class)
public abstract class ExplosionListenerMixin {

    @Shadow @Final private World world;
    @Shadow @Nullable public abstract LivingEntity getCausingEntity();
    @Shadow public abstract List<BlockPos> getAffectedBlocks();
    @Shadow @Nullable public abstract Entity getEntity();

    @Inject(method = "collectBlocksAndDamageEntities", at = @At("RETURN"))
    private void getExplodedBlocks(CallbackInfo ci){

        if(canStoreExplosion(this.getCausingEntity(), this.getEntity()))
            storeExplosion(this.getAffectedBlocks());

    }

    @Unique
    private void storeExplosion(List<BlockPos> affectedBlocksPos){

        //Don't store empty explosions
        if(affectedBlocksPos.isEmpty()) return;

        ArrayList<AffectedBlock> affectedBlocks = new ArrayList<>();

        for (BlockPos pos : affectedBlocksPos) {

            //Let's not store a bunch of unnecessary air blocks
            if (!world.getBlockState(pos).isAir()) {

                affectedBlocks.add(AffectedBlock.newAffectedBlock(pos, world));

            }

        }


        //Add a new ExplosionEvent to the list, passing in our list of affected blocks
        ExplosionListHandler.getExplosionEventList().add(ExplosionEvent.newExplosionEvent(affectedBlocks, world, affectedBlocksPos));

    }

    @Unique
    private boolean canStoreExplosion(LivingEntity causingLivingEntity, Entity causingEntity){

        return (causingLivingEntity instanceof CreeperEntity && ExplosionSourceConfig.getHealCreeperExplosions())
                || (causingLivingEntity instanceof GhastEntity && ExplosionSourceConfig.getHealGhastExplosions())
                || (causingLivingEntity instanceof WitherEntity && ExplosionSourceConfig.getHealWitherExplosions())
                || (causingEntity instanceof TntEntity && ExplosionSourceConfig.getHealTNTExplosions())
                || (causingEntity instanceof TntMinecartEntity && ExplosionSourceConfig.getHealTNTMinecartExplosions());

    }

}

package xd.arkosammy.mixin;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.registry.Registries;
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
import xd.arkosammy.configuration.tables.WhitelistConfig;
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

        if(affectedBlocksPos.isEmpty()) return;
        ArrayList<AffectedBlock> affectedBlocks = new ArrayList<>();

        for (BlockPos pos : affectedBlocksPos) {
            if (world.getBlockState(pos).isAir() || world.getBlockState(pos).getBlock().equals(Blocks.TNT)) {
                continue; // Skip the current iteration if the block state is air or TNT
            }
            String blockIdentifier = Registries.BLOCK.getId(world.getBlockState(pos).getBlock()).toString();
            if (!WhitelistConfig.getEnableWhitelist() || WhitelistConfig.getWhitelist().contains(blockIdentifier)) {
                affectedBlocks.add(AffectedBlock.newAffectedBlock(pos, world));
            }
        }

        if(affectedBlocks.isEmpty()) return;
        ExplosionListHandler.getExplosionEventList().add(ExplosionEvent.newExplosionEvent(affectedBlocks, world));
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

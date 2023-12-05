package xd.arkosammy.mixin;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.decoration.EndCrystalEntity;
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
import xd.arkosammy.configuration.tables.PreferencesConfig;
import xd.arkosammy.configuration.tables.WhitelistConfig;
import xd.arkosammy.explosions.AffectedBlock;
import xd.arkosammy.explosions.ExplosionEvent;
import xd.arkosammy.explosions.ExplosionUtils;
import xd.arkosammy.handlers.ExplosionListHandler;
import java.util.ArrayList;
import java.util.List;


@Mixin(Explosion.class)
public abstract class ExplosionListenerMixin implements ExplosionDamageSourceInvoker {

    @Shadow @Final private World world;
    @Shadow @Nullable public abstract LivingEntity getCausingEntity();
    @Shadow public abstract List<BlockPos> getAffectedBlocks();
    @Shadow @Nullable public abstract Entity getEntity();

    @Shadow @Final private DamageSource damageSource;

    @Override
    public DamageSource creeper_healing$getDamageSource() {
        return this.damageSource;
    }

    @Inject(method = "collectBlocksAndDamageEntities", at = @At("RETURN"))
    private void storeCurrentExplosionIfNeeded(CallbackInfo ci){
        if(shouldStoreExplosionFromSourceType(this.getCausingEntity(), this.getEntity(), this.damageSource))
            storeExplosion(this.getAffectedBlocks());
    }

    @Unique
    private void storeExplosion(List<BlockPos> affectedBlocksPos){
        if(affectedBlocksPos.isEmpty()) return;
        ArrayList<AffectedBlock> affectedBlocks = new ArrayList<>();
        for (BlockPos pos : affectedBlocksPos) {
            if (world.getBlockState(pos).isAir() || world.getBlockState(pos).getBlock().equals(Blocks.TNT) || world.getBlockState(pos).getBlock().equals(Blocks.FIRE) || world.getBlockState(pos).getBlock().equals(Blocks.SOUL_FIRE)) {
                continue; // Skip the current iteration if the block state is air, TNT, or fire
            }
            String blockIdentifier = Registries.BLOCK.getId(world.getBlockState(pos).getBlock()).toString();
            if (!PreferencesConfig.ENABLE_WHITELIST.getEntry().getValue() || WhitelistConfig.getWhitelist().contains(blockIdentifier)) {
                affectedBlocks.add(AffectedBlock.newAffectedBlock(pos, world));
            }
        }
        if(affectedBlocks.isEmpty()) return;
        ExplosionListHandler.getExplosionEventList().add(ExplosionEvent.newExplosionEvent(affectedBlocks, world));
    }

    @Unique
    private boolean shouldStoreExplosionFromSourceType(LivingEntity causingLivingEntity, Entity causingEntity, DamageSource damageSource){
        return (causingLivingEntity instanceof CreeperEntity && ExplosionSourceConfig.HEAL_CREEPER_EXPLOSIONS.getEntry().getValue())
                || (causingLivingEntity instanceof GhastEntity && ExplosionSourceConfig.HEAL_GHAST_EXPLOSIONS.getEntry().getValue())
                || (causingLivingEntity instanceof WitherEntity && ExplosionSourceConfig.HEAL_WITHER_EXPLOSIONS.getEntry().getValue())
                || (causingEntity instanceof TntEntity && ExplosionSourceConfig.HEAL_TNT_EXPLOSIONS.getEntry().getValue())
                || (causingEntity instanceof TntMinecartEntity && ExplosionSourceConfig.HEAL_TNT_MINECART_EXPLOSIONS.getEntry().getValue())
                || (damageSource.isOf(DamageTypes.BAD_RESPAWN_POINT) && ExplosionSourceConfig.HEAL_BED_AND_RESPAWN_ANCHOR_EXPLOSIONS.getEntry().getValue())
                || (causingEntity instanceof EndCrystalEntity && ExplosionSourceConfig.HEAL_END_CRYSTAL_EXPLOSIONS.getEntry().getValue());

    }

    @Inject(method = "affectWorld", at = @At(value = "HEAD"))
    private void setDropItemsThreadLocal(boolean particles, CallbackInfo ci){
        ExplosionUtils.SHOULD_NOT_DROP_ITEMS.set(false);
    }

    @Inject(method = "affectWorld", at = @At(value = "RETURN"))
    private void clearDropItemsThreadLocal(boolean particles, CallbackInfo ci){
        ExplosionUtils.SHOULD_NOT_DROP_ITEMS.set(false);
    }
}

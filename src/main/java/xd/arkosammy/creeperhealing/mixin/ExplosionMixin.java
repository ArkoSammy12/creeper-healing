package xd.arkosammy.creeperhealing.mixin;

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
import xd.arkosammy.creeperhealing.config.ConfigSettings;
import xd.arkosammy.creeperhealing.config.ConfigUtils;
import xd.arkosammy.creeperhealing.util.ExplosionManager;
import xd.arkosammy.creeperhealing.util.ExplosionUtils;
import xd.arkosammy.creeperhealing.explosions.ducks.ExplosionAccessor;
import xd.arkosammy.monkeyconfig.settings.BooleanSetting;

import java.util.List;

@Mixin(Explosion.class)
public abstract class ExplosionMixin implements ExplosionAccessor {

    @Shadow @Final private World world;

    @Shadow @Final private DamageSource damageSource;

    @Shadow public abstract @Nullable LivingEntity getCausingEntity();

    @Shadow public abstract @Nullable Entity getEntity();

    @Shadow public abstract List<BlockPos> getAffectedBlocks();

    @Unique
    private boolean willBeHealed = false;

    @Override
    public World creeper_healing$getWorld(){
        return this.world;
    }

    @Override
    public DamageSource creeper_healing$getDamageSource() {
        return this.damageSource;
    }

    @Override
    public boolean creeper_healing$shouldHeal() {
        LivingEntity causingLivingEntity = this.getCausingEntity();
        Entity causingEntity = this.getEntity();
        DamageSource damageSource = ((ExplosionAccessor)this).creeper_healing$getDamageSource();
        if(this.getAffectedBlocks().isEmpty()){
            return false;
        }

        boolean shouldHealExplosion = false;
        if (causingLivingEntity instanceof CreeperEntity && ConfigUtils.getSettingValue(ConfigSettings.HEAL_CREEPER_EXPLOSIONS.getSettingLocation(), BooleanSetting.class)){
            shouldHealExplosion = true;
        } else if (causingLivingEntity instanceof GhastEntity && ConfigUtils.getSettingValue(ConfigSettings.HEAL_GHAST_EXPLOSIONS.getSettingLocation(), BooleanSetting.class)){
            shouldHealExplosion = true;
        } else if (causingLivingEntity instanceof WitherEntity && ConfigUtils.getSettingValue(ConfigSettings.HEAL_WITHER_EXPLOSIONS.getSettingLocation(), BooleanSetting.class)){
            shouldHealExplosion = true;
        } else if (causingEntity instanceof TntEntity && ConfigUtils.getSettingValue(ConfigSettings.HEAL_TNT_EXPLOSIONS.getSettingLocation(), BooleanSetting.class)){
            shouldHealExplosion = true;
        } else if (causingEntity instanceof TntMinecartEntity && ConfigUtils.getSettingValue(ConfigSettings.HEAL_TNT_MINECART_EXPLOSIONS.getSettingLocation(), BooleanSetting.class)){
            shouldHealExplosion = true;
        } else if (damageSource.isOf(DamageTypes.BAD_RESPAWN_POINT) && ConfigUtils.getSettingValue(ConfigSettings.HEAL_BED_AND_RESPAWN_ANCHOR_EXPLOSIONS.getSettingLocation(), BooleanSetting.class)){
            shouldHealExplosion = true;
        } else if (causingEntity instanceof EndCrystalEntity && ConfigUtils.getSettingValue(ConfigSettings.HEAL_END_CRYSTAL_EXPLOSIONS.getSettingLocation(), BooleanSetting.class)){
            shouldHealExplosion = true;
        }
        this.willBeHealed = shouldHealExplosion;
        return shouldHealExplosion;
    }

    @Override
    public boolean creeper_healing$willBeHealed(){
        return this.willBeHealed;
    }


    @Inject(method = "collectBlocksAndDamageEntities", at = @At("RETURN"))
    private void storeCurrentExplosionIfNeeded(CallbackInfo ci){
        ExplosionManager.getInstance().processExplosion((Explosion) (Object) this);
    }

    // Make sure the thread local is reset when entering and after exiting "affectWorld"
    @Inject(method = "affectWorld", at = @At(value = "HEAD"))
    private void setDropItemsThreadLocal(boolean particles, CallbackInfo ci){
        ExplosionUtils.DROP_EXPLOSION_ITEMS.set(true);
        ExplosionUtils.DROP_BLOCK_INVENTORY_ITEMS.set(true);
    }

    @Inject(method = "affectWorld", at = @At(value = "RETURN"))
    private void clearDropItemsThreadLocal(boolean particles, CallbackInfo ci){
        ExplosionUtils.DROP_EXPLOSION_ITEMS.set(true);
        ExplosionUtils.DROP_BLOCK_INVENTORY_ITEMS.set(true);
    }
}

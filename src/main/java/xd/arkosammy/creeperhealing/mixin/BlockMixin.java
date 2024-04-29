package xd.arkosammy.creeperhealing.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xd.arkosammy.creeperhealing.config.ConfigManager;
import xd.arkosammy.creeperhealing.config.settings.ConfigSettings;
import xd.arkosammy.creeperhealing.util.ExcludedBlocks;
import xd.arkosammy.creeperhealing.util.ExplosionUtils;
import xd.arkosammy.creeperhealing.explosions.ducks.ExplosionAccessor;

@Mixin(Block.class)
public abstract class BlockMixin {

    @SuppressWarnings("UnreachableCode")
    @ModifyReturnValue(method = "shouldDropItemsOnExplosion", at=@At("RETURN"))
    private boolean shouldExplosionDropItems(boolean original, @Local Explosion explosion){
        Entity causingEntity = explosion.getEntity();
        Entity causingLivingEntity = explosion.getCausingEntity();
        DamageSource damageSource = ((ExplosionAccessor)explosion).creeper_healing$getDamageSource();

        if(ExcludedBlocks.isExcluded((Block)(Object)this)) {
           return original;
        }

        boolean shouldDropItems = false;
        if (causingLivingEntity instanceof CreeperEntity && ConfigManager.getInstance().getAsBooleanSetting(ConfigSettings.DROP_ITEMS_ON_CREEPER_EXPLOSIONS.getId()).getValue()){
            shouldDropItems = true;
        } else if (causingLivingEntity instanceof GhastEntity && ConfigManager.getInstance().getAsBooleanSetting(ConfigSettings.DROP_ITEMS_ON_GHAST_EXPLOSIONS.getId()).getValue()){
            shouldDropItems = true;
        } else if (causingLivingEntity instanceof WitherEntity && ConfigManager.getInstance().getAsBooleanSetting(ConfigSettings.DROP_ITEMS_ON_WITHER_EXPLOSIONS.getId()).getValue()){
            shouldDropItems = true;
        } else if (causingEntity instanceof TntEntity && ConfigManager.getInstance().getAsBooleanSetting(ConfigSettings.DROP_ITEMS_ON_TNT_EXPLOSIONS.getId()).getValue()){
            shouldDropItems = true;
        } else if (causingEntity instanceof TntMinecartEntity && ConfigManager.getInstance().getAsBooleanSetting(ConfigSettings.DROP_ITEMS_ON_TNT_MINECART_EXPLOSIONS.getId()).getValue()){
            shouldDropItems = true;
        } else if (damageSource.isOf(DamageTypes.BAD_RESPAWN_POINT) && ConfigManager.getInstance().getAsBooleanSetting(ConfigSettings.DROP_ITEMS_ON_BED_AND_RESPAWN_ANCHOR_EXPLOSIONS.getId()).getValue()){
            shouldDropItems = true;
        } else if (causingEntity instanceof EndCrystalEntity && ConfigManager.getInstance().getAsBooleanSetting(ConfigSettings.DROP_ITEMS_ON_END_CRYSTAL_EXPLOSIONS.getId()).getValue()){
            shouldDropItems = true;
        }

        ExplosionUtils.DROP_EXPLOSION_ITEMS.set(shouldDropItems);
        return shouldDropItems && original;
    }

}

package xd.arkosammy.mixin;

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
import xd.arkosammy.configuration.tables.ExplosionItemDropConfig;

@Mixin(Block.class)
public abstract class ExplosionItemDropMixin {

    @ModifyReturnValue(method = "shouldDropItemsOnExplosion", at=@At("RETURN"))
    private boolean shouldDropItems(boolean dropItems, @Local Explosion explosion){
        Entity causingEntity = explosion.getEntity();
        Entity causingLivingEntity = explosion.getCausingEntity();
        DamageSource damageSource = explosion.getDamageSource();
        if(causingLivingEntity instanceof CreeperEntity && !ExplosionItemDropConfig.getDropItemsOnCreeperExplosions()){
            return false;
        } else if (causingLivingEntity instanceof GhastEntity && !ExplosionItemDropConfig.getDropItemsOnGhastExplosions()){
            return false;
        } else if (causingLivingEntity instanceof WitherEntity && !ExplosionItemDropConfig.getDropItemsOnWitherExplosions()) {
            return false;
        } else if (causingEntity instanceof TntEntity && !ExplosionItemDropConfig.getDropItemsOnTNTExplosions()){
            return false;
        } else if (causingEntity instanceof TntMinecartEntity && !ExplosionItemDropConfig.getDropItemsOnTNTMinecartExplosions()){
            return false;
        } else if (damageSource.isOf(DamageTypes.BAD_RESPAWN_POINT) && !ExplosionItemDropConfig.getDropItemsOnBedAndRespawnAnchorExplosions()){
            return false;
        } else if (causingEntity instanceof EndCrystalEntity && !ExplosionItemDropConfig.getDropItemsOnEndCrystalExplosions()){
            return false;
        }
        return dropItems;
    }

}

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
import xd.arkosammy.CreeperHealing;
import xd.arkosammy.configuration.tables.ExplosionItemDropConfig;

@Mixin(Block.class)
public abstract class ExplosionItemDropMixin {

    @ModifyReturnValue(method = "shouldDropItemsOnExplosion", at=@At("RETURN"))
    private boolean shouldExplosionDropItems(boolean dropItems, @Local Explosion explosion){
        Entity causingEntity = explosion.getEntity();
        Entity causingLivingEntity = explosion.getCausingEntity();
        DamageSource damageSource = explosion.getDamageSource();
        boolean shouldNotDropItems = (causingLivingEntity instanceof CreeperEntity && !ExplosionItemDropConfig.getDropItemsOnCreeperExplosions())
                || (causingLivingEntity instanceof GhastEntity && !ExplosionItemDropConfig.getDropItemsOnGhastExplosions())
                || (causingLivingEntity instanceof WitherEntity && !ExplosionItemDropConfig.getDropItemsOnWitherExplosions())
                || (causingEntity instanceof TntEntity && !ExplosionItemDropConfig.getDropItemsOnTNTExplosions())
                || (causingEntity instanceof TntMinecartEntity && !ExplosionItemDropConfig.getDropItemsOnTNTMinecartExplosions())
                || (damageSource.isOf(DamageTypes.BAD_RESPAWN_POINT) && !ExplosionItemDropConfig.getDropItemsOnBedAndRespawnAnchorExplosions())
                || (causingEntity instanceof EndCrystalEntity && !ExplosionItemDropConfig.getDropItemsOnEndCrystalExplosions());
        if (shouldNotDropItems) {
            CreeperHealing.SHOULD_NOT_DROP_ITEMS.set(true);
        }
        return !shouldNotDropItems && dropItems;
    }

}

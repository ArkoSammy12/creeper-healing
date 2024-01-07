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
import xd.arkosammy.creeperhealing.configuration.ExplosionItemDropConfig;
import xd.arkosammy.creeperhealing.explosions.ExplosionUtils;
import xd.arkosammy.creeperhealing.explosions.ducks.IExplosionDamageSourceAccessor;

@Mixin(Block.class)
public abstract class ExplosionItemDropMixin {

    @ModifyReturnValue(method = "shouldDropItemsOnExplosion", at=@At("RETURN"))
    private boolean shouldExplosionDropItems(boolean dropItems, @Local Explosion explosion){
        Entity causingEntity = explosion.getEntity();
        Entity causingLivingEntity = explosion.getCausingEntity();
        DamageSource damageSource = ((IExplosionDamageSourceAccessor)explosion).creeper_healing$getDamageSource();
        boolean shouldNotDropItems = (causingLivingEntity instanceof CreeperEntity && !ExplosionItemDropConfig.DROP_ITEMS_ON_CREEPER_EXPLOSIONS.getEntry().getValue())
                || (causingLivingEntity instanceof GhastEntity && !ExplosionItemDropConfig.DROP_ITEMS_ON_GHAST_EXPLOSIONS.getEntry().getValue())
                || (causingLivingEntity instanceof WitherEntity && !ExplosionItemDropConfig.DROP_ITEMS_ON_WITHER_EXPLOSIONS.getEntry().getValue())
                || (causingEntity instanceof TntEntity && !ExplosionItemDropConfig.DROP_ITEMS_ON_TNT_EXPLOSIONS.getEntry().getValue())
                || (causingEntity instanceof TntMinecartEntity && !ExplosionItemDropConfig.DROP_ITEMS_ON_TNT_MINECART_EXPLOSIONS.getEntry().getValue())
                || (damageSource.isOf(DamageTypes.BAD_RESPAWN_POINT) && !ExplosionItemDropConfig.DROP_ITEMS_ON_BED_AND_RESPAWN_ANCHOR_EXPLOSIONS.getEntry().getValue())
                || (causingEntity instanceof EndCrystalEntity && !ExplosionItemDropConfig.DROP_ITEMS_ON_END_CRYSTAL_EXPLOSIONS.getEntry().getValue());
        if (shouldNotDropItems) {
            ExplosionUtils.SHOULD_NOT_DROP_ITEMS.set(true);
        }
        return !shouldNotDropItems && dropItems;
    }

}

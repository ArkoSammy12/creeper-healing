package xd.arkosammy.creeperhealing.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
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
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import xd.arkosammy.creeperhealing.config.ConfigSettings;
import xd.arkosammy.creeperhealing.config.ConfigUtils;
import xd.arkosammy.creeperhealing.util.ExcludedBlocks;
import xd.arkosammy.creeperhealing.util.ExplosionUtils;
import xd.arkosammy.creeperhealing.explosions.ducks.ExplosionAccessor;
import xd.arkosammy.monkeyconfig.settings.BooleanSetting;

@Mixin(Block.class)
public abstract class BlockMixin {

    @Shadow public abstract BlockState getDefaultState();

    @SuppressWarnings("UnreachableCode")
    @ModifyReturnValue(method = "shouldDropItemsOnExplosion", at = @At("RETURN"))
    private boolean shouldExplosionDropItems(boolean original, @Local Explosion explosion){

        // Hardcoded exception. Place before all other logic
        if(ExcludedBlocks.isExcluded((Block)(Object)this)) {
            ExplosionUtils.DROP_EXPLOSION_ITEMS.set(original);
            ExplosionUtils.DROP_BLOCK_INVENTORY_ITEMS.set(original);
            return original;
        }

        // Allow the explosion to drop items normally if it cannot be healed
        if(!((ExplosionAccessor)explosion).creeper_healing$willBeHealed()) {
            ExplosionUtils.DROP_EXPLOSION_ITEMS.set(original);
            ExplosionUtils.DROP_BLOCK_INVENTORY_ITEMS.set(original);
            return original;
        }

        Entity causingEntity = explosion.getEntity();
        Entity causingLivingEntity = explosion.getCausingEntity();
        DamageSource damageSource = ((ExplosionAccessor)explosion).creeper_healing$getDamageSource();

        boolean shouldDropItems = false;
        if (causingLivingEntity instanceof CreeperEntity && ConfigUtils.getSettingValue(ConfigSettings.DROP_ITEMS_ON_END_CRYSTAL_EXPLOSIONS.getSettingLocation(), BooleanSetting.class)){
            shouldDropItems = true;
        } else if (causingLivingEntity instanceof GhastEntity && ConfigUtils.getSettingValue(ConfigSettings.DROP_ITEMS_ON_GHAST_EXPLOSIONS.getSettingLocation(), BooleanSetting.class)){
            shouldDropItems = true;
        } else if (causingLivingEntity instanceof WitherEntity && ConfigUtils.getSettingValue(ConfigSettings.DROP_ITEMS_ON_WITHER_EXPLOSIONS.getSettingLocation(), BooleanSetting.class)){
            shouldDropItems = true;
        } else if (causingEntity instanceof TntEntity && ConfigUtils.getSettingValue(ConfigSettings.DROP_ITEMS_ON_TNT_EXPLOSIONS.getSettingLocation(), BooleanSetting.class)){
            shouldDropItems = true;
        } else if (causingEntity instanceof TntMinecartEntity && ConfigUtils.getSettingValue(ConfigSettings.DROP_ITEMS_ON_TNT_MINECART_EXPLOSIONS.getSettingLocation(), BooleanSetting.class)){
            shouldDropItems = true;
        } else if (damageSource.isOf(DamageTypes.BAD_RESPAWN_POINT) && ConfigUtils.getSettingValue(ConfigSettings.DROP_ITEMS_ON_BED_AND_RESPAWN_ANCHOR_EXPLOSIONS.getSettingLocation(), BooleanSetting.class)){
            shouldDropItems = true;
        } else if (causingEntity instanceof EndCrystalEntity && ConfigUtils.getSettingValue(ConfigSettings.DROP_ITEMS_ON_END_CRYSTAL_EXPLOSIONS.getSettingLocation(), BooleanSetting.class)){
            shouldDropItems = true;
        }

        // Do not drop the inventories of blocks if the inventory will be restored later
        if(ConfigUtils.getSettingValue(ConfigSettings.RESTORE_BLOCK_NBT.getSettingLocation(), BooleanSetting.class)) {
            ExplosionUtils.DROP_BLOCK_INVENTORY_ITEMS.set(false);
        }

        // Do not drop the item of the block itself if it is a container and its inventory is to be restored
        if(this.getDefaultState().hasBlockEntity() && !ExplosionUtils.DROP_BLOCK_INVENTORY_ITEMS.get()) {
            shouldDropItems = false;
        }

        ExplosionUtils.DROP_EXPLOSION_ITEMS.set(shouldDropItems);
        return shouldDropItems && original;
    }

}

package xd.arkosammy.creeperhealing.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.Registries;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import xd.arkosammy.creeperhealing.config.ConfigSettings;
import xd.arkosammy.creeperhealing.config.ConfigUtils;
import xd.arkosammy.creeperhealing.explosions.ducks.ExplosionImplDuck;
import xd.arkosammy.creeperhealing.util.ExcludedBlocks;
import xd.arkosammy.creeperhealing.util.ExplosionUtils;
import xd.arkosammy.monkeyconfig.settings.BooleanSetting;
import xd.arkosammy.monkeyconfig.settings.list.StringListSetting;

import java.util.List;

@Mixin(Block.class)
public abstract class BlockMixin {

    @Shadow
    public abstract BlockState getDefaultState();

    @SuppressWarnings("UnreachableCode")
    @ModifyReturnValue(method = "shouldDropItemsOnExplosion", at = @At("RETURN"))
    private boolean shouldExplosionDropItems(boolean original, Explosion explosion) {

        // Hardcoded exception. Place before all other logic
        if (ExcludedBlocks.isExcluded((Block) (Object) this)) {
            ExplosionUtils.DROP_BLOCK_ITEMS.set(original);
            ExplosionUtils.DROP_CONTAINER_INVENTORY_ITEMS.set(original);
            return original;
        }

        // Allow the explosion to drop items normally if it cannot be healed
        if (!((ExplosionImplDuck) explosion).creeperhealing$shouldHeal()) {
            ExplosionUtils.DROP_BLOCK_ITEMS.set(original);
            ExplosionUtils.DROP_CONTAINER_INVENTORY_ITEMS.set(original);
            return original;
        }

        World.ExplosionSourceType explosionSourceType = ((ExplosionImplDuck) explosion).creeperhealing$getExplosionSourceType();
        boolean shouldDropItems = switch (explosionSourceType) {
            case MOB -> {
                if (!ConfigUtils.getSettingValue(ConfigSettings.DROP_ITEMS_ON_MOB_EXPLOSIONS.getSettingLocation(), BooleanSetting.class)) {
                    yield false;
                }
                LivingEntity causingEntity = explosion.getCausingEntity();
                if (causingEntity == null) {
                    yield true;
                }
                String entityId = Registries.ENTITY_TYPE.getId(causingEntity.getType()).toString();
                List<? extends String> dropItemsOnMobExplosionsBlacklist = ConfigUtils.getSettingValue(ConfigSettings.DROP_ITEMS_ON_MOB_EXPLOSIONS_BLACKLIST.getSettingLocation(), StringListSetting.class);
                yield !dropItemsOnMobExplosionsBlacklist.contains(entityId);
            }
            case BLOCK ->
                    ConfigUtils.getSettingValue(ConfigSettings.DROP_ITEMS_ON_BLOCK_EXPLOSIONS.getSettingLocation(), BooleanSetting.class);
            case TNT ->
                    ConfigUtils.getSettingValue(ConfigSettings.DROP_ITEMS_ON_TNT_EXPLOSIONS.getSettingLocation(), BooleanSetting.class);
            case TRIGGER ->
                    ConfigUtils.getSettingValue(ConfigSettings.DROP_ITEMS_ON_TRIGGERED_EXPLOSIONS.getSettingLocation(), BooleanSetting.class);
            case null, default ->
                    ConfigUtils.getSettingValue(ConfigSettings.DROP_ITEMS_ON_OTHER_EXPLOSIONS.getSettingLocation(), BooleanSetting.class);
        };

        // Do not drop the inventories of blocks if the inventory will be restored later
        if (ConfigUtils.getSettingValue(ConfigSettings.RESTORE_BLOCK_NBT.getSettingLocation(), BooleanSetting.class)) {
            ExplosionUtils.DROP_CONTAINER_INVENTORY_ITEMS.set(false);
        }

        // Do not drop the item of the block itself if it is a container and its inventory is to be restored
        if (this.getDefaultState().hasBlockEntity() && !ExplosionUtils.DROP_CONTAINER_INVENTORY_ITEMS.get()) {
            shouldDropItems = false;
        }

        ExplosionUtils.DROP_BLOCK_ITEMS.set(shouldDropItems);
        return shouldDropItems && original;
    }

}

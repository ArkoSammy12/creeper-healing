package io.github.arkosammy12.creeperhealing.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.arkosammy12.creeperhealing.explosions.ducks.ServerWorldDuck;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import io.github.arkosammy12.creeperhealing.config.ConfigSettings;
import io.github.arkosammy12.creeperhealing.config.ConfigUtils;
import io.github.arkosammy12.creeperhealing.explosions.ducks.ExplosionImplDuck;
import io.github.arkosammy12.creeperhealing.util.ExcludedBlocks;
import io.github.arkosammy12.creeperhealing.util.ExplosionUtils;
import xd.arkosammy.monkeyconfig.settings.BooleanSetting;
import xd.arkosammy.monkeyconfig.settings.list.StringListSetting;

import java.util.Collections;
import java.util.List;

@Mixin(Block.class)
public abstract class BlockMixin {

    @Shadow
    public abstract BlockState getDefaultState();

    @WrapMethod(method = "getDroppedStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;)Ljava/util/List;")
    private static List<ItemStack> modifyDroppedStacks(BlockState state, ServerWorld world, BlockPos pos, @Nullable BlockEntity blockEntity, Operation<List<ItemStack>> original) {
        return shouldDropStacks(state, world, pos) ? original.call(state, world, pos, blockEntity) : Collections.emptyList();
    }

    @WrapMethod(method = "getDroppedStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;)Ljava/util/List;")
    private static List<ItemStack> modifyDroppedStacks(BlockState state, ServerWorld world, BlockPos pos, @Nullable BlockEntity blockEntity, @Nullable Entity entity, ItemStack stack, Operation<List<ItemStack>> original) {
        return shouldDropStacks(state, world, pos) ? original.call(state, world, pos, blockEntity, entity, stack) : Collections.emptyList();
    }

    @Unique
    private static boolean shouldDropStacks(BlockState state, ServerWorld world, BlockPos pos) {
        if (ExcludedBlocks.isExcluded(state)) {
            return true;
        }
        return !(((ServerWorldDuck) world).creeperhealing$isAffectedPosition(pos) && !ExplosionUtils.DROP_BLOCK_ITEMS.get());
    }

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

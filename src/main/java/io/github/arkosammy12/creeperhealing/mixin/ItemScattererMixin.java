package io.github.arkosammy12.creeperhealing.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ItemScatterer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import io.github.arkosammy12.creeperhealing.util.ExcludedBlocks;
import io.github.arkosammy12.creeperhealing.util.ExplosionUtils;

@Mixin(ItemScatterer.class)
public abstract class ItemScattererMixin {

    @WrapOperation(method = "spawn(Lnet/minecraft/world/World;DDDLnet/minecraft/inventory/Inventory;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ItemScatterer;spawn(Lnet/minecraft/world/World;DDDLnet/minecraft/item/ItemStack;)V"))
    private static void cancelItemScatteringFromInventoryBlocks(World world, double x, double y, double z, ItemStack stack, Operation<Void> original, @Local(argsOnly = true) Inventory inventory) {
        // Hardcoded exception. Place before all other logic
        if (inventory instanceof BlockEntity blockEntity && ExcludedBlocks.isExcluded(blockEntity.getCachedState().getBlock())) {
            original.call(world, x, y, z, stack);
            return;
        }
        if (ExplosionUtils.DROP_CONTAINER_INVENTORY_ITEMS.get()) {
            original.call(world, x, y, z, stack);
        }
    }

}

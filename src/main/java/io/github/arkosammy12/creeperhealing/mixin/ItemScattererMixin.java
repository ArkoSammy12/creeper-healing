package io.github.arkosammy12.creeperhealing.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.Containers;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import io.github.arkosammy12.creeperhealing.util.ExcludedBlocks;
import io.github.arkosammy12.creeperhealing.util.ExplosionUtils;

@Mixin(Containers.class)
public abstract class ItemScattererMixin {

    @WrapOperation(method = "dropContents(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/Container;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Containers;dropItemStack(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/item/ItemStack;)V"))
    private static void cancelItemScatteringFromInventoryBlocks(Level world, double x, double y, double z, ItemStack stack, Operation<Void> original, @Local(argsOnly = true) Container inventory) {
        // Hardcoded exception. Place before all other logic
        if (inventory instanceof BlockEntity blockEntity && ExcludedBlocks.isExcluded(blockEntity.getBlockState().getBlock())) {
            original.call(world, x, y, z, stack);
            return;
        }
        if (ExplosionUtils.DROP_CONTAINER_INVENTORY_ITEMS.get()) {
            original.call(world, x, y, z, stack);
        }
    }

}

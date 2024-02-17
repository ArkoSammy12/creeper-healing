package xd.arkosammy.creeperhealing.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ItemScatterer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xd.arkosammy.creeperhealing.util.ExplosionUtils;

@Mixin(ItemScatterer.class)
public class ItemScattererMixin {

    @WrapOperation(method = "spawn(Lnet/minecraft/world/World;DDDLnet/minecraft/inventory/Inventory;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ItemScatterer;spawn(Lnet/minecraft/world/World;DDDLnet/minecraft/item/ItemStack;)V"))
    private static void cancelItemScatteringFromInventoryBlocks(World world, double x, double y, double z, ItemStack stack, Operation<Void> original){
        if(ExplosionUtils.DROP_BLOCK_INVENTORY_ITEMS.get() != null && ExplosionUtils.DROP_BLOCK_INVENTORY_ITEMS.get()){
            original.call(world, x, y, z, stack);
        }
    }

}

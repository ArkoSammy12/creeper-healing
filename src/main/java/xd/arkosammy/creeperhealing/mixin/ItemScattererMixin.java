package xd.arkosammy.creeperhealing.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.inventory.Inventory;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xd.arkosammy.creeperhealing.util.ExplosionUtils;

@Mixin(ItemScatterer.class)
public class ItemScattererMixin {

    @WrapOperation(method = "onStateReplaced", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ItemScatterer;spawn(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/inventory/Inventory;)V"))
    private static void cancelItemScatteringFromInventoryBlocks(World world, BlockPos pos, Inventory inventory, Operation<Void> original){
        if(ExplosionUtils.DROP_BLOCK_INVENTORY_ITEMS.get() != null && ExplosionUtils.DROP_BLOCK_INVENTORY_ITEMS.get()){
            original.call(world, pos, inventory);
        }
    }

}

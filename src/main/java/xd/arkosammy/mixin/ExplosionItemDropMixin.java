package xd.arkosammy.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xd.arkosammy.configuration.tables.PreferencesConfig;

@Mixin(Block.class)
public abstract class ExplosionItemDropMixin {

    @ModifyReturnValue(method = "shouldDropItemsOnExplosion", at=@At("RETURN"))
    private boolean shouldDropItems(boolean dropItems){
        if(!PreferencesConfig.getDropItemsOnExplosions()){
            return false;
        }
        return dropItems;
    }

}

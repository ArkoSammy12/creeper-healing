package xd.arkosammy.mixin;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import xd.arkosammy.explosions.AffectedBlock;
import xd.arkosammy.handlers.ExplosionListHandler;

import java.util.List;

@Mixin(PotionEntity.class)
public abstract class HealingPotionMixin {

    @Inject(method = "onCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/thrown/PotionEntity;applySplashPotion(Ljava/util/List;Lnet/minecraft/entity/Entity;)V"), locals =  LocalCapture.CAPTURE_FAILSOFT)
    private void onSplashPotionHit(HitResult hitResult, CallbackInfo ci, ItemStack itemStack, Potion potion) {

        List<StatusEffect> statusEffects = potion.getEffects().stream().map(StatusEffectInstance::getEffectType).toList();

        if(statusEffects.contains(StatusEffects.INSTANT_HEALTH)) {

            BlockPos potionHitPosition;
            if (hitResult instanceof BlockHitResult blockHitResult) {
                potionHitPosition = blockHitResult.getBlockPos().offset(blockHitResult.getSide());
            } else {
                potionHitPosition = new BlockPos((int) Math.round(hitResult.getPos().getX()), (int) Math.round(hitResult.getPos().getY()), (int) Math.round(hitResult.getPos().getZ()));
            }

            ExplosionListHandler.getExplosionEventList().forEach(explosionEvent -> {

                List<BlockPos> affectedBlockPositions = explosionEvent.getAffectedBlocksList().stream().map(AffectedBlock::getPos).toList();

                if(affectedBlockPositions.contains(potionHitPosition)){

                    explosionEvent.setExplosionTimer(-1);

                }

            });

        }

    }



}

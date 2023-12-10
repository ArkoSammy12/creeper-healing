package xd.arkosammy.creeperhealing.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.potion.Potion;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xd.arkosammy.creeperhealing.configuration.PreferencesConfig;
import xd.arkosammy.creeperhealing.explosions.AffectedBlock;
import xd.arkosammy.creeperhealing.explosions.ExplosionHealingMode;
import xd.arkosammy.creeperhealing.handlers.ExplosionListHandler;

import java.util.List;

@Mixin(PotionEntity.class)
public abstract class HealingPotionMixin {

    @Inject(method = "onCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/thrown/PotionEntity;applySplashPotion(Ljava/util/List;Lnet/minecraft/entity/Entity;)V"))
    private void affectExplosionOnSplashPotionHit(HitResult hitResult, CallbackInfo ci, @Local Potion potion) {
        List<StatusEffect> statusEffects = potion.getEffects().stream().map(StatusEffectInstance::getEffectType).toList();
        BlockPos potionHitPosition;
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult) hitResult;
            potionHitPosition = blockHitResult.getBlockPos().offset(blockHitResult.getSide());
        } else if(hitResult.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHitResult = (EntityHitResult) hitResult;
            potionHitPosition = entityHitResult.getEntity().getBlockPos();
        } else {
            return;
        }
        if(statusEffects.contains(StatusEffects.INSTANT_HEALTH) && PreferencesConfig.HEAL_ON_HEALING_POTION_SPLASH.getEntry().getValue()) {
            ExplosionListHandler.getExplosionEventList().forEach(explosionEvent -> {
                List<BlockPos> affectedBlockPositions = explosionEvent.getAffectedBlocksList().stream().map(AffectedBlock::getPos).toList();
                if(affectedBlockPositions.contains(potionHitPosition)){
                    explosionEvent.setExplosionTimer(-1);
                    explosionEvent.getAffectedBlocksList().forEach(affectedBlock -> affectedBlock.setAffectedBlockTimer(1));
                }
            });
        } else if (statusEffects.contains(StatusEffects.REGENERATION) && PreferencesConfig.HEAL_ON_REGENERATION_POTION_SPLASH.getEntry().getValue()){
            ExplosionListHandler.getExplosionEventList().forEach(explosionEvent -> {
                List<BlockPos> affectedBlockPositions = explosionEvent.getAffectedBlocksList().stream().map(AffectedBlock::getPos).toList();
                if(affectedBlockPositions.contains(potionHitPosition) && explosionEvent.getExplosionMode() == ExplosionHealingMode.DEFAULT_MODE){
                    explosionEvent.setExplosionTimer(-1);
                }
            });

        }
    }
}

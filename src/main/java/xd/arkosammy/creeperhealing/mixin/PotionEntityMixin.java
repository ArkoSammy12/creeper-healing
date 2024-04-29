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
import xd.arkosammy.creeperhealing.config.settings.ConfigSettings;
import xd.arkosammy.creeperhealing.explosions.AbstractExplosionEvent;
import xd.arkosammy.creeperhealing.config.ConfigManager;
import xd.arkosammy.creeperhealing.util.ExplosionManager;

import java.util.List;

@Mixin(PotionEntity.class)
public abstract class PotionEntityMixin {

    // Make explosions start healing when you throw a potion of healing or regeneration on them
    @Inject(method = "onCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/thrown/PotionEntity;applySplashPotion(Ljava/util/List;Lnet/minecraft/entity/Entity;)V"))
    private void affectExplosionOnSplashPotionHit(HitResult hitResult, CallbackInfo ci, @Local Potion potion) {
        List<StatusEffect> statusEffects = potion.getEffects().stream().map(StatusEffectInstance::getEffectType).toList();
        BlockPos potionHitPosition = switch (hitResult.getType()){
            case BLOCK -> ((BlockHitResult)hitResult).getBlockPos().offset(((BlockHitResult)hitResult).getSide());
            case ENTITY -> ((EntityHitResult)hitResult).getEntity().getBlockPos();
            case MISS -> null;
        };
        if(potionHitPosition == null){
            return;
        }
        if (statusEffects.contains(StatusEffects.INSTANT_HEALTH) && ConfigManager.getInstance().getAsBooleanSetting(ConfigSettings.HEAL_ON_HEALING_POTION_SPLASH.getId()).getValue()){
            for(AbstractExplosionEvent explosionEvent : ExplosionManager.getInstance().getExplosionEvents()){
                boolean potionHitExplosion = explosionEvent.getAffectedBlocks().stream().anyMatch(affectedBlock -> affectedBlock.getPos().equals(potionHitPosition));
                if(potionHitExplosion){
                    explosionEvent.setHealTimer(1);
                    explosionEvent.getAffectedBlocks().forEach(affectedBlock -> affectedBlock.setTimer(1));
                }
            }
        } else if (statusEffects.contains(StatusEffects.REGENERATION) && ConfigManager.getInstance().getAsBooleanSetting(ConfigSettings.HEAL_ON_REGENERATION_POTION_SPLASH.getId()).getValue()){
            for(AbstractExplosionEvent explosionEvent : ExplosionManager.getInstance().getExplosionEvents()){
                boolean potionHitExplosion = explosionEvent.getAffectedBlocks().stream().anyMatch(affectedBlock -> affectedBlock.getPos().equals(potionHitPosition));
                if(potionHitExplosion){
                    explosionEvent.setHealTimer(1);
                }
            }
        }
    }
}

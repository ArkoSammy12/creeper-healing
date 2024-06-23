package xd.arkosammy.creeperhealing.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xd.arkosammy.creeperhealing.config.ConfigSettings;
import xd.arkosammy.creeperhealing.config.ConfigUtils;
import xd.arkosammy.creeperhealing.explosions.AbstractExplosionEvent;
import xd.arkosammy.creeperhealing.util.ExplosionManager;
import xd.arkosammy.monkeyconfig.settings.BooleanSetting;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mixin(PotionEntity.class)
public abstract class PotionEntityMixin {

    // Make explosions start healing when you throw a potion of healing or regeneration on them
    @Inject(method = "onCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/thrown/PotionEntity;applySplashPotion(Ljava/lang/Iterable;Lnet/minecraft/entity/Entity;)V"))
    private void affectExplosionOnSplashPotionHit(HitResult hitResult, CallbackInfo ci, @Local PotionContentsComponent potionContentsComponent) {

        Iterable<StatusEffectInstance> statusEffectsIterable = potionContentsComponent.getEffects();
        List<StatusEffectInstance> statusEffects = new ArrayList<>();

        for(StatusEffectInstance statusEffectInstance : statusEffectsIterable){
            statusEffects.add(statusEffectInstance);
        }

        BlockPos potionHitPosition = switch (hitResult.getType()){
            case BLOCK -> ((BlockHitResult)hitResult).getBlockPos().offset(((BlockHitResult)hitResult).getSide());
            case ENTITY -> ((EntityHitResult)hitResult).getEntity().getBlockPos();
            case MISS -> null;
        };
        if(potionHitPosition == null){
            return;
        }

        Optional<RegistryKey<StatusEffect>> instantHealthOptionalKey = StatusEffects.INSTANT_HEALTH.getKey();
        if(instantHealthOptionalKey.isEmpty()) {
            return;
        }
        Optional<RegistryEntry.Reference<StatusEffect>> instantHealthOptionalEntry = Registries.STATUS_EFFECT.getEntry(instantHealthOptionalKey.get());
        if(instantHealthOptionalEntry.isEmpty()) {
            return;
        }
        boolean hasInstantHealth = statusEffects.stream().anyMatch(statusEffect -> statusEffect.equals(instantHealthOptionalEntry.get()));

        Optional<RegistryKey<StatusEffect>> regenerationOptionalKey = StatusEffects.REGENERATION.getKey();
        if(regenerationOptionalKey.isEmpty()) {
            return;
        }
        Optional<RegistryEntry.Reference<StatusEffect>> regenerationOptionalEntry = Registries.STATUS_EFFECT.getEntry(regenerationOptionalKey.get());
        if(regenerationOptionalEntry.isEmpty()) {
            return;
        }
        boolean hasRegeneration = statusEffects.stream().anyMatch(statusEffect -> statusEffect.equals(regenerationOptionalEntry.get()));

        boolean healOnHealingPotion = ConfigUtils.getSettingValue(ConfigSettings.HEAL_ON_HEALING_POTION_SPLASH.getSettingLocation(), BooleanSetting.class);
        boolean healOnRegenerationPotion = ConfigUtils.getSettingValue(ConfigSettings.HEAL_ON_REGENERATION_POTION_SPLASH.getSettingLocation(), BooleanSetting.class);
        if (hasInstantHealth && healOnHealingPotion){
            for(AbstractExplosionEvent explosionEvent : ExplosionManager.getInstance().getExplosionEvents()){
                boolean potionHitExplosion = explosionEvent.getAffectedBlocks().stream().anyMatch(affectedBlock -> affectedBlock.getPos().equals(potionHitPosition));
                if(potionHitExplosion){
                    explosionEvent.setHealTimer(1);
                    explosionEvent.getAffectedBlocks().forEach(affectedBlock -> affectedBlock.setTimer(1));
                }
            }
        } else if (hasRegeneration && healOnRegenerationPotion){
            for(AbstractExplosionEvent explosionEvent : ExplosionManager.getInstance().getExplosionEvents()){
                boolean potionHitExplosion = explosionEvent.getAffectedBlocks().stream().anyMatch(affectedBlock -> affectedBlock.getPos().equals(potionHitPosition));
                if(potionHitExplosion){
                    explosionEvent.setHealTimer(1);
                }
            }
        }
    }
}

package xd.arkosammy.creeperhealing.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.SharedConstants;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xd.arkosammy.creeperhealing.CreeperHealing;
import xd.arkosammy.creeperhealing.ExplosionManagerRegistrar;
import xd.arkosammy.creeperhealing.blocks.SingleAffectedBlock;
import xd.arkosammy.creeperhealing.config.ConfigSettings;
import xd.arkosammy.creeperhealing.config.ConfigUtils;
import xd.arkosammy.creeperhealing.explosions.AbstractExplosionEvent;
import xd.arkosammy.creeperhealing.explosions.DaytimeExplosionEvent;
import xd.arkosammy.creeperhealing.explosions.ExplosionEvent;
import xd.arkosammy.creeperhealing.util.callbacks.DaylightCycleEvents;
import xd.arkosammy.creeperhealing.util.callbacks.SplashPotionCallbacks;
import xd.arkosammy.creeperhealing.util.callbacks.TimeCommandCallbacks;
import xd.arkosammy.monkeyconfig.settings.BooleanSetting;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

public final class Events {

    private Events() {
        throw new AssertionError();
    }

    public static void registerEvents() {

        ServerTickEvents.END_SERVER_TICK.register(CreeperHealing.EXPLOSION_MANAGER::tick);
        DaylightCycleEvents.ON_NIGHT_SKIPPED.register(Events::onNightSkipped);
        SplashPotionCallbacks.ON_COLLISION.register(Events::onSplashPotionHit);
        ExplosionManagerRegistrar.getInstance().registerExplosionManager(CreeperHealing.EXPLOSION_MANAGER);
        TimeCommandCallbacks.ON_TIME_EXECUTE_SET.register(Events::onTimeCommand);
        TimeCommandCallbacks.ON_TIME_EXECUTE_ADD.register(Events::onTimeCommand);

    }

    // Start healing DaytimeExplosionEvents when the night is skipped
    private static void onNightSkipped(ServerWorld world, BooleanSupplier shouldKeepTicking) {
        for (ExplosionEvent explosionEvent : CreeperHealing.EXPLOSION_MANAGER.getExplosionEvents().toList()) {
            if (explosionEvent instanceof DaytimeExplosionEvent daytimeExplosionEvent) {
                daytimeExplosionEvent.setHealTimer(1);
            }
        }
    }

    private static void onSplashPotionHit(PotionEntity potionEntity, PotionContentsComponent potionContentsComponent, HitResult hitResult, World world) {
        Iterable<StatusEffectInstance> statusEffectsIterable = potionContentsComponent.getEffects();
        List<StatusEffectInstance> statusEffects = new ArrayList<>();

        for (StatusEffectInstance statusEffectInstance : statusEffectsIterable) {
            statusEffects.add(statusEffectInstance);
        }

        BlockPos potionHitPosition = switch (hitResult.getType()) {
            case BLOCK -> ((BlockHitResult) hitResult).getBlockPos().offset(((BlockHitResult) hitResult).getSide());
            case ENTITY -> ((EntityHitResult) hitResult).getEntity().getBlockPos();
            case MISS -> null;
        };

        if (potionHitPosition == null) {
            return;
        }
        RegistryEntry.Reference<StatusEffect> instantHealthEffect = StatusEffects.INSTANT_HEALTH.getKey().flatMap(Registries.STATUS_EFFECT::getEntry).orElse(null);
        if (instantHealthEffect == null) {
            return;
        }
        boolean hasInstantHealth = statusEffects.stream().anyMatch(statusEffect -> statusEffect.equals(instantHealthEffect));
        RegistryEntry.Reference<StatusEffect> regenerationEffect = StatusEffects.REGENERATION.getKey().flatMap(Registries.STATUS_EFFECT::getEntry).orElse(null);
        if (regenerationEffect == null) {
            return;
        }
        boolean hasRegeneration = statusEffects.stream().anyMatch(statusEffect -> statusEffect.equals(regenerationEffect));
        boolean healOnHealingPotion = ConfigUtils.getSettingValue(ConfigSettings.HEAL_ON_HEALING_POTION_SPLASH.getSettingLocation(), BooleanSetting.class);
        boolean healOnRegenerationPotion = ConfigUtils.getSettingValue(ConfigSettings.HEAL_ON_REGENERATION_POTION_SPLASH.getSettingLocation(), BooleanSetting.class);
        if (hasInstantHealth && healOnHealingPotion) {
            for (ExplosionEvent explosionEvent : CreeperHealing.EXPLOSION_MANAGER.getExplosionEvents().toList()) {
                boolean potionHitExplosion = explosionEvent.getAffectedBlocks().anyMatch(affectedBlock -> affectedBlock.getBlockPos().equals(potionHitPosition));
                if (potionHitExplosion && explosionEvent instanceof AbstractExplosionEvent abstractExplosionEvent) {
                    abstractExplosionEvent.setHealTimer(1);
                    abstractExplosionEvent.getAffectedBlocks().forEach(affectedBlock -> {
                        if (affectedBlock instanceof SingleAffectedBlock singleAffectedBlock) {
                            singleAffectedBlock.setTimer(1);
                        }
                    });
                }
            }
        } else if (hasRegeneration && healOnRegenerationPotion) {
            for (ExplosionEvent explosionEvent : CreeperHealing.EXPLOSION_MANAGER.getExplosionEvents().toList()) {
                boolean potionHitExplosion = explosionEvent.getAffectedBlocks().anyMatch(affectedBlock -> affectedBlock.getBlockPos().equals(potionHitPosition));
                if (potionHitExplosion && explosionEvent instanceof AbstractExplosionEvent abstractExplosionEvent) {
                    abstractExplosionEvent.setHealTimer(1);
                }
            }
        }
    }

    // Recalculate DaytimeExplosionEvents' timers when ticks are added or set
    private static void onTimeCommand(ServerCommandSource serverCommandSource, int time, int newTime) {
        for (ExplosionEvent explosionEvent : CreeperHealing.EXPLOSION_MANAGER.getExplosionEvents().toList()) {
            if (explosionEvent instanceof DaytimeExplosionEvent daytimeExplosionEvent && explosionEvent.getHealTimer() > 0) {
                daytimeExplosionEvent.setHealTimer(SharedConstants.TICKS_PER_IN_GAME_DAY - newTime);
            }
        }
    }

}

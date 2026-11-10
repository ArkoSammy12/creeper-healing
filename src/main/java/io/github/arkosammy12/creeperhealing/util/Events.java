package io.github.arkosammy12.creeperhealing.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.SharedConstants;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.projectile.throwableitemprojectile.AbstractThrownPotion;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Holder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import io.github.arkosammy12.creeperhealing.CreeperHealing;
import io.github.arkosammy12.creeperhealing.ExplosionManagerRegistrar;
import io.github.arkosammy12.creeperhealing.blocks.SingleAffectedBlock;
import io.github.arkosammy12.creeperhealing.config.ConfigUtils;
import io.github.arkosammy12.creeperhealing.explosions.AbstractExplosionEvent;
import io.github.arkosammy12.creeperhealing.explosions.DaytimeExplosionEvent;
import io.github.arkosammy12.creeperhealing.explosions.ExplosionEvent;
import io.github.arkosammy12.creeperhealing.util.callbacks.DaylightCycleEvents;
import io.github.arkosammy12.creeperhealing.util.callbacks.SplashPotionCallbacks;
import io.github.arkosammy12.creeperhealing.util.callbacks.TimeCommandCallbacks;

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
    private static void onNightSkipped(ServerLevel world, BooleanSupplier shouldKeepTicking) {
        for (ExplosionEvent explosionEvent : CreeperHealing.EXPLOSION_MANAGER.getExplosionEvents().toList()) {
            if (explosionEvent instanceof DaytimeExplosionEvent daytimeExplosionEvent) {
                daytimeExplosionEvent.setHealTimer(1);
            }
        }
    }

    private static void onSplashPotionHit(AbstractThrownPotion potionEntity, PotionContents potionContentsComponent, HitResult hitResult, Level world) {
        Iterable<MobEffectInstance> statusEffectsIterable = potionContentsComponent.getAllEffects();
        List<MobEffectInstance> statusEffects = new ArrayList<>();

        for (MobEffectInstance statusEffectInstance : statusEffectsIterable) {
            statusEffects.add(statusEffectInstance);
        }

        BlockPos potionHitPosition = switch (hitResult.getType()) {
            case BLOCK -> ((BlockHitResult) hitResult).getBlockPos().relative(((BlockHitResult) hitResult).getDirection());
            case ENTITY -> ((EntityHitResult) hitResult).getEntity().blockPosition();
            case MISS -> null;
        };

        if (potionHitPosition == null) {
            return;
        }
        Holder<MobEffect> instantHealthEffect = MobEffects.INSTANT_HEALTH;
        boolean hasInstantHealth = statusEffects.stream().anyMatch(statusEffect -> statusEffect.equals(instantHealthEffect));
        Holder<MobEffect> regenerationEffect = MobEffects.REGENERATION;
        boolean hasRegeneration = statusEffects.stream().anyMatch(statusEffect -> statusEffect.equals(regenerationEffect));
        boolean healOnHealingPotion = ConfigUtils.getRawBooleanSetting(ConfigUtils.HEAL_ON_HEALING_POTION_SPLASH);
        boolean healOnRegenerationPotion = ConfigUtils.getRawBooleanSetting(ConfigUtils.HEAL_ON_REGENERATION_POTION_SPLASH);
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
    private static void onTimeCommand(CommandSourceStack serverCommandSource, int time, int newTime) {
        for (ExplosionEvent explosionEvent : CreeperHealing.EXPLOSION_MANAGER.getExplosionEvents().toList()) {
            if (explosionEvent instanceof DaytimeExplosionEvent daytimeExplosionEvent && explosionEvent.getHealTimer() > 0) {
                daytimeExplosionEvent.setHealTimer(SharedConstants.TICKS_PER_GAME_DAY - newTime);
            }
        }
    }

}

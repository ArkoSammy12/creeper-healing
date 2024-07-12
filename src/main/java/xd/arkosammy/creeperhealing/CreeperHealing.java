package xd.arkosammy.creeperhealing;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xd.arkosammy.creeperhealing.blocks.SingleAffectedBlock;
import xd.arkosammy.creeperhealing.config.ConfigSettings;
import xd.arkosammy.creeperhealing.config.ConfigUtils;
import xd.arkosammy.creeperhealing.config.SettingGroups;
import xd.arkosammy.creeperhealing.explosions.AbstractExplosionEvent;
import xd.arkosammy.creeperhealing.explosions.DaytimeExplosionEvent;
import xd.arkosammy.creeperhealing.explosions.DefaultSerializedExplosion;
import xd.arkosammy.creeperhealing.explosions.ExplosionEvent;
import xd.arkosammy.creeperhealing.explosions.ducks.ExplosionAccessor;
import xd.arkosammy.creeperhealing.explosions.factories.DefaultExplosionFactory;
import xd.arkosammy.creeperhealing.managers.DefaultExplosionManager;
import xd.arkosammy.creeperhealing.util.callbacks.DaylightCycleEvents;
import xd.arkosammy.creeperhealing.util.callbacks.ExplosionCallbacks;
import xd.arkosammy.creeperhealing.util.callbacks.SplashPotionCallbacks;
import xd.arkosammy.monkeyconfig.managers.ConfigManager;
import xd.arkosammy.monkeyconfig.managers.TomlConfigManager;
import xd.arkosammy.monkeyconfig.registrars.DefaultConfigRegistrar;
import xd.arkosammy.monkeyconfig.settings.BooleanSetting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;

public class CreeperHealing implements ModInitializer {

	public static final String MOD_ID = "creeper-healing";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final DefaultExplosionManager EXPLOSION_MANAGER = new DefaultExplosionManager(DefaultSerializedExplosion.CODEC);
	public static final ConfigManager CONFIG_MANAGER = new TomlConfigManager(MOD_ID, SettingGroups.getSettingGroups(), ConfigSettings.getSettingBuilders());

	@Override
	public void onInitialize() {
		DefaultConfigRegistrar.INSTANCE.registerConfigManager(CONFIG_MANAGER);
		ServerLifecycleEvents.SERVER_STARTING.register(CreeperHealing::onServerStarting);
		ServerLifecycleEvents.SERVER_STOPPING.register(CreeperHealing::onServerStopping);
		ServerTickEvents.END_SERVER_TICK.register(EXPLOSION_MANAGER::tick);
		SplashPotionCallbacks.ON_COLLISION.register(CreeperHealing::onSplashPotionHit);
		DaylightCycleEvents.ON_NIGHT_SKIPPED.register(CreeperHealing::onNightSkipped);
		LOGGER.info("I will try my best to heal your explosions :)");

		ExplosionCallbacks.AFTER_EXPLOSION.register((explosion) -> {
			final Set<BlockPos> calculatedPositions = ((ExplosionAccessor) explosion).creeperhealing$getCalculatedBlockPositions();
			final Map<BlockPos, Pair<BlockState, BlockEntity>> savedStatesAndEntities = ((ExplosionAccessor) explosion).creeperhealing$getSavedStatesAndEntities();
			final DefaultExplosionFactory explosionFactory = new DefaultExplosionFactory(savedStatesAndEntities, explosion.getAffectedBlocks(), new ArrayList<>(calculatedPositions), explosion.getEntity(), explosion.getCausingEntity(), ((ExplosionAccessor) explosion).creeperhealing$getDamageSource(), ((ExplosionAccessor) explosion).creeperhealing$getWorld());
			EXPLOSION_MANAGER.addExplosionEvent(explosionFactory);
		});

	}

	private static void onServerStarting(MinecraftServer server) {
		EXPLOSION_MANAGER.onServerStarting(server);
	}

	private static void onServerStopping(MinecraftServer server) {
		EXPLOSION_MANAGER.onServerStopping(server);
	}

	private static void onNightSkipped(ServerWorld world, BooleanSupplier shouldKeepTicking) {
		for(ExplosionEvent explosionEvent : CreeperHealing.EXPLOSION_MANAGER.getExplosionEvents().toList()){
			if(explosionEvent instanceof DaytimeExplosionEvent daytimeExplosionEvent){
				daytimeExplosionEvent.setHealTimer(1);
			}
		}
	}

	private static void onSplashPotionHit(PotionEntity potionEntity, PotionContentsComponent potionContentsComponent, HitResult hitResult, World world) {
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
		if (hasInstantHealth && healOnHealingPotion){
			for(ExplosionEvent explosionEvent : CreeperHealing.EXPLOSION_MANAGER.getExplosionEvents().toList()){
				boolean potionHitExplosion = explosionEvent.getAffectedBlocks().anyMatch(affectedBlock -> affectedBlock.getBlockPos().equals(potionHitPosition));
				if(potionHitExplosion && explosionEvent instanceof AbstractExplosionEvent abstractExplosionEvent){
					abstractExplosionEvent.setHealTimer(1);
					abstractExplosionEvent.getAffectedBlocks().forEach(affectedBlock -> {
						if (affectedBlock instanceof SingleAffectedBlock singleAffectedBlock) {
							singleAffectedBlock.setTimer(1);
						}
					});
				}
			}
		} else if (hasRegeneration && healOnRegenerationPotion){
			for(ExplosionEvent explosionEvent : CreeperHealing.EXPLOSION_MANAGER.getExplosionEvents().toList()){
				boolean potionHitExplosion = explosionEvent.getAffectedBlocks().anyMatch(affectedBlock -> affectedBlock.getBlockPos().equals(potionHitPosition));
				if(potionHitExplosion && explosionEvent instanceof AbstractExplosionEvent abstractExplosionEvent){
					abstractExplosionEvent.setHealTimer(1);
				}
			}
		}
	}

}
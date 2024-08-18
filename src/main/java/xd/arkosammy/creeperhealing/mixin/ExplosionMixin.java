package xd.arkosammy.creeperhealing.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import xd.arkosammy.creeperhealing.ExplosionManagerRegistrar;
import xd.arkosammy.creeperhealing.config.ConfigSettings;
import xd.arkosammy.creeperhealing.config.ConfigUtils;
import xd.arkosammy.creeperhealing.explosions.ducks.ExplosionDuck;
import xd.arkosammy.creeperhealing.managers.DefaultExplosionManager;
import xd.arkosammy.creeperhealing.util.ExcludedBlocks;
import xd.arkosammy.creeperhealing.util.ExplosionContext;
import xd.arkosammy.creeperhealing.util.ExplosionUtils;
import xd.arkosammy.monkeyconfig.settings.BooleanSetting;
import xd.arkosammy.monkeyconfig.settings.list.StringListSetting;

import java.util.*;

@Mixin(Explosion.class)
public abstract class ExplosionMixin implements ExplosionDuck {

    @Shadow @Final private World world;

    @Shadow public abstract @Nullable Entity getEntity();

    @Shadow public abstract List<BlockPos> getAffectedBlocks();

    @Shadow @Nullable public abstract LivingEntity getCausingEntity();

    @Unique
    @Nullable
    private World.ExplosionSourceType explosionSourceType = null;

    @Unique
    private final Map<BlockPos, Pair<BlockState, BlockEntity>> affectedStatesAndBlockEntities = new HashMap<>();

    @Unique
    private final Set<BlockPos> vanillaAffectedPositions = new HashSet<>();

    @Unique
    private final Set<BlockPos> indirectlyAffectedPositions = new HashSet<>();

    @Override
    public void creeperhealing$setExplosionSourceType(World.ExplosionSourceType explosionSourceType) {
        this.explosionSourceType = explosionSourceType;
    }

    @Override
    public World.ExplosionSourceType creeperhealing$getExplosionSourceType() {
        return this.explosionSourceType;
    }

    @Override
    public boolean creeperhealing$shouldHeal() {
        if (this.vanillaAffectedPositions.isEmpty()) {
            return false;
        }
        World.ExplosionSourceType explosionSourceType = (this.explosionSourceType);
        boolean shouldHeal = switch (explosionSourceType) {
            case MOB -> {
                if (!ConfigUtils.getSettingValue(ConfigSettings.HEAL_MOB_EXPLOSIONS.getSettingLocation(), BooleanSetting.class)) {
                    yield false;
                }
                LivingEntity causingEntity = this.getCausingEntity();
                if (causingEntity == null) {
                    yield true;
                }
                String entityId = Registries.ENTITY_TYPE.getId(causingEntity.getType()).toString();
                List<? extends String> healMobExplosionsBlacklist = ConfigUtils.getSettingValue(ConfigSettings.HEAL_MOB_EXPLOSIONS_BLACKLIST.getSettingLocation(), StringListSetting.class);
                yield !healMobExplosionsBlacklist.contains(entityId);
            }
            case BLOCK -> ConfigUtils.getSettingValue(ConfigSettings.HEAL_BLOCK_EXPLOSIONS.getSettingLocation(), BooleanSetting.class);
            case TNT -> ConfigUtils.getSettingValue(ConfigSettings.HEAL_TNT_EXPLOSIONS.getSettingLocation(), BooleanSetting.class);
            case TRIGGER -> ConfigUtils.getSettingValue(ConfigSettings.HEAL_TRIGGERED_EXPLOSIONS.getSettingLocation(), BooleanSetting.class);
            case null, default -> ConfigUtils.getSettingValue(ConfigSettings.HEAL_OTHER_EXPLOSIONS.getSettingLocation(), BooleanSetting.class);
        };
        return shouldHeal;
    }

    // Save the affected block states and block entities before the explosion takes effect
    @WrapMethod(method = "collectBlocksAndDamageEntities")
    private void collectAffectedBlockStatesAndBlockEntities(Operation<Void> original) {
        original.call();
        if (world.isClient()) {
            return;
        }
        this.vanillaAffectedPositions.addAll(ExplosionUtils.filterPositionsToHeal(this.getAffectedBlocks(), (pos) -> this.world.getBlockState(pos)));
        this.indirectlyAffectedPositions.addAll(ExplosionUtils.getIndirectlyAffectedBlocks(this.getAffectedBlocks(), this.world));
        for (BlockPos pos : this.vanillaAffectedPositions) {
            this.affectedStatesAndBlockEntities.put(pos, new Pair<>(this.world.getBlockState(pos), this.world.getBlockEntity(pos)));
        }
        for (BlockPos pos : this.indirectlyAffectedPositions) {
            this.affectedStatesAndBlockEntities.put(pos, new Pair<>(this.world.getBlockState(pos), this.world.getBlockEntity(pos)));
        }
    }

    @WrapMethod(method = "affectWorld")
    private void onAffectWorld(boolean particles, Operation<Void> original) {
        // If adding more logic here, remember to check logical server side with World#isClient
        ExplosionUtils.DROP_BLOCK_ITEMS.set(true);
        ExplosionUtils.DROP_CONTAINER_INVENTORY_ITEMS.set(true);

        original.call(particles);

        ExplosionUtils.DROP_BLOCK_ITEMS.set(true);
        ExplosionUtils.DROP_CONTAINER_INVENTORY_ITEMS.set(true);
        if (world.isClient() || !this.creeperhealing$shouldHeal()) {
            this.vanillaAffectedPositions.clear();
            this.affectedStatesAndBlockEntities.clear();
            this.indirectlyAffectedPositions.clear();
            return;
        }
        List<BlockPos> filteredIndirectlyAffectedPositions = new ArrayList<>();
        for (BlockPos pos : this.indirectlyAffectedPositions) {
            Pair<BlockState, BlockEntity> pair = this.affectedStatesAndBlockEntities.get(pos);
            if (pair == null) {
                continue;
            }
            BlockState oldState = pair.getLeft();
            // Hardcoded exception, place before all other logic
            if (ExcludedBlocks.isExcluded(oldState)) {
                continue;
            }
            BlockState newState = this.world.getBlockState(pos);
            if (!Objects.equals(oldState, newState)) {
                filteredIndirectlyAffectedPositions.add(pos);
            }
        }
        List<BlockPos> filteredAffectedPositions = new ArrayList<>();
        for (BlockPos pos : this.vanillaAffectedPositions) {
            Pair<BlockState, BlockEntity> pair = this.affectedStatesAndBlockEntities.get(pos);
            if (pair == null) {
                continue;
            }
            BlockState state = pair.getLeft();
            // Hardcoded exception, place before all other logic
            if (ExcludedBlocks.isExcluded(state)) {
                continue;
            }
            filteredAffectedPositions.add(pos);
        }
        Map<BlockPos, Pair<BlockState, BlockEntity>> filteredSavedStatesAndBlockEntities = new HashMap<>();
        for (Map.Entry<BlockPos, Pair<BlockState, BlockEntity>> entry : this.affectedStatesAndBlockEntities.entrySet()) {
            BlockPos entryPos = entry.getKey();
            if (filteredAffectedPositions.contains(entryPos) || filteredIndirectlyAffectedPositions.contains(entryPos)) {
                filteredSavedStatesAndBlockEntities.put(entryPos, entry.getValue());
            }
        }
        ExplosionContext explosionContext = new ExplosionContext(
                filteredAffectedPositions,
                filteredIndirectlyAffectedPositions,
                filteredSavedStatesAndBlockEntities,
                this.world,
                this.explosionSourceType
        );
        ExplosionManagerRegistrar.getInstance().emitExplosionContext(DefaultExplosionManager.ID, explosionContext);
        this.vanillaAffectedPositions.clear();
        this.affectedStatesAndBlockEntities.clear();
        this.indirectlyAffectedPositions.clear();
    }

}

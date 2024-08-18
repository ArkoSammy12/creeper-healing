package xd.arkosammy.creeperhealing.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xd.arkosammy.creeperhealing.ExplosionManagerRegistrar;
import xd.arkosammy.creeperhealing.config.ConfigSettings;
import xd.arkosammy.creeperhealing.config.ConfigUtils;
import xd.arkosammy.creeperhealing.explosions.ducks.ExplosionDuck;
import xd.arkosammy.creeperhealing.managers.DefaultExplosionManager;
import xd.arkosammy.creeperhealing.util.EmptyWorld;
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
    @Inject(method = "collectBlocksAndDamageEntities", at = @At("RETURN"))
    private void collectAffectedBlockStatesAndBlockEntities(CallbackInfo ci) {
        if (world.isClient()) {
            return;
        }
        this.vanillaAffectedPositions.addAll(ExplosionUtils.filterPositionsToHeal(this.getAffectedBlocks(), (pos) -> this.world.getBlockState(pos)));
        this.checkForIndirectlyAffectedPositions();
        for (BlockPos pos : this.vanillaAffectedPositions) {
            this.affectedStatesAndBlockEntities.put(pos, new Pair<>(this.world.getBlockState(pos), this.world.getBlockEntity(pos)));
        }
        for (BlockPos pos : this.indirectlyAffectedPositions) {
            this.affectedStatesAndBlockEntities.put(pos, new Pair<>(this.world.getBlockState(pos), this.world.getBlockEntity(pos)));
        }
    }

    // Make sure the thread local is reset when entering and exiting Explosion#affectWorld
    @Inject(method = "affectWorld", at = @At(value = "HEAD"))
    private void setThreadLocals(boolean particles, CallbackInfo ci) {
        // If adding more logic here, remember to check logical server side with World#isClient
        ExplosionUtils.DROP_BLOCK_ITEMS.set(true);
        ExplosionUtils.DROP_CONTAINER_INVENTORY_ITEMS.set(true);
    }

    // Filter out indirectly affected positions whose corresponding state did not change before and after the explosion.
    // Filter out entries in the affected states and block entities map with block position keys not in the affected positions.
    // Emit an ExplosionContext object for ExplosionManagers to receive.
    @Inject(method = "affectWorld", at = @At(value = "RETURN"))
    private void emitExplosionContext(boolean particles, CallbackInfo ci) {
        ExplosionUtils.DROP_BLOCK_ITEMS.set(true);
        ExplosionUtils.DROP_CONTAINER_INVENTORY_ITEMS.set(true);
        if (world.isClient()) {
            this.vanillaAffectedPositions.clear();
            this.affectedStatesAndBlockEntities.clear();
            this.indirectlyAffectedPositions.clear();
            return;
        }
        if (!this.creeperhealing$shouldHeal()) {
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
            // Hardcoded exception, place before all other logic
            BlockState state = this.affectedStatesAndBlockEntities.get(pos).getLeft();
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

    // Recursively find indirectly affected positions connected to the main affected positions.
    // Start from the "edge" of the blast radius and visit each neighbor until a neighbor has no
    // non-visited positions, the neighbor is surrounded by air, or we hit the max recursion depth.
    @Unique
    private void checkForIndirectlyAffectedPositions() {

        // Only consider block positions with adjacent non-affected positions
        List<BlockPos> edgeAffectedPositions = new ArrayList<>();
        for (BlockPos vanillaAffectedPosition : this.getAffectedBlocks()) {
            if (world.getBlockState(vanillaAffectedPosition).isAir()) {
                continue;
            }
            for (Direction direction : Direction.values()) {
                BlockPos neighborPos = vanillaAffectedPosition.offset(direction);
                BlockState neighborState = world.getBlockState(neighborPos);
                // No blocks will be connected to the neighbor position if the state is air
                if (neighborState.isAir()) {
                    continue;
                }
                if (!this.getAffectedBlocks().contains(neighborPos)) {
                    edgeAffectedPositions.add(vanillaAffectedPosition);
                    break;
                }
            }
        }

        // Pass in a custom WorldView implementation that always returns an air BlockState when calling
        // WorldView#getBlockState on it. This guarantees that further checks with BlockState#canPlaceAt
        // are done in what will look like an empty world
        EmptyWorld emptyWorld = new EmptyWorld(this.world);
        Set<BlockPos> newPositions = new HashSet<>();
        for (BlockPos filteredPosition : edgeAffectedPositions) {
            checkNeighbors(512, filteredPosition, newPositions, emptyWorld);
        }
        this.indirectlyAffectedPositions.addAll(ExplosionUtils.filterPositionsToHeal(newPositions, (pos) -> this.world.getBlockState(pos)));
    }

    @Unique
    private void checkNeighbors(int maxCheckDepth, BlockPos currentPosition, Set<BlockPos> newPositions, EmptyWorld emptyWorld) {
        if (maxCheckDepth <= 0) {
            return;
        }
        for (Direction neighborDirection : Direction.values()) {
            BlockPos neighborPos = currentPosition.offset(neighborDirection);
            BlockState neighborState = this.world.getBlockState(neighborPos);

            // If the block cannot be placed at an empty position also surrounded by air, then we assume
            // the block needs a supporting block to be placed.
            if (neighborState.isAir() || neighborState.canPlaceAt(emptyWorld, neighborPos) || this.getAffectedBlocks().contains(neighborPos)) {
                continue;
            }
            if (newPositions.add(neighborPos)) {
                this.checkNeighbors(maxCheckDepth - 1, neighborPos, newPositions, emptyWorld);
            }
        }
    }

}

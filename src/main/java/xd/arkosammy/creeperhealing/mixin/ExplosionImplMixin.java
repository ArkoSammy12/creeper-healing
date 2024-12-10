package xd.arkosammy.creeperhealing.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionImpl;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import xd.arkosammy.creeperhealing.ExplosionManagerRegistrar;
import xd.arkosammy.creeperhealing.config.ConfigSettings;
import xd.arkosammy.creeperhealing.config.ConfigUtils;
import xd.arkosammy.creeperhealing.explosions.ducks.ExplosionImplDuck;
import xd.arkosammy.creeperhealing.managers.DefaultExplosionManager;
import xd.arkosammy.creeperhealing.util.EmptyWorld;
import xd.arkosammy.creeperhealing.util.ExcludedBlocks;
import xd.arkosammy.creeperhealing.util.ExplosionContext;
import xd.arkosammy.creeperhealing.util.ExplosionUtils;
import xd.arkosammy.monkeyconfig.settings.BooleanSetting;
import xd.arkosammy.monkeyconfig.settings.list.StringListSetting;

import java.util.*;

@Mixin(ExplosionImpl.class)
public abstract class ExplosionImplMixin implements Explosion, ExplosionImplDuck {

    @Shadow public abstract @Nullable Entity getEntity();

    @Shadow @Nullable public abstract LivingEntity getCausingEntity();

    @Shadow public abstract ServerWorld getWorld();

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
        if (this.getWorld().isClient()) {
            return false;
        }
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
    @ModifyReturnValue(method = "getBlocksToDestroy", at = @At("RETURN"))
    private List<BlockPos> collectAffectedBlocks(List<BlockPos> original) {
        if (this.getWorld().isClient()) {
            return original;
        }
        this.vanillaAffectedPositions.addAll(ExplosionUtils.filterPositionsToHeal(original, (pos) -> this.getWorld().getBlockState(pos)));
        this.checkForIndirectlyAffectedPositions();
        for (BlockPos pos : this.vanillaAffectedPositions) {
            this.affectedStatesAndBlockEntities.put(pos, new Pair<>(this.getWorld().getBlockState(pos), this.getWorld().getBlockEntity(pos)));
        }
        for (BlockPos pos : this.indirectlyAffectedPositions) {
            this.affectedStatesAndBlockEntities.put(pos, new Pair<>(this.getWorld().getBlockState(pos), this.getWorld().getBlockEntity(pos)));
        }
        return original;
    }

    @WrapMethod(method = "destroyBlocks")
    private void onDestroyBlocks(List<BlockPos> positions, Operation<Void> original) {
        // Make sure the thread local is reset when entering and exiting ExplosionImpl#destroyBlocks
        // If adding more logic here, remember to check logical server side with World#isClient
        ExplosionUtils.DROP_BLOCK_ITEMS.set(true);
        ExplosionUtils.DROP_CONTAINER_INVENTORY_ITEMS.set(true);

        original.call(positions);

        // Filter out indirectly affected positions whose corresponding state did not change before and after the explosion.
        // Filter out entries in the affected states and block entities map with block position keys not in the affected positions.
        // Emit an ExplosionContext object for ExplosionManagers to receive.
        ExplosionUtils.DROP_BLOCK_ITEMS.set(true);
        ExplosionUtils.DROP_CONTAINER_INVENTORY_ITEMS.set(true);
        if ((this.getWorld().isClient() || !(this.getWorld() instanceof ServerWorld serverWorld)) || !this.creeperhealing$shouldHeal()) {
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
            BlockState newState = this.getWorld().getBlockState(pos);
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
                serverWorld,
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
        for (BlockPos vanillaAffectedPosition : this.vanillaAffectedPositions) {
            if (this.getWorld().getBlockState(vanillaAffectedPosition).isAir()) {
                continue;
            }
            for (Direction direction : Direction.values()) {
                BlockPos neighborPos = vanillaAffectedPosition.offset(direction);
                BlockState neighborState = this.getWorld().getBlockState(neighborPos);
                // No blocks will be connected to the neighbor position if the state is air
                if (neighborState.isAir()) {
                    continue;
                }
                if (!this.vanillaAffectedPositions.contains(neighborPos)) {
                    edgeAffectedPositions.add(vanillaAffectedPosition);
                    break;
                }
            }
        }

        // Pass in a custom WorldView implementation that always returns an air BlockState when calling
        // WorldView#getBlockState on it. This guarantees that further checks with BlockState#canPlaceAt
        // are done in what will look like an empty world
        EmptyWorld emptyWorld = new EmptyWorld(this.getWorld());
        Set<BlockPos> newPositions = new HashSet<>();
        for (BlockPos filteredPosition : edgeAffectedPositions) {
            checkNeighbors(512, filteredPosition, newPositions, emptyWorld);
        }
        this.indirectlyAffectedPositions.addAll(ExplosionUtils.filterPositionsToHeal(newPositions, (pos) -> this.getWorld().getBlockState(pos)));
    }

    @Unique
    private void checkNeighbors(int maxCheckDepth, BlockPos currentPosition, Set<BlockPos> newPositions, EmptyWorld emptyWorld) {
        if (maxCheckDepth <= 0) {
            return;
        }
        for (Direction neighborDirection : Direction.values()) {
            BlockPos neighborPos = currentPosition.offset(neighborDirection);
            BlockState neighborState = this.getWorld().getBlockState(neighborPos);

            // If the block cannot be placed at an empty position also surrounded by air, then we assume
            // the block needs a supporting block to be placed.
            if (neighborState.isAir() || neighborState.canPlaceAt(emptyWorld, neighborPos) || this.vanillaAffectedPositions.contains(neighborPos)) {
                continue;
            }
            if (newPositions.add(neighborPos)) {
                this.checkNeighbors(maxCheckDepth - 1, neighborPos, newPositions, emptyWorld);
            }
        }
    }

}

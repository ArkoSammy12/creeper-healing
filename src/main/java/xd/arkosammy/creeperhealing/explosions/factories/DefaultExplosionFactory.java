package xd.arkosammy.creeperhealing.explosions.factories;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import xd.arkosammy.creeperhealing.blocks.AffectedBlock;
import xd.arkosammy.creeperhealing.blocks.SingleAffectedBlock;
import xd.arkosammy.creeperhealing.config.ConfigSettings;
import xd.arkosammy.creeperhealing.config.ConfigUtils;
import xd.arkosammy.creeperhealing.explosions.*;
import xd.arkosammy.creeperhealing.util.ExplosionUtils;

import java.util.*;

public class DefaultExplosionFactory implements ExplosionEventFactory<AbstractExplosionEvent> {

    private final Map<BlockPos, Pair<BlockState, BlockEntity>> affectedStatesAndBlockEntities;
    private final ServerWorld world;
    private final int blastRadius;
    private final Set<BlockPos> affectedPositions = new HashSet<>();
    private final BlockPos center;
    private final List<BlockPos> vanillaAffectedPositions;

    public DefaultExplosionFactory(Map<BlockPos, Pair<BlockState, BlockEntity>> affectedStatesAndBlockEntities, List<BlockPos> vanillaAffectedPositions, List<BlockPos> indirectlyExplodedPositions, ServerWorld world) {
        this.affectedStatesAndBlockEntities = affectedStatesAndBlockEntities;
        this.world = world;
        this.affectedPositions.addAll(vanillaAffectedPositions);
        this.affectedPositions.addAll(indirectlyExplodedPositions);
        this.blastRadius = ExplosionUtils.getMaxExplosionRadius(vanillaAffectedPositions);
        this.center = ExplosionUtils.calculateCenter(vanillaAffectedPositions);
        this.vanillaAffectedPositions = vanillaAffectedPositions;
    }


    @Override
    public @Nullable AbstractExplosionEvent createExplosionEvent() {
        List<AffectedBlock> affectedBlocks = this.processAffectedPositions(new ArrayList<>(this.affectedPositions), this.world);
        if (affectedBlocks == null || affectedBlocks.isEmpty()) {
            return null;
        }
        ExplosionHealingMode healingMode = ConfigUtils.getEnumSettingValue(ConfigSettings.MODE.getSettingLocation());
        AbstractExplosionEvent explosionEvent = switch (healingMode) {
            case DEFAULT_MODE -> new DefaultExplosionEvent(affectedBlocks, this.blastRadius, this.center);
            case DAYTIME_HEALING_MODE -> new DaytimeExplosionEvent(affectedBlocks, this.blastRadius, this.center);
            case DIFFICULTY_BASED_HEALING_MODE -> new DifficultyBasedExplosionEvent(affectedBlocks, this.blastRadius, this.center);
            case BLAST_RESISTANCE_BASED_HEALING_MODE -> new BlastResistanceBasedExplosionEvent(affectedBlocks, this.blastRadius, this.center);
        };
        explosionEvent.setup(this.world);
        return explosionEvent;
    }

    @Override
    public @Nullable AbstractExplosionEvent createExplosionEvent(List<BlockPos> affectedPositions, ServerWorld world) {
        List<AffectedBlock> affectedBlocks = this.processAffectedPositions(affectedPositions, world);
        if (affectedBlocks == null || affectedBlocks.isEmpty()) {
            return null;
        }
        BlockPos center = ExplosionUtils.calculateCenter(affectedPositions);
        int radius = ExplosionUtils.getMaxExplosionRadius(affectedPositions);
        ExplosionHealingMode healingMode = ConfigUtils.getEnumSettingValue(ConfigSettings.MODE.getSettingLocation());
        AbstractExplosionEvent explosionEvent = switch (healingMode) {
            case DEFAULT_MODE -> new DefaultExplosionEvent(affectedBlocks, radius, center);
            case DAYTIME_HEALING_MODE -> new DaytimeExplosionEvent(affectedBlocks, radius, center);
            case DIFFICULTY_BASED_HEALING_MODE -> new DifficultyBasedExplosionEvent(affectedBlocks, radius, center);
            case BLAST_RESISTANCE_BASED_HEALING_MODE -> new BlastResistanceBasedExplosionEvent(affectedBlocks, radius, center);
        };
        explosionEvent.setup(world);
        return explosionEvent;
    }

    @Override
    public @Nullable AbstractExplosionEvent createExplosionEvent(List<AffectedBlock> affectedBlocks, long healTimer) {
        return this.createExplosionEvent(affectedBlocks, healTimer, ConfigUtils.getBlockPlacementDelay());
    }

    @Override
    public @Nullable AbstractExplosionEvent createExplosionEvent(List<AffectedBlock> affectedBlocks, long healTimer, long blockHealDelay) {
        return this.createExplosionEvent(affectedBlocks, healTimer, blockHealDelay, 0);
    }

    public @Nullable AbstractExplosionEvent createExplosionEvent(List<AffectedBlock> affectedBlocks, long healTimer, long blockHealDelay, int blockCounter) {
        if (affectedBlocks.isEmpty()) {
            return null;
        }
        List<BlockPos> affectedPositions = affectedBlocks.stream().map(AffectedBlock::getBlockPos).toList();
        BlockPos center = ExplosionUtils.calculateCenter(affectedPositions);
        int radius = ExplosionUtils.getMaxExplosionRadius(affectedPositions);
        List<AffectedBlock> sortedAffectedBlocks = ExplosionUtils.sortAffectedBlocks(affectedBlocks, this.world);
        sortedAffectedBlocks.forEach(affectedBlock -> {
            if (affectedBlock instanceof SingleAffectedBlock singleAffectedBlock) {
                singleAffectedBlock.setTimer(blockHealDelay);
            }
        });
        ExplosionHealingMode healingMode = ConfigUtils.getEnumSettingValue(ConfigSettings.MODE.getSettingLocation());
        AbstractExplosionEvent explosionEvent = switch (healingMode) {
            case DEFAULT_MODE -> new DefaultExplosionEvent(sortedAffectedBlocks, healTimer, blockCounter, radius, center);
            case DAYTIME_HEALING_MODE -> new DaytimeExplosionEvent(sortedAffectedBlocks, healTimer, blockCounter, radius, center);
            case DIFFICULTY_BASED_HEALING_MODE -> new DifficultyBasedExplosionEvent(sortedAffectedBlocks, healTimer, blockCounter, radius, center);
            case BLAST_RESISTANCE_BASED_HEALING_MODE -> new BlastResistanceBasedExplosionEvent(sortedAffectedBlocks, healTimer, blockCounter, radius, center);
        };
        explosionEvent.setup(world);
        return explosionEvent;
    }

    @Override
    public List<BlockPos> getAffectedPositions() {
        return this.vanillaAffectedPositions;
    }

    @Override
    public ServerWorld getWorld() {
        return this.world;
    }

    @Nullable
    private List<AffectedBlock> processAffectedPositions(List<BlockPos> affectedPositions, ServerWorld world) {
        List<BlockPos> positionsToHeal = ExplosionUtils.filterPositionsToHeal(affectedPositions, (pos) -> this.affectedStatesAndBlockEntities.get(pos).getLeft());
        if (positionsToHeal.isEmpty()) {
            return null;
        }
        List<AffectedBlock> affectedBlocks = new ArrayList<>();
        for (BlockPos pos : positionsToHeal) {
            BlockState affectedState = this.affectedStatesAndBlockEntities.get(pos).getLeft();
            BlockEntity affectedBlockEntity = this.affectedStatesAndBlockEntities.get(pos).getRight();
            AffectedBlock affectedBlock = AffectedBlock.newInstance(pos, affectedState, affectedBlockEntity, world);
            affectedBlocks.add(affectedBlock);
        }
        List<AffectedBlock> sortedAffectedBlocks = ExplosionUtils.sortAffectedBlocks(affectedBlocks, world);
        return sortedAffectedBlocks;
    }

}

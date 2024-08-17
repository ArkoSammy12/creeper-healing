package xd.arkosammy.creeperhealing.explosions.factories;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import xd.arkosammy.creeperhealing.blocks.AffectedBlock;
import xd.arkosammy.creeperhealing.blocks.SingleAffectedBlock;
import xd.arkosammy.creeperhealing.config.ConfigSettings;
import xd.arkosammy.creeperhealing.config.ConfigUtils;
import xd.arkosammy.creeperhealing.explosions.*;
import xd.arkosammy.creeperhealing.util.ExcludedBlocks;
import xd.arkosammy.creeperhealing.util.ExplosionUtils;
import xd.arkosammy.monkeyconfig.settings.BooleanSetting;
import xd.arkosammy.monkeyconfig.settings.list.StringListSetting;

import java.util.*;

public class DefaultExplosionFactory implements ExplosionEventFactory<AbstractExplosionEvent> {

    private final Map<BlockPos, Pair<BlockState, BlockEntity>> affectedStatesAndBlockEntities;
    private final World world;
    private final int blastRadius;
    private final Set<BlockPos> affectedPositions = new HashSet<>();
    private final BlockPos center;
    private final List<BlockPos> vanillaAffectedPositions;

    public DefaultExplosionFactory(Map<BlockPos, Pair<BlockState, BlockEntity>> affectedStatesAndBlockEntities, List<BlockPos> vanillaAffectedPositions, List<BlockPos> indirectlyExplodedPositions, World world) {
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
    public @Nullable AbstractExplosionEvent createExplosionEvent(List<BlockPos> affectedPositions, World world) {
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
    public World getWorld() {
        return this.world;
    }

    @Nullable
    private List<AffectedBlock> processAffectedPositions(List<BlockPos> affectedPositions, World world) {
        List<AffectedBlock> affectedBlocks = new ArrayList<>();
        boolean whitelistEnabled = ConfigUtils.getSettingValue(ConfigSettings.ENABLE_WHITELIST.getSettingLocation(), BooleanSetting.class);
        List<? extends String> whitelist = ConfigUtils.getSettingValue(ConfigSettings.WHITELIST.getSettingLocation(), StringListSetting.class);
        for (BlockPos affectedPosition : affectedPositions) {
            // Hardcoded exception. Place before all logic
            BlockState affectedState = this.affectedStatesAndBlockEntities.get(affectedPosition).getLeft();
            if (affectedState == null || ExcludedBlocks.isExcluded(affectedState)) {
                continue;
            }
            boolean isStateUnhealable = affectedState.isAir() || affectedState.isOf(Blocks.TNT) || affectedState.isIn(BlockTags.FIRE);
            if (isStateUnhealable) {
                continue;
            }
            String affectedBlockIdentifier = Registries.BLOCK.getId(affectedState.getBlock()).toString();
            boolean whitelistContainsIdentifier = whitelist.contains(affectedBlockIdentifier);
            if (!whitelistEnabled || whitelistContainsIdentifier) {
                BlockEntity affectedBlockEntity = affectedStatesAndBlockEntities.get(affectedPosition).getRight();
                affectedBlocks.add(AffectedBlock.newInstance(affectedPosition, affectedState, affectedBlockEntity, world));
            }
        }
        if (affectedBlocks.isEmpty()) {
            return null;
        }
        List<AffectedBlock> sortedAffectedBlocks = ExplosionUtils.sortAffectedBlocks(affectedBlocks, world);
        return sortedAffectedBlocks;
    }

}

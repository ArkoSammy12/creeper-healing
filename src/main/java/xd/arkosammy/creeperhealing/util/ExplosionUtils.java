package xd.arkosammy.creeperhealing.util;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import xd.arkosammy.creeperhealing.blocks.AffectedBlock;
import xd.arkosammy.creeperhealing.config.ConfigSettings;
import xd.arkosammy.creeperhealing.config.ConfigUtils;
import xd.arkosammy.monkeyconfig.settings.BooleanSetting;

import java.util.*;
import java.util.stream.Collectors;

public final class ExplosionUtils {

    private ExplosionUtils() {
        throw new AssertionError();
    }

    public static final ThreadLocal<Boolean> DROP_BLOCK_ITEMS = ThreadLocal.withInitial(() -> true);
    public static final ThreadLocal<Boolean> DROP_CONTAINER_INVENTORY_ITEMS = ThreadLocal.withInitial(() -> true);
    public static final ThreadLocal<Boolean> FALLING_BLOCK_SCHEDULE_TICK = ThreadLocal.withInitial(() -> true);

    public static void pushEntitiesUpwards(World world, BlockPos pos, BlockState state, boolean isTallBlock) {
        if (!state.isSolidBlock(world, pos)) {
            return;
        }
        int amountToPush = isTallBlock ? 2 : 1;
        for (Entity entity : world.getEntitiesByClass(LivingEntity.class, new Box(pos), Entity::isAlive)) {
            if (areAboveBlocksFree(world, pos, entity, amountToPush)) {
                entity.refreshPositionAfterTeleport(entity.getPos().withAxis(Direction.Axis.Y, entity.getBlockY() + amountToPush));
            }
        }
    }

    private static boolean areAboveBlocksFree(World world, BlockPos pos, Entity entity, int amountToPush) {
        for (int i = pos.getY(); i < pos.offset(Direction.Axis.Y, (int) Math.ceil(entity.getStandingEyeHeight())).getY(); i++) {
            BlockPos currentPos = pos.withY(i + amountToPush);
            if (world.getBlockState(currentPos).isSolidBlock(world, currentPos)) {
                return false;
            }
        }
        return true;
    }

    // The goal is to heal blocks inwards from the edge of the explosion, bottom to top, non-transparent blocks first
    public static @NotNull List<AffectedBlock> sortAffectedBlocks(@NotNull List<AffectedBlock> affectedBlocksList, World world) {
        List<AffectedBlock> sortedAffectedBlocks = new ArrayList<>(affectedBlocksList);
        List<BlockPos> affectedBlocksAsPositions = sortedAffectedBlocks.stream().map(AffectedBlock::getBlockPos).collect(Collectors.toList());
        int centerX = getCenterXCoordinate(affectedBlocksAsPositions);
        int centerZ = getCenterZCoordinate(affectedBlocksAsPositions);
        Comparator<AffectedBlock> distanceToCenterComparator = Comparator.comparingInt(affectedBlock -> (int) -(Math.round(Math.pow(affectedBlock.getBlockPos().getX() - centerX, 2) + Math.pow(affectedBlock.getBlockPos().getZ() - centerZ, 2))));
        sortedAffectedBlocks.sort(distanceToCenterComparator);
        Comparator<AffectedBlock> yLevelComparator = Comparator.comparingInt(affectedBlock -> affectedBlock.getBlockPos().getY());
        sortedAffectedBlocks.sort(yLevelComparator);
        Comparator<AffectedBlock> transparencyComparator = (currentAffectedBlock, nextAffectedBlock) -> {
            boolean isCurrentAffectedBlockTransparent = currentAffectedBlock.getBlockState().isTransparent(world, currentAffectedBlock.getBlockPos());
            boolean isNextAffectedBlockTransparent = nextAffectedBlock.getBlockState().isTransparent(world, nextAffectedBlock.getBlockPos());
            return Boolean.compare(isCurrentAffectedBlockTransparent, isNextAffectedBlockTransparent);
        };
        sortedAffectedBlocks.sort(transparencyComparator);
        return sortedAffectedBlocks;
    }

    public static BlockPos calculateCenter(Collection<BlockPos> affectedPositions) {
        int centerX = ExplosionUtils.getCenterXCoordinate(affectedPositions);
        int centerY = ExplosionUtils.getCenterYCoordinate(affectedPositions);
        int centerZ = ExplosionUtils.getCenterZCoordinate(affectedPositions);
        return new BlockPos(centerX, centerY, centerZ);
    }

    public static int getCenterXCoordinate(Collection<BlockPos> affectedCoordinates) {
        int maxX = affectedCoordinates.stream()
                .mapToInt(Vec3i::getX)
                .max()
                .orElse(0);
        int minX = affectedCoordinates.stream()
                .mapToInt(Vec3i::getX)
                .min()
                .orElse(0);
        return (maxX + minX) / 2;
    }

    public static int getCenterYCoordinate(Collection<BlockPos> affectedCoordinates) {
        int maxY = affectedCoordinates.stream()
                .mapToInt(Vec3i::getY)
                .max()
                .orElse(0);
        int minY = affectedCoordinates.stream()
                .mapToInt(Vec3i::getY)
                .min()
                .orElse(0);
        return (maxY + minY) / 2;
    }

    public static int getCenterZCoordinate(Collection<BlockPos> affectedCoordinates) {
        int maxZ = affectedCoordinates.stream()
                .mapToInt(Vec3i::getZ)
                .max()
                .orElse(0);
        int minZ = affectedCoordinates.stream()
                .mapToInt(Vec3i::getZ)
                .min()
                .orElse(0);
        return (maxZ + minZ) / 2;
    }

    public static int getMaxExplosionRadius(Collection<BlockPos> affectedCoordinates) {
        int[] radii = new int[3];
        int maxX = affectedCoordinates.stream()
                .mapToInt(Vec3i::getX)
                .max()
                .orElse(0);
        int minX = affectedCoordinates.stream()
                .mapToInt(Vec3i::getX)
                .min()
                .orElse(0);
        radii[0] = (maxX - minX) / 2;

        int maxY = affectedCoordinates.stream()
                .mapToInt(Vec3i::getY)
                .max()
                .orElse(0);
        int minY = affectedCoordinates.stream()
                .mapToInt(Vec3i::getY)
                .min()
                .orElse(0);
        radii[1] = (maxY - minY) / 2;

        int maxZ = affectedCoordinates.stream()
                .mapToInt(Vec3i::getZ)
                .max()
                .orElse(0);
        int minZ = affectedCoordinates.stream()
                .mapToInt(Vec3i::getZ)
                .min()
                .orElse(0);
        radii[2] = (maxZ - minZ) / 2;

        return Arrays.stream(radii).max().orElse(0);
    }

    public static void playBlockPlacementSoundEffect(World world, BlockPos blockPos, BlockState blockState) {
        boolean placementSoundEffectSetting = ConfigUtils.getSettingValue(ConfigSettings.BLOCK_PLACEMENT_SOUND_EFFECT.getSettingLocation(), BooleanSetting.class);
        boolean doPlacementSoundEffect = placementSoundEffectSetting && !world.isClient() && !blockState.isAir();
        if (!doPlacementSoundEffect) {
            return;
        }
        world.playSound(null, blockPos, blockState.getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, blockState.getSoundGroup().getVolume(), blockState.getSoundGroup().getPitch());
    }

    public static void spawnParticles(World world, BlockPos blockPos) {
        if (!(world instanceof ServerWorld serverWorld)) {
            return;
        }
        boolean blockPlacementParticlesSetting = ConfigUtils.getSettingValue(ConfigSettings.BLOCK_PLACEMENT_PARTICLES.getSettingLocation(), BooleanSetting.class);
        if (!blockPlacementParticlesSetting) {
            return;
        }
        serverWorld.spawnParticles(ParticleTypes.CLOUD, blockPos.getX(), blockPos.getY() + 2, blockPos.getZ(), 1, 0, 1, 0, 0.001);
    }

}

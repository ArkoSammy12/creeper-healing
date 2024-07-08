package xd.arkosammy.creeperhealing.util;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
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

    private ExplosionUtils(){}
    public static final ThreadLocal<Boolean> DROP_EXPLOSION_ITEMS = ThreadLocal.withInitial(() -> true);
    public static final ThreadLocal<Boolean> DROP_BLOCK_INVENTORY_ITEMS = ThreadLocal.withInitial(() -> true);
    public static final ThreadLocal<Boolean> FALLING_BLOCK_SCHEDULE_TICK = ThreadLocal.withInitial(() -> true);

     public static void pushEntitiesUpwards(World world, BlockPos pos, boolean isTallBlock) {
        final int amountToPush = isTallBlock ? 2 : 1;
        for(Entity entity : world.getEntitiesByClass(LivingEntity.class, new Box(pos), Entity::isAlive)){
            if(areAboveBlocksFree(world, pos, entity, amountToPush)) {
                entity.refreshPositionAfterTeleport(entity.getPos().withAxis(Direction.Axis.Y, entity.getBlockY() + amountToPush));
            }
        }
    }

    private static boolean areAboveBlocksFree(World world, BlockPos pos, Entity entity, int amountToPush){
        for(int i = pos.getY(); i < pos.offset(Direction.Axis.Y, (int) Math.ceil(entity.getStandingEyeHeight())).getY(); i++){
            final BlockPos currentPos = pos.withY(i + amountToPush);
            if(world.getBlockState(currentPos).isSolidBlock(world, currentPos)) {
                return false;
            }
        }
        return true;
    }

     // The goal is to heal blocks inwards from the edge of the explosion, bottom to top, non-transparent blocks first
     static @NotNull List<AffectedBlock> sortAffectedBlocks(@NotNull List<AffectedBlock> affectedBlocksList, World world){
        final List<AffectedBlock> sortedAffectedBlocks = new ArrayList<>(affectedBlocksList);
        final List<BlockPos> affectedBlocksAsPositions = sortedAffectedBlocks.stream().map(AffectedBlock::getBlockPos).collect(Collectors.toList());
        final int centerX = getCenterXCoordinate(affectedBlocksAsPositions);
        final int centerZ = getCenterZCoordinate(affectedBlocksAsPositions);
        final Comparator<AffectedBlock> distanceToCenterComparator = Comparator.comparingInt(affectedBlock -> (int) -(Math.round(Math.pow(affectedBlock.getBlockPos().getX() - centerX, 2) + Math.pow(affectedBlock.getBlockPos().getZ() - centerZ, 2))));
        sortedAffectedBlocks.sort(distanceToCenterComparator);
        final Comparator<AffectedBlock> yLevelComparator = Comparator.comparingInt(affectedBlock -> affectedBlock.getBlockPos().getY());
        sortedAffectedBlocks.sort(yLevelComparator);
        final Comparator<AffectedBlock> transparencyComparator = (affectedBlock1, affectedBlock2) -> {
            boolean isAffectedBlock1Transparent = affectedBlock1.getBlockState().isTransparent(world, affectedBlock1.getBlockPos());
            boolean isAffectedBlock2Transparent = affectedBlock2.getBlockState().isTransparent(world, affectedBlock2.getBlockPos());
            return Boolean.compare(isAffectedBlock1Transparent, isAffectedBlock2Transparent);
        };
        sortedAffectedBlocks.sort(transparencyComparator);
        return sortedAffectedBlocks;
    }

    static int getCenterXCoordinate(List<BlockPos> affectedCoordinates){
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

    static int getCenterYCoordinate(List<BlockPos> affectedCoordinates){
        int maxY = affectedCoordinates.stream()
                .mapToInt(Vec3i::getY)
                .max()
                .orElse(0);
        int minY = affectedCoordinates.stream()
                .mapToInt(Vec3i::getY)
                .min()
                .orElse(0);
        return (maxY + minY)/2;
    }

    static int getCenterZCoordinate(List<BlockPos> affectedCoordinates){
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

    static int getMaxExplosionRadius(List<BlockPos> affectedCoordinates){
        final int[] radii = new int[3];
        final int maxX = affectedCoordinates.stream()
                .mapToInt(Vec3i::getX)
                .max()
                .orElse(0);
        final int minX = affectedCoordinates.stream()
                .mapToInt(Vec3i::getX)
                .min()
                .orElse(0);
        radii[0] = (maxX - minX)/2;

        final int maxY = affectedCoordinates.stream()
                .mapToInt(Vec3i::getY)
                .max()
                .orElse(0);
        final int minY = affectedCoordinates.stream()
                .mapToInt(Vec3i::getY)
                .min()
                .orElse(0);
        radii[1] = (maxY - minY)/2;

        final int maxZ = affectedCoordinates.stream()
                .mapToInt(Vec3i::getZ)
                .max()
                .orElse(0);
        final int minZ = affectedCoordinates.stream()
                .mapToInt(Vec3i::getZ)
                .min()
                .orElse(0);
        radii[2] = (maxZ - minZ)/2;

        return Arrays.stream(radii).max().orElse(0);
    }

    public static boolean shouldPlayBlockPlacementSound(World world, BlockState state) {
         final boolean blockPlacementSoundEffect = ConfigUtils.getSettingValue(ConfigSettings.BLOCK_PLACEMENT_SOUND_EFFECT.getSettingLocation(), BooleanSetting.class);
         return !world.isClient && !state.isAir() && blockPlacementSoundEffect;
    }

}

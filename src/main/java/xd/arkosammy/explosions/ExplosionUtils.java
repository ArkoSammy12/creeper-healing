package xd.arkosammy.explosions;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import xd.arkosammy.configuration.tables.PreferencesConfig;
import xd.arkosammy.handlers.ExplosionListHandler;
import java.util.*;
import java.util.stream.Collectors;

public final class ExplosionUtils {

    private ExplosionUtils(){}

    /**
     * Pushes players upwards to prevent them from being stuck in explosion-created holes.
     *
     * @param world       The world where the explosion occurred.
     * @param pos         The position of the explosion.
     * @param isTallBlock Indicates whether the block is tall (e.g., scaffolding).
     */
     public static void pushPlayersUpwards(World world, BlockPos pos, boolean isTallBlock) {
         int amountToPush = isTallBlock ? 2 : 1;
         for(Entity entity : world.getEntitiesByClass(LivingEntity.class, new Box(pos), Entity::isAlive)){
            if(isAboveBlockFree(world, pos, entity, amountToPush)) {
                entity.refreshPositionAfterTeleport(entity.getPos().withAxis(Direction.Axis.Y, entity.getBlockY() + amountToPush));
            }
        }
    }

    private static boolean isAboveBlockFree(World world, BlockPos pos, Entity entity, int amountToPush){
        for(int i = pos.getY(); i < pos.offset(Direction.Axis.Y, (int) Math.ceil(entity.getStandingEyeHeight())).getY(); i++){
            BlockPos currentPos = pos.withY(i + amountToPush);
            if(world.getBlockState(currentPos).isSolidBlock(world, currentPos))
                return false;
        }
        return true;
    }

    /*
    Obtain colliding explosions by filtering out explosions that have already started healing.
    An explosion collides with another if the distance between their centers is less than or equal to the sum of their radii
     */
    public static Set<ExplosionEvent> getCollidingWaitingExplosions(List<BlockPos> affectedBlockPosList){
        Set<ExplosionEvent> collidingExplosions = new LinkedHashSet<>();
        BlockPos centerOfNewExplosion = new BlockPos(calculateMidXCoordinate(affectedBlockPosList), calculateMidYCoordinate(affectedBlockPosList), calculateMidZCoordinate(affectedBlockPosList));
        int newExplosionAverageRadius = getMaxRadius(affectedBlockPosList);
        for(ExplosionEvent explosionEvent : ExplosionListHandler.getExplosionEventList()){
             if(explosionEvent.getExplosionTimer() > 0){
                 BlockPos centerOfCurrentExplosion = new BlockPos(calculateMidXCoordinate(explosionEvent.getAffectedBlocksList().stream().map(AffectedBlock::getPos).collect(Collectors.toList())), calculateMidYCoordinate(explosionEvent.getAffectedBlocksList().stream().map(AffectedBlock::getPos).collect(Collectors.toList())), calculateMidZCoordinate(explosionEvent.getAffectedBlocksList().stream().map(AffectedBlock::getPos).collect(Collectors.toList())));
                 int currentExplosionAverageRadius = getMaxRadius(explosionEvent.getAffectedBlocksList().stream().map(AffectedBlock::getPos).collect(Collectors.toList()));
                 //Floor the distance for best chance of distance coinciding with the sum of the radii
                 if(Math.floor(Math.sqrt(centerOfNewExplosion.getSquaredDistance(centerOfCurrentExplosion))) <= newExplosionAverageRadius + currentExplosionAverageRadius){
                     collidingExplosions.add(explosionEvent);
                 }
             }
         }
         return collidingExplosions;
    }

     public static @NotNull List<AffectedBlock> sortAffectedBlocksList(@NotNull List<AffectedBlock> affectedBlocksList, MinecraftServer server){

        List<AffectedBlock> sortedAffectedBlocks = new ArrayList<>(affectedBlocksList);

        int centerX = calculateMidXCoordinate(affectedBlocksList.stream().map(AffectedBlock::getPos).collect(Collectors.toList()));
        int centerZ = calculateMidZCoordinate(affectedBlocksList.stream().map(AffectedBlock::getPos).collect(Collectors.toList()));

         //Sort by distance to the center of the explosion
         Comparator<AffectedBlock> distanceToCenterComparator = Comparator.comparingInt(affectedBlock -> (int) -(Math.round(Math.pow(affectedBlock.getPos().getX() - centerX, 2) + Math.pow(affectedBlock.getPos().getZ() - centerZ, 2))));
         sortedAffectedBlocks.sort(distanceToCenterComparator);

         //Sort by Y level
         Comparator<AffectedBlock> yLevelComparator = Comparator.comparingInt(affectedBlock -> affectedBlock.getPos().getY());
        sortedAffectedBlocks.sort(yLevelComparator);

         //Heal non-transparent blocks first
         Comparator<AffectedBlock> transparencyComparator = (affectedBlock1, affectedBlock2) -> {
            boolean isBlockInfo1Transparent = affectedBlock1.getState().isTransparent(affectedBlock1.getWorld(server), affectedBlock1.getPos());
            boolean isBlockInfo2Transparent = affectedBlock2.getState().isTransparent(affectedBlock2.getWorld(server), affectedBlock2.getPos());
            return Boolean.compare(isBlockInfo1Transparent, isBlockInfo2Transparent);
        };
        sortedAffectedBlocks.sort(transparencyComparator);

        return sortedAffectedBlocks;

    }

    private static int calculateMidXCoordinate(List<BlockPos> affectedCoordinates){
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

    private static int calculateMidYCoordinate(List<BlockPos> affectedCoordinates){

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

    private static int calculateMidZCoordinate(List<BlockPos> affectedCoordinates){
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

    private static int getMaxRadius(List<BlockPos> affectedCoordinates){

        int[] radii = new int[3];

        int maxX = affectedCoordinates.stream()
                .mapToInt(Vec3i::getX)
                .max()
                .orElse(0);
        int minX = affectedCoordinates.stream()
                .mapToInt(Vec3i::getX)
                .min()
                .orElse(0);
        radii[0] = (maxX - minX)/2;

        int maxY = affectedCoordinates.stream()
                .mapToInt(Vec3i::getY)
                .max()
                .orElse(0);
        int minY = affectedCoordinates.stream()
                .mapToInt(Vec3i::getY)
                .min()
                .orElse(0);
        radii[1] = (maxY - minY)/2;

        int maxZ = affectedCoordinates.stream()
                .mapToInt(Vec3i::getZ)
                .max()
                .orElse(0);
        int minZ = affectedCoordinates.stream()
                .mapToInt(Vec3i::getZ)
                .min()
                .orElse(0);
        radii[2] = (maxZ - minZ)/2;

        return Arrays.stream(radii).max().orElse(0);

    }

    public static boolean shouldPlaySound(World world, BlockState state) {
        return !world.isClient && !state.isAir() && PreferencesConfig.getBlockPlacementSoundEffect();
    }

    public static boolean shouldPlaceBlock(@NotNull World world, BlockPos pos){

        if(world.isAir(pos)) return true;

        else if(world.getBlockState(pos).getFluidState().getFluid().equals(Fluids.FLOWING_WATER) && PreferencesConfig.getHealOnFlowingWater()) return true;

        else return world.getBlockState(pos).getFluidState().getFluid().equals(Fluids.FLOWING_LAVA) && PreferencesConfig.getHealOnFlowingLava();


    }

    public static boolean shouldPlaceDoubleBlock(@NotNull World world, BlockPos firstHalfPos, BlockPos secondHalfPos){

        //Place both halves if both halves' coordinates are empty
        if(world.isAir(firstHalfPos) && world.isAir(secondHalfPos)) {

            return true;

        } else if (((world.getBlockState(firstHalfPos).getFluidState().getFluid().equals(Fluids.FLOWING_WATER) && world.isAir(secondHalfPos))
                || (world.isAir(firstHalfPos) && world.getBlockState(secondHalfPos).getFluidState().getFluid().equals(Fluids.FLOWING_WATER))
                || (world.getBlockState(firstHalfPos).getFluidState().getFluid().equals(Fluids.FLOWING_WATER) && world.getBlockState(secondHalfPos).getFluidState().getFluid().equals(Fluids.FLOWING_WATER)))
                && PreferencesConfig.getHealOnFlowingWater()){

            return true;

        } else return (((world.getBlockState(firstHalfPos).getFluidState().getFluid().equals(Fluids.FLOWING_LAVA) && world.isAir(secondHalfPos))
                || (world.isAir(firstHalfPos) && world.getBlockState(secondHalfPos).getFluidState().getFluid().equals(Fluids.FLOWING_LAVA))
                || (world.getBlockState(firstHalfPos).getFluidState().getFluid().equals(Fluids.FLOWING_LAVA) && world.getBlockState(secondHalfPos).getFluidState().getFluid().equals(Fluids.FLOWING_LAVA)))
                && PreferencesConfig.getHealOnFlowingLava());

    }

}

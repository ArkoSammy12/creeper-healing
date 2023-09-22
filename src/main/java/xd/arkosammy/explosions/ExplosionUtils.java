package xd.arkosammy.explosions;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import xd.arkosammy.configuration.tables.PreferencesConfig;
import xd.arkosammy.handlers.ExplosionListHandler;
import java.util.*;

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
    An explosion collides with another one if the list of affected coordinates of the explosion that just happened
    matches with any of the coordinates of the affected blocklist of any waiting explosion.
    We can do this because the affected coordinates of an explosion can include air blocks,
    which means that these coordinates have the possibility of extending beyond the actual range of blocks that will be blown up.
     */
    public static Set<ExplosionEvent> getCollidingWaitingExplosions(List<BlockPos> affectedBlockPosList){
         Set<ExplosionEvent> collidingExplosions = new LinkedHashSet<>();
         for(ExplosionEvent explosionEvent : ExplosionListHandler.getExplosionEventList()){
             if(explosionEvent.getExplosionTimer() > 0){
                 for(AffectedBlock affectedBlock : explosionEvent.getAffectedBlocksList()){
                     if(affectedBlockPosList.contains(affectedBlock.getPos())){
                         collidingExplosions.add(explosionEvent);
                     }
                 }
             }
         }
         return collidingExplosions;
    }

     public static @NotNull List<AffectedBlock> sortAffectedBlocksList(@NotNull List<AffectedBlock> affectedBlocksList, MinecraftServer server){

        List<AffectedBlock> sortedAffectedBlocks = new ArrayList<>(affectedBlocksList);

        int centerX = calculateMidXCoordinate(affectedBlocksList);
        int centerZ = calculateMidZCoordinate(affectedBlocksList);

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

    private static int calculateMidXCoordinate(List<AffectedBlock> affectedBlocks){
        int maxX = affectedBlocks.stream()
                .mapToInt(affectedBlock -> affectedBlock.getPos().getX())
                .max()
                .orElse(0);
        int minX = affectedBlocks.stream()
                .mapToInt(affectedBlock -> affectedBlock.getPos().getX())
                .min()
                .orElse(0);
        return (maxX + minX) / 2;
    }

    private static int calculateMidZCoordinate(List<AffectedBlock> affectedBlocks){
        int maxX = affectedBlocks.stream()
                .mapToInt(affectedBlock -> affectedBlock.getPos().getZ())
                .max()
                .orElse(0);
        int minX = affectedBlocks.stream()
                .mapToInt(affectedBlock -> affectedBlock.getPos().getZ())
                .min()
                .orElse(0);
        return (maxX + minX) / 2;
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

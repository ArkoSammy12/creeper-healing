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

            if(world.getBlockState(currentPos).isSolidBlock(world, currentPos)) return false;

        }

        return true;

    }

    public static Set<ExplosionEvent> compareWithWaitingExplosions(List<BlockPos> affectedBlockPos){

         Set<ExplosionEvent> matchedExplosions = new LinkedHashSet<>();

         for(ExplosionEvent explosionEvent : ExplosionListHandler.getExplosionEventList()){

             if(explosionEvent.getExplosionTimer() > 0){

                 for(AffectedBlock affectedBlock : explosionEvent.getAffectedBlocksList()){

                     if(affectedBlockPos.contains(affectedBlock.getPos())){

                         matchedExplosions.add(explosionEvent);


                     }

                 }

             }

         }

         return matchedExplosions;

    }

    /**
     * Sorts a list of affected blocks based on various criteria to optimize healing order.
     *
     * @param affectedBlocksList The list of affected blocks to be sorted.
     * @param server             The Minecraft server instance.
     * @return The sorted list of affected blocks.
     */
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

    /**
     * Checks if a sound should be played when placing a block.
     *
     * @param world The world where the block is placed.
     * @param state The block state being placed.
     * @return True if a sound should be played; otherwise, false.
     */
    public static boolean shouldPlaySound(World world, BlockState state) {
        return !world.isClient && !state.isAir() && PreferencesConfig.getBlockPlacementSoundEffect();
    }

    /**
     * Checks if a block should be placed at the specified position.
     *
     * @param world The world where the check is performed.
     * @param pos   The position to check.
     * @return True if a block should be placed at the position; otherwise, false.
     */
    public static boolean shouldPlaceBlock(@NotNull World world, BlockPos pos){

        if(world.isAir(pos)) return true;

        else if(world.getBlockState(pos).getFluidState().getFluid().equals(Fluids.FLOWING_WATER) && PreferencesConfig.getHealOnFlowingWater()) return true;

        else return world.getBlockState(pos).getFluidState().getFluid().equals(Fluids.FLOWING_LAVA) && PreferencesConfig.getHealOnFlowingLava();


    }

    /**
     * Checks if a double block (e.g., doors, beds) should be placed at the specified positions.
     *
     * @param world        The world where the check is performed.
     * @param firstHalfPos The position of the first half of the double block.
     * @param secondHalfPos The position of the second half of the double block.
     * @return True if the double block should be placed; otherwise, false.
     */

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

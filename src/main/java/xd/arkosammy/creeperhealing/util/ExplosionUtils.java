package xd.arkosammy.creeperhealing.util;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
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
import xd.arkosammy.monkeyconfig.settings.StringSetting;
import xd.arkosammy.monkeyconfig.settings.list.StringListSetting;

import java.util.*;
import java.util.function.Function;
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

    public static List<BlockPos> filterPositionsToHeal(Collection<BlockPos> positions, Function<BlockPos, BlockState> positionToStateMapper) {
        List<BlockPos> affectedPositions = new ArrayList<>();
        boolean whitelistEnabled = ConfigUtils.getSettingValue(ConfigSettings.ENABLE_WHITELIST.getSettingLocation(), BooleanSetting.class);
        List<? extends String> whitelist = ConfigUtils.getSettingValue(ConfigSettings.WHITELIST.getSettingLocation(), StringListSetting.class);
        for (BlockPos affectedPosition : positions) {
            // Hardcoded exception. Place before all logic
            BlockState affectedState = positionToStateMapper.apply(affectedPosition);
            if (ExcludedBlocks.isExcluded(affectedState)) {
                continue;
            }
            boolean stateCannotHeal = affectedState.isAir() || affectedState.isOf(Blocks.TNT) || affectedState.isIn(BlockTags.FIRE);
            if (stateCannotHeal) {
                continue;
            }
            String affectedBlockIdentifier = Registries.BLOCK.getId(affectedState.getBlock()).toString();
            boolean whitelistContainsIdentifier = whitelist.contains(affectedBlockIdentifier);
            if (!whitelistEnabled || whitelistContainsIdentifier) {
                affectedPositions.add(affectedPosition);
            }
        }
        return affectedPositions;
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

    // Recursively find indirectly affected positions connected to the main affected positions.
    // Start from the "edge" of the blast radius and visit each neighbor until a neighbor has no
    // non-visited positions, the neighbor is surrounded by air, or we hit the max recursion depth.
    public static List<BlockPos> getIndirectlyAffectedBlocks(List<BlockPos> affectedPositions, World world) {

        // Only consider block positions with adjacent non-affected positions
        List<BlockPos> edgeAffectedPositions = new ArrayList<>();
        for (BlockPos vanillaAffectedPosition : affectedPositions) {
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
                if (!affectedPositions.contains(neighborPos)) {
                    edgeAffectedPositions.add(vanillaAffectedPosition);
                    break;
                }
            }
        }

        // Pass in a custom WorldView implementation that always returns an air BlockState when calling
        // WorldView#getBlockState on it. This guarantees that further checks with BlockState#canPlaceAt
        // are done in what will look like an empty world
        EmptyWorld emptyWorld = new EmptyWorld(world);
        Set<BlockPos> newPositions = new HashSet<>();
        for (BlockPos filteredPosition : edgeAffectedPositions) {
            checkNeighbors(512, filteredPosition, affectedPositions, newPositions, world, emptyWorld);
        }
        return new ArrayList<>(ExplosionUtils.filterPositionsToHeal(newPositions, world::getBlockState));
    }

    public static void checkNeighbors(int maxCheckDepth, BlockPos currentPosition, List<BlockPos> affectedPositions, Set<BlockPos> newPositions, World normalWorld, EmptyWorld emptyWorld) {
        if (maxCheckDepth <= 0) {
            return;
        }
        for (Direction neighborDirection : Direction.values()) {
            BlockPos neighborPos = currentPosition.offset(neighborDirection);
            BlockState neighborState = normalWorld.getBlockState(neighborPos);

            // If the block cannot be placed at an empty position also surrounded by air, then we assume
            // the block needs a supporting block to be placed.
            if (neighborState.isAir() || neighborState.canPlaceAt(emptyWorld, neighborPos) || affectedPositions.contains(neighborPos)) {
                continue;
            }
            if (newPositions.add(neighborPos)) {
                checkNeighbors(maxCheckDepth - 1, neighborPos, affectedPositions, newPositions, normalWorld, emptyWorld);
            }
        }
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
        String healingParticleTypeSetting = ConfigUtils.getSettingValue(ConfigSettings.HEALING_PARTICLE_TYPE.getSettingLocation(), StringSetting.class);
        Identifier healingParticleIdentifier = Identifier.tryParse(healingParticleTypeSetting);
        SimpleParticleType particleType = ParticleTypes.CLOUD;
        if (healingParticleIdentifier != null) {
            ParticleType<?> particleTypeFromRegistry = Registries.PARTICLE_TYPE.get(healingParticleIdentifier);
            if (particleTypeFromRegistry instanceof SimpleParticleType simpleParticleType) {
                particleType = simpleParticleType;
            }
        }
        serverWorld.spawnParticles(particleType, blockPos.getX(), blockPos.getY() + 2, blockPos.getZ(), 1, 0, 1, 0, 0.001);
    }

}

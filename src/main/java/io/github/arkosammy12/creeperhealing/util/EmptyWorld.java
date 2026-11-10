package io.github.arkosammy12.creeperhealing.util;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Holder;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.attribute.EnvironmentAttributeReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * An instance of {@link  LevelReader} backed by another instance.
 * This class has the explicit purpose of simulating an empty world to be used
 * when calling {@link  BlockState#canSurvive} to check if the block needs a supporting block.
 * This way we guarantee that all positions read from this instance correspond to empty block states.
 */
public class EmptyWorld implements LevelReader {

    private final Level world;

    public EmptyWorld(Level world) {
        this.world = world;
    }

    @Nullable
    @Override
    public ChunkAccess getChunk(int chunkX, int chunkZ, ChunkStatus leastStatus, boolean create) {

        return new EmptyLevelChunk(this.world, new ChunkPos(chunkX, chunkZ), world.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.THE_VOID));
    }

    @Override
    public boolean hasChunk(int chunkX, int chunkZ) {
        return false;
    }

    @Override
    public int getHeight(Heightmap.Types heightmap, int x, int z) {
        return this.world.getHeight(heightmap, x, z);
    }

    @Override
    public int getSkyDarken() {
        return 15;
    }

    @Override
    public BiomeManager getBiomeManager() {
        return this.world.getBiomeManager();
    }

    @Override
    public Holder<Biome> getUncachedNoiseBiome(int biomeX, int biomeY, int biomeZ) {
        return world.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.THE_VOID);
    }

    @Override
    public boolean isClientSide() {
        return false;
    }

    @Override
    public int getSeaLevel() {
        return this.world.getSeaLevel();
    }

    @Override
    public DimensionType dimensionType() {
        return this.world.dimensionType();
    }

    @Override
    public RegistryAccess registryAccess() {
        return this.world.registryAccess();
    }

    @Override
    public FeatureFlagSet enabledFeatures() {
        return this.world.enabledFeatures();
    }

    @Override
    public EnvironmentAttributeReader environmentAttributes() {
        return this.world.environmentAttributes();
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return this.world.getLightEngine();
    }

    @Override
    public WorldBorder getWorldBorder() {
        return this.world.getWorldBorder();
    }

    @Override
    public List<VoxelShape> getEntityCollisions(@Nullable Entity entity, AABB box) {
        return new ArrayList<>();
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        return null;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return Fluids.EMPTY.defaultFluidState();
    }
}

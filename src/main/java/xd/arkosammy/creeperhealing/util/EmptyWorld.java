package xd.arkosammy.creeperhealing.util;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * An instance of {@link  WorldView} backed by another instance.
 * This class has the explicit purpose of simulating an empty world to be used
 * when calling {@link  BlockState#canPlaceAt} to check if the block needs a supporting block.
 * This way we guarantee that all positions read from this instance correspond to empty block states.
 */
public class EmptyWorld implements WorldView {

    private final World world;

    public EmptyWorld(World world) {
        this.world = world;
    }

    @Nullable
    @Override
    public Chunk getChunk(int chunkX, int chunkZ, ChunkStatus leastStatus, boolean create) {
        return new EmptyChunk(this.world, new ChunkPos(chunkX, chunkZ), this.world.getRegistryManager().get(RegistryKeys.BIOME).entryOf(BiomeKeys.THE_VOID));
    }

    @Override
    public boolean isChunkLoaded(int chunkX, int chunkZ) {
        return true;
    }

    @Override
    public int getTopY(Heightmap.Type heightmap, int x, int z) {
        return this.world.getTopY(heightmap, x, z);
    }

    @Override
    public int getAmbientDarkness() {
        return 15;
    }

    @Override
    public BiomeAccess getBiomeAccess() {
        return this.world.getBiomeAccess();
    }

    @Override
    public RegistryEntry<Biome> getGeneratorStoredBiome(int biomeX, int biomeY, int biomeZ) {
        return this.world.getRegistryManager().get(RegistryKeys.BIOME).entryOf(BiomeKeys.THE_VOID);
    }

    @Override
    public boolean isClient() {
        return false;
    }

    @Override
    public int getSeaLevel() {
        return this.world.getSeaLevel();
    }

    @Override
    public DimensionType getDimension() {
        return this.world.getDimension();
    }

    @Override
    public DynamicRegistryManager getRegistryManager() {
        return this.world.getRegistryManager();
    }

    @Override
    public FeatureSet getEnabledFeatures() {
        return this.world.getEnabledFeatures();
    }

    @Override
    public float getBrightness(Direction direction, boolean shaded) {
        return 0;
    }

    @Override
    public LightingProvider getLightingProvider() {
        return this.world.getLightingProvider();
    }

    @Override
    public WorldBorder getWorldBorder() {
        return this.world.getWorldBorder();
    }

    @Override
    public List<VoxelShape> getEntityCollisions(@Nullable Entity entity, Box box) {
        return new ArrayList<>();
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        return null;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return Blocks.AIR.getDefaultState();
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return Fluids.EMPTY.getDefaultState();
    }
}

package xd.arkosammy.creeperhealing.util;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.Heightmap;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * An instance of {@link  WorldView} backed by another instance.
 * This class has the explicit purpose of simulating an empty world to be used
 * when calling {@link  BlockState#canPlaceAt} to check if the block needs a supporting block.
 * This way we guarantee that all positions read from this instance correspond to empty block states.
 */
public class EmptyWorld implements WorldView {

    private final WorldView worldView;

    public EmptyWorld(WorldView worldView) {
        this.worldView = worldView;
    }

    /**
     * @return <b>null</b> always to prevent block states from being read by the returned chunks of the backing {@link WorldView} instance.
     */
    @Nullable
    @Override
    public Chunk getChunk(int chunkX, int chunkZ, ChunkStatus leastStatus, boolean create) {
        return null;
    }

    @Override
    public boolean isChunkLoaded(int chunkX, int chunkZ) {
        return this.worldView.isChunkLoaded(chunkX, chunkZ);
    }

    @Override
    public int getTopY(Heightmap.Type heightmap, int x, int z) {
        return this.worldView.getTopY(heightmap, x, z);
    }

    @Override
    public int getAmbientDarkness() {
        return this.worldView.getAmbientDarkness();
    }

    @Override
    public BiomeAccess getBiomeAccess() {
        return this.worldView.getBiomeAccess();
    }

    @Override
    public RegistryEntry<Biome> getGeneratorStoredBiome(int biomeX, int biomeY, int biomeZ) {
        return this.worldView.getGeneratorStoredBiome(biomeX, biomeY, biomeZ);
    }

    @Override
    public boolean isClient() {
        return this.worldView.isClient();
    }

    @Override
    public int getSeaLevel() {
        return this.worldView.getSeaLevel();
    }

    @Override
    public DimensionType getDimension() {
        return this.worldView.getDimension();
    }

    @Override
    public DynamicRegistryManager getRegistryManager() {
        return this.worldView.getRegistryManager();
    }

    @Override
    public FeatureSet getEnabledFeatures() {
        return this.worldView.getEnabledFeatures();
    }

    @Override
    public float getBrightness(Direction direction, boolean shaded) {
        return this.worldView.getBrightness(direction, shaded);
    }

    @Override
    public LightingProvider getLightingProvider() {
        return this.worldView.getLightingProvider();
    }

    @Override
    public WorldBorder getWorldBorder() {
        return this.worldView.getWorldBorder();
    }

    @Override
    public List<VoxelShape> getEntityCollisions(@Nullable Entity entity, Box box) {
        return this.worldView.getEntityCollisions(entity, box);
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

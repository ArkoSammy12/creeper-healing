package xd.arkosammy.creeperhealing.explosions.ducks;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;
import java.util.Set;

public interface ExplosionAccessor {

    Set<BlockPos> creeperhealing$getCalculatedBlockPositions();

    World creeperhealing$getWorld();

    DamageSource creeperhealing$getDamageSource();

    boolean creeperhealing$willBeHealed();

    Map<BlockPos, Pair<BlockState, BlockEntity>> creeperhealing$getSavedStatesAndEntities();

}

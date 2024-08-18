package xd.arkosammy.creeperhealing.support.trickster.tricks;

import dev.enjarai.trickster.spell.Fragment;
import dev.enjarai.trickster.spell.Pattern;
import dev.enjarai.trickster.spell.SpellContext;
import dev.enjarai.trickster.spell.fragment.FragmentType;
import dev.enjarai.trickster.spell.fragment.VectorFragment;
import dev.enjarai.trickster.spell.fragment.VoidFragment;
import dev.enjarai.trickster.spell.trick.Trick;
import dev.enjarai.trickster.spell.trick.blunder.BlunderException;
import net.minecraft.util.math.BlockPos;
import xd.arkosammy.creeperhealing.CreeperHealing;
import xd.arkosammy.creeperhealing.blocks.SingleAffectedBlock;
import xd.arkosammy.creeperhealing.explosions.AbstractExplosionEvent;

import java.util.List;

public class HealExplosionTrick extends Trick {

    public HealExplosionTrick() {
        super(Pattern.of(3, 1, 5, 7, 3, 6, 7, 4, 3, 0, 4, 8, 7));
    }

    @Override
    public Fragment activate(SpellContext ctx, List<Fragment> fragments) throws BlunderException {
        VectorFragment vectorFragment = expectInput(fragments, FragmentType.VECTOR, 0);
        float manaCost = (float) CreeperHealing.EXPLOSION_MANAGER.getExplosionEvents().count() / 10;
        ctx.useMana(this, manaCost);
        BlockPos spellPosition = vectorFragment.toBlockPos();
        CreeperHealing.EXPLOSION_MANAGER.getExplosionEvents().forEach(explosionEvent -> {
            boolean spellHitExplosion = explosionEvent.getAffectedBlocks().anyMatch(affectedBlock -> affectedBlock.getBlockPos().equals(spellPosition));
            if (spellHitExplosion && explosionEvent instanceof AbstractExplosionEvent abstractExplosionEvent) {
                abstractExplosionEvent.setHealTimer(1);
                abstractExplosionEvent.getAffectedBlocks().forEach(affectedBlock -> {
                    if (affectedBlock instanceof SingleAffectedBlock singleAffectedBlock) {
                        singleAffectedBlock.setTimer(1);
                    }
                });
            }
        });
        return VoidFragment.INSTANCE;
    }
}

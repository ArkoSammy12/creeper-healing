package xd.arkosammy.runnables;

import net.minecraft.world.World;
import xd.arkosammy.util.BlockInfo;
import xd.arkosammy.handlers.ExplosionHealerHandler;
import java.util.ArrayList;

public class ExplosionQueueRunnable implements Runnable{

    private World world;
    private ArrayList<BlockInfo> blockInfoList;

    //Constructor of ExplosionQueueRunnable for the .schedule() function, to schedule a new TickSchedule event
    public ExplosionQueueRunnable(World world, ArrayList<BlockInfo> blockInfoList) {

        this.world = world;
        this.blockInfoList = blockInfoList;

    }

    //The run() method of a Runnable gets called when the executorService says it is time to do so

    @Override
    public void run() {

        ExplosionHealerHandler.handleBlockList(world, blockInfoList);

    }
}



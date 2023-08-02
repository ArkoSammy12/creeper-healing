package xd.arkosammy.events;

import xd.arkosammy.util.BlockInfo;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

//Our CreeperExplosion events will each contain a list of the BlockInfos necessary to restore blocks blown up by creepers
public class CreeperExplosionEvent {

    private static ConcurrentLinkedQueue<CreeperExplosionEvent> explosionEvents = new ConcurrentLinkedQueue<>();
    private ArrayList<BlockInfo> blockList;

    public CreeperExplosionEvent(ArrayList<BlockInfo> blockList){

        setBlockList(blockList);

    }

    public void setBlockList(ArrayList<BlockInfo> blockList){

        this.blockList = blockList;

    }

    public ArrayList<BlockInfo> getBlockList(){

        return this.blockList;

    }

    public static ConcurrentLinkedQueue<CreeperExplosionEvent> getExplosionEvents(){

        return explosionEvents;

    }


}

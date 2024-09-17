package DataUtils;

import RedisReplication.ReplicaManager;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class MasterWriteCommands {
    private static BlockingQueue<String> blockingQueue = new LinkedBlockingDeque<>();

    public static void addWriteCommands(String command){
        blockingQueue.add(command);
        ReplicaManager.propagateToAllReplicas(command);
    }

    public static void removeWriteCommands(String command){
        blockingQueue.remove(command);
    }
    public static String getWriteCommand(){
        try {
            return blockingQueue.poll();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
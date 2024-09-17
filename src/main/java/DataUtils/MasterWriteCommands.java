package DataUtils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class MasterWriteCommands {
    private static BlockingQueue<String> blockingQueue = new LinkedBlockingDeque<>();

    public static void addWriteCommands(String command){
        blockingQueue.add(command);
    }

    public static void removeWriteCommands(String command){
        blockingQueue.remove(command);
    }
    public static String getWriteCommand(){
        try {
            return blockingQueue.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

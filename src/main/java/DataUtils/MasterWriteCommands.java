package DataUtils;

import RedisReplication.ReplicaManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class MasterWriteCommands {
    private static List<String> writeCommands = Collections.synchronizedList(new ArrayList<>());

    // Add a write command to the list and propagate
    public static synchronized void addWriteCommand(String command) {
        System.out.println("Adding write commands to a list and propogating to to the replicas");
        writeCommands.add(command);
        ReplicaManager.propagateToAllReplicas(command); // propagate to all replicas as soon as it's added
    }

    // Get the list of commands (if needed for manual processing)
    public static synchronized List<String> getWriteCommands() {
        System.out.println("Getting write commands from a list");
        return new ArrayList<>(writeCommands);
    }

    // Clear commands after processing (if needed)
    public static synchronized void clearWriteCommands() {
        writeCommands.clear();
    }
}

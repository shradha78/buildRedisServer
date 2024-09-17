package RedisReplication;

import RedisServer.ClientSession;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.*;

public class ReplicaManager {
    private static List<OutputStream> replicaStream = Collections.synchronizedList(new ArrayList<>());

    // Add a new replica to the list
    public static synchronized void addReplica(OutputStream replicaStream) {
        ReplicaManager.replicaStream.add(replicaStream);
    }

    // Propagate command to all connected replicas
    public static synchronized void propagateToAllReplicas(String command) {
        System.out.println("Propogating to all replicas : " + command);
        for (OutputStream replica : replicaStream) {
            try {

                replica.write(command.getBytes());
                replica.flush();
            } catch (IOException e) {
                System.out.println("Error propagating command to replica: " + e.getMessage());
                // You might want to remove the replica if there are issues
            }
        }
        System.out.println("All replicas propagated");
    }
}

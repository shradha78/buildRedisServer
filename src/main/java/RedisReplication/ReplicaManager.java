package RedisReplication;

import RedisServer.ClientSession;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.*;

public class ReplicaManager {
    private static List<ClientSession> replicaSession = Collections.synchronizedList(new ArrayList<>());

    // Add a new replica to the list
    public static synchronized void addReplica(ClientSession clientSession) {
        replicaSession.add(clientSession);
    }

    // Propagate command to all connected replicas
    public static synchronized void propagateToAllReplicas(String command) {
        for (ClientSession clientSession : replicaSession) {
            try {
                OutputStream replicaStream = clientSession.getOutputStream();
                replicaStream.write(command.getBytes());
                replicaStream.flush();
            } catch (IOException e) {
                System.out.println("Error propagating command to replica: " + e.getMessage());
                // You might want to remove the replica if there are issues
            }
        }
    }
}

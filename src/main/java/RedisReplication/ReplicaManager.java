package RedisReplication;

import RedisServer.ClientSession;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class ReplicaManager {
    private static Set<ClientSession> replicas = new HashSet<>();

    public static synchronized void addReplica(ClientSession session) {
        replicas.add(session);
    }

    // Remove replica if it disconnects
    public static synchronized void removeReplica(ClientSession session) {
        replicas.remove(session);
    }

    // Get all connected replicas
    public static synchronized Set<ClientSession> getConnectedReplicas() {
        return new HashSet<>(replicas); // return a copy to avoid concurrent modifications
    }

    // Send a command to all replicas
    public static synchronized void propagateToAllReplicas(String command) {
        for (ClientSession replica : replicas) {
            try {
                replica.sendCommand(command);
            } catch (IOException e) {
                System.err.println("Failed to send command to replica: " + replica);
                // Optionally, remove the replica if it's disconnected
                removeReplica(replica);
            }
        }
    }
}

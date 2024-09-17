package RedisReplication;

import java.io.IOException;
import java.net.Socket;

public class RedisSlaveServer {
        private final String masterHost;
        private final int masterPort;
        private final int replicaPort;

        public RedisSlaveServer(String masterHost, int masterPort, int replicaPort) {
            this.masterHost = masterHost;
            this.masterPort = masterPort;
            this.replicaPort = replicaPort;
        }

    public String getMasterHost() {
        return masterHost;
    }

    public int getMasterPort() {
        return masterPort;
    }

    public int getReplicaPort() {
        return replicaPort;
    }

    public void connectToMaster() throws IOException {
            System.out.println("Connecting to master at " + masterHost + ":" + masterPort);
            try (Socket masterSocket = new Socket(masterHost, masterPort)) {
                RedisReplicaHandshake handshake = new RedisReplicaHandshake(masterSocket, replicaPort);
                handshake.startHandshake();
            }
        }
    }

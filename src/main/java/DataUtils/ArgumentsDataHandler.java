package DataUtils;

import RedisReplication.RedisServerConfig;
import RedisReplication.RedisSlaveServer;

import java.io.IOException;

public class ArgumentsDataHandler {

    public static void handleTestArgumentsForConfigurations(String[] args) {
        System.out.println("Handling command line arguments for configuration");
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--dir")) {
                ConfigurationData.addConfigDetails("dir", args[++i]);
            }
            if (args[i].equals("--dbfilename")) {
                ConfigurationData.addConfigDetails("dbfilename", args[++i]);
            }
            if (args[i].equals("--port")) {
                ReplicationDataHandler.setPortToConnect(Integer.parseInt(args[++i]));
            }
            if (args[i].equals("--replicaof")) {
                ReplicationDataHandler.setIsReplica(true);
                String replicaInfo = args[++i];
                String[] parts = replicaInfo.split(" ");
                if (parts.length == 2) {
                    ReplicationDataHandler.setMaster_host(parts[0]);
                    ReplicationDataHandler.setMaster_port(Integer.parseInt(parts[1]));
                    RedisSlaveServer slaveServer = new RedisSlaveServer(ReplicationDataHandler.getMaster_host(),
                                                                        ReplicationDataHandler.getMaster_port(),
                                                                        ReplicationDataHandler.getPortToConnect()
                                                                        );
                    try {
                        slaveServer.connectToMaster();
                    } catch (IOException e) {
                        System.out.println("Failed to connect with Master");
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }
}
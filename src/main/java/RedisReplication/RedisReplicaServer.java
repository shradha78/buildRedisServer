package RedisReplication;

import RedisServer.ClientHandler;
import RedisServer.ClientSession;

import java.io.IOException;
import java.net.Socket;

public class RedisReplicaServer {
    private final String masterHost;
    private final int masterPort;
    private final int replicaPort;
    Socket masterSocket;

    public RedisReplicaServer(String masterHost, int masterPort, int replicaPort) {
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
        try {
             this.masterSocket = new Socket(masterHost, masterPort);
             RedisReplicaHandshake handshake = new RedisReplicaHandshake(masterSocket, replicaPort);
//            new Thread(() ->{
                try {
                    System.out.println("Starting handshake on thread : " + Thread.currentThread().getName());
                    handshake.startHandshake();
                }catch (IOException e){
                    e.printStackTrace();
                    System.out.println("Failed to start handshake.");
                }
//            }).start();

        } catch (IOException e) {
            System.out.println("Failed to connect to master at " + masterHost + ":" + masterPort);
            throw new RuntimeException(e);
        }
    }


    public void initializeSlaveServer(RedisReplicaServer slaveServer) {

        System.out.println("Slave server details : " + slaveServer.getMasterHost() + " " + slaveServer.getMasterPort() + " " + slaveServer.getReplicaPort());
            try {
                slaveServer.connectToMaster();
                System.out.println("Replica server is fully initialized and listening on port " + replicaPort);
                new Thread(() -> {
                    try {
                        System.out.println("Master socket is : "+ masterSocket.getInetAddress() + ":" + masterSocket.getPort());
                        new Thread(new ClientHandler(masterSocket,new ClientSession(masterSocket,true))).start();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
            } catch (IOException e) {
                System.out.println("Failed to connect with Master");
                e.printStackTrace();
            }
        }

}
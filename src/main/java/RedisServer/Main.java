package RedisServer;

import DataUtils.ReplicationDataHandler;
import RedisReplication.RedisReplicaServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;


public class Main {

    public static void main(String[] args) {
        // You can use print statements as follows for debugging, they'll be visible when running tests.
        System.out.println("Logs from your program will appear here!");

        //handling command line arguments
        DataUtils.ArgumentsDataHandler.handleTestArgumentsForConfigurations(args);

        if (DataUtils.ReplicationDataHandler.isIsReplica()) {
            RedisReplicaServer redisReplicaServer = new RedisReplicaServer(
                    DataUtils.ReplicationDataHandler.getMaster_host(),
                    DataUtils.ReplicationDataHandler.getMaster_port(),
                    DataUtils.ReplicationDataHandler.getPortToConnect()
            );
            System.out.println("Replica thread: " + Thread.currentThread().getName());
            new Thread(() ->redisReplicaServer.initializeSlaveServer(redisReplicaServer)).start();
//            try {
//                new Thread(new ClientHandler(redisReplicaServer.getMasterSocket(),new ClientSession(redisReplicaServer.getMasterSocket(),true))).start();
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
        }

        Socket clientSocket = null;

        int port = 6379;
        port = DataUtils.ReplicationDataHandler.isIsReplica() ? ReplicationDataHandler.getPortToConnect() : port;
        listenToPort(clientSocket, port);

    }


    public static void listenToPort(Socket clientSocket, int port)   {

        ServerSocket serverSocket;
        try {
            System.out.println("Listening on port " + port);
            serverSocket = new ServerSocket(port);
            // Since the tester restarts your program quite often, setting SO_REUSEADDR
            // ensures that we don't run into 'Address already in use' errors
            serverSocket.setReuseAddress(true);

            while (true) {
                // Wait for connection from client.
                clientSocket = serverSocket.accept();
                boolean isReplica = DataUtils.ReplicationDataHandler.isIsReplica();
                ClientSession session = new ClientSession(clientSocket,isReplica);
                new Thread(new ClientHandler(clientSocket,session)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IOException: " + e.getMessage());

        } finally {
            try {
                if (clientSocket != null) {
                    System.out.println("Closing client socket");
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
            }
        }
    }

}

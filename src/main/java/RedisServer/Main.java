package RedisServer;

import DataUtils.ReplicationDataHandler;
import RedisCommandExecutor.RedisParser.RedisCommandParser;
import RedisCommandExecutor.RedisParser.RedisProtocolParser;
import RedisReplication.RedisSlaveServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;


public class Main {

    public static void main(String[] args) {
        // You can use print statements as follows for debugging, they'll be visible when running tests.
        System.out.println("Logs from your program will appear here!");
        args = new String[]{"--port", "6380", "--replicaof", "localhost 6379"};
        //handling command line arguments
        DataUtils.ArgumentsDataHandler.handleTestArgumentsForConfigurations(args);

        if (DataUtils.ReplicationDataHandler.isIsReplica()) {
            RedisSlaveServer redisSlaveServer = new RedisSlaveServer(
                    DataUtils.ReplicationDataHandler.getMaster_host(),
                    DataUtils.ReplicationDataHandler.getMaster_port(),
                    DataUtils.ReplicationDataHandler.getPortToConnect()
            );
            System.out.println("Replica thread: " + Thread.currentThread().getName());
            redisSlaveServer.initializeSlaveServer(redisSlaveServer);
        }

        Socket clientSocket = null;

        int port = 6379;

        listenToPort(clientSocket, port);

    }


    private static void listenToPort(Socket clientSocket, int port) {

        ServerSocket serverSocket;
        try {

            port = DataUtils.ReplicationDataHandler.getPortToConnect() != 0  ? ReplicationDataHandler.getPortToConnect() : port;
            System.out.println("Listening on port " + port);
            serverSocket = new ServerSocket(port);
            // Since the tester restarts your program quite often, setting SO_REUSEADDR
            // ensures that we don't run into 'Address already in use' errors
            serverSocket.setReuseAddress(true);

            while (true) {
                // Wait for connection from client.
                clientSocket = serverSocket.accept();
                boolean isReplica = DataUtils.ReplicationDataHandler.isIsReplica();

                final Socket finalClientSocket = clientSocket;
                final boolean finalIsReplica = isReplica;
                ClientSession session = new ClientSession(finalClientSocket,finalIsReplica);
                new Thread(new ClientHandler(finalClientSocket,session)).start();
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

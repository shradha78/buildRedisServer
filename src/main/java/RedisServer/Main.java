package RedisServer;

import DataUtils.ReplicationDataHandler;
import RedisCommandExecutor.RedisCommands.CommandFactory;
import RedisCommandExecutor.RedisCommands.IRedisCommandHandler;
import RedisCommandExecutor.RedisParser.RedisCommand;
import RedisCommandExecutor.RedisParser.RedisCommandParser;
import RedisCommandExecutor.RedisParser.RedisProtocolParser;
import RedisReplication.RedisSlaveServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static DataUtils.CommandsQueue.queueCommands;

import static RedisResponses.ShortParsedResponses.sendErrorResponse;


public class Main {
    private static RedisCommandParser redisCommandParser;
    private static RedisProtocolParser redisProtocolParser;

    public static CountDownLatch latch = new CountDownLatch(1);



    public static void main(String[] args) {
        // You can use print statements as follows for debugging, they'll be visible when running tests.
        System.out.println("Logs from your program will appear here!");

        //handling command line arguments
        DataUtils.ArgumentsDataHandler.handleTestArgumentsForConfigurations(args);

        if (DataUtils.ReplicationDataHandler.isIsReplica()) {
            RedisSlaveServer redisSlaveServer = new RedisSlaveServer(
                                DataUtils.ReplicationDataHandler.getMaster_host(),
                                DataUtils.ReplicationDataHandler.getMaster_port(),
                                DataUtils.ReplicationDataHandler.getPortToConnect()
                        );
            new Thread(()->{
            redisSlaveServer.initializeSlaveServer(redisSlaveServer);
            }).start();
        }

        Socket clientSocket = null;

        int port = 6379;

        listenToPort(clientSocket, port);

    }


    private static void listenToPort(Socket clientSocket, int port) {

        ServerSocket serverSocket;

        redisCommandParser = new RedisCommandParser();

        redisProtocolParser = new RedisProtocolParser();

        try {
            port = DataUtils.ReplicationDataHandler.getPortToConnect() != 0  ? ReplicationDataHandler.getPortToConnect() : port;

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

                new Thread(() -> {
                    try {

                        System.out.println("Connected with Client : " + finalClientSocket.getPort() );
                        System.out.println("Is client a slave : " + finalIsReplica);

                        ClientSession session = new ClientSession(finalClientSocket,finalIsReplica);

                        handlingClientCommands(finalClientSocket, session);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();

            System.out.println("IOException: " + e.getMessage());

        } finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
            }
        }
    }

    private static void handlingClientCommands(Socket clientSocket, ClientSession session) throws IOException {

        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));

            OutputStream outputStream = clientSocket.getOutputStream();

            PrintWriter printWriter = new PrintWriter(outputStream, true);

           while (true) {
                try {
                    long currentTime = 0;

                    while (br.ready()) {
                        //parsing input from the client
                        List<String> parsedInput = redisProtocolParser.parseRESPMessage(br, currentTime);

                        //After parsing the input from client, separating command and its arguments
                        RedisCommand command = redisCommandParser.parseCommand(parsedInput, currentTime);

                        //maintaining queue of commands, specifically for MULTI and EXEC Command
                        queueCommands(command, session);

                        try {
                            processCommand(command, outputStream, session);//based on commands, it will process output
                        } catch (IOException e) {
                            System.out.println("Error while processing command: " + e.getMessage());
                            break;
                        }
                    }

                } catch (IOException e) {
                    outputStream.write("-ERR invalid input\r\n".getBytes());
                    break;
                }
           }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IOException while handling client commands: " + e.getMessage());
        } finally {
            try {
                if (clientSocket != null && !clientSocket.isClosed()) {
                    System.out.println("Closing client socket.");
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.out.println("IOException while closing socket: " + e.getMessage());
            }
        }
    }

    public static void processCommand(RedisCommand command, OutputStream outputStream, ClientSession session) throws IOException {
        System.out.println("Processing command: " + command.getCommand());
        IRedisCommandHandler redisCommandHandler = CommandFactory.getCommandFromAvailableCommands(command.getCommand());

//        System.out.printf("Checking value for redis command handler %s\n", redisCommandHandler != null ? redisCommandHandler.getClass().getName() : "null");
        System.out.println("Is session a replica in processing ? " + session.isReplica());
        if (redisCommandHandler != null) {
            redisCommandHandler.execute(command.getListOfCommandArguments(), null, session);
        } else {
            sendErrorResponse(outputStream, " Unknown Command");
        }

    }

}

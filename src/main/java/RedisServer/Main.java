package RedisServer;

import RedisCommandExecutor.RedisCommands.CommandFactory;
import RedisCommandExecutor.RedisCommands.IRedisCommandHandler;
import RedisCommandExecutor.RedisCommandsParser.RedisCommand;
import RedisCommandExecutor.RedisCommandsParser.RedisCommandParser;
import RedisCommandExecutor.RedisCommandsParser.RedisProtocolParser;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import static DataUtils.CommandsQueue.queueCommands;

import static RedisResponses.ShortParsedResponses.sendErrorResponse;

public class Main {
    private static RedisCommandParser redisCommandParser;
    private static RedisProtocolParser redisProtocolParser;

    public static void main(String[] args) {
        // You can use print statements as follows for debugging, they'll be visible when running tests.
        System.out.println("Logs from your program will appear here!");

        Socket clientSocket = null;

        int port = 6379;

        listenToPort(clientSocket, port);

    }

    private static void listenToPort(Socket clientSocket, int port) {

        ServerSocket serverSocket;

        redisCommandParser = new RedisCommandParser();

        redisProtocolParser = new RedisProtocolParser();

        try {
            serverSocket = new ServerSocket(port);
            // Since the tester restarts your program quite often, setting SO_REUSEADDR
            // ensures that we don't run into 'Address already in use' errors
            serverSocket.setReuseAddress(true);

            while (true) {
                // Wait for connection from client.
                clientSocket = serverSocket.accept();

                final Socket finalClientSocket = clientSocket;

                new Thread(() -> {
                    try {

                        System.out.println("Connected with Client : " + finalClientSocket.getPort() );

                        ClientSession session = new ClientSession();

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
                    System.out.println("Sending commands for parsing \n");
                    long currentTime = 0;
                    //parsing input from the client
                    List<String> parsedInput = redisProtocolParser.parseRESPMessage(br,currentTime);

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
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.out.println("IOException while closing socket: " + e.getMessage());
            }
        }
    }

    public static void processCommand(RedisCommand command, OutputStream outputStream, ClientSession session) throws IOException {

        IRedisCommandHandler redisCommandHandler = CommandFactory.getCommandFromAvailableCommands(command.getCommand());

        System.out.printf("Checking value for redis command handler %s\n", redisCommandHandler != null ? redisCommandHandler.getClass().getName() : "null");

        if (redisCommandHandler != null) {
            System.out.println("***** Control is here, processing command");
            redisCommandHandler.execute(command.getListOfCommandArguments(), outputStream, session);
        } else {
            sendErrorResponse(outputStream, " Unknown Command");
        }

    }

}

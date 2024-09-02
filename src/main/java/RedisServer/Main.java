package RedisServer;

import RedisCommandExecutor.CommandFactory;
import RedisCommandExecutor.IRedisCommandHandler;
import RedisCommandExecutor.RedisStreams;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.*;

import static RedisCommandExecutor.IncrCommand.sendErrorResponse;

public class Main {
    private static RedisCommandParser redisCommandParser;
    private static RedisProtocolParser redisProtocolParser;
    public static HashMap<String, KeyValue> storeKeyValue;
    public static Map<String, RedisStreams> streams;


    public static void main(String[] args) {
        // You can use print statements as follows for debugging, they'll be visible when running tests.
        System.out.println("Logs from your program will appear here!");
        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        int port = 6379;

       listenToPort(clientSocket, port);

//        //Debug
//        redisCommandParser = new RedisCommandParser();
//        redisProtocolParser = new RedisProtocolParser();
//        storeKeyValue = new HashMap<>();
//        streams = new HashMap<>();
//        try {
//            handlingClientCommands(null, null);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }

    private static void listenToPort(Socket clientSocket, int port) {
        ServerSocket serverSocket;
        redisCommandParser = new RedisCommandParser();
        redisProtocolParser = new RedisProtocolParser();
        storeKeyValue = new HashMap<>();
        streams = new ConcurrentHashMap<>();

        try {
            serverSocket = new ServerSocket(port);
            // Since the tester restarts your program quite often, setting SO_REUSEADDR
            // ensures that we don't run into 'Address already in use' errors
            serverSocket.setReuseAddress(true);

            while (true) {
                // Wait for connection from client.
                clientSocket = serverSocket.accept();
                final Socket finalClientSocket = clientSocket;
                //Socket finalClientSocket = clientSocket;
                new Thread(() -> {
                    try {
                        System.out.printf("Connected with Client : " + finalClientSocket.getPort() + "\n");
                        ClientSession session = new ClientSession();
//                        handlingClientCommands(finalClientSocket, session);
                        handlingClientCommands(finalClientSocket, session);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        } catch (IOException e) {
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
//            OutputStream outputStream = null;
            while (true) {
                try {
                    List<String> messageParts = redisProtocolParser.parseRESPMessage(br);
                    RedisCommand command = redisCommandParser.parseCommand(messageParts);//simply putting it to a custom DS Redis Command
                    queueCommands(command, session);
                    processCommand(command,outputStream,session);//based on commands, it will process output
                    //Debug
//                    RedisCommand command = new RedisCommand("XADD", new ArrayList<>(Arrays.asList("shradha", "0-1", "temperature", "36")));
//                    processCommand(command, outputStream, session);//based on commands, it will process output
////                    command = new RedisCommand("XREAD", new ArrayList<>(Arrays.asList("BLOCK", "1000", "streams", "shradha", "0-1")));
////                    processCommand(command, outputStream, session);//based on commands, it will process output
//                    command = new RedisCommand("XADD", new ArrayList<>(Arrays.asList("shradha", "0-2", "temperature", "40")));
//                    processCommand(command, outputStream, session);//based on commands, it will process output
//                    command = new RedisCommand("XREAD", new ArrayList<>(Arrays.asList("streams", "shradha", "0-1")));
//                    processCommand(command, outputStream, session);//br
                } catch (IOException e) {
                    outputStream.write("-ERR invalid input\r\n".getBytes());
                   break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            clientSocket.close();
        }
    }

    public static void processCommand(RedisCommand command, OutputStream outputStream, ClientSession session) throws IOException {
        IRedisCommandHandler redisCommandHandler = CommandFactory.getCommandFromAvailableCommands(command.getCommand());
        System.out.printf("Checking value for redis command handler " + redisCommandHandler.getClass().getName() + "\n");
        if (redisCommandHandler != null) {
            redisCommandHandler.execute(command.getListOfActions(), outputStream, session);
        } else {
            sendErrorResponse(outputStream, " Unknown Command");
        }
    }

    private static void queueCommands(RedisCommand command, ClientSession session) {
        Queue<RedisCommand> queueOfCommandsForMultiAndExec = session.getCommandQueue();

        if (!queueOfCommandsForMultiAndExec.isEmpty()) {
            while (!queueOfCommandsForMultiAndExec.isEmpty() && !queueOfCommandsForMultiAndExec.peek().getCommand().equals("MULTI")) {
                queueOfCommandsForMultiAndExec.poll();
            }
        }
        queueOfCommandsForMultiAndExec.add(command);
    }

}

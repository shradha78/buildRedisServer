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
    public static HashMap<String, RedisStreams> streams;

    private static final int BLOCKING_THREAD_POOL_SIZE = 5;
    private static final int NON_BLOCKING_THREAD_POOL_SIZE = 10;

    private static ExecutorService blockingThreadPool;
    private static ExecutorService nonBlockingThreadPool;
    public static void main(String[] args){
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");
        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        int port = 6379;
        blockingThreadPool = Executors.newFixedThreadPool(BLOCKING_THREAD_POOL_SIZE);
        nonBlockingThreadPool = Executors.newFixedThreadPool(NON_BLOCKING_THREAD_POOL_SIZE);
       listenToPort(clientSocket, port);
    }

    private static void listenToPort(Socket clientSocket, int port) {
        ServerSocket serverSocket;
        redisCommandParser = new RedisCommandParser();
        redisProtocolParser = new RedisProtocolParser();
        storeKeyValue = new HashMap<>();
        streams = new HashMap<>();

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
                nonBlockingThreadPool.submit(() -> {
                    try {
                        System.out.printf("Connected with Client : " + finalClientSocket.getPort() + "\n");
                        ClientSession session = new ClientSession();
                        handlingClientCommands(finalClientSocket, session);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        }catch (IOException e) {
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
    private static void handlingClientCommands(Socket clientSocket, ClientSession session)  throws IOException{

        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
            OutputStream outputStream = clientSocket.getOutputStream();
            while(true){
                try{
                    List<String> messageParts = redisProtocolParser.parseRESPMessage(br);
                    RedisCommand command = redisCommandParser.parseCommand(messageParts);//simply putting it to a custom DS Redis Command
                    queueCommands(command,session);
                    processCommand(command,outputStream,session);//based on commands, it will process output
                }catch (IOException e){
                    outputStream.write("-ERR invalid input\r\n".getBytes());
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            clientSocket.close();
        }
    }

    public static void processCommand(RedisCommand command, OutputStream outputStream, ClientSession session) throws IOException {
        IRedisCommandHandler redisCommandHandler = CommandFactory.getCommandFromAvailableCommands(command.getCommand());
        System.out.printf("Checking value for redis command handler " + redisCommandHandler.getClass().getName() + "\n");
        if (redisCommandHandler != null) {
            Runnable task = () -> {
                try {
                    redisCommandHandler.execute(command.getListOfActions(), outputStream, session);
                } catch (IOException e) {
                    System.err.println("Error executing command: " + e.getMessage());
                }
            };

            if (redisCommandHandler.isBlockingCommand()) {
                blockingThreadPool.submit(task);
            } else {
                nonBlockingThreadPool.submit(task);
            }
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

    public static void shutdown() {
        nonBlockingThreadPool.shutdown();
        blockingThreadPool.shutdown();
    }
}

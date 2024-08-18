package RedisServer;

import RedisCommandExecutor.CommandFactory;
import RedisCommandExecutor.IRedisCommandHandler;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.*;

import static RedisCommandExecutor.IncrCommand.sendErrorResponse;

public class Main {
    private static RedisCommandParser redisCommandParser;
    private static RedisProtocolParser redisProtocolParser;
    public static HashMap<String, KeyValue> storeKeyValue;
    public static void main(String[] args){
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");
        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        int port = 6379;
       listenToPort(clientSocket, port);
    }

    private static void listenToPort(Socket clientSocket, int port) {
        ServerSocket serverSocket;
        redisCommandParser = new RedisCommandParser();
        redisProtocolParser = new RedisProtocolParser();
        storeKeyValue = new HashMap<>();

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
                System.out.printf("Connected with Client : " + finalClientSocket.getPort() + "\n");
                    try {
                        handlingClientCommands(finalClientSocket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
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
    private static void handlingClientCommands(Socket clientSocket)  throws IOException{
        ClientSession session = new ClientSession();
        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
            OutputStream outputStream = clientSocket.getOutputStream();
            while(true){
                try{
                    System.out.printf("Going to RESP parser \n");
                    List<String> messageParts = redisProtocolParser.parseRESPMessage(br);
                    System.out.printf("Going to command Parser \n");
                    RedisCommand command = redisCommandParser.parseCommand(messageParts);//simply putting it to a custom DS Redis Command
                    System.out.printf("Going to queuing commands \n");
                    queueCommands(command,session);
                    System.out.printf("Going to process command method \n");
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
        System.out.printf("In Processing Command \n");
        IRedisCommandHandler redisCommandHandler = CommandFactory.getCommandFromAvailableCommands(command.getCommand());

        System.out.printf("Checking value for redis command handler " + redisCommandHandler.getClass().getName() + "\n");
        if (redisCommandHandler != null) {
            System.out.printf("command is : " + command.getCommand() +"\n");
            System.out.printf("Arguments: " + command.getListOfActions() + "\n");
            redisCommandHandler.execute(command.getListOfActions(), outputStream,session);
        } else {
            sendErrorResponse(outputStream, " Unknown Command");
        }
    }

    private static void queueCommands(RedisCommand command, ClientSession session) {
        System.out.println("Processing Queue");

        Queue<RedisCommand> queueOfCommandsForMultiAndExec = session.getCommandQueue();

        if (!queueOfCommandsForMultiAndExec.isEmpty()) {
            while (!queueOfCommandsForMultiAndExec.isEmpty() && !queueOfCommandsForMultiAndExec.peek().getCommand().equals("MULTI")) {
                queueOfCommandsForMultiAndExec.poll();
            }
        }
        queueOfCommandsForMultiAndExec.add(command);
        if (!queueOfCommandsForMultiAndExec.isEmpty()) {
            System.out.println("Command on front of queue: " + queueOfCommandsForMultiAndExec.peek().getCommand());
        }
    }
}

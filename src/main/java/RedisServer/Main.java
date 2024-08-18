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
        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
            OutputStream outputStream = clientSocket.getOutputStream();
            while(true){
                try{
                    System.out.printf("Going to RESP parser ");
                    List<String> messageParts = redisProtocolParser.parseRESPMessage(br);
                    System.out.printf("Going to command Parser \n");
                    RedisCommand command = redisCommandParser.parseCommand(messageParts);//simply putting it to a custom DS Redis Command
                    System.out.printf("Going to process command method \n");
                    processCommand(command,outputStream);//based on commands, it will process output
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

    private static void processCommand(RedisCommand command, OutputStream outputStream) throws IOException {
        System.out.printf("In Processing Command \n");
        IRedisCommandHandler redisCommandHandler = CommandFactory.getCommandFromAvailableCommands(command.getCommand());
        System.out.printf("Checking value for redis command handler " + redisCommandHandler.getClass().getName() +"\n");
        if (redisCommandHandler != null) {
            System.out.printf("command is : " + command.getCommand());
            System.out.printf("Arguments: " + command.getListOfActions() + "\n");
            redisCommandHandler.execute(command.getListOfActions(), outputStream);
        } else {
            sendErrorResponse(outputStream, " Unknown Command");
        }
//        switch (commandName) {
//            case "ECHO":
//                if (command.getListOfActions().size() != 1) {
//                    outputStream.write("-ERR wrong number of arguments for 'ECHO' command\r\n".getBytes());
//                } else {
//                    String response = command.getListOfActions().get(0);
//                    sendBulkStringResponse(outputStream, response, "Response Bulk String is : ");
//                }
//                break;
//            case "PING":
//                    outputStream.write("+PONG\r\n".getBytes());
//                    System.out.printf("Received PONG from client! \n");
//                break;
//            case "GET":
//                    String key = command.getListOfActions().get(0);
//                    KeyValue keyValue = storeKeyValue.get(key);
//                    if (keyValue == null || keyValue.isExpired()) {
//                        storeKeyValue.remove(key);
//                        sendBulkStringResponse(outputStream, "", "Value has expired or doesn't exist");
//                    } else {
//                        sendBulkStringResponse(outputStream, keyValue.getValue(), "Response for GET ");
//                    }
//                break;
//            case "SET":
//                   String setKey = command.getListOfActions().get(0);
//                   String setValue = command.getListOfActions().get(1);
//                   long expiryTime = 0;
//                  if (command.getListOfActions().size() > 2
//                          && command.getListOfActions().get(2).equalsIgnoreCase("PX")) {
//                    int seconds = Integer.parseInt(command.getListOfActions().get(3));
//                    expiryTime = System.currentTimeMillis() + seconds; //storing future expiry time
//                    System.out.printf("Expiry time is  " + expiryTime + "\n");
//                   }
//                   storeKeyValue.put(setKey,new KeyValue(setValue, expiryTime));
//                   sendSimpleOKResponse(outputStream);
//                break;
//            case "INCR":
//                 String keyIncr = command.getListOfActions().get(0);
//                 KeyValue keyValueIncr = storeKeyValue.containsKey(keyIncr) ? storeKeyValue.get(keyIncr) : new KeyValue("0",0);
//                 String value = keyValueIncr.getValue();
//                 int valueIncr = 0;
//                 try {
//                     valueIncr = Integer.parseInt(value);
//                     valueIncr += 1;
//                 }catch(NumberFormatException numberFormatException){
//                     sendErrorResponse(outputStream, "value is not an integer or out of range");
//                     return;
//                 }
//                 storeKeyValue.put(keyIncr, (new KeyValue(String.valueOf(valueIncr),0)));
//                 sendIntegerResponse(outputStream, String.valueOf(valueIncr),"Integer value is ");
//                break;
//            default:
//                sendErrorResponse(outputStream,"Unknown Command");
//                break;
//        }
    }

//    public static void sendErrorResponse(OutputStream outputStream, String message) throws IOException {
//        outputStream.write(("-ERR" +  message + "\r\n").getBytes());
//    }
//
//    public static void sendSimpleOKResponse(OutputStream outputStream) throws IOException {
//      outputStream.write("+OK\r\n".getBytes());
//    }
//
//    public static void sendBulkStringResponse(OutputStream outputStream, String value, String debugPrintStatement) throws IOException {
//        if(value.equals("")){
//            String responseBulkNullString = "$-1\r\n";
//            outputStream.write(responseBulkNullString.getBytes());
//            return;
//        }
//        String responseBulkString = "$" + value.length() + "\r\n" + value + "\r\n";
//        System.out.printf(debugPrintStatement + responseBulkString + "\n");
//        outputStream.write(responseBulkString.getBytes());
//    }
//    private static void sendIntegerResponse(OutputStream outputStream, String value, String debugPrintStatement) throws IOException {
//        String responseInteger = ":" + value + "\r\n";
//        System.out.printf(debugPrintStatement + responseInteger + "\n");
//        outputStream.write(responseInteger.getBytes());
//    }

}
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;

public class Main {
    private static RedisCommandParser redisCommandParser;
    private static RedisProtocolParser redisProtocolParser;
    public static HashMap<String, String> storeKeyValue;
  public static void main(String[] args){
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");

     // Uncomment this block to pass the first stage
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
                    List<String> messageParts = redisProtocolParser.parseRESPMessage(br);
                    RedisCommand command = redisCommandParser.parseCommand(messageParts);//simply putting it to a custom DS Redis Command
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
      String commandName = command.getCommand().toUpperCase();
        switch (commandName) {
            case "ECHO":
                if (command.getListOfActions().size() != 1) {
                    outputStream.write("-ERR wrong number of arguments for 'ECHO' command\r\n".getBytes());
                } else {
                    String response = command.getListOfActions().get(0);
                    sendBulkStringResponse(outputStream, response, "Response Bulk String is : ");
                }
                break;
            case "PING":
                    outputStream.write("+PONG\r\n".getBytes());
                    System.out.printf("Received PONG from client! \n");
                break;
            case "GET":
                    String key = command.getListOfActions().get(0);
                    String value = storeKeyValue.getOrDefault(key,"");
                    sendBulkStringResponse(outputStream, value, "Response for GET ");
                break;
            case "SET":
                   String setKey = command.getListOfActions().get(0);
                   String setValue = command.getListOfActions().get(1);
                   storeKeyValue.put(setKey,setValue);
                   sendSimpleOKResponse(outputStream);
                break;

            default:
                sendErrorResponse(outputStream,"Unknown Command");
                break;
        }
    }

    private static void sendErrorResponse(OutputStream outputStream, String message) throws IOException {
        outputStream.write(("-ERR" +  message + "\r\n").getBytes());
    }

    private static void sendSimpleOKResponse(OutputStream outputStream) throws IOException {
      outputStream.write("+OK\r\n".getBytes());
    }

    private static void sendBulkStringResponse(OutputStream outputStream, String value, String debugPrintStatement) throws IOException {
        if(value.equals("")){
            String responseBulkNullString = "$-1\r\n";
            outputStream.write(responseBulkNullString.getBytes());
            return;
        }
        String responseBulkString = "$" + value.length() + "\r\n" + value + "\r\n";
        System.out.printf(debugPrintStatement + responseBulkString + "\n");
        outputStream.write(responseBulkString.getBytes());
    }

}

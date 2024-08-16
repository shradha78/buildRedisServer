import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Main {
    private static RedisCommandParser redisCommandParser;
    private static RedisProtocolParser redisProtocolParser;
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
                       // readMultiplePingsFromSameConnection(finalClientSocket);
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

    private static void readMultiplePingsFromSameConnection(Socket clientSocket)  throws IOException{
      try {
          BufferedReader br = new BufferedReader(
                  new InputStreamReader(clientSocket.getInputStream()));
          OutputStream outputStream = clientSocket.getOutputStream();
          String line;
          while ((line = br.readLine()) != null) {
              System.out.printf("send response of ping : " + line + "\n");
              if (line.equalsIgnoreCase("PING")) {
                  outputStream.write("+PONG\r\n".getBytes());
                  System.out.println("Received PONG from client!");
              } else if ("eof".equalsIgnoreCase(line)) {
                  System.out.printf("eof");
              }
          }
      }catch (IOException e) {
          e.printStackTrace();
      }finally {
          clientSocket.close();
      }
    }

    private static void handlingClientCommands(Socket clientSocket)  throws IOException{
        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
            OutputStream outputStream = clientSocket.getOutputStream();
            while(true){
                try{
                    List<String> messageParts = redisProtocolParser.parseRESPTypeArrayMessage(br);
                    RedisCommand command = redisCommandParser.parseCommand(messageParts);
                    processCommand(command,outputStream);
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
                    String respBulkString = "$" + response.length() + "\r\n" + response + "\r\n";
                    System.out.printf("Response Bulk String is : " + respBulkString);
                    outputStream.write(respBulkString.getBytes());
                }
                break;

            default:
                outputStream.write("-ERR unknown command\r\n".getBytes());
                break;
        }
    }

}

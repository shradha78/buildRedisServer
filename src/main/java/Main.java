import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Main {
  public static void main(String[] args){
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");

     // Uncomment this block to pass the first stage
        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        int port = 6379;
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
                        readMultiplePingsFromSameConnection(finalClientSocket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        }
        catch (IOException e) {
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
      } catch (IOException e) {
          e.printStackTrace();
      }finally {
          clientSocket.close();
      }
    }

}

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
          // Wait for connection from client.
          clientSocket = serverSocket.accept();
          System.out.printf("Connected with Client \n" + clientSocket.getPort());
            Socket finalClientSocket = clientSocket;
            new Thread(()->{
                try {
                    readMultiplePingsFromSameConnection(finalClientSocket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
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
      BufferedReader br = null;
      OutputStream output = null;
      try {
          br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
          output = clientSocket.getOutputStream();
          String line;
          while ((line = br.readLine()) != null) {
              System.out.printf("Received: %s\n", line);
              if (line.equalsIgnoreCase("PING")) {
                  output.write("+PONG\r\n".getBytes(StandardCharsets.UTF_8) );
                  output.flush();
                  System.out.printf("Received PONG from client! \n");
              }
          }
      }catch (IOException e){
          throw new RuntimeException(e);
      }
    }
}

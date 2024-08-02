import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

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
          System.out.printf("Connected with Client \n");
          readMultiplePingsFromSameConnection(clientSocket);
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
          BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
          PrintWriter outputStream = new PrintWriter(clientSocket.getOutputStream(),true);
          String line;
          while ((line = br.readLine()) != null) {
              System.out.printf("Received: %s\n", line);
              if (line.equalsIgnoreCase("PING")) {
                  outputStream.print("+PONG\r\n");
                  outputStream.flush();
                  System.out.printf("Received PONG from client! \n");
              }
          }
      }catch (IOException e){
          throw new RuntimeException(e);
      }
    }

}

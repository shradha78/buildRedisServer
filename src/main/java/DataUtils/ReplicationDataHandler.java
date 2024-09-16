package DataUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ReplicationDataHandler {
    private static int portToConnect = 0;
    private static boolean isReplica = false;
    private static String master_host ="";
    private static int master_port;

    public static String getMaster_host() {
        return master_host;
    }

    public static void setMaster_host(String master_host) {
        ReplicationDataHandler.master_host = master_host;
    }

    public static int getMaster_port() {
        return master_port;
    }

    public static void setMaster_port(int master_port) {
        ReplicationDataHandler.master_port = master_port;
    }

    public static void setPortToConnect(int portToConnect) {
        ReplicationDataHandler.portToConnect = portToConnect;
    }

    public static void setIsReplica(boolean isReplica) {
        ReplicationDataHandler.isReplica = isReplica;
    }

    public static int getPortToConnect() {
        return portToConnect;
    }

    public static boolean isIsReplica() {
        return isReplica;
    }

    public static void sendPingToMaster(String masterHost, int masterPort) {
        try (Socket socket = new Socket(masterHost, masterPort);
             OutputStream outputStream = socket.getOutputStream();
             InputStream inputStream = socket.getInputStream()) {

            // Send PING command in RESP format
            String pingCommand = "*1\r\n$4\r\nPING\r\n";
            outputStream.write(pingCommand.getBytes());
            System.out.println("PING sent to master at " + masterHost + ":" + masterPort);

            // Read the PONG response from the master (optional, depending on handshake logic)
            byte[] buffer = new byte[1024];
            int bytesRead = inputStream.read(buffer);
            String response = new String(buffer, 0, bytesRead);
            System.out.println("Response from master: " + response);

            // Ensure that the PING is sent as part of the initial connection and does not interfere with future commands
            if (response.startsWith("+PONG")) {
                System.out.println("Master responded with PONG. Handshake part 1 complete.");
            }

        } catch (IOException e) {
            System.err.println("Failed to send PING to master: " + e.getMessage());
        }
    }
}

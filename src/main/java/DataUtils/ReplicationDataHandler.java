package DataUtils;

import RedisReplication.RedisInstance;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

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

    static void sendPingToMaster(String masterHost, int masterPort) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(masterHost, masterPort), 5000); // 5 second timeout
            OutputStream out = socket.getOutputStream();
            try {
                out.write("*1\r\n$4\r\nping\r\n".getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            out.flush();
            System.out.println("PING sent to " + masterHost + ":" + masterPort);
        } catch (IOException e) {
            System.err.println("Failed to send PING to " + masterHost + ":" +
                    masterPort);
            e.printStackTrace();
        }
    }
}

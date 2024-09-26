package DataUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class ReplicationDataHandler {
    private static int portToConnect = 0;
    private static boolean isReplica = false;
    private static String master_host ="";
    private static int master_port;
    public static List<BlockingQueue<String>> queues = new ArrayList<>();

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

}

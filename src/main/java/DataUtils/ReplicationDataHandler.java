package DataUtils;

public class ReplicationDataHandler {
    private static int portToConnect = 0;
    private static boolean isReplica = false;

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

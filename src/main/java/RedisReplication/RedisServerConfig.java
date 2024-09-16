package RedisReplication;

public class RedisServerConfig {
    private static String role = "master";
    private static String replicationId="8371b4fb1155b71f4a04d3e1bc3e18c4a990aeeb";
    private static String replicationOffset="0";

    public static String getRole() {
        return role;
    }

    public static void setRole(String role) {
        RedisServerConfig.role = role;
    }

    public static String getReplicationId() {
        return replicationId;
    }

    public static void setReplicationId(String replicationId) {
        RedisServerConfig.replicationId = replicationId;
    }

    public static String getReplicationOffset() {
        return replicationOffset;
    }

    public static void setReplicationOffset(String replicationOffset) {
        RedisServerConfig.replicationOffset = replicationOffset;
    }
}

package RedisReplication;

public class RedisServerConfig {
    private String role;
    private String replicationId;
    private String replicationOffset;
    private static RedisServerConfig instance = null;

    private RedisServerConfig() {
        this.role =  "master";
        this.replicationId = "8371b4fb1155b71f4a04d3e1bc3e18c4a990aeeb";
        this.replicationOffset = "0";
    }

    public static RedisServerConfig getInstance(){
        if(instance == null){
            synchronized (RedisServerConfig.class){
                if(instance == null){
                    instance = new RedisServerConfig();
                }
            }
        }
        return instance;
    }
    public String getRole() {
        return role;
    }

    public RedisServerConfig setRole(String role) {
        this.role = role;
        return this;
    }

    public String getReplicationId() {
        return replicationId;
    }

    public RedisServerConfig setReplicationId(String replicationId) {
        this.replicationId = replicationId;
        return this;
    }

    public String getReplicationOffset() {
        return replicationOffset;
    }

    public RedisServerConfig setReplicationOffset(String replicationOffset) {
        this.replicationOffset = replicationOffset;
        return this;
    }
}

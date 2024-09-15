package RedisReplication;

public class RedisInstance {
    String role;
    String replicationId;
    String replicationOffset;

    public String getRole() {
        return role;
    }

    public RedisInstance setRole(String role) {
        this.role = role;
        return this;
    }

    public String getReplicationId() {
        return replicationId;
    }

    public RedisInstance setReplicationId(String replicationId) {
        this.replicationId = replicationId;
        return this;
    }

    public String getReplicationOffset() {
        return replicationOffset;
    }

    public RedisInstance setReplicationOffset(String replicationOffset) {
        this.replicationOffset = replicationOffset;
        return this;
    }
}

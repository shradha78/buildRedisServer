public class KeyValue {
    private String value;
    private long expiryTime; // Store expiry time in milliseconds

    public KeyValue(String value, long expiryTime) {
        this.value = value;
        this.expiryTime = expiryTime;
    }
    public String getValue() {
        return value;
    }

    public long getExpiryTime() {
        return expiryTime;
    }

    public boolean isExpired() {
        return expiryTime > 0 && System.currentTimeMillis() > expiryTime;
    }
}
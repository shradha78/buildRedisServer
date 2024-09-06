package DataUtils;

public class KeyValue {
    private  String key;
    private String value;
    private long expiryTime; // Store expiry time in milliseconds

    public KeyValue(String key, String value, long expiryTime) {
        this.key = key;
        this.value = value;
        this.expiryTime = expiryTime;
    }
    public String getKey(){
        return key;
    }
    public String getValue() {
        return value;
    }

    public long getExpiryTime() {
        return expiryTime;
    }

    public boolean isExpired() {
        System.out.println("Current Times is " + System.currentTimeMillis() + "\n");
        return expiryTime > 0 && System.currentTimeMillis() > expiryTime;
    }
}

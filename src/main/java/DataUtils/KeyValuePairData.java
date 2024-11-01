package DataUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KeyValuePairData {
    public static Map<String, KeyValue> keyValueStore = new ConcurrentHashMap<>();//created to fetch data based on key
                    //key, <key, value, expiry>
    public static Map<String, KeyValue> getKeyValueStore() {
        return keyValueStore;
    }

    public static synchronized void addKeyValueData(String key, KeyValue keyValue) {
        keyValueStore.put(key, keyValue);
    }

    public static String getValue(String key) {
        return keyValueStore.get(key).getValue();
    }

    public static synchronized KeyValue getSpecificKeyDetails(String key){
        return keyValueStore.get(key);
    }

    public static void removeKeyValueData(String key) {
        keyValueStore.remove(key);
    }

    public static boolean containsKey(String key) {
        return keyValueStore.containsKey(key);
    }

    public static List<String> getAllKeys() {
        List<String> keys = new ArrayList<>();
        for(String key : keyValueStore.keySet()){
            keys.add(key);
        }
        return keys;
    }
}

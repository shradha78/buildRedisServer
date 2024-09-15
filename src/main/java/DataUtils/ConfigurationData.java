package DataUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigurationData {
    private static Map<String,String> RdbCongifurationValueStore = new ConcurrentHashMap<>();

    public static void addConfigDetails(String configName, String configValue) {
        RdbCongifurationValueStore.put(configName, configValue);
    }

    public static String getConfigDetails(String configName) {
        return RdbCongifurationValueStore.containsKey(configName) ? RdbCongifurationValueStore.get(configName) : null;
    }

    public static List<String> getAllConfigDetails() {
        List<String> configDetails = new ArrayList<>();
        for(Map.Entry<String,String> entry : RdbCongifurationValueStore.entrySet()) {
           configDetails.add(entry.getKey());
           configDetails.add(entry.getValue());
        }
        return configDetails;
    }

}

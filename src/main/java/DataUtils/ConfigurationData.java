package DataUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigurationData {
    private static Map<String,String> congifurationValueStore = new ConcurrentHashMap<>();

    public static void handleTestArgumentsForConfigurations(String[] args) {
        for(int i = 0 ; i < args.length ; i++) {
            if(args[i].equals("--dir")) {
                addConfigDetails("dir" ,args[++i]);
            }
            if(args[i].equals("--dbfilename")) {
                addConfigDetails("dbfilename" ,args[++i]);
            }
            if(args[i].equals("--port")) {
                RedisServer.Main.portToConnect = Integer.parseInt(args[++i]);
            }
        }
    }

    public static void addConfigDetails(String configName, String configValue) {
        congifurationValueStore.put(configName, configValue);
    }

    public static String getConfigDetails(String configName) {
        return congifurationValueStore.containsKey(configName) ? congifurationValueStore.get(configName) : null;
    }

    public static List<String> getAllConfigDetails() {
        List<String> configDetails = new ArrayList<>();
        for(Map.Entry<String,String> entry : congifurationValueStore.entrySet()) {
           configDetails.add(entry.getKey());
           configDetails.add(entry.getValue());
        }
        return configDetails;
    }

}

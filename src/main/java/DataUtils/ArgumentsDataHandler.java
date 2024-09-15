package DataUtils;

public class ArgumentsDataHandler {

    public static void handleTestArgumentsForConfigurations(String[] args) {
        for(int i = 0 ; i < args.length ; i++) {
            if(args[i].equals("--dir")) {
                ConfigurationData.addConfigDetails("dir" ,args[++i]);
            }
            if(args[i].equals("--dbfilename")) {
                ConfigurationData.addConfigDetails("dbfilename" ,args[++i]);
            }
            if(args[i].equals("--port")) {
                ReplicationDataHandler.setPortToConnect(Integer.parseInt(args[++i]));
            }
            if(args[i].equals("--replicaof")) {
                ReplicationDataHandler.setIsReplica(true);
            }
        }
    }
}

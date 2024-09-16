package RedisCommandExecutor.RedisCommands;

import RedisReplication.RedisServerConfig;
import RedisServer.ClientSession;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class InfoCommand implements IRedisCommandHandler{
    @Override
    public void execute(List<String> args, OutputStream outputStream, ClientSession session) throws IOException {
        System.out.println("INFO Command");
        String infoArgument = args.get(0);
        RedisServerConfig redisServerConfig = new RedisServerConfig();

        if(infoArgument.equals("replication")) {
            if (DataUtils.ReplicationDataHandler.isIsReplica()) {
              redisServerConfig.setRole("slave");
              redisServerConfig.setReplicationId("");
              redisServerConfig.setReplicationOffset("");
            } else {
                redisServerConfig.setRole("master");
                redisServerConfig.setReplicationId("8371b4fb1155b71f4a04d3e1bc3e18c4a990aeeb");
                redisServerConfig.setReplicationOffset("0");
            }
            StringBuilder sb = new StringBuilder();
            sb.append("role:").append(redisServerConfig.getRole()).append("\n");
            sb.append("master_replid:").append(redisServerConfig.getReplicationId()).append("\n");
            sb.append("master_repl_offset:").append(redisServerConfig.getReplicationOffset()).append("\n");
            RedisResponses.ShortParsedResponses.sendResponseForInfo(outputStream,sb.toString());
        }
    }
}

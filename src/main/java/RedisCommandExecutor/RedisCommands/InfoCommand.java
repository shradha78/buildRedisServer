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
        RedisServerConfig redisServerConfig = RedisServerConfig.getInstance();

        if(infoArgument.equals("replication")) {
            if (DataUtils.ReplicationDataHandler.isIsReplica()) {
              redisServerConfig.setRole("slave");
              redisServerConfig.setReplicationId("");
              redisServerConfig.setReplicationOffset("");
            }
            StringBuilder sb = new StringBuilder();
            sb.append("role:").append(redisServerConfig.getRole()).append("\n");
            sb.append("master_replid:").append(redisServerConfig.getReplicationId()).append("\n");
            sb.append("master_repl_offset:").append(redisServerConfig.getReplicationOffset()).append("\n");
            RedisResponses.ShortParsedResponses.sendResponseForInfo(outputStream,sb.toString());
        }
    }
}

package RedisCommandExecutor.RedisCommands;

import DataUtils.ReplicationDataHandler;
import RedisReplication.RedisInstance;
import RedisServer.ClientSession;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class InfoCommand implements IRedisCommandHandler{
    @Override
    public void execute(List<String> args, OutputStream outputStream, ClientSession session) throws IOException {
        System.out.println("INFO Command");
        String infoArgument = args.get(0);
        RedisReplication.RedisInstance redisInstance = new RedisInstance();

        if(infoArgument.equals("replication")) {
            if (DataUtils.ReplicationDataHandler.isIsReplica()) {
              redisInstance.setRole("slave");
              redisInstance.setReplicationId("");
              redisInstance.setReplicationOffset("");
            } else {
                redisInstance.setRole("master");
                redisInstance.setReplicationId("8371b4fb1155b71f4a04d3e1bc3e18c4a990aeeb");
                redisInstance.setReplicationOffset("0");
            }
            StringBuilder sb = new StringBuilder();
            sb.append("role:").append(redisInstance.getRole()).append("\n");
            sb.append("master_replid:").append(redisInstance.getReplicationId()).append("\n");
            sb.append("master_repl_offset:").append(redisInstance.getReplicationOffset()).append("\n");
            RedisResponses.ShortParsedResponses.sendResponseForInfo(outputStream,sb.toString());
        }
    }
}

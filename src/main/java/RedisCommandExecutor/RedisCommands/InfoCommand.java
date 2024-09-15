package RedisCommandExecutor.RedisCommands;

import RedisServer.ClientSession;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class InfoCommand implements IRedisCommandHandler{
    @Override
    public void execute(List<String> args, OutputStream outputStream, ClientSession session) throws IOException {
        System.out.println("INFO Command");
        String infoArgument = args.get(0);
        String role = "";
        String master_replid = "";
        String master_repl_offset = "";

        if(infoArgument.equals("replication")) {
            if (DataUtils.ReplicationDataHandler.isIsReplica()) {
                role = "slave";
            } else {
                role = "master";
                master_replid = "8371b4fb1155b71f4a04d3e1bc3e18c4a990aeeb";
                master_repl_offset = "0";
            }
            StringBuilder sb = new StringBuilder();
            sb.append("role:").append(role).append("\n");
            sb.append("master_replid:").append(master_replid).append("\n");
            sb.append("master_repl_offset:").append(master_repl_offset).append("\n");
            RedisResponses.ShortParsedResponses.sendResponseForInfo(outputStream,sb.toString());
        }
    }
}

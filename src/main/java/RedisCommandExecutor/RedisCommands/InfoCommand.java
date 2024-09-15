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

        if(infoArgument.equals("replication")){
            if(DataUtils.ReplicationDataHandler.isIsReplica()){
                role = "slave";
            }else{
                role = "master";
                master_replid = "8371b4fb1155b71f4a04d3e1bc3e18c4a990aeeb";
                master_repl_offset = "0";
            }
            RedisResponses.ShortParsedResponses.sendBulkStringResponse(outputStream,"role:"+role,"Info command output is : ");
            RedisResponses.ShortParsedResponses.sendBulkStringResponse(outputStream,"master_replid:"+master_replid,"Info command output is : ");
            RedisResponses.ShortParsedResponses.sendBulkStringResponse(outputStream,"master_repl_offset:"+master_repl_offset,"Info command output is : ");
        }
    }
}

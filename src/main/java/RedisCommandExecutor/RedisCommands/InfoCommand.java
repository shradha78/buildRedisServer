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

        if(infoArgument.equals("replication")){
            if(DataUtils.ReplicationDataHandler.isIsReplica()){
                role = "slave";
            }else{
                role = "master";
            }
            RedisResponses.ShortParsedResponses.sendBulkStringResponse(outputStream,"role:"+role,"Info command output is : ");
        }
    }
}

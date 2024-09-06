package RedisCommandExecutor.RedisCommands;

import RedisServer.ClientSession;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import static RedisResponses.ShortParsedResponses.sendBulkStringResponse;


public class EchoCommand implements IRedisCommandHandler{
    @Override
    public void execute(List<String> commandArgs, OutputStream outputStream, ClientSession session) throws IOException {
        if(MultiCommandCheckerUtils.checkForMultiCommandInQueue(outputStream,session)){
            return;
        }
        if (commandArgs.size() != 1) {
            outputStream.write("-ERR wrong number of arguments for 'ECHO' command\r\n".getBytes());
        } else {
            String response = commandArgs.get(0);
            sendBulkStringResponse(outputStream, response, "Response Bulk String is : ");
        }
    }


}

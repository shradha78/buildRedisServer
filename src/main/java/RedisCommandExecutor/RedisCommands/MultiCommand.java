package RedisCommandExecutor.RedisCommands;

import RedisServer.ClientSession;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import static RedisResponses.ShortParsedResponses.sendSimpleOKResponse;

public class MultiCommand implements IRedisCommandHandler{

    @Override
    public void execute(List<String> args, OutputStream outputStream, ClientSession session) throws IOException {
        sendSimpleOKResponse(outputStream);
    }
}

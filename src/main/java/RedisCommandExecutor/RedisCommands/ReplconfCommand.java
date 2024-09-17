package RedisCommandExecutor.RedisCommands;

import RedisServer.ClientSession;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class ReplconfCommand implements IRedisCommandHandler{
    @Override
    public void execute(List<String> args, OutputStream outputStream, ClientSession session) throws IOException {
        RedisResponses.ShortParsedResponses.sendSimpleOKResponse(outputStream);
    }
}

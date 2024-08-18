package RedisCommandExecutor;

import RedisServer.ClientSession;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public interface IRedisCommandHandler {
    void execute(List<String> args, OutputStream outputStream, ClientSession session) throws IOException;
}

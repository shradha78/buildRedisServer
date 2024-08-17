package RedisCommandExecutor;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public interface IRedisCommandHandler {
    void execute(List<String> args, OutputStream outputStream) throws IOException;
}

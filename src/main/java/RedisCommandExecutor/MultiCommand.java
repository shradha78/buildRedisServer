package RedisCommandExecutor;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import static RedisCommandExecutor.SETCommand.sendSimpleOKResponse;

public class MultiCommand implements IRedisCommandHandler{

    @Override
    public void execute(List<String> args, OutputStream outputStream) throws IOException {
        sendSimpleOKResponse(outputStream);
    }
}

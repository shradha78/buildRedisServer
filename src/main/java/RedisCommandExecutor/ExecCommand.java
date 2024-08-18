package RedisCommandExecutor;

import RedisServer.Main;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import static RedisCommandExecutor.IncrCommand.sendErrorResponse;

public class ExecCommand implements IRedisCommandHandler{
    @Override
    public void execute(List<String> args, OutputStream outputStream) throws IOException {
            if(!Main.queueOfCommandsForMultiAndExec.peek().getCommand().equals("MULTI")){
                sendErrorResponse(outputStream,"EXEC without MULTI");
            }
    }

}

package RedisCommandExecutor;

import RedisServer.ClientSession;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class PingCommand implements IRedisCommandHandler{
    @Override
    public void execute(List<String> args, OutputStream outputStream, ClientSession session) throws IOException {
        if(MultiCommandCheckerUtils.checkForMultiCommandInQueue(outputStream,session)){
            return;
        }
        outputStream.write("+PONG\r\n".getBytes());
        System.out.printf("Received PONG from client! \n");
    }
}

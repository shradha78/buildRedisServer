package RedisCommandExecutor.RedisCommands;

import RedisReplication.RedisServerConfig;
import RedisServer.ClientSession;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class PSYNCCommand implements IRedisCommandHandler{
    @Override
    public void execute(List<String> args, OutputStream outputStream, ClientSession session) throws IOException {
        StringBuilder response = new StringBuilder();
        response.append("+FULLRESYNC ")
                .append(RedisServerConfig.getReplicationId()).append(" ")
                .append(RedisServerConfig.getReplicationOffset()).append("\r\n");
        outputStream.write(response.toString().getBytes());
        outputStream.flush();
    }
}

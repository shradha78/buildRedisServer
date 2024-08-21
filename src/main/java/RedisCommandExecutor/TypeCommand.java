package RedisCommandExecutor;

import RedisServer.ClientSession;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import static RedisCommandExecutor.EchoCommand.sendBulkStringResponse;

public class TypeCommand implements IRedisCommandHandler{
    @Override
    public void execute(List<String> args, OutputStream outputStream, ClientSession session) throws IOException {
        String key = args.get(0);
        RedisServer.KeyValue keyValue = RedisServer.Main.storeKeyValue.get(key);
        if (keyValue == null || keyValue.isExpired()) {
            RedisServer.Main.storeKeyValue.remove(key);
            sendSimpleResponse(outputStream, "none");
        } else {
            sendSimpleResponse(outputStream, "string");
        }
    }

    public void sendSimpleResponse(OutputStream outputStream, String string) throws IOException {
        outputStream.write(("+"+string+"\r\n").getBytes());
    }
}

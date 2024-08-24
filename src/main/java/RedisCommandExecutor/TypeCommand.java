package RedisCommandExecutor;

import RedisServer.ClientSession;
import RedisServer.Main;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import static RedisCommandExecutor.EchoCommand.sendBulkStringResponse;

public class TypeCommand implements IRedisCommandHandler{
    @Override
    public void execute(List<String> args, OutputStream outputStream, ClientSession session) throws IOException {
        String key = args.get(0);
        if (Main.streams.containsKey(key)) {
            sendSimpleResponse(outputStream, "stream");
            return; // We can return early since we've found the correct type
        }

        // Check in storeKeyValue if it's not in streams
        RedisServer.KeyValue keyValue = RedisServer.Main.storeKeyValue.get(key);
        if (keyValue == null || keyValue.isExpired()) {
            // Key doesn't exist or has expired
            RedisServer.Main.storeKeyValue.remove(key);
            sendSimpleResponse(outputStream, "none");
        } else {
            // Key exists and is not expired, assuming it's a string type
            sendSimpleResponse(outputStream, "string");
        }
    }

    public void sendSimpleResponse(OutputStream outputStream, String string) throws IOException {
        outputStream.write(("+"+string+"\r\n").getBytes());
    }
}

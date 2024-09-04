package RedisCommandExecutor;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import RedisServer.ClientSession;
import RedisServer.Main;
import RedisServer.KeyValue;
import static RedisCommandExecutor.EchoCommand.sendBulkStringResponse;

public class GETCommand implements IRedisCommandHandler{
    @Override
    public void execute(List<String> args, OutputStream outputStream, ClientSession session) throws IOException {
        System.out.println("In class GETCommand \n");
        if(MultiCommandCheckerUtils.checkForMultiCommandInQueue(outputStream,session)){
            return;
        }
        String key = args.get(0);
        System.out.println("Key here is : " + key);
        RedisServer.KeyValue keyValue = RedisServer.Main.storeKeyValue.get(key);
        if (keyValue == null || keyValue.isExpired()) {
            RedisServer.Main.storeKeyValue.remove(key);
            sendBulkStringResponse(outputStream, "", "Value has expired or doesn't exist");
        } else {
            sendBulkStringResponse(outputStream, keyValue.getValue(), "Response for GET ");
        }
    }
}

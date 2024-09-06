package RedisCommandExecutor.RedisCommands;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import DataUtils.KeyValuePairData;
import RedisServer.ClientSession;
import DataUtils.KeyValue;

import static RedisResponses.ShortParsedResponses.sendBulkStringResponse;


public class GETCommand implements IRedisCommandHandler{
    @Override
    public void execute(List<String> args, OutputStream outputStream, ClientSession session) throws IOException {

        System.out.println("In class GETCommand \n");

        if(MultiCommandCheckerUtils.checkForMultiCommandInQueue(outputStream,session)){
            return;
        }

        String key = args.get(0);

        System.out.println("Key here is : " + key);

        KeyValue keyValue = DataUtils.KeyValuePairData.getSpecificKeyDetails(key);

        if (keyValue == null || keyValue.isExpired()) {
            KeyValuePairData.removeKeyValueData(key);

            sendBulkStringResponse(outputStream, "", "Value has expired or doesn't exist");
        } else {
            sendBulkStringResponse(outputStream, keyValue.getValue(), "Response for GET ");

        }
    }
}

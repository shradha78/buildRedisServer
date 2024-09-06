package RedisCommandExecutor.RedisCommands;

import DataUtils.KeyValue;
import DataUtils.KeyValuePairData;
import RedisServer.ClientSession;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import static RedisResponses.ShortParsedResponses.sendSimpleResponse;


public class TypeCommand implements IRedisCommandHandler{
    @Override
    public void execute(List<String> args, OutputStream outputStream, ClientSession session) throws IOException {
        String key = args.get(0);
        if (DataUtils.StreamsData.containsStreamKey(key)) {
            sendSimpleResponse(outputStream, "stream");
            return; // We can return early since we've found the correct type
        }

        // Check in storeKeyValue if it's not in streams
        KeyValue keyValue = KeyValuePairData.getSpecificKeyDetails(key);
        if (keyValue == null || keyValue.isExpired()) {
            // Key doesn't exist or has expired
            KeyValuePairData.removeKeyValueData(key);

            sendSimpleResponse(outputStream, "none");
        } else {
            // Key exists and is not expired, assuming it's a string type
            sendSimpleResponse(outputStream, "string");
        }
    }


}

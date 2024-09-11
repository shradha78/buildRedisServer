package RedisCommandExecutor.RedisCommands;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import DataUtils.ConfigurationData;
import DataUtils.KeyValuePairData;
import RedisCommandExecutor.RedisParser.RdbParser;
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

        String dir = ConfigurationData.getConfigDetails("dir");
        String file = ConfigurationData.getConfigDetails("dbfilename");
        Path filePath = null;
        if(dir != null && file != null) {
            filePath = Paths.get(dir, file);
            if (Files.exists(filePath)) {
                try {
                    RdbParser.load(filePath);
                    System.out.println("File loaded");
                } catch (Exception e) {
                    System.out.println("Exception loading rdb file " + e);
                    System.out.println("RDB file not found. Treating database as empty.");
                    RedisResponses.LongParsedResponses.sendArrayRESPresponseForStrings(outputStream, new ArrayList<>());
                }
            }
        }

        KeyValue keyValue = DataUtils.KeyValuePairData.getSpecificKeyDetails(key);

        if (keyValue == null || keyValue.isExpired()) {
            KeyValuePairData.removeKeyValueData(key);

            sendBulkStringResponse(outputStream, "", "Value has expired or doesn't exist");
        } else {
            sendBulkStringResponse(outputStream, keyValue.getValue(), "Response for GET ");

        }
    }
}

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

import static DataUtils.RdbDataHandler.parsingRDBFile;
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

        parsingRDBFile(outputStream);//In case the key exists in db file


        KeyValue keyValue = DataUtils.KeyValuePairData.getSpecificKeyDetails(key);
        System.out.println("In GET command is session, a replica? "+session.isReplica());
            if (keyValue == null || keyValue.isExpired()) {
                KeyValuePairData.removeKeyValueData(key);
                sendBulkStringResponse(outputStream, "", "Value has expired or doesn't exist");
            } else {
                sendBulkStringResponse(outputStream, keyValue.getValue(), "Response for GET ");
            }
    }
}

package RedisCommandExecutor.RedisCommands;

import DataUtils.ConfigurationData;
import RedisCommandExecutor.RedisParser.RdbParser;
import RedisServer.ClientSession;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static DataUtils.RdbDataHandler.parsingRDBFile;

public class KeysCommand implements IRedisCommandHandler{
    @Override
    public void execute(List<String> args, OutputStream outputStream, ClientSession session) throws IOException {
        parsingRDBFile(outputStream);
        handleAllKeys(args, outputStream);
    }

    private static void handleAllKeys(List<String> args, OutputStream outputStream) throws IOException {
        String key = args.get(0);
        List<String> allKeys = new ArrayList<>();
        if(key.equals("*")){
            allKeys = DataUtils.KeyValuePairData.getAllKeys();
        }
        RedisResponses.LongParsedResponses.sendArrayRESPresponseForStrings(outputStream, allKeys);
    }
}

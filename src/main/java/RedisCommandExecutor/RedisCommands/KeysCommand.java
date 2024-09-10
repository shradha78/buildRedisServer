package RedisCommandExecutor.RedisCommands;

import RedisCommandExecutor.RedisParser.RedisFileParser;
import RedisServer.ClientSession;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class KeysCommand implements IRedisCommandHandler{
    @Override
    public void execute(List<String> args, OutputStream outputStream, ClientSession session) throws IOException {
        String filePath = DataUtils.ConfigurationData.getConfigDetails("dir") + "/" + DataUtils.ConfigurationData.getConfigDetails("dbfilename");
        System.out.printf("FilePath is : " + filePath);
        File rdbFile = new File(filePath);
        if (!rdbFile.exists()) {
            System.out.println("RDB file not found. Treating database as empty.");
            RedisResponses.LongParsedResponses.sendArrayRESPresponseForStrings(outputStream,new ArrayList<>());
            return;
        }
        RedisFileParser.parseRDBFile(DataUtils.ConfigurationData.getConfigDetails("dbfilename"));
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
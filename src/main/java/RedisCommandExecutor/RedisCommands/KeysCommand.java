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

public class KeysCommand implements IRedisCommandHandler{
    @Override
    public void execute(List<String> args, OutputStream outputStream, ClientSession session) throws IOException {
        String dir = ConfigurationData.getConfigDetails("dir");
        String file = ConfigurationData.getConfigDetails("dbfilename");
        Path filePath = null;
        if(dir != null && file != null) {
            filePath = Paths.get(dir, file);
            if(Files.exists(filePath)) {
                try {
                    RdbParser.load(filePath);
                    System.out.println("File loaded");
                    handleAllKeys(args, outputStream);
                } catch (Exception e) {
                    System.out.println("Exception loading rdb file " + e);
                    System.out.println("RDB file not found. Treating database as empty.");
                    RedisResponses.LongParsedResponses.sendArrayRESPresponseForStrings(outputStream,new ArrayList<>());
                }
            }
        }
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

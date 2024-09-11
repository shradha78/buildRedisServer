package DataUtils;

import RedisCommandExecutor.RedisParser.RdbParser;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class RdbDataHandler {

    public static void parsingRDBFile(OutputStream outputStream) throws IOException {
        String dir = ConfigurationData.getConfigDetails("dir");
        String file = ConfigurationData.getConfigDetails("dbfilename");
        Path filePath = null;
        if(dir != null && file != null) {
            filePath = Paths.get(dir, file);
            if(Files.exists(filePath)) {
                try {
                    RdbParser.load(filePath);
                    System.out.println("File loaded");
                } catch (Exception e) {
                    System.out.println("Exception loading rdb file " + e);
                    System.out.println("RDB file not found. Treating database as empty.");
                    RedisResponses.LongParsedResponses.sendArrayRESPresponseForStrings(outputStream,new ArrayList<>());
                }
            }
        }
    }
}

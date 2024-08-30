package RedisCommandExecutor;

import RedisServer.ClientSession;
import RedisServer.KeyValue;
import RedisServer.Main;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import static RedisCommandExecutor.XRangeCommand.*;

public class XReadCommand implements IRedisCommandHandler{
    @Override
    public void execute(List<String> args, OutputStream outputStream, ClientSession session) throws IOException {
        int numberOfArgs = args.size();
        int k = numberOfArgs/2;
        for(int i = 0; i < numberOfArgs/2 ; i++ ) {
            String key = args.get(i);
            String id = args.get(k);
            long rangeFrom = 0;
            String[] idParts = id.split("-");
            rangeFrom = Long.parseLong(idParts[0]) + Long.parseLong(idParts[1]);
            RedisStreams streamKey = Main.streams.get(key);
            Map<String, KeyValue> listOfValuesInStreamWithKey = streamKey.getListOfAllValuesForXReadStream(rangeFrom);
            sendArrayRESPresponse(outputStream, listOfValuesInStreamWithKey);
            k++;
        }
    }

    private boolean isSpecificRange(List<String> args) {
        return !args.get(1).equals("-") && !args.get(2).equals("+");
    }

    private long parseId(String id) {
        String[] idParts = id.split("-");
        return Long.parseLong(idParts[0]) + Long.parseLong(idParts[1]);
    }

}

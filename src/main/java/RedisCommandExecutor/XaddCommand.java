package RedisCommandExecutor;

import RedisServer.ClientSession;
import RedisServer.KeyValue;
import RedisServer.Main;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

import static RedisCommandExecutor.EchoCommand.sendBulkStringResponse;

public class XaddCommand implements IRedisCommandHandler{
    @Override
    public void execute(List<String> args, OutputStream outputStream, ClientSession session) throws IOException {
        System.out.printf("In XADD Command");
        String streamKey = args.get(0);
        String streamKey_id = args.get(1);
        KeyValue newKeyValueToBeAdded = new KeyValue(args.get(2), args.get(3),0);

        if(Main.streams.containsKey(streamKey)){
            RedisStreams redisStreams = Main.streams.get(streamKey);
            List<KeyValue> existingKeyValueInStreams = redisStreams.getListOfValuesForStreamID(streamKey_id);
            existingKeyValueInStreams.add(newKeyValueToBeAdded);
            streamKey_id = redisStreams.addEntryToStreamID(streamKey_id,existingKeyValueInStreams);
        }else{
            RedisStreams redisStreams = new RedisStreams(streamKey);
            List<KeyValue> keyValueInStreams = new ArrayList<>();
            keyValueInStreams.add(newKeyValueToBeAdded);
            streamKey_id = redisStreams.addEntryToStreamID(streamKey_id, keyValueInStreams);
            Main.streams.put(streamKey, redisStreams);
        }
        sendBulkStringResponse(outputStream, streamKey_id,"Stream Bulk String Output : ");

    }
}

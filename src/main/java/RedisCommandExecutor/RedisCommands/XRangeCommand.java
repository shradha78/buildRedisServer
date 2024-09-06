package RedisCommandExecutor.RedisCommands;

import RedisServer.ClientSession;
import DataUtils.KeyValue;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import static RedisResponses.LongParsedResponses.sendArrayRESPresponse;

public class XRangeCommand implements IRedisCommandHandler{
    @Override
    public void execute(List<String> args, OutputStream outputStream, ClientSession session) throws IOException {
        String key = args.get(0);
        long rangeFrom = 0;
        long rangeTo = 0;

        if (isSpecificRange(args)) {
            rangeFrom = parseId(args.get(1));
            rangeTo = parseId(args.get(2));
        } else {
            if (args.get(1).equals("-")) {
                rangeTo = parseId(args.get(2));
            } else {
                rangeFrom = parseId(args.get(1));
            }
        }

        RedisStreams streamKey = DataUtils.StreamsData.getStreamData(key);
        Map<String, KeyValue> listOfValuesInStreamWithKey = streamKey.getListOfStreamsDataWithinRange(rangeFrom, rangeTo);
        sendArrayRESPresponse(outputStream, listOfValuesInStreamWithKey);
    }

    private boolean isSpecificRange(List<String> args) {
        return !args.get(1).equals("-") && !args.get(2).equals("+");
    }

    private long parseId(String id) {
        String[] idParts = id.split("-");
        return Long.parseLong(idParts[0]) + Long.parseLong(idParts[1]);
    }


}

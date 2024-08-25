package RedisCommandExecutor;

import RedisServer.ClientSession;
import RedisServer.KeyValue;
import RedisServer.Main;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static RedisCommandExecutor.EchoCommand.sendBulkStringResponse;

public class XRangeCommand implements IRedisCommandHandler{
    @Override
    public void execute(List<String> args, OutputStream outputStream, ClientSession session) throws IOException {
        String key = args.get(0);
        String[] idFromSplitArray = args.get(1).split("-");
        String[] idToSplitArray = args.get(2).split("-");
        long rangeFrom = Long.parseLong(idFromSplitArray[0]) + Long.parseLong(idFromSplitArray[1]);
        long rangeTo = Long.parseLong(idToSplitArray[0]) + Long.parseLong(idToSplitArray[1]);
        RedisStreams streamKey = Main.streams.get(key);
        Map<String,KeyValue> listOfValuesInStreamWithKey = streamKey.getListOfAllValuesWithinStreamRange(rangeFrom,rangeTo);
        sendArrayRESPresponse(outputStream, listOfValuesInStreamWithKey);
    }
    public void sendArrayRESPresponse(OutputStream outputStream, Map<String,KeyValue> list) throws IOException {
        StringBuilder sb = new StringBuilder();
        // Start with the array header
        sb.append("*").append(list.size()).append("\r\n");
        outputStream.write(sb.toString().getBytes());

        // Add each element in the list
        for(Map.Entry<String,KeyValue> entry : list.entrySet() ){
            sendBulkStringResponse(outputStream, entry.getKey(),"Sending Response for Array : ");
            sendBulkStringResponse(outputStream, entry.getValue().getValue(),"Sending Response for Array : ");
        }
    }
}

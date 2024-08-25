package RedisCommandExecutor;

import RedisServer.ClientSession;
import RedisServer.KeyValue;
import RedisServer.Main;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class XRangeCommand implements IRedisCommandHandler{
    @Override
    public void execute(List<String> args, OutputStream outputStream, ClientSession session) throws IOException {
        String key = args.get(0);
        long rangeFrom = Long.parseLong(args.get(1));
        long rangeTo = Long.parseLong(args.get(2));
        RedisStreams streamKey = Main.streams.get(key);
        List<KeyValue> listOfValuesInStreamWithKey = streamKey.getListOfAllValuesWithinStreamRange(rangeFrom,rangeTo);
        sendArrayRESPresponse(outputStream, listOfValuesInStreamWithKey);


    }
    public void sendArrayRESPresponse(OutputStream outputStream, List<KeyValue> list) throws IOException {
        StringBuilder sb = new StringBuilder();
        // Start with the array header
        sb.append("*").append(list.size()).append("\r\n");

        // Add each element in the list
        for (KeyValue element : list) {
            sb.append("$").append(element.getValue().length()).append("\r\n");
            sb.append(element.getValue()).append("\r\n");
        }
        outputStream.write(sb.toString().getBytes());
    }
}

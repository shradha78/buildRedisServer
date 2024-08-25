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
    public void sendArrayRESPresponse(OutputStream outputStream, Map<String, KeyValue> list) throws IOException {
        StringBuilder sb = new StringBuilder();

        // Start with the array header indicating the number of key-value pairs.
        // Each entry in the map represents a sub-array with 2 elements: key and an array of field-value pairs.
        sb.append("*").append(list.size()).append("\r\n");

        // Add each key and its corresponding value array to the response
        for (Map.Entry<String, KeyValue> entry : list.entrySet()) {
            String key = entry.getKey();
            KeyValue value = entry.getValue();

            // For each key, start a new array with 2 elements
            sb.append("*2").append("\r\n");

            // Add the key as the first bulk string
            sb.append("$").append(key.length()).append("\r\n");
            sb.append(key).append("\r\n");

            // Start a new array for the field-value pair
            sb.append("*2").append("\r\n");

            // Assuming that the KeyValue object has two parts: field and value
            String field = value.getKey(); // You'll need to adjust this based on your actual class
            String val = value.getValue();

            // Add the field as a bulk string
            sb.append("$").append(field.length()).append("\r\n");
            sb.append(field).append("\r\n");

            // Add the value as a bulk string
            sb.append("$").append(val.length()).append("\r\n");
            sb.append(val).append("\r\n");
        }

        // Write the entire response to the output stream
        outputStream.write(sb.toString().getBytes());
    }
}

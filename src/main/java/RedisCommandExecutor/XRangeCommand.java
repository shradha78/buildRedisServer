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

        RedisStreams streamKey = Main.streams.get(key);
        Map<String, KeyValue> listOfValuesInStreamWithKey = streamKey.getListOfAllValuesWithinStreamRange(rangeFrom, rangeTo);
        sendArrayRESPresponse(outputStream, listOfValuesInStreamWithKey);
    }

    private boolean isSpecificRange(List<String> args) {
        return !args.get(1).equals("-") && !args.get(2).equals("+");
    }

    private long parseId(String id) {
        String[] idParts = id.split("-");
        return Long.parseLong(idParts[0]) + Long.parseLong(idParts[1]);
    }

    public static void sendArrayRESPresponse(OutputStream outputStream, Map<String, KeyValue> list) throws IOException {
        StringBuilder sb = new StringBuilder();
        System.out.printf("WRITING RESPONSE");

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

package RedisCommandExecutor;

import RedisServer.ClientSession;
import RedisServer.KeyValue;
import RedisServer.Main;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static RedisCommandExecutor.XRangeCommand.*;

public class XReadCommand implements IRedisCommandHandler{
    @Override
    public void execute(List<String> args, OutputStream outputStream, ClientSession session) throws IOException {
        TreeMap<String,Map<String,KeyValue>> responseMap = new TreeMap<>();
        int numberOfArgs = args.size();
        int k = numberOfArgs / 2 + 1;

        for (int i = 1; i <= numberOfArgs / 2; i++) {
            String key = args.get(i);
            String id = args.get(k);

            long rangeFrom = 0;
            String[] idParts = id.split("-");
            rangeFrom = Long.parseLong(idParts[0]) + Long.parseLong(idParts[1]);

            RedisStreams streamKey = Main.streams.get(key);
            Map<String,KeyValue> listOfValuesInStreamWithKey = streamKey.getListOfAllValuesForXReadStream(rangeFrom);

            responseMap.put(key, listOfValuesInStreamWithKey);//adding stream key and all values from that stream
            k++;
        }

        sendArrayRESPresponseForXRead(outputStream, responseMap);
    }

    public static void sendArrayRESPresponseForXRead(OutputStream outputStream, Map<String,Map<String,KeyValue>> streamEntries) throws IOException {
        StringBuilder sb = new StringBuilder();
        System.out.println("WRITING RESPONSE IN XREAD");

        // Start with the array header indicating the number of streams
        sb.append("*").append(streamEntries.size()).append("\r\n");

        for (Map.Entry<String,Map<String,KeyValue>> streamEntry : streamEntries.entrySet()) {
            String streamKey = streamEntry.getKey();
            Map<String,KeyValue> idKeyValuePairs = streamEntry.getValue();

            // Add the stream key and the number of entries
            sb.append("*2").append("\r\n");

            // Add the stream key as a bulk string
            sb.append("$").append(streamKey.length()).append("\r\n");
            sb.append(streamKey).append("\r\n");

            // Start another array for the list of ID-field-value pairs
            sb.append("*").append(idKeyValuePairs.size()).append("\r\n");

            for (Map.Entry<String,KeyValue> entry : idKeyValuePairs.entrySet()) {
                String id = entry.getKey(); // Assuming the key is used as the ID
                String field = entry.getValue().getKey(); // Field should be the actual key
                String value = entry.getValue().getValue();

                // Each entry starts with an array containing the ID and its field-value pair array
                sb.append("*2").append("\r\n");

                // Add the ID as a bulk string
                sb.append("$").append(id.length()).append("\r\n");
                sb.append(id).append("\r\n");

                // Add the field-value pair array
                sb.append("*2").append("\r\n");

                // Add the field as a bulk string
                sb.append("$").append(field.length()).append("\r\n");
                sb.append(field).append("\r\n");

                // Add the value as a bulk string
                sb.append("$").append(value.length()).append("\r\n");
                sb.append(value).append("\r\n");
            }
        }

        // Write the entire response to the output stream
        outputStream.write(sb.toString().getBytes());
    }

}

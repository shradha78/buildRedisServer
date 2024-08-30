package RedisCommandExecutor;

import RedisServer.ClientSession;
import RedisServer.KeyValue;
import RedisServer.Main;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static RedisCommandExecutor.XRangeCommand.*;

public class XReadCommand implements IRedisCommandHandler{
    @Override
    public void execute(List<String> args, OutputStream outputStream, ClientSession session) throws IOException {
        Map<String, List<KeyValue>> responseMap = new HashMap<>();
        int numberOfStreams = args.size() / 2;

        for (int i = 0; i < numberOfStreams; i++) {
            String key = args.get(2 * i + 1);
            String id = args.get(2 * i + 2);

            long rangeFrom = 0;
            String[] idParts = id.split("-");
            rangeFrom = Long.parseLong(idParts[0]) + Long.parseLong(idParts[1]);

            RedisStreams streamKey = Main.streams.get(key);
            List<KeyValue> listOfValuesInStreamWithKey = streamKey.getListOfAllValuesForXReadStream(rangeFrom);

            responseMap.put(key, listOfValuesInStreamWithKey);
        }

        sendArrayRESPresponseForXRead(outputStream, responseMap);
    }

    public static void sendArrayRESPresponseForXRead(OutputStream outputStream, Map<String, List<KeyValue>> streamEntries) throws IOException {
        StringBuilder sb = new StringBuilder();
        System.out.println("WRITING RESPONSE IN XREAD");

        // Start with the array header indicating the number of streams
        sb.append("*").append(streamEntries.size()).append("\r\n");

        for (Map.Entry<String, List<KeyValue>> streamEntry : streamEntries.entrySet()) {
            String streamKey = streamEntry.getKey();
            List<KeyValue> keyValuePairs = streamEntry.getValue();

            // Add the stream key and the number of entries
            sb.append("*2").append("\r\n");

            // Add the stream key as a bulk string
            sb.append("$").append(streamKey.length()).append("\r\n");
            sb.append(streamKey).append("\r\n");

            // Start another array for the list of ID-field-value pairs
            sb.append("*").append(keyValuePairs.size()).append("\r\n");

            for (KeyValue kv : keyValuePairs) {
                String id = kv.getKey(); // Assuming the key is used as the ID

                // Each entry starts with an array containing the ID and its field-value pair
                sb.append("*2").append("\r\n");

                // Add the ID as a bulk string
                sb.append("$").append(id.length()).append("\r\n");
                sb.append(id).append("\r\n");

                // Add the field-value pair
                String field = "field";  // Assuming a fixed field name (adjust as needed)
                String value = kv.getValue();

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

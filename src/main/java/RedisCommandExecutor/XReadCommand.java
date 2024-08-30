package RedisCommandExecutor;

import RedisServer.ClientSession;
import RedisServer.KeyValue;
import RedisServer.Main;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

import static RedisCommandExecutor.EchoCommand.sendBulkStringResponse;
import static RedisCommandExecutor.XRangeCommand.*;

public class XReadCommand implements IRedisCommandHandler{
    @Override
    public void execute(List<String> args, OutputStream outputStream, ClientSession session) throws IOException {
        int startIndex = 1;
        int numberOfArgs = args.size();
        int streamCount = numberOfArgs / 2;
        boolean isBlocked = false;
        long blockTimeout = 0;

        // Check if BLOCK keyword is present and adjust arguments parsing
        if (args.get(0).equalsIgnoreCase("BLOCK")) {
            startIndex = 3;
            streamCount = (numberOfArgs - 3) / 2 + 1;
            blockTimeout = Long.parseLong(args.get(1));
            isBlocked = true;
        }

        if (isBlocked) {
            // Handle blocking with timeout
            handleBlockingXRead(args, startIndex, streamCount, blockTimeout, outputStream);
        } else {
            // Handle non-blocking XREAD
            processStreams(args, startIndex, streamCount, outputStream);
        }
    }

    private void handleBlockingXRead(List<String> args, int startIndex, int streamCount, long blockTimeout, OutputStream outputStream) throws IOException {
        long startTime = System.currentTimeMillis();
        long endTime = startTime + blockTimeout;

        while (System.currentTimeMillis() < endTime) {
            Map<String, Map<String, KeyValue>> responseMap = processStreams(args, startIndex, streamCount, null);

            if (!responseMap.isEmpty()) {
                sendArrayRESPresponseForXRead(outputStream, responseMap);
                return; // Exit after sending response
            }

            try {
                Thread.sleep(50); // Short delay before rechecking
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore interrupt status
                throw new IOException("Thread interrupted during BLOCK wait", e);
            }
        }

        // Send null response if no data was available
        sendBulkStringResponse(outputStream, "", "There's a timeout and no value recieved");
    }

    private Map<String, Map<String, KeyValue>> processStreams(List<String> args, int startIndex, int streamCount, OutputStream outputStream) throws IOException {
        Map<String, Map<String, KeyValue>> responseMap = new LinkedHashMap<>();
        int currentIndex = startIndex;

        for (int i = 0; i < streamCount; i++) {
            String key = args.get(currentIndex);
            String id = args.get(currentIndex + streamCount);

            long rangeFrom = parseIdToRange(id);

            RedisStreams streamKey = Main.streams.get(key);
            if (streamKey != null) {
                Map<String, KeyValue> values = streamKey.getListOfAllValuesForXReadStream(rangeFrom);
                responseMap.put(key, values);
            } else {
                responseMap.put(key, new LinkedHashMap<>()); // Handle missing stream key
            }

            currentIndex++;
        }

        if (outputStream != null) {
            sendArrayRESPresponseForXRead(outputStream, responseMap);
        }

        return responseMap;
    }

    private long parseIdToRange(String id) {
        String[] idParts = id.split("-");
        if (idParts.length == 2) {
            return Long.parseLong(idParts[0]) + Long.parseLong(idParts[1]);
        }
        return 0;
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

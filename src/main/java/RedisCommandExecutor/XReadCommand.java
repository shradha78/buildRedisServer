package RedisCommandExecutor;

import RedisServer.ClientSession;
import RedisServer.KeyValue;
import RedisServer.Main;
import RedisServer.RedisCommand;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static RedisCommandExecutor.EchoCommand.sendBulkStringResponse;
import static RedisCommandExecutor.XRangeCommand.*;
import static RedisServer.Main.commandQueue;
import static RedisServer.Main.processCommand;

public class XReadCommand implements IRedisCommandHandler{
    private static final long POLL_INTERVAL_MS = 100;
    @Override
    public void execute(List<String> args, OutputStream outputStream, ClientSession session) throws IOException {
        int startIndex = 1;
        int numberOfArgs = args.size();
        int streamCount = numberOfArgs / 2 ;
        boolean isBlocked = false;
        long blockTimeout = 0;
        int streamCountPairs = streamCount / 2;

        // Check if BLOCK keyword is present and adjust arguments parsing
        if (args.get(0).equalsIgnoreCase("BLOCK")) {
            startIndex = 3;
            streamCount = (numberOfArgs - 3);
            blockTimeout = Long.parseLong(args.get(1));
            isBlocked = true;
            streamCountPairs = streamCount / 2 - 1;
        }

        if (isBlocked) {
            // Handle blocking with timeout
            handleBlockingXRead(args, startIndex, streamCountPairs, blockTimeout, outputStream, session);
        } else {
            // Handle non-blocking XREAD
            Map<String, Map<String, KeyValue>> responseMap =  processStreams(args, startIndex, streamCount, 0,outputStream);
            sendArrayRESPresponseForXRead(outputStream, responseMap);
        }
    }

    private void handleBlockingXRead(List<String> args, int startIndex, int streamCount, long blockTimeout, OutputStream outputStream, ClientSession session) throws IOException {
        long startTime = System.currentTimeMillis();
        long endTime = startTime + blockTimeout;
        String streamKey = args.get(startIndex);
        RedisStreams stream = Main.streams.get(streamKey);
        Map<String, Map<String, KeyValue>> responseMap = null;

        synchronized (stream) {
            while (System.currentTimeMillis() < endTime) {
                System.out.println("XREAD checking for new data...");
                responseMap = processStreams(args, startIndex, streamCount, 0, null);

                if (responseMap != null && !responseMap.isEmpty()) {
                    System.out.println("XREAD received data.");
                    sendArrayRESPresponseForXRead(outputStream, responseMap);
                    return;
                }

                try {
                    long timeRemaining = endTime - System.currentTimeMillis();
                    if (timeRemaining > 0) {
                        stream.wait(timeRemaining);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Thread interrupted during BLOCK wait", e);
                }
            }
        }
        System.out.println("XREAD timed out without data.");

        // If no new data was added within the block timeout, return null response
        sendBulkStringResponse(outputStream,"","There's a timeout or no value");
    }

    private Map<String, Map<String, KeyValue>> processStreams(List<String> args, int startIndex, int streamCount,int k, OutputStream outputStream) throws IOException {
        Map<String, Map<String, KeyValue>> responseMap = new LinkedHashMap<>();
        System.out.println("In Process Streams \n");
//
        int currentIndex = startIndex;
        String key = "";
        String id = "";
            for (int i = startIndex; i <= streamCount; i++) {
                System.out.println("Checking if being processed here \n");
                key = args.get(i + k);
                id = args.get(i + k + streamCount);
                System.out.printf("key = %s , id = %s \n",key,id);

                long rangeFrom = parseIdToRange(id);

                RedisStreams streamKey = Main.streams.get(key);
                if (streamKey != null) {
                    System.out.println("There's value for this key in stream \n");
                    Map<String, KeyValue> values = null;
                    try {
                        values = streamKey.getListOfAllValuesForXReadStream(rangeFrom);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(!values.isEmpty())responseMap.put(key, values);
                }

            }

            if (outputStream != null) {
                sendArrayRESPresponseForXRead(outputStream, responseMap);
            }
        System.out.println("Does response map has values ? " + (responseMap.isEmpty()?"No" : "Yes" )+ "\n");

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
            sb.append("*2").append("\r\n");

            sb.append("$").append(streamKey.length()).append("\r\n");
            sb.append(streamKey).append("\r\n");

            sb.append("*").append(idKeyValuePairs.size()).append("\r\n");

            for (Map.Entry<String,KeyValue> entry : idKeyValuePairs.entrySet()) {
                String id = entry.getKey(); // Assuming the key is used as the ID
                String field = entry.getValue().getKey(); // Field should be the actual key
                String value = entry.getValue().getValue();
                System.out.println("In Writing response --> id = " + id  +" field = " + field + "value = " + value + "\n");

                sb.append("*2").append("\r\n");

                sb.append("$").append(id.length()).append("\r\n");
                sb.append(id).append("\r\n");

                sb.append("*2").append("\r\n");

                sb.append("$").append(field.length()).append("\r\n");
                sb.append(field).append("\r\n");

                sb.append("$").append(value.length()).append("\r\n");
                sb.append(value).append("\r\n");
            }
        }
        System.out.println("OUTPUT --> " + sb.toString());
        // Write the entire response to the output stream
        outputStream.write(sb.toString().getBytes());
    }

    private void handlePing(OutputStream outputStream) throws IOException {
        System.out.println("Handling PING command");
        outputStream.write("+PONG\r\n".getBytes());
    }

}

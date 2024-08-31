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
    private static final long POLL_INTERVAL_MS = 100;
    @Override
    public void execute(List<String> args, OutputStream outputStream, ClientSession session) throws IOException {
        int startIndex = 1;
        int numberOfArgs = args.size();
        int streamCount = numberOfArgs / 2;
        boolean isBlocked = false;
        long blockTimeout = 0;
        int streamCountPairs = streamCount / 2;

        // Check if BLOCK keyword is present and adjust arguments parsing
        if (args.get(0).equalsIgnoreCase("BLOCK")) {
            startIndex = 3;
            streamCount = (numberOfArgs - 3);
            blockTimeout = Long.parseLong(args.get(1));
            isBlocked = true;
            streamCountPairs = streamCount / 2;
        }

        if (isBlocked) {
            // Handle blocking with timeout
            handleBlockingXRead(args, startIndex, streamCountPairs, blockTimeout, outputStream);
        } else {
            // Handle non-blocking XREAD
            processStreams(args, startIndex, streamCount, outputStream);
        }
    }

    private void handleBlockingXRead(List<String> args, int startIndex, int streamCount, long blockTimeout, OutputStream outputStream) throws IOException {
        long startTime = System.currentTimeMillis();
        long endTime = startTime + blockTimeout;
        System.out.printf("XREAD blocking start time: %d, will timeout at: %d\n", startTime, endTime);
        System.out.printf("Checking args here in handleBlockingXRead  \n");
//        for(String s : args){
//            System.out.printf("args =%d\n",s);
//        }
        Map<String, Map<String, KeyValue>> responseMap = processStreams(args, startIndex, streamCount, null);
        boolean timeout = true;

        System.out.println("OUTSIDE WHILE LOOP");
        while (System.currentTimeMillis() < endTime) {

            System.out.println("BEFORE process streams.......");
            responseMap = processStreams(args, startIndex, streamCount, null);
            long currentTime = System.currentTimeMillis();
            System.out.printf("XREAD polling at: %d\n", currentTime);

            if (responseMap != null && !responseMap.isEmpty()) {
                timeout = false;
                System.out.printf("XREAD found data at: %d\n", currentTime);
                break;
            }

            try {
                System.out.println("SLEEPING FOR ::: " + System.currentTimeMillis());
                Thread.sleep(POLL_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Thread interrupted during BLOCK wait", e);
            }
        }

        if (timeout) {
            System.out.printf("Timeout");
            sendBulkStringResponse(outputStream, "", "There's a timeout and no value received");
            return;
        } else {
            sendArrayRESPresponseForXRead(outputStream, responseMap);
            return;
        }
    }

    private Map<String, Map<String, KeyValue>> processStreams(List<String> args, int startIndex, int streamCount, OutputStream outputStream) throws IOException {
        Map<String, Map<String, KeyValue>> responseMap = new LinkedHashMap<>();
        System.out.printf("In Process Streams \n");
        System.out.printf("Checking args here \n");
//        for(String s : args){
//            System.out.printf("args = %d\n",s);
//        }
        int currentIndex = startIndex;
            for (int i = 0; i < streamCount; i++) {
                System.out.printf("Checking if being processed here \n");
                String key = args.get(i + 3);
                String id = args.get(i + 3 + streamCount);
                System.out.printf("key = %s , id = %s \n",key,id);

                long rangeFrom = parseIdToRange(id);

                RedisStreams streamKey = Main.streams.get(key);
                if (streamKey != null) {
                    System.out.printf("There's value for this key in stream \n");
                    Map<String, KeyValue> values = null;
                    try {
                        values = streamKey.getListOfAllValuesForXReadStream(rangeFrom, XaddCommand.writeSemaphore, XaddCommand.readSemaphore);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    responseMap.put(key, values);
                } else {
                    responseMap.put(key, new LinkedHashMap<>());
                }

            }

            if (outputStream != null) {
                sendArrayRESPresponseForXRead(outputStream, responseMap);
            }
        System.out.printf("Does response map has values ? " + (responseMap.isEmpty()?"No" : "Yes" )+ "\n");

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

        // Write the entire response to the output stream
        outputStream.write(sb.toString().getBytes());
    }

    public boolean isBlockingCommand(){
        return true;
    }

}

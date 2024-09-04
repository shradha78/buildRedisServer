package RedisCommandExecutor;

import RedisServer.ClientSession;
import RedisServer.KeyValue;
import RedisServer.Main;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static RedisCommandExecutor.EchoCommand.sendBulkStringResponse;
import static RedisCommandExecutor.XRangeCommand.*;

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
            handleBlockingXRead(args, startIndex, streamCountPairs, blockTimeout, outputStream);
        } else {
            // Handle non-blocking XREAD
            Map<String, Map<String, KeyValue>> responseMap =  processStreams(args, startIndex, streamCount, 0,outputStream);
            sendArrayRESPresponseForXRead(outputStream, responseMap);
        }
    }

    private void handleBlockingXRead(List<String> args, int startIndex, int streamCount, long blockTimeout, OutputStream outputStream) throws IOException {

        Map<String, Map<String, KeyValue>> responseMap = processStreams(args, startIndex, streamCount, 3, null);
        boolean timeout = true;
        RedisStreams.lock.lock();
        System.out.println("OUTSIDE WHILE LOOP");
        try {
            long remainingTime = blockTimeout;
            while (responseMap.isEmpty() && remainingTime > 0) {
                long startTime = System.currentTimeMillis();
                try {
                    System.out.printf("XREAD Thread going to waitttt\n");
                    RedisStreams.newEntryCondition.await(remainingTime, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    System.out.printf("XREAD Thread interrupted \n");
                    Thread.currentThread().interrupt(); // Handle interruption
                }
                long elapsedTime = System.currentTimeMillis() - startTime;
                remainingTime -= elapsedTime;
                System.out.printf("XREAD Remaining Time : " + remainingTime +"\n");
                responseMap = processStreams(args, startIndex, streamCount, 3, null);
                  // Check if new entries were added
            }
            if (!responseMap.isEmpty()) {
                sendArrayRESPresponseForXRead(outputStream, responseMap);
                return;
            } else {
                sendBulkStringResponse(outputStream, "", "Timeouttt");
            }
        }finally {
            RedisStreams.lock.unlock();
        }
    }

    private Map<String, Map<String, KeyValue>> processStreams(List<String> args, int startIndex, int streamCount,int k, OutputStream outputStream) throws IOException {
        Map<String, Map<String, KeyValue>> responseMap = new LinkedHashMap<>();
        System.out.printf("In Process Streams \n");
//
        int currentIndex = startIndex;
        String key = "";
        String id = "";
            for (int i = startIndex; i <= streamCount; i++) {
                System.out.printf("Checking if being processed here \n");
                key = args.get(i + k);
                id = args.get(i + k + streamCount);
                System.out.printf("key = %s , id = %s \n",key,id);

                long rangeFrom = parseIdToRange(id);

                RedisStreams streamKey = Main.streams.get(key);
                if (streamKey != null) {
                    System.out.printf("There's value for this key in stream \n");
                    Map<String, KeyValue> values = null;
                    try {
                        values = streamKey.getListOfAllValuesForXReadStream(rangeFrom);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    responseMap.put(key, values);
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
                System.out.printf("In Writing response --> id = %s , field = %s, value =%s\n",id,field,value);

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
        System.out.printf("OUTPUT --> " + sb.toString());
        // Write the entire response to the output stream
        outputStream.write(sb.toString().getBytes());
    }

    public boolean isBlockingCommand(){
        return false;
    }

}

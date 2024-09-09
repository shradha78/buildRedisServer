package RedisCommandExecutor.RedisCommands;

import DataUtils.RedisStreams;
import RedisServer.ClientSession;
import DataUtils.KeyValue;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;


import static RedisResponses.LongParsedResponses.sendArrayRESPresponseForXRead;
import static RedisResponses.ShortParsedResponses.sendBulkStringResponse;


public class XReadCommand implements IRedisCommandHandler{
    private static final long POLL_INTERVAL_MS = 100;
    @Override
    public void execute(List<String> args, OutputStream outputStream, ClientSession session) throws IOException {
        System.out.println("#######XReadCommand");
        int startIndex = 1;
        int numberOfArgs = args.size();
        int streamCount = (numberOfArgs-1) / 2 ;
        boolean isBlocked = false;
        long blockTimeout = 0;
        int streamCountPairs = streamCount / 2;

        // Check if BLOCK keyword is present and adjust arguments parsing
        if (args.get(0).equalsIgnoreCase("BLOCK")) {
            startIndex = 3;
            streamCount = (numberOfArgs - 3)/2 +2;
            blockTimeout = Long.parseLong(args.get(1));
            isBlocked = true;
            streamCountPairs = numberOfArgs - 1;
        }

        if (isBlocked) {
            // Handle blocking with timeout
            handleBlockingXRead(args, startIndex, streamCount, blockTimeout, outputStream, session);
        } else {
            // Handle non-blocking XREAD
            System.out.println("Handling without block ");
            Map<String, Map<String, KeyValue>> responseMap =  processingStreamsDataForXRead(args, startIndex, streamCount, streamCount,outputStream);
            sendArrayRESPresponseForXRead(outputStream, responseMap);
        }
    }

    private void handleBlockingXRead(List<String> args, int startIndex, int streamCount, long blockTimeout, OutputStream outputStream, ClientSession session) throws IOException {

        long startTime = System.currentTimeMillis();

        long endTime = startTime + blockTimeout;
        Map<String, Map<String, KeyValue>> responseMap = new HashMap<>();

        boolean timeout = true;

        if(blockTimeout == 0) {
            blockWithoutTimeout(args, startIndex, streamCount, outputStream, responseMap);
            return;
        }

        blockWithTimeout(args, startIndex, streamCount, outputStream, endTime, responseMap, timeout);

    }

    private void blockWithTimeout(List<String> args, int startIndex, int streamCount, OutputStream outputStream, long endTime, Map<String, Map<String, KeyValue>> responseMap, boolean timeout) throws IOException {
        while (System.currentTimeMillis() < endTime) {
            if (responseMap != null && !responseMap.isEmpty()) {
                timeout = false;
                break;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            responseMap = processingStreamsDataForXRead(args, startIndex, streamCount, 1, null);

        }
        // responseMap = processingStreamsDataForXRead(args, startIndex, streamCount, 3, outputStream);

        if(!timeout && !responseMap.isEmpty()){
            sendArrayRESPresponseForXRead(outputStream, responseMap);
        }else{
            // If no new data was added within the block timeout, return null response
            sendBulkStringResponse(outputStream,"","There's a timeout or no value");
        }
    }

    private void blockWithoutTimeout(List<String> args, int startIndex, int streamCount, OutputStream outputStream, Map<String, Map<String, KeyValue>> responseMap) throws IOException {
        while(responseMap == null || responseMap.isEmpty()){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            responseMap = processingStreamsDataForXRead(args, startIndex, streamCount, 1, null);
        }
        sendArrayRESPresponseForXRead(outputStream, responseMap);
    }

    private Map<String, Map<String, KeyValue>> processingStreamsDataForXRead(List<String> args, int startIndex, int streamCount, int k, OutputStream outputStream) throws IOException {
        Map<String, Map<String, KeyValue>> responseMap = new LinkedHashMap<>();
        System.out.println("Retrieving values for XREAD");

        int currentIndex = startIndex;
        String key = "";
        String id = "";
            for (int i = startIndex; i <= streamCount; i++) {
                System.out.println("Checking if being processed here \n");

                key = args.get(i);

                id = args.get(i + k);

                System.out.printf("key = %s , id = %s \n",key,id);

                long rangeFrom = 0;
                if(id.equals("$")){
                    rangeFrom = parseIdToRange(RedisStreams.getLastStreamId());
                }else{
                    rangeFrom = Long.parseLong(id);
                }

                RedisStreams streamKey = DataUtils.StreamsData.getStreamDataForValidation(key);
                if (streamKey != null) {

                    System.out.println("There's value for this key in stream \n");

                    Map<String, KeyValue> values = null;

                    try {
                        values = streamKey.getListOfStreamsDataForXread(rangeFrom);
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


}

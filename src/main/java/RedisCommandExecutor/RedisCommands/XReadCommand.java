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
            System.out.println("Handling without block ");
            Map<String, Map<String, KeyValue>> responseMap =  processingStreamsDataForXRead(args, startIndex, streamCount, 0,outputStream);
            sendArrayRESPresponseForXRead(outputStream, responseMap);
        }
    }

    private void handleBlockingXRead(List<String> args, int startIndex, int streamCount, long blockTimeout, OutputStream outputStream, ClientSession session) throws IOException {

        long startTime = System.currentTimeMillis();

        long endTime = startTime + blockTimeout;

        Map<String, Map<String, KeyValue>> responseMap = new HashMap<>();

        boolean timeout = true;

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

            responseMap = processingStreamsDataForXRead(args, startIndex, streamCount, 3, null);

        }
       // responseMap = processingStreamsDataForXRead(args, startIndex, streamCount, 3, outputStream);

        if(!timeout && !responseMap.isEmpty()){
            sendArrayRESPresponseForXRead(outputStream, responseMap);
        }else{
            // If no new data was added within the block timeout, return null response
            sendBulkStringResponse(outputStream,"","There's a timeout or no value");
        }


    }

    private Map<String, Map<String, KeyValue>> processingStreamsDataForXRead(List<String> args, int startIndex, int streamCount, int k, OutputStream outputStream) throws IOException {
        Map<String, Map<String, KeyValue>> responseMap = new LinkedHashMap<>();
        System.out.println("Retrieving values for XREAD");

        int currentIndex = startIndex;
        String key = "";
        String id = "";
            for (int i = startIndex; i <= streamCount; i++) {
                System.out.println("Checking if being processed here \n");

                key = args.get(i + k);

                id = args.get(i + k + streamCount);

                System.out.printf("key = %s , id = %s \n",key,id);

                long rangeFrom = parseIdToRange(id);

                RedisStreams streamKey = DataUtils.StreamsData.getStreamDataForProcessingXREAD(key);
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

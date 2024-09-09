package DataUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StreamsData {
    public static Map<String, RedisStreams> streams = new ConcurrentHashMap<>();

    public static Map<String, RedisStreams> getAllStreams() {
        return streams;
    }

    public static void addDataToStreams(String streamKey, RedisStreams stream) {
        streams.put(streamKey, stream);
    }

    public static RedisStreams getStreamDataForProcessingXREAD(String streamKey) {
        return streams.containsKey(streamKey) ? streams.get(streamKey) : null;
    }

    public static RedisStreams getStreamDataForValidation(String streamKey) {
        return streams.containsKey(streamKey) ? streams.get(streamKey) : new RedisStreams(streamKey, new LinkedHashMap<>());
    }

    public static boolean containsStreamKey(String streamKey) {
        return streams.containsKey(streamKey);
    }

}

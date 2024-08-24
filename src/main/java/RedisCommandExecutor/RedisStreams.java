package RedisCommandExecutor;

import RedisServer.KeyValue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class RedisStreams {
    private String streamKey;//key value of stream
    private Map<String, List<KeyValue>> streamEntries;//Key id of stream
    private long lastTimestamp;
    private long sequenceNumber;

    public RedisStreams(String streamKey) {
        this.streamKey = streamKey;
        this.streamEntries = new LinkedHashMap<>();//sequence retained
        lastTimestamp =0;
        sequenceNumber=0;
    }

    public String getStreamKey() {
        return streamKey;
    }

    public String addEntryToStreamID(String id, List<KeyValue> entriesForStream){
        if(id == null || id.isEmpty()){
            id = generateId();
        }
        streamEntries.put(id, entriesForStream);
        return id;
    }

    public List<KeyValue> getListOfValuesForStreamID(String id) {
        if(streamEntries.containsKey(id)) {
            return streamEntries.get(id);
        }else{
            return new ArrayList<>();
        }
    }

    public boolean containsEntry(String id) {
        return streamEntries.containsKey(id);
    }

    public String getType() {
        return "stream";
    }

    private String generateId() {
        long currentTimestamp = System.currentTimeMillis();
        if(currentTimestamp == lastTimestamp) {
            synchronized (RedisStreams.class) { //double check lock
                if (currentTimestamp == lastTimestamp) {
                    sequenceNumber++;
                }
            }
        }else {
            lastTimestamp = currentTimestamp;
            sequenceNumber = 0;
        }
        return currentTimestamp + "-" + sequenceNumber;
    }


}

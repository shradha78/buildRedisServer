package RedisCommandExecutor;

import RedisServer.KeyValue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RedisStreams {
    private final String streamKey;
    private final Map<String, List<KeyValue>> streamEntries;
    private long lastTimestamp;
    private long sequenceNumber;

    public RedisStreams(String streamKey) {
        this.streamKey = streamKey;
        this.streamEntries = new LinkedHashMap<>();
        this.lastTimestamp = System.currentTimeMillis();
        this.sequenceNumber = 0;
    }

    public String getStreamKey() {
        return streamKey;
    }

    public String addEntryToStreamID(String id, KeyValue entry) {
        if (id == null || id.isEmpty()) {
            id = generateId();
        }
        streamEntries.computeIfAbsent(id, k -> new ArrayList<>()).add(entry);
        return id;
    }

    private String generateId() {
        long currentTimestamp = System.currentTimeMillis();
        if (currentTimestamp == lastTimestamp) {
            synchronized (RedisStreams.class) {
                if (currentTimestamp == lastTimestamp) {
                    sequenceNumber++;
                }
            }
        } else {
            lastTimestamp = currentTimestamp;
            sequenceNumber = 0;
        }
        return currentTimestamp + "-" + sequenceNumber;
    }

    public Constants validateStreamId(String id) {
        String[] idSplit = id.split("-");
        long idTimestamp = Long.parseLong(idSplit[0]);
        long idSequenceNum = Long.parseLong(idSplit[1]);

        String lastIDEnteredInStream = getLastStreamId();
        if (lastIDEnteredInStream.isEmpty()) {
            if (idTimestamp == 0 && idSequenceNum == 0) {
                return Constants.GREATER_THAN_ZERO;
            }
            return Constants.VALID;
        }

        String[] lastIdSplit = lastIDEnteredInStream.split("-");
        long lastIdTimestamp = Long.parseLong(lastIdSplit[0]);
        long lastIdSequence = Long.parseLong(lastIdSplit[1]);

        if (idTimestamp > lastIdTimestamp || (idTimestamp == lastIdTimestamp && idSequenceNum > lastIdSequence)) {
            return Constants.VALID;
        } else {
            return Constants.EQUAL_OR_SMALLER;
        }
    }

    private String getLastStreamId() {
        String lastKey = "";
        for (String key : streamEntries.keySet()) {
            lastKey = key;
        }
        return lastKey;
    }
}
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
    private String lastStreamId = "";
    public RedisStreams(String streamKey) {
        this.streamKey = streamKey;
        this.streamEntries = new LinkedHashMap<>();
        this.lastTimestamp = System.currentTimeMillis();
        this.sequenceNumber = 0;
    }

    public String addEntryToStreamID(String id, KeyValue entry) {
        if (id.equals("*")) {
            id = autoGenerateId();
        } else {
            id = processId(id);
        }
        streamEntries.computeIfAbsent(id, k -> new ArrayList<>()).add(entry);
        lastStreamId = id;
        return id;
    }

    private String processId(String id) {
        String[] idSplit = id.split("-");
        long idTimestamp = Long.parseLong(idSplit[0]);
        if (idSplit[1].equals("*")) {
            long sequenceNumber = autogenerateSequenceNumber(idTimestamp);
            return idTimestamp + "-" + sequenceNumber;
        }
        return id;
    }

    private long autogenerateSequenceNumber(long idTimestamp) {
        if (idTimestamp == lastTimestamp) {
            return ++sequenceNumber;
        }
        if(idTimestamp == 0){
            return 1;
        }
        lastTimestamp = idTimestamp;
        sequenceNumber = 0;
        return sequenceNumber;
    }

    private String autoGenerateId() {
        long currentTimestamp = System.currentTimeMillis();
        if (currentTimestamp == lastTimestamp) {
            return currentTimestamp + "-" + (++sequenceNumber);
        }
        lastTimestamp = currentTimestamp;
        sequenceNumber = 0;
        return currentTimestamp + "-" + sequenceNumber;
    }

    public Constants validateStreamId(String id) {
        String[] idSplit = id.split("-");
        long idTimestamp = Long.parseLong(idSplit[0]);
        long idSequenceNum = idSplit[1].equals("*") ? autogenerateSequenceNumber(idTimestamp) : Long.parseLong(idSplit[1]);

        if (idTimestamp == 0 && idSequenceNum == 0) {
            return Constants.GREATER_THAN_ZERO;
        }

        if (!lastStreamId.isEmpty()) {
            String[] lastIdSplit = lastStreamId.split("-");
            long lastIdTimestamp = Long.parseLong(lastIdSplit[0]);
            long lastIdSequence = Long.parseLong(lastIdSplit[1]);

            if (idTimestamp < lastIdTimestamp || (idTimestamp == lastIdTimestamp && idSequenceNum <= lastIdSequence)) {
                return Constants.EQUAL_OR_SMALLER;
            }
        }
        return Constants.VALID;
    }

    private String getLastStreamId() {
        return lastStreamId;
    }
}
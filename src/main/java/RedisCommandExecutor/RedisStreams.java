package RedisCommandExecutor;

import RedisServer.KeyValue;

import java.util.*;

public class RedisStreams {
    private final String streamKey;
    private final Map<String, KeyValue> streamEntries;
    private long lastTimestamp;
    private long sequenceNumber;
    private String lastStreamId = "";
    private final Map<Long, Long> sequenceNumbersByTimestamp ;

    public RedisStreams(String streamKey) {
        this.streamKey = streamKey;
        this.streamEntries = new LinkedHashMap<>();
        this.lastTimestamp = System.currentTimeMillis();
        this.sequenceNumber = 0;
        this.sequenceNumbersByTimestamp = new HashMap<>();
    }

    public String addEntryToStreamID(String id, KeyValue entry) {
        if (id.equals("*")) {
            id = autoGenerateId();
        } else {
            id = processId(id);
        }
        streamEntries.put(id,entry);
        System.out.printf("Timestamp for entry added : " + lastTimestamp + "\n");
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
        System.out.printf("##### IN Auto generate SEQ: lastTimestamp=%d, idTimestamp=%d\n", lastTimestamp, idTimestamp);

        if (idTimestamp == lastTimestamp) {
            System.out.printf("If equal to last entry******** \n");
            return ++sequenceNumber;
        }
        if (idTimestamp == 0) {
            System.out.printf("If equal to 0 " + idTimestamp + "\n");
            return 1;
        }

        sequenceNumber = 0;
        return sequenceNumber;
    }

    private String autoGenerateId() {
        long currentTimestamp = System.currentTimeMillis();
        System.out.printf("^^^^^^ Auto generating ID here: lastTimestamp=%d, currentTimestamp=%d\n", lastTimestamp, currentTimestamp);

        if (currentTimestamp == lastTimestamp) {
            // Increment sequence number if timestamp is the same
            return currentTimestamp + "-" + autogenerateSequenceNumber(currentTimestamp);
        }
        return currentTimestamp + "-" + autogenerateSequenceNumber(currentTimestamp);
    }

    public Constants validateStreamId(String id) {
        if(id.equals("*")) return Constants.VALID;
        String[] idSplit = id.split("-");
        long idTimestamp = Long.parseLong(idSplit[0]);
        long idSequenceNum = idSplit[1].equals("*") ? 10000000 : Long.parseLong(idSplit[1]);
        if(idSequenceNum == 10000000 ) return Constants.VALID;
        System.out.printf("********* Stream Id : "+ idTimestamp + " " + idSequenceNum + "\n");
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
            lastTimestamp = lastIdTimestamp;
        }

        return Constants.VALID;
    }

    private String getLastStreamId() {
        return lastStreamId;
    }

    public Map<String,KeyValue> getListOfAllValuesWithinStreamRange(long idFrom, long idTo){
        Map<String,KeyValue> list = new HashMap<>();
        for(Map.Entry<String, KeyValue> entry : streamEntries.entrySet()){
            String[] idSplit = entry.getKey().split("-");
            long id = Long.parseLong(idSplit[0]) + Long.parseLong(idSplit[1]);
            if(id >= idFrom && id <= idTo){
                System.out.printf("****** IN Getting list : " + entry.getKey() + "____" +  entry.getValue().getKey() +"------" + entry.getValue().getValue() + "\n");
                list.put(entry.getKey(),entry.getValue());
            }
        }
        return list;
    }
}
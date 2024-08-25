package RedisCommandExecutor;

import RedisServer.KeyValue;

import java.util.*;

public class RedisStreams {
    private final String streamKey;
    private final Map<String, KeyValue> streamEntries;
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
        System.out.printf("### Adding to Stream : "+ id + "------" + entry.getKey() + "_____" + entry.getValue() + "\n");
        streamEntries.put(id,entry);
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
        System.out.printf("##### IN Auto generate SEQ: lastTimestamp=%d, idTimestamp=%d, sequenceNumber=%d\n", lastTimestamp, idTimestamp, sequenceNumber);
        if (idTimestamp == lastTimestamp) {
            System.out.printf("Incrementing sequenceNumber for same timestamp\n");
            return ++sequenceNumber;
        }
        lastTimestamp = idTimestamp; // Update lastTimestamp here
        System.out.printf("Setting new lastTimestamp=%d\n", lastTimestamp);
        sequenceNumber = 0;
        return sequenceNumber;
    }

    private String autoGenerateId() {
        long currentTimestamp = System.currentTimeMillis();
        System.out.printf("^^^^^^ Auto generating ID here: lastTimestamp=%d, currentTimestamp=%d\n", lastTimestamp, currentTimestamp);
        if (currentTimestamp == lastTimestamp) {
            System.out.printf("^^^ Auto Id generated: " + currentTimestamp + " " + (sequenceNumber + 1) + "\n");
            return currentTimestamp + "-" + (++sequenceNumber);
        }
        lastTimestamp = currentTimestamp; // Update lastTimestamp here
        sequenceNumber = 0;
        return currentTimestamp + "-" + sequenceNumber;
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
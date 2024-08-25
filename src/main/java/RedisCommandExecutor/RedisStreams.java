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
        System.out.printf("##### IN Auto generate SEQ : " + idTimestamp + "\n");
        if (idTimestamp == lastTimestamp) {
            System.out.printf("If equal to last entry******** \n");
            return ++sequenceNumber;
        }
        lastTimestamp = idTimestamp;
        if(idTimestamp == 0){
            System.out.printf("If equal to 0  " + idTimestamp +"\n");
            return 1;
        }
        sequenceNumber = 0;
        return sequenceNumber;
    }

    private String autoGenerateId() {
        long currentTimestamp = System.currentTimeMillis();
        System.out.printf("^^^^^^ Auto generating ID here : " + currentTimestamp + "\n");

        if (currentTimestamp == lastTimestamp) {
            // If the current timestamp is the same as the last one, increment the sequence number.
            System.out.printf("*****Here if id exists Already ");
            sequenceNumber++;
        } else {
            // If the timestamp is different, reset the sequence number to 0.
            lastTimestamp = currentTimestamp;
            sequenceNumber = 0;
        }
        String generatedId = currentTimestamp + "-" + sequenceNumber;
        System.out.printf("^^^ Auto Id generated : " + generatedId + "\n");
        return generatedId;
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
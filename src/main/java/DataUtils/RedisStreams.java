package DataUtils;

import java.util.*;

public class RedisStreams {
    private final String streamKey;
    private final Map<String, KeyValue> streamsDataPerTimestamp;//stream key, <timestamp Value>
    private long lastTimestamp = 0;
    private long sequenceNumber = 0;
    private static String lastStreamId = "";


    public RedisStreams(String streamKey,Map<String, KeyValue> streamsDataPerTimestamp, long sequenceNumber, long lastTimestamp, String lastStreamId) {
        this.streamKey = streamKey;
        this.streamsDataPerTimestamp = streamsDataPerTimestamp;
        this.sequenceNumber = sequenceNumber;
        this.lastTimestamp = lastTimestamp;
        this.lastStreamId = lastStreamId;
    }

    public String addEntryToStreamID(String id, KeyValue entry) throws InterruptedException {
        try {
            if (id.equals("*")) {
                id = autoGenerateId();
            } else {
                id = processId(id);
            }
            System.out.printf("XADD adding entry at start time: %d\n", System.currentTimeMillis());
            streamsDataPerTimestamp.put(id, entry);
            StreamsData.addDataToStreams(streamKey,new RedisStreams(streamKey,streamsDataPerTimestamp,sequenceNumber,lastTimestamp,id));
            lastStreamId = id;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return id;
    }

    private String processId(String id) {

        String[] idSplit = id.split("-");

        long idTimestamp = Long.parseLong(idSplit[0]);

        if (idSplit[1].equals("*")) {
            long sequenceNumber = autogenerateSequenceNumber(idTimestamp);
            lastTimestamp = idTimestamp;
            return idTimestamp + "-" + sequenceNumber;
        }

        return id;
    }

    private long autogenerateSequenceNumber(long idTimestamp) {
        System.out.printf("##### IN Auto generate SEQ: lastTimestamp=%d, idTimestamp=%d\n", lastTimestamp, idTimestamp);

        if (idTimestamp == lastTimestamp) {
            System.out.println("If equal to last entry******** \n");
            return ++sequenceNumber;
        }
        if (idTimestamp == 0) {
            System.out.println("If equal to 0 " + idTimestamp + "\n");
            return 1;
        }

        sequenceNumber = 0;
        return sequenceNumber;
    }

    private String autoGenerateId() {

        long currentTimestamp = System.currentTimeMillis();

        if (currentTimestamp == lastTimestamp) {
            // Increment sequence number if timestamp is the same
            return currentTimestamp + "-" + autogenerateSequenceNumber(currentTimestamp);
        }
        long sequenceNumber = autogenerateSequenceNumber(currentTimestamp);
        lastTimestamp = currentTimestamp;
        return currentTimestamp + "-" + sequenceNumber;
    }

    public Constants validateStreamId(String id) {
        if(id.equals("*")) return Constants.VALID;

        String[] idSplit = id.split("-");

        long idTimestamp = Long.parseLong(idSplit[0]);

        long idSequenceNum = idSplit[1].equals("*") ? 10000000 : Long.parseLong(idSplit[1]);//if id is 1-*, then also its valid

        if(idSequenceNum == 10000000 ) return Constants.VALID;

        System.out.println("********* Stream Id : "+ idTimestamp + " " + idSequenceNum );

        if (idTimestamp == 0 && idSequenceNum == 0) {
            return Constants.GREATER_THAN_ZERO;
        }

        if (!lastStreamId.isEmpty()) {
            String[] lastStreamIdSplit = lastStreamId.split("-");
            long lastStreamIdTimestamp = Long.parseLong(lastStreamIdSplit[0]);
            long lastStreamIdSequence = Long.parseLong(lastStreamIdSplit[1]);

            if (idTimestamp < lastStreamIdTimestamp || (idTimestamp == lastStreamIdTimestamp && idSequenceNum <= lastStreamIdSequence)) {
                return Constants.EQUAL_OR_SMALLER;
            }
        }
        return Constants.VALID;
    }

    public Map<String,KeyValue> getListOfStreamsDataWithinRange(long idFrom, long idTo){

        Map<String,KeyValue> list = new HashMap<>();

        for(Map.Entry<String, KeyValue> entry : streamsDataPerTimestamp.entrySet()){

            String[] idSplit = entry.getKey().split("-");

            long id = Long.parseLong(idSplit[0]) + Long.parseLong(idSplit[1]);

            boolean withinRange = (idFrom == 0 && id <= idTo) ||
                    (idTo == 0 && id >= idFrom) ||
                    (id >= idFrom && id <= idTo);

            if (withinRange) {
                System.out.printf("****** IN Getting list : %s____%s------%s%n",
                        entry.getKey(),
                        entry.getValue().getKey(),
                        entry.getValue().getValue());
                list.put(entry.getKey(), entry.getValue());
            }
        }

        return list;
    }

    public  Map<String,KeyValue> getListOfStreamsDataForXread(long idFrom) throws InterruptedException {

        Map<String, KeyValue> list = new LinkedHashMap<>();

        System.out.println("In XREAD READING DATA ********** \n");
        try {

            for (Map.Entry<String, KeyValue> entry : streamsDataPerTimestamp.entrySet()) {

                String[] idSplit = entry.getKey().split("-");

                long id = Long.parseLong(idSplit[0]) + Long.parseLong(idSplit[1]);
                //System.out.println("Reading id =%l \n",id );
                boolean withinRange = (id > idFrom);

                System.out.println("Value for withinRange = " +withinRange );

                if (withinRange) {
                    System.out.printf("****** IN XREAD Getting list : %s____%s------%s%n",
                            entry.getKey(),
                            entry.getValue().getKey(),
                            entry.getValue().getValue());
                    list.put(entry.getKey(), entry.getValue());
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return list;
    }

    public boolean checkIfValueIsAddedToMainStreams(String streamKey){
        return streamsDataPerTimestamp.containsKey(streamKey);
    }

    public static String getSecondLastStreamTimestamp(String streamKey) {
        List<Map.Entry<String,KeyValue>> entryList = new ArrayList<>(StreamsData.streams.get(streamKey).streamsDataPerTimestamp.entrySet());

        if (entryList.size() < 2) {
            return null; // Not enough entries to get the second-to-last one
        }

        // Get the second-to-last entry
        return entryList.get(entryList.size() - 2).getKey();
    }

}